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

import roll.learner.LearnerType;
import roll.learner.dfa.tree.ValueNode;
import roll.learner.fdfa.LearnerLeading;
import roll.learner.fdfa.table.LearnerLeadingTable.CeAnalysisLeadingHelper;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWordPair;
import roll.table.HashableValue;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LearnerLeadingTree extends LearnerOmegaTree implements LearnerLeading{

    public LearnerLeadingTree(Options options, Alphabet alphabet
            , MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.FDFA_LEADING_TREE;
    }
    
    @Override
    protected HashableValue processMembershipQuery(Word label, ExprValue valueExpr) {
        assert valueExpr instanceof ExprValueWordPair;
        ExprValueWordPair valueExprPair = (ExprValueWordPair) valueExpr;
        Query<HashableValue> query = getQuerySimple(label.concat(valueExprPair.getLeft()), valueExprPair.getRight());
        return membershipOracle.answerMembershipQuery(query);
    }
    
    @Override
    protected HashableValue processMembershipQuery(Word prefix, Word suffix) {
        prefix = prefix.concat(suffix);
        assert loop != null;
        Query<HashableValue> query = new QuerySimple<>(null, prefix, loop, -1);
        return membershipOracle.answerMembershipQuery(query);
    }
    
    @Override
    protected boolean isAccepting(ValueNode state) {
        return false;
    }
    
    // remember the loop of current counterexample
    protected Word loop;
    
    protected class CeAnalyzerLeadingTree extends CeAnalyzerTree {
        
        private final CeAnalysisLeadingHelper ceAnalysisLeadingHelper;
        public CeAnalyzerLeadingTree(ExprValue exprValue, HashableValue result, LearnerLeading learner) {
            super(exprValue, result);
            this.ceAnalysisLeadingHelper = new CeAnalysisLeadingHelper(learner);
        }
        
        @Override
        public void analyze() {
            this.leafBranch = result;
            this.nodePrevBranch = getHashableValueBoolean(!result.isAccepting());
            // only has one leaf
            if(tree.getRoot().isLeaf()) {
                this.wordExpr = getExprValueWord(alphabet.getEmptyWord(), this.exprValue.getRight());
                this.nodePrev = tree.getRoot();
                this.wordLeaf = getExprValueWord(exprValue.getLeft());
                return ;
            }
            // when root is not a terminal node
            CeAnalysisResult result = findBreakIndex();
            update(result);
        }
        
        @Override
        protected Word getWordExperiment() {
            return ceAnalysisLeadingHelper.computeWordExperiment(exprValue);
        }

        @Override
        protected void update(CeAnalysisResult result) {
            Word wordCE = getWordExperiment();
            Word wordPrev = getStateLabel(result.prevState);         // S(j-1)
            this.wordExpr = ceAnalysisLeadingHelper.computeNewExprValue(exprValue, result);  // y[j+1..n]
            this.wordLeaf = getExprValueWord(wordPrev.append(wordCE.getLetter(result.breakIndex))); // S(j-1)y[j]
            this.nodePrev = states.get(result.currState).node;          // S(j)
        }
    }
    
    @Override
    protected CeAnalyzerTree getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
        return new CeAnalyzerLeadingTree(exprValue, result, this);
    }

    @Override
    public void setCeAnalysisLoop(Word loop) {
        this.loop = loop;
    }

    @Override
    public Word getCeAnalysisLoop() {
        return loop;
    }

}
