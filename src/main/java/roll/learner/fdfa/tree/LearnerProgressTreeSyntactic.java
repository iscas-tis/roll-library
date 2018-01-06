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

package roll.learner.fdfa.tree;

import gnu.trove.iterator.TIntObjectIterator;
import roll.learner.LearnerType;
import roll.learner.dfa.tree.ValueNode;
import roll.learner.fdfa.LearnerLeading;
import roll.learner.fdfa.LearnerProgressSyntactic;
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

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LearnerProgressTreeSyntactic extends LearnerProgressTree implements LearnerProgressSyntactic {

    public LearnerProgressTreeSyntactic(Options options, Alphabet alphabet,
            MembershipOracle<HashableValue> membershipOracle, LearnerLeading learnerLeading, int state) {
        super(options, alphabet, membershipOracle, learnerLeading, state);
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.FDFA_SYNTACTIC_TREE;
    }
    
    @Override
    public void startLearning() {
        initialize();
    }
    
    protected class CeAnalyzerProgressTreeSyntactic extends CeAnalyzerProgressTree {

        public CeAnalyzerProgressTreeSyntactic(ExprValue exprValue, HashableValue result) {
            super(exprValue, result);
        }
        
        @Override
        public void analyze() {
            this.leafBranch = result;
            this.nodePrevBranch = getHashableValueBoolean(!result.isAccepting());
            // only has one leaf
            if(tree.getRoot().isLeaf()) {
                this.wordExpr = getExprValueWord(alphabet.getEmptyWord());
                this.nodePrev = tree.getRoot();
                this.wordLeaf = getExprValueWord(getWordExperiment());
                return ;
            }
            // when root is not a terminal node
            CeAnalysisResult result = findBreakIndex();
            update(result);
        }
    }
    
    
    @Override
    public String toString() {
        return TreePrinterBoolean.toString(tree);
    }
    
    @Override
    protected CeAnalyzerTree getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
        return new CeAnalyzerProgressTreeSyntactic(exprValue, result);
    }
    
    // ---------------------------------------------------------------------------------------------
    // there is a chance that new lead node is not yet in the tree
    
    // find partition (leaf node) for the given word
    private Partition findNodePartition(Word word) {
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
    
    @Override
    protected void updatePredecessors() {
        
        TIntObjectIterator<ISet> iterator = nodeToSplit.getValue().predecessors.iterator();
        Node<ValueNode> parent = nodeToSplit.getParent();
        ISet letterDeleted = UtilISet.newISet();
        while(iterator.hasNext()) {
            iterator.advance();
            int letter = iterator.key();
            ISet statePrevs = iterator.value().clone(); 
            ISet stateRemoved = UtilISet.newISet();
            // when addNode is called, statePrevs will not add states, 
            // but iterator.value() may add new states
            // since we do not care new added states, they are correct
            for(final int stateNr : statePrevs) {
                ValueNode statePrev = states.get(stateNr);
                Partition partition = findNodePartition(statePrev.label.append(letter), parent);
                if(partition.found) {
                    if (partition.node != nodeToSplit) {
                        updateTransition(stateNr, letter, partition.node.getValue().id);
                        stateRemoved.set(stateNr); // remove this predecessor
                    } // change to other leaf node
                }else {
                    Node<ValueNode> node = addNode(partition.node, partition.branch
                            , getExprValueWord(statePrev.label.append(letter)));
                    updateTransition(stateNr, letter, node.getValue().id);
                    stateRemoved.set(stateNr);  // remove this predecessor
                }
            }
            ISet temp = iterator.value().clone(); 
            temp.andNot(stateRemoved);
            if(temp.isEmpty()) {
                letterDeleted.set(letter);
            }else {
                iterator.setValue(temp);
            }
        }
        
        for(final int letter : letterDeleted) {
            nodeToSplit.getValue().predecessors.remove(letter);
        }
        
    }

    
    private class Partition {
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
        if(result.isAccepting()) nodeLeaf.setAcceting();
        
        updatePredecessors(stateLeaf.id, 0, alphabet.getLetterSize() - 1);
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
                updateTransition(stateNr, letter, nodeSucc.node.getValue().id);
            }else {
                Node<ValueNode> node = addNode(nodeSucc.node, nodeSucc.branch, getExprValueWord(wordSucc));
                updateTransition(stateNr, letter, node.getValue().id);
            }
        }
    }
}
