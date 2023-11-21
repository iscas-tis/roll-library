package roll.learner.sdfa;

import java.util.LinkedList;

import gnu.trove.iterator.TIntObjectIterator;
import roll.automata.DFA;
import roll.automata.SDFA;
import roll.automata.StateNFA;
import roll.learner.LearnerType;
import roll.learner.dfa.tree.LearnerDFATree;
import roll.learner.dfa.tree.ValueNode;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.tree.Node;
import roll.tree.TreePrinterBoolean;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

public class LearnerSDFATree2 extends LearnerDFATree {

	private SDFA sdfa;

	public LearnerSDFATree2(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
	}

	@Override
	public LearnerType getLearnerType() {
		return LearnerType.SDFA_TREE;
	}

	protected DFA createConjecture() {
		this.sdfa = new SDFA(alphabet);
		return sdfa;
	}

	@Override
	protected void setRejecting(ValueNode state) {
		if (state.node.isRejecting()) {
			sdfa.setReject(state.id);
		}
	}
	
	@Override
	protected void constructHypothesis() {
		// make sure the tree is closed and consistent
		updatePredecessors();
		
		DFA dfa = createConjecture();
		for(int i = 0; i < states.size(); i ++) {
		    dfa.createState();
		}
		
		for(ValueNode state : states) {
			for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
				StateNFA s = dfa.getState(state.id);
				Word wa = state.label.append(letter);
				Partition partition = findNodePartition(wa, tree.getRoot());
                if(partition.found) {
                	s.addTransition(letter, partition.node.getValue().id);
                }else {
                	throw new RuntimeException("Succeccor not found: " + wa.toString());
                }
			}
			if(isAccepting(state)) {
			    dfa.setFinal(state.id);;
			}
			
			setRejecting(state);
			
			if(state.label.isEmpty()) {
			    dfa.setInitial(state.id);
			}
		}
		this.hypothesis = dfa;
	}

	// sift the word to one equivalence class
	
	// find partition (leaf node) for the given word
    private Partition findNodePartition(Word word) {
//    	System.out.println("Trying to find the word partition in tree: " + word.toString());
//    	System.out.println("Tree: " + TreePrinterBoolean.toString(tree));
        return findNodePartition(word, tree.getRoot());
    }
    
    private Partition findNodePartition(Word word, Node<ValueNode> nodeCurr) {
        while(! nodeCurr.isLeaf()) {
            HashableValue result = processMembershipQuery(word, nodeCurr.getLabel());
            Node<ValueNode> node = nodeCurr.getChild(result);
            if(node == null) {
                return new Partition(false, nodeCurr, result); // new leaf node
            }
            nodeCurr = node; // possibly we can not find that node
        }
        return new Partition(true, nodeCurr, null);
    }
	
    class Partition {
        boolean found ;
        Node<ValueNode> node;
        HashableValue branch;
        
        public Partition(boolean f, Node<ValueNode> n, HashableValue v) {
            this.found = f;
            this.node = n;
            this.branch = v;
        }
    }
    
    private Node<ValueNode> addNode(Node<ValueNode> parent, HashableValue branch, ExprValue nodeLabel) {
        Node<ValueNode> nodeLeaf = getValueNode(parent, branch, nodeLabel);
        ValueNode stateLeaf =  new ValueNode(states.size(), nodeLabel.get());
        stateLeaf.node = nodeLeaf;
        nodeLeaf.setValue(stateLeaf);
        states.add(stateLeaf); // add new state
        
        parent.addChild(branch, nodeLeaf);
        // test whether this node is accepting
        Word period = nodeLabel.get();
        HashableValue result = processMembershipQuery(period, tree.getRoot().getLabel());
        
        if (result.isAccepting())
			nodeLeaf.setAcceting();
		else if (result.isRejecting())
			nodeLeaf.setRejecting();
        
//        updatePredecessors(stateLeaf.id, 0, alphabet.getLetterSize() - 1);
        return nodeLeaf;
    }
    
    // may add new leaf node during successor update
    @Override
    protected void updatePredecessors(int stateNr, int from, int to) {
        assert stateNr < states.size()
        && from >= 0
        && to < alphabet.getLetterSize();
        
        ValueNode state = states.get(stateNr);
        Word label = state.label;
        for(int letter = from; letter <= to; letter ++) {
            Word wordSucc = label.append(letter);
            Partition nodeSucc = findNodePartition(wordSucc);
            if(nodeSucc.found) {
//                updateTransition(stateNr, letter, nodeSucc.node.getValue().id);
            }else {
                addNode(nodeSucc.node, nodeSucc.branch, getExprValueWord(wordSucc));
//                updateTransition(stateNr, letter, node.getValue().id);
            }
        }
    }
    
    @Override
    protected void updatePredecessors() {
        
        LinkedList<ValueNode> queue = new LinkedList<>();
        ISet visited = UtilISet.newISet();
        for (ValueNode node : states) {
        	queue.add(node);
        	visited.set(node.id);
        }
        
        while(! queue.isEmpty()) {
            ValueNode currState = queue.poll();
            visited.set(currState.id);
            // when addNode is called, statePrevs will not add states, 
            // but iterator.value() may add new states
            // since we do not care new added states, they are correct
            for (int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
            	Word wa = currState.label.append(letter);
                Partition partition = findNodePartition(wa, tree.getRoot());
                if(! partition.found) {
                    Node<ValueNode> node = addNode(partition.node, partition.branch
                            , getExprValueWord(wa));
                    if (!visited.get(node.getValue().id)) {
                    	queue.add(node.getValue());
                    	visited.set(node.getValue().id);
                    }
                }
            }
        }
        
//        for(final int letter : letterDeleted) {
//            nodeToSplit.getValue().predecessors.remove(letter);
//        }
        
    }

	@Override
	protected CeAnalyzerTree getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
		return new CeAnalyzerSDFATree(exprValue, result);
	}

	// analyze counterexample
	protected class CeAnalyzerSDFATree extends CeAnalyzerTree {

		public CeAnalyzerSDFATree(ExprValue exprValue, HashableValue result) {
			super(exprValue, result);
		}
		
		@Override
		public void analyze() {
			this.leafBranch = result;
			// only has one leaf
			if(tree.getRoot().isLeaf()) {
				this.wordExpr = getExprValueWord(alphabet.getEmptyWord());
				this.nodePrev = tree.getRoot();
				this.wordLeaf = getExprValueWord(getWordExperiment());
				this.nodePrevBranch = processMembershipQuery(alphabet.getEmptyWord(), wordExpr);
				return ;
			}
			// when root is not a terminal node
			CeAnalysisResult result = findBreakIndex();
			update(result);
		}
		
        @Override
        protected void update(CeAnalysisResult result) {
            super.update(result);
            this.nodePrevBranch = processMembershipQuery(getStateLabel(result.currState), wordExpr);
        }
	}

}
