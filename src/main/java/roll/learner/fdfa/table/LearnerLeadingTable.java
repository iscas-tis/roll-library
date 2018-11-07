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

package roll.learner.fdfa.table;

import roll.learner.LearnerType;
import roll.learner.fdfa.LearnerLeading;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWordPair;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LearnerLeadingTable extends LearnerOmegaTable implements LearnerLeading {
    
    public LearnerLeadingTable(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
    }
    
    @Override
    protected ExprValue getInitialColumnExprValue() {
        Word wordEmpty = alphabet.getEmptyWord();
        ExprValue exprValue = getExprValueWord(wordEmpty, wordEmpty);
        return exprValue;
    }
    
    @Override
    protected Query<HashableValue> processMembershipQuery(ObservationRow row, int offset, ExprValue valueExpr) {
        assert valueExpr instanceof ExprValueWordPair;
        Word prefix = row.getWord();        //u
        Word left = valueExpr.getLeft();    //x
        prefix = prefix.concat(left);       //ux
        Word suffix = valueExpr.getRight();  // ux(y)^w
        HashableValue result = processMembershipQuery(row, prefix, suffix, offset);
        Query<HashableValue> query = getQuerySimple(row, prefix, suffix, offset);
        query.answerQuery(result);
        return query;
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.FDFA_LEADING_TABLE;
    }
    
    @Override
    protected boolean isAccepting(int state) {
        return false;
    }
    
    //only for values
    @Override
    protected HashableValue processMembershipQuery(Word prefix, Word suffix) {
        prefix = prefix.concat(suffix);
        assert loop != null;
        Query<HashableValue> query = new QuerySimple<>(null, prefix, loop, -1);
        return membershipOracle.answerMembershipQuery(query);
    }
    
    // remember the loop of current counterexample
    protected Word loop;
    
    protected class CeAnalyzerLeadingTable extends CeAnalyzerTable {
        private final CeAnalysisLeadingHelper ceAnalysisLeadingHelper;
        public CeAnalyzerLeadingTable(ExprValue exprValue, HashableValue result, LearnerLeading learner) {
            super(exprValue, result);
            this.ceAnalysisLeadingHelper = new CeAnalysisLeadingHelper(learner);
        }
        
        @Override
        protected Word getWordExperiment() {
            return ceAnalysisLeadingHelper.computeWordExperiment(exprValue);
        }

        @Override
        protected void update(CeAnalysisResult result) {
            wordExpr = ceAnalysisLeadingHelper.computeNewExprValue(exprValue, result);
        }
    }
    
    @Override
    protected CeAnalyzer getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
        return new CeAnalyzerLeadingTable(exprValue, result, this);
    }

    @Override
    public void setCeAnalysisLoop(Word loop) {
        this.loop = loop;
    }

    @Override
    public Word getCeAnalysisLoop() {
        return loop;
    }
    
    public static class CeAnalysisLeadingHelper {
        LearnerLeading learnerLeading;
        public CeAnalysisLeadingHelper(LearnerLeading learnerLeading) {
            this.learnerLeading = learnerLeading;
        }
        
        public Word computeWordExperiment(ExprValue exprValue) {
            learnerLeading.setCeAnalysisLoop(exprValue.getRight());
            return exprValue.getLeft();
        }
        
        public ExprValue computeNewExprValue(ExprValue exprValue, CeAnalysisResult result) {
            Word wordCE = exprValue.getLeft();
            return learnerLeading.getExprValueWord(wordCE.getSuffix(result.breakIndex + 1)
                    , learnerLeading.getCeAnalysisLoop());  // y[j+1..n]
        }
    }

    @Override
    public Word getStateLabel(int state) {
        // TODO Auto-generated method stub
        return null;
    }

}
