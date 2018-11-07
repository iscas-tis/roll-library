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

package roll.learner.nfa.table;

import java.util.ArrayList;
import java.util.List;

import roll.automata.NFA;
import roll.automata.StateNFA;
import roll.learner.LearnerFA;
import roll.learner.LearnerType;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWord;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.table.ObservationTableAbstract;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * make use of left congruence of a reverse deterministic finite automaton (rDFA)
 * 
 * */

public class LearnerNFATable extends LearnerFA<NFA> {

    protected ObservationTableAbstract observationTable;
    
    public LearnerNFATable(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        observationTable = new ObservationTableNFA();
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.NFA_RDSTAR;
    }
    
    @Override
    protected ExprValue getInitialColumnExprValue() {
        Word wordEmpty = alphabet.getEmptyWord();
        ExprValue exprValue = getExprValueWord(wordEmpty);
        return exprValue;
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
                // newWord = a . row
                Word newWord = lowerRow.getWord().preappend(letterNr);      // preappend
                ObservationRow row = observationTable.getTableRow(newWord); // already existing
                if(row != null) continue;
                ObservationRow newRow = observationTable.addLowerRow(newWord);
                newLowerRows.add(newRow);
            }
            // 3. process membership queries
            processMembershipQueries(newLowerRows, 0, observationTable.getColumns().size());
            lowerRow = observationTable.getUnclosedLowerRow();
        }
        
        constructHypothesis();
    }
    
    @Override
    protected void constructHypothesis() {
        
        hypothesis = new NFA(alphabet);
        
        List<ObservationRow> upperTable = observationTable.getUpperTable();
        
        for(int rowNr = 0; rowNr < upperTable.size(); rowNr ++) {
            hypothesis.createState();
        }
        
        ISet inits = UtilISet.newISet();
        for(int rowNr = 0; rowNr < upperTable.size(); rowNr ++) {
            for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
                int predNr = getPredecessorRow(rowNr, letterNr);
                assert predNr != -1: "predecessor index -1";
                StateNFA state = hypothesis.getState(predNr);
                state.addTransition(letterNr, rowNr);
            }
            
            if(upperTable.get(rowNr).getWord().isEmpty()) {
                hypothesis.setFinal(rowNr);
            }
            
            if(isAccepting(rowNr)) {
                inits.set(rowNr);
            }
        }
        
//        if(inits.isEmpty()) {
//            nfa.createState();
//            nfa.setInitial(upperTable.size());
//        }else 
        if(inits.cardinality() <= 1) {
            for(final int init : inits) {
                hypothesis.setInitial(init);
            }
        }else {
            // |inits| > 1
            hypothesis.createState();
            hypothesis.setInitial(upperTable.size());
            StateNFA initState = hypothesis.getState(upperTable.size());
            for(final int init : inits) {
                StateNFA state = hypothesis.getState(init);
                for(final int letter : state.getEnabledLetters()) {
                    for(final int succ : state.getSuccessors(letter)) {
                        initState.addTransition(letter, succ);
                    }
                }
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
    
    protected int getPredecessorRow(int state, int letter) {
        ObservationRow stateRow = observationTable.getUpperTable().get(state);
        // get the predecessor
        Word predWord = stateRow.getWord().preappend(letter);
        // search in upper table
        return observationTable.getUpperTableRowIndex(predWord);
    }

    // 
    protected HashableValue processMembershipQuery(Word row, Word column) {
        // the membership query for column . row
        Query<HashableValue> query = new QuerySimple<>(null, column, row, -1);
        return membershipOracle.answerMembershipQuery(query);
    }
    
    protected HashableValue processMembershipQuery(Query<HashableValue> query) {
        return membershipOracle.answerMembershipQuery(query);
    }

    protected void processMembershipQueries(List<ObservationRow> rows
            , int colOffset, int length) {
        List<Query<HashableValue>> results = new ArrayList<>();
        List<ExprValue> columns = observationTable.getColumns();
        int endNr = length + colOffset;
        for(ObservationRow row : rows) {
            for(int colNr = colOffset; colNr < endNr; colNr ++) {
                results.add(processMembershipQuery(row, colNr, columns.get(colNr)));
            }
        }
        putQueryAnswers(results);
    }
    
    protected Query<HashableValue> processMembershipQuery(ObservationRow row, int offset, ExprValue valueExpr) {
        // ask membership query for column . row
        Query<HashableValue> query = new QuerySimple<>(row, valueExpr.get(), row.getWord(), offset);
        HashableValue result = membershipOracle.answerMembershipQuery(query);
        // put result for (row, column) 
        Query<HashableValue> queryResult = new QuerySimple<>(row, row.getWord(), valueExpr.get(), offset);
        queryResult.answerQuery(result);
        return queryResult;
    }
        
    protected void putQueryAnswers(List<Query<HashableValue>> queries) {
        for(Query<HashableValue> query : queries) {
            putQueryAnswers(query);
        }
    }
    
    protected void putQueryAnswers(Query<HashableValue> query) {
        ObservationRow row = query.getPrefixRow();
        HashableValue result = query.getQueryAnswer();
        assert result != null;
        row.set(query.getSuffixColumn(), result);
    }

    @Override
    public void refineHypothesis(Query<HashableValue> ceQuery) {
        ExprValue exprValue = getCounterExampleWord(ceQuery);
        HashableValue result = ceQuery.getQueryAnswer();
        if(result == null) {
            result = processMembershipQuery(ceQuery);
        }
        CeAnalyzer analyzer = new CeAnalyzer(exprValue, result);
        analyzer.analyze();
        observationTable.addColumn(analyzer.getNewExpriment()); // add new experiment
        processMembershipQueries(observationTable.getUpperTable(), observationTable.getColumns().size() - 1, 1);
        processMembershipQueries(observationTable.getLowerTable(), observationTable.getColumns().size() - 1, 1);
        
        makeTableClosed();
    }
    
    private class CeAnalyzer {

        private final Word wordCE;
        private final HashableValue mqResult;
        private ExprValue experiment;
        
        public CeAnalyzer(ExprValue exprValue, HashableValue result) {
            wordCE = exprValue.get();
            mqResult = result;
        }
        
        public void analyze() {
            // get the initial state from automaton
            // find a state u such that the MQ for wordCE[1..k] . a . u
            // and for wordCE[1..k] . u' not the same
            // u
            Word predWord = alphabet.getEmptyWord();
            for(int k = wordCE.length() - 1; k >= 1; k --) {
                int letter = wordCE.getLetter(k);
                Word currWord = predWord.preappend(letter);
                int upperIndex = observationTable.getUpperTableRowIndex(currWord);
                assert upperIndex != -1;
                // u'
                Word reprWord = observationTable.getUpperTable().get(upperIndex).getWord();
                // prefix
                Word prefix = wordCE.getPrefix(k);
                HashableValue currResult = processMembershipQuery(reprWord, prefix);
                if(! currResult.valueEqual(mqResult)) {
                    experiment = new ExprValueWord(prefix);
                    break;
                }
                // update u
                predWord = reprWord;
            }
        }
        
        public ExprValue getNewExpriment() {
            return experiment;
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

    @Override
    protected Word getStateLabel(int state) {
        return null;
    }

}
