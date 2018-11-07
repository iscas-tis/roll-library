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

package roll.learner.dfa.table;

import java.util.ArrayList;
import java.util.List;

import roll.automata.DFA;
import roll.automata.StateNFA;
import roll.learner.LearnerDFA;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.table.ObservationTableAbstract;
import roll.words.Alphabet;
import roll.words.Word;

public abstract class LearnerDFATable extends LearnerDFA {
    
    protected ObservationTableAbstract observationTable;
    
    public LearnerDFATable(Options options, Alphabet alphabet
            , MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        this.observationTable = getTableInstance();
    }
    
    @Override
    protected void initialize() {
        initializeTable(observationTable);
        makeTableClosed();
    }
    
    protected void makeTableClosed() {
        ObservationRow lowerRow = observationTable.getUnclosedLowerRow();
        
        while(lowerRow != null) {
            // 1. move to upper table
            observationTable.moveRowFromLowerToUpper(lowerRow);
            // 2. add one letter to lower table
            List<ObservationRow> newLowerRows = new ArrayList<>();
            for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
                Word newWord = lowerRow.getWord().append(letterNr);
                ObservationRow row = observationTable.getTableRow(newWord); // already existing
                if(row != null) continue;
                ObservationRow newRow = observationTable.addLowerRow(newWord);
                newLowerRows.add(newRow);
            }
            // 3. process membership queries
            processMembershipQueries(observationTable, newLowerRows, 0, observationTable.getColumns().size());
            lowerRow = observationTable.getUnclosedLowerRow();
        }
        
        constructHypothesis();
    }
    
    // return counter example for hypothesis
    @Override
    public void refineHypothesis(Query<HashableValue> ceQuery) {
        
        ExprValue exprValue = getCounterExampleWord(ceQuery);
        HashableValue result = ceQuery.getQueryAnswer();
        if(result == null) {
            result = processMembershipQuery(ceQuery);
        }
        CeAnalyzer analyzer = getCeAnalyzerInstance(exprValue, result);
        analyzer.analyze();
        observationTable.addColumn(analyzer.getNewExpriment()); // add new experiment
        processMembershipQueries(observationTable, observationTable.getUpperTable(), observationTable.getColumns().size() - 1, 1);
        processMembershipQueries(observationTable, observationTable.getLowerTable(), observationTable.getColumns().size() - 1, 1);
        
        makeTableClosed();
    }
    
    
    // Default learner for DFA
    @Override
    protected void constructHypothesis() {
        
        hypothesis = new DFA(alphabet);
        
        List<ObservationRow> upperTable = observationTable.getUpperTable();
        
        for(int rowNr = 0; rowNr < upperTable.size(); rowNr ++) {
            hypothesis.createState();
        }
        
        for(int rowNr = 0; rowNr < upperTable.size(); rowNr ++) {
            StateNFA state = hypothesis.getState(rowNr);
            for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
                int succNr = getSuccessorRow(rowNr, letterNr);
                state.addTransition(letterNr, succNr);
            }
            if(getStateLabel(rowNr).isEmpty()) {
                hypothesis.setInitial(rowNr);
            }
            if(isAccepting(rowNr)) {
                hypothesis.setFinal(rowNr);
            }
        }
        
    }
    
    // a state is accepting iff it accepts empty language
    protected boolean isAccepting(int state) {
        ObservationRow stateRow = observationTable.getUpperTable().get(state);
        int emptyNr = observationTable.getColumnIndex(getExprValueWord(alphabet.getEmptyWord()));
        assert emptyNr != -1 : "index -> " + emptyNr;
        return stateRow.getValues().get(emptyNr).isAccepting();
    }

    protected int getSuccessorRow(int state, int letter) {
        ObservationRow stateRow = observationTable.getUpperTable().get(state);
        Word succWord = stateRow.getWord().append(letter);
        int succ = observationTable.getUpperTableRowIndex(succWord);
        return succ;
    }

    
    @Override
    public Word getStateLabel(int state) {
        return observationTable.getUpperTable().get(state).getWord();
    }
    
    protected ObservationTableAbstract getTableInstance() {
        return new ObservationTableDFA();
    }
    
    protected class CeAnalyzerTable extends CeAnalyzer {

        public CeAnalyzerTable(ExprValue exprValue, HashableValue result) {
            super(exprValue, result);
        }

        @Override
        protected void update(CeAnalysisResult result) {
            Word wordCE = getWordExperiment();
            wordExpr = getExprValueWord(wordCE.getSuffix(result.breakIndex + 1));  // y[j+1..n]
        }
    }

    @Override
    public String toString() {
        return observationTable.toString();
    }
    
    @Override
    public String toHTML() {
        return "<pre>" + toString() + "</pre>";
    }

    
}
