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

public class LearnerLeadingTable extends LearnerOmegaTable implements LearnerLeading {
    
    public LearnerLeadingTable(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
    }
    
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
    
    //only for values
    @Override
    protected HashableValue processMembershipQuery(Word prefix, Word suffix) {
        prefix = prefix.concat(suffix);
        assert loop != null;
        Query<HashableValue> query = new QuerySimple<>(null, prefix, loop, -1);
        return membershipOracle.answerMembershipQuery(query);
    }
    
    protected Word loop;
    
    protected class CeAnalyzerLeadingTable extends CeAnalyzerTable {

        public CeAnalyzerLeadingTable(ExprValue exprValue, HashableValue result) {
            super(exprValue, result);
        }
        
        @Override
        protected Word getWordExperiment() {
            loop = this.exprValue.getRight();
            return this.exprValue.getLeft();
        }

        @Override
        protected void update(CeAnalysisResult result) {
            Word wordCE = exprValue.getLeft();
            wordExpr = getExprValueWord(wordCE.getSuffix(result.breakIndex + 1), loop);  // y[j+1..n]
        }
    }
    
    @Override
    protected CeAnalyzer getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
        return new CeAnalyzerLeadingTable(exprValue, result);
    }

}
