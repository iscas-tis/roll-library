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

import roll.automata.DFA;
import roll.automata.StateDFA;
import roll.learner.LearnerType;
import roll.main.Options;
import roll.query.MembershipOracle;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.tree.LCA;
import roll.tree.Node;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * original Tree algorithm proposed by M.J.Keans-U.V.Vazirani
 *   An introduction to Computational Learning Theory
 *   
 *   
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LearnerDFATreeKV extends LearnerDFATree {

    public LearnerDFATreeKV(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
    }
    
    @Override
    protected void updatePredecessors(int stateNr, int from, int to) {
        
    }
    
    @Override
    protected void constructHypothesis() {
        DFA dfa = new DFA(alphabet);
        for(int i = 0; i < states.size(); i ++) {
            dfa.createState();
        }
        
        for(ValueNode stateNode : states) {
            for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
                Word succWord = stateNode.label.append(letter);
                Node<ValueNode> succNode = sift(succWord);
                StateDFA state = dfa.getState(stateNode.id);
                state.addTransition(letter, succNode.getValue().id);
            }
            if(stateNode.node.isAccepting()) {
                dfa.setFinal(stateNode.id);
            }
            if(stateNode.label.isEmpty()) {
                dfa.setInitial(stateNode.id);
            }
        }
        this.dfa = dfa;
    }
    
    @Override
    protected CeAnalyzerTreeKV getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
        return new CeAnalyzerTreeKV(exprValue, result);
    }
    
    protected class CeAnalyzerTreeKV extends CeAnalyzerTree {

        public CeAnalyzerTreeKV(ExprValue exprValue, HashableValue result) {
            super(exprValue, result);
        }

        @Override
        public void analyze() {
            
            if(tree.getRoot().isLeaf()) {
                this.wordExpr = getExprValueWord(alphabet.getEmptyWord());
                this.nodePrev = tree.getRoot();
                this.wordLeaf = getExprValueWord(exprValue.get());
                this.leafBranch = result;
                this.nodePrevBranch = getHashableValueBoolean(!result.isAccepting());
                return ;
            }
            
            Word wordCE = this.exprValue.get();
            int state = dfa.getInitialState();
            Node<ValueNode> treeNodePrev = null;
            Node<ValueNode> treeNodeCurr = null;
            ValueNode stateNodeCurr = null;
            int j = -1;
            for(int length = 1; length <= wordCE.length(); length ++) {
                Word prefix = wordCE.getPrefix(length);
                treeNodeCurr = sift(prefix);
                Word nodeLabel = treeNodeCurr.getLabel().get();
                state = dfa.getSuccessor(state, wordCE.getLetter(length - 1));
                stateNodeCurr = states.get(state);
                if(nodeLabel.equals(stateNodeCurr.label)) { // si == si hat
                    treeNodePrev = treeNodeCurr;
                    continue;
                }else { // si != si hat
                    j = length - 1;
                    break;
                }
            }
            // j is the least i such that si != si hat
            // get common ancestor d for sj != sj hat
            LCA<ValueNode> lca = tree.getLCA(treeNodeCurr, stateNodeCurr.node);
            if (lca.firstChild == treeNodeCurr) {
                leafBranch = lca.firstBranch;
                nodePrevBranch = lca.secondBranch;
            }else {
                leafBranch = lca.secondBranch;
                nodePrevBranch = lca.firstBranch;
            }
            Word lcaLabel = lca.commonAncestor.getLabel().get();
            // new experiment word r[j]d
            wordExpr = getExprValueWord(wordCE.getLetterWord(wordCE.getLetter(j)).concat(lcaLabel)); 
            // new leaf label r[0..j-1]
            wordLeaf = getExprValueWord(wordCE.getPrefix(j));
            // previous node s(j-1)
            nodePrev = treeNodePrev;
        }
        
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.DFA_TREE_KV;
    }

}
