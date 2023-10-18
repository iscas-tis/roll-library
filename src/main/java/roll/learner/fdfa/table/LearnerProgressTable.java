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

import java.util.List;

import roll.learner.fdfa.LearnerLeading;
import roll.learner.fdfa.LearnerProgress;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

abstract class LearnerProgressTable extends LearnerOmegaTable implements LearnerProgress {

    protected final LearnerLeading learnerLeading;
    protected int state;
    protected final Word label;
	public LearnerProgressTable(Options options, Alphabet alphabet
	        , MembershipOracle<HashableValue> membershipOracle
	        , LearnerLeading learnerLeading, int state) {
        super(options, alphabet, membershipOracle);
        this.state = state;
        this.learnerLeading = learnerLeading;
        this.label = learnerLeading.getStateLabel(state);
    }

    @Override
    public Word getLeadingLabel() {
        return label;
    }
    
    @Override
    public LearnerLeading getLearnerLeading() {
        return learnerLeading;
    }

    @Override
    public int getLeadingState() {
        return state;
    }
        
    @Override
    protected Query<HashableValue> processMembershipQuery(ObservationRow row, int offset, ExprValue valueExpr) {
        Word x = row.getWord(); //x
        Word e = valueExpr.get(); //e
        Word suffix = x.concat(e); //(xe)^w
        HashableValue resultLeft = processMembershipQuery(row, label, suffix, offset);
        Query<HashableValue> query = getQuerySimple(row, label, suffix, offset);
        HashableValue result = prepareRowHashableValue(resultLeft.get(), x, e);
        query.answerQuery(result);
        return query;
    }
    
    @Override
    protected HashableValue processMembershipQuery(Word prefix, Word suffix) {
        Word loop = prefix.concat(suffix);
        Query<HashableValue> query = new QuerySimple<>(null, label, loop, -1);
        HashableValue mqResult = membershipOracle.answerMembershipQuery(query);
        HashableValue result = getCeAnalyzerHashableValue(mqResult.get(), prefix, suffix);
        return result;
    }
    
    protected class CeAnalyzerProgressTable extends CeAnalyzerTable {

        public CeAnalyzerProgressTable(ExprValue exprValue, HashableValue result) {
            super(exprValue, result);
        }
        
        @Override
        protected Word getWordExperiment() {
            return this.exprValue.getRight();
        }
    }
    
    @Override
    protected CeAnalyzer getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
        return new CeAnalyzerProgressTable(exprValue, result);
    }
    
    @Override
    public Word getExperimentWordLimit(int state, int letter) {
    	Word wordRep = this.getStateLabel(state);
//    	System.out.println("StateLead = " + state + ", word = " + wordRep.toStringWithAlphabet());
    	List<HashableValue> repRow = this.observationTable.getUpperTableRow(wordRep).getValues();
    	Word wordExt = wordRep.append(letter);
    	List<HashableValue> extRow = this.observationTable.getTableRow(wordExt).getValues();
    	for (int colNr = 0; colNr < this.observationTable.getColumns().size(); colNr ++) {
    		boolean isRepAcc = repRow.get(colNr).isAccepting();
    		boolean isExtAcc = extRow.get(colNr).isAccepting();
//    		System.out.println("repRow: " + repRow.get(colNr) + " = " + isRepAcc);
//    		System.out.println("extRow: " + extRow.get(colNr) + " = " + isExtAcc);
    		Word expr = this.observationTable.getColumns().get(colNr).get();
    		if (isRepAcc && !isExtAcc) {
    			return expr.preappend(letter); 
    		}else if (!isRepAcc && isExtAcc) {
    			return expr;
    		}
    	}
    	throw new RuntimeException("Experiment not found in getExperimentWordLimit(int, int)");
    }

}
