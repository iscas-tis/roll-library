/* Copyright (c) 2016, 2017                                               */
/*       Institute of Software, Chinese Academy of Sciences               */
/* This file is part of ROLL, a Regular Omega Language Learning library.  */
/* ROLL is free software: you can redistribute it and/or modify           */
/* it under the terms of the GNU General Public License as published by   */
/* the Free Software Foundation, either version 3 of the License, or      */
/* (at your option) any later version.                                    */

/* This program is distributed in the hope that it will be useful,        */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          */
/* GNU General Public License for more details.                           */

/* You should have received a copy of the GNU General Public License      */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package roll.learner.dfa.tree;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import gnu.trove.iterator.TIntObjectIterator;
import roll.automata.DFA;
import roll.automata.StateDFA;
import roll.learner.LearnerDFA;
import roll.main.Options;
import roll.query.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.tree.Node;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * Maler and Pnueli way to treat counterexample
 * */

// only apply to DFA
public abstract class LearnerDFATree extends LearnerDFA {

	protected TreeImpl tree;
	// updates for tree
    protected List<ValueNode> states;
    
    
	public LearnerDFATree(Options options, Alphabet alphabet,
			MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
		states = new ArrayList<>();
	}
	
	protected ValueNode createNode(Node<ValueNode> node) {
		ValueNode valueNode = new ValueNode(states.size(), node.getLabel().get());
		states.add(valueNode);
		valueNode.node = node;
		node.setValue(valueNode);
		return valueNode;
	}

    @Override
	protected void initialize() {
		
		Word wordEmpty = alphabet.getEmptyWord();
		ExprValue label = getExprValueWord(wordEmpty);
		Node<ValueNode> root = getValueNode(null, null, label);  
		states.clear();
		// init empty state
		ValueNode stateLamda = createNode(root);
		
		tree = new TreeImpl(root);
		tree.setLamdaLeaf(root);
		
		updatePredecessors(stateLamda.id, 0, alphabet.getLetterSize() - 1);
		nodeToSplit = null;
		constructHypothesis();
	}
	

	protected Node<ValueNode> nodeToSplit;
	
	protected void constructHypothesis() {
		// construct machine according to KV tree
		if (nodeToSplit != null) {
			updatePredecessors();
		}
		
		DFA dfa = new DFA(alphabet);
		for(int i = 0; i < states.size(); i ++) {
		    dfa.createState();
		}
		
		for(ValueNode state : states) {
			for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
				BitSet preds = state.predecessors.get(letter);
				if(preds == null) continue;
				for(int predNr = preds.nextSetBit(0)
						; predNr >= 0
						; predNr = preds.nextSetBit(predNr + 1)) {
					StateDFA s = dfa.getState(predNr);
					s.addTransition(letter, state.id);
				}
			}
			if(state.node.isAccepting()) {
			    dfa.setFinal(state.id);;
			}
			if(state.label.isEmpty()) {
			    dfa.setInitial(state.id);
			}
		}
		this.dfa = dfa;
	}
	
	// needs to check , s <- a - t then t has a successor s 
	protected void updatePredecessors() {
		
		TIntObjectIterator<BitSet> iterator = nodeToSplit.getValue().predecessors.iterator();
		Node<ValueNode> parent = nodeToSplit.getParent();
		BitSet letterToDeleted = new BitSet();
		while(iterator.hasNext()) {
			iterator.advance();
			int letter = iterator.key();
			BitSet statePrevs = iterator.value();
			BitSet stateLeft = (BitSet) statePrevs.clone();
			for(int stateNr = statePrevs.nextSetBit(0)
					; stateNr >= 0
					; stateNr = statePrevs.nextSetBit(stateNr + 1)) {
				ValueNode statePrev = states.get(stateNr);
				Node<ValueNode> nodeOther = sift(statePrev.label.append(letter), parent);
				if (nodeOther != nodeToSplit) {
					updateTransition(stateNr, letter, nodeOther.getValue().id);
					stateLeft.clear(stateNr);
				}
			}
			if(stateLeft.isEmpty()) {
				letterToDeleted.set(letter);
			}else {
				iterator.setValue(stateLeft);
			}
		}
		
		for(int letter = letterToDeleted.nextSetBit(0)
				; letter >= 0
				; letter = letterToDeleted.nextSetBit(letter + 1)) {
			nodeToSplit.getValue().predecessors.remove(letter);
		}
		
	}


	@Override
	public void refineHypothesis(Query<HashableValue> query) {
		ExprValue exprValue = getCounterExampleWord(query);
		HashableValue result = processMembershipQuery(query);
	    nodeToSplit = updateTree(exprValue, result);
	    constructHypothesis();
	}

	
	protected Node<ValueNode> getValueNode(Node<ValueNode> parent, HashableValue branch, ExprValue label) {
		return new NodeImpl(parent, branch, label);
	}
	

	// get corresponding 
    protected Node<ValueNode> sift(Word word) {
 		return sift(word, tree.getRoot());
	}
	
	protected Node<ValueNode> sift(Word word, Node<ValueNode> nodeCurr) {
		while(! nodeCurr.isLeaf()) {
			ExprValue exprValue = nodeCurr.getLabel();
			HashableValue result = processMembershipQuery(word, exprValue);
			nodeCurr = nodeCurr.getChild(result);
		}
		return nodeCurr;
	}
	
	protected HashableValue processMembershipQuery(Word word, ExprValue exprValue) {
		Word suffix = exprValue.get();
		return membershipOracle.answerMembershipQuery(new QuerySimple<>(word, suffix));
	}

	// word will never be empty word
	protected Node<ValueNode> updateTree(ExprValue exprValue, HashableValue result) { 
		
		CeAnalyzerTree analyzer = getCeAnalyzerInstance(exprValue, result);
		analyzer.analyze();
		
		// replace nodePrev with new experiment node nodeExpr 
		// and two child r[1..length-1] and nodePrev
		
		ExprValue wordExpr = analyzer.getNodeExpr();
		Node<ValueNode> nodePrev = analyzer.getNodeToSplit();
		Node<ValueNode> parent = nodePrev.getParent();
		
		// new experiment word
		boolean rootChanged = false;
		// replace nodePrev
		Node<ValueNode> nodeExpr = getValueNode(parent, nodePrev.fromBranch(), wordExpr); 
		if(parent != null) {
			parent.addChild(nodePrev.fromBranch(), nodeExpr);
		}else { // became root node
			tree = new TreeImpl(nodeExpr);
			rootChanged = true;
		}
		
		// state for r[1..length-1]
		HashableValue branchNodeLeaf = analyzer.getLeafBranch();
		HashableValue branchNodePrev = analyzer.getNodeSplitBranch();
		Node<ValueNode> nodeLeaf = getValueNode(nodeExpr, branchNodeLeaf, analyzer.getNodeLeaf());
		ValueNode stateLeaf = createNode(nodeLeaf); 
		
		Node<ValueNode> nodePrevNew = getValueNode(nodeExpr, branchNodePrev, nodePrev.getLabel());
		nodePrevNew.setValue(nodePrev.getValue()); // To update
		nodePrevNew.getValue().node = nodePrevNew; // update node
		
		nodeExpr.addChild(branchNodeLeaf, nodeLeaf);
		nodeExpr.addChild(branchNodePrev, nodePrevNew);
		
		// update outgoing transitions for nodeLeaf
		updatePredecessors(stateLeaf.id, 0, alphabet.getLetterSize() - 1);
				
		// needs to be changed
		if(rootChanged) {
			if(! nodePrev.isAccepting()) nodeLeaf.setAcceting();
			else nodePrevNew.setAcceting();
		}else {
			if(nodePrev.isAccepting()) {
				nodeLeaf.setAcceting();
				nodePrevNew.setAcceting();
			}
		}

		
		Word wordNodePrev = nodePrevNew.getLabel().get();
		if(wordNodePrev.isEmpty()) {
			tree.setLamdaLeaf(nodePrevNew);
		}
		
		return nodePrevNew;
	}
	
	// update the information
	protected void updatePredecessors(int stateNr, int from, int to) {
		assert stateNr < states.size() 
	    && from >= 0
	    && to < alphabet.getLetterSize();
		
		ValueNode state = states.get(stateNr);
		
		Word label = state.label;
		for(int letter = from; letter <= to; letter ++) {
			Word wordSucc = label.append(letter);
			Node<ValueNode> nodeSucc = sift(wordSucc);
			updateTransition(stateNr, letter, nodeSucc.getValue().id);
		}
	}
	
	@Override
	public Word getStateLabel(int state) {
		return states.get(state).label;
	}
	
	protected void updateTransition(int from, int letter, int to) {
		assert from < states.size() 
		    && to < states.size() 
		    && letter < alphabet.getLetterSize();
		states.get(to).addPredecessor(from, letter);
	}
	
	@Override
	protected CeAnalyzerTree getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
		return new CeAnalyzerTree(exprValue, result);
	}
		
	// analyze counterexample
	protected class CeAnalyzerTree extends CeAnalyzer {
		
		protected Node<ValueNode> nodePrev = tree.getLamdaLeaf();
		protected ExprValue wordExpr;
		protected ExprValue wordLeaf;
		protected HashableValue leafBranch;
		protected HashableValue nodePrevBranch;
		
		public CeAnalyzerTree(ExprValue exprValue, HashableValue result) {
			super(exprValue, result);
		}
		
		// find prefix whose successor needs to be added
		@Override
		public void analyze() {
			boolean isAcc = result.get();
			this.leafBranch = getHashableValueBoolean(isAcc);
			this.nodePrevBranch = getHashableValueBoolean(!isAcc);
			// only has one leaf
			if(tree.getRoot().isLeaf()) {
				this.wordExpr = getExprValueWord(alphabet.getEmptyWord());
				this.nodePrev = tree.getRoot();
				this.wordLeaf = getExprValueWord(exprValue.get());
				return ;
			}
			
			Word wordCE = this.exprValue.get();
			// get the initial state from automaton
			int letterNr = 0, stateCurr = -1, statePrev = dfa.getInitialState();
			
			// binary search, low and high are the lengths of prefix
			int low = 0, high = wordCE.length() - 1;
			while (low <= high) {

				int mid = (low + high) / 2;

				assert mid < wordCE.length();

				int sI = dfa.getSuccessor(wordCE.getPrefix(mid));
				int sJ = dfa.getSuccessor(sI, wordCE.getLetter(mid));

				Word sILabel = getStateLabel(sI);
				Word sJLabel = getStateLabel(sJ);

				HashableValue memSIAV = processMembershipQuery(sILabel, wordCE.getSuffix(mid));
				HashableValue memSJV = processMembershipQuery(sJLabel, wordCE.getSuffix(mid + 1));

				if (! memSIAV.valueEqual(memSJV)) {
					statePrev = sI;
					letterNr = mid;
					stateCurr = sJ;
					break;
				}

				if (memSIAV.valueEqual(result)) {
					low = mid + 1;
				} else {
					high = mid;
				}
			}
			
			Word wordPrev = states.get(statePrev).label;         // S(j-1)
			this.wordExpr = getExprValueWord(wordCE.getSuffix(letterNr + 1));  // y[j+1..n]
			this.wordLeaf = getExprValueWord(wordPrev.append(wordCE.getLetter(letterNr))); // S(j-1)y[j]
			this.nodePrev = states.get(stateCurr).node;          // S(j)
		}
		
		public ExprValue getNodeLeaf() {
			return wordLeaf;
		}
		
		public ExprValue getNodeExpr() {
			return wordExpr;
		}
		
		public Node<ValueNode> getNodeToSplit() {
			return nodePrev;
		}
		
		public HashableValue getLeafBranch() {
			return leafBranch;
		}
		
		public HashableValue getNodeSplitBranch() {
			return nodePrevBranch;
		}
		
	}
	
	public String toString() {
		return tree.toString();
	}

}
