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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TIntProcedure;
import roll.automata.DFA;
import roll.automata.StateNFA;
import roll.learner.LearnerType;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.table.ObservationTableAbstract;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * original L* proposed by Dana Angulin
 *    Learning Regular Sets from Queries and Counterexamples
 *    
 *    
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LearnerDFATableLStar extends LearnerDFATable {

    public LearnerDFATableLStar(Options options
            , Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
    }
    
    // main loop for L* algorithm 
    // a table is closed here 
    // when it is both closed and consistent
    @Override
    protected void makeTableClosed() {
        boolean ready = false;
        while(! ready) {
            ready = closeTable();
            ready = ready && makeTableConsistent();  
        }
        constructHypothesis();
    }
    
    @Override
    protected void constructHypothesis() {
        hypothesis = new DFA(alphabet);
        List<ObservationRow> upperTable = observationTable.getUpperTable();

        // get all distinguished rows in upper table
        TObjectIntMap<List<HashableValue>> valuesMap = new TObjectIntHashMap<>();
        TIntIntMap stateIndexMap = new TIntIntHashMap();
        for(int rowNr = 0; rowNr < upperTable.size(); rowNr ++) {
            List<HashableValue> values = upperTable.get(rowNr).getValues();
            if(valuesMap.containsKey(values)) continue;
            // new state now
            StateNFA state = hypothesis.createState();
            stateIndexMap.put(rowNr, state.getId());
            valuesMap.put(values, rowNr);
        }
        
        // add transitions now
        valuesMap.forEachValue(new TIntProcedure() {
            @Override
            public boolean execute(int rowNr) {
                // we first get current state
                StateNFA state = hypothesis.getState(stateIndexMap.get(rowNr));
                for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
                    List<HashableValue> succValues = getSuccessorRowValues(rowNr, letter);
                    assert succValues != null : "Didnot find successor in upper table";
                    assert valuesMap.containsKey(succValues): "Didnot find row index in value map";
                    int succRowNr = valuesMap.get(succValues);
                    state.addTransition(letter, stateIndexMap.get(succRowNr));
                }
                
                if(isAccepting(rowNr)) {
                    hypothesis.setFinal(state.getId());
                }
                
                // we use rowNr because it checked the specific row in upper table
                if(getStateLabel(rowNr).isEmpty()) {
                    hypothesis.setInitial(state.getId());
                }
                
                return true;
            }
            
        });
    }
    
    
    private boolean closeTable() {
        ObservationRow lowerRow = observationTable.getUnclosedLowerRow();
        boolean isClosed = lowerRow == null;
        
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
        
        return isClosed;
    }
    
    private boolean makeTableConsistent() {
        ExprValue exprValue = observationTable.getInconsistentColumn();
        boolean isConsistent = exprValue == null;
        
        while(exprValue != null) {
            // 1. add to columns
            int columnIndex = observationTable.addColumn(exprValue);
            // 2. add result of new column to upper table
            processMembershipQueries(observationTable, observationTable.getUpperTable(), columnIndex, 1);
            // 3. process membership queries
            processMembershipQueries(observationTable, observationTable.getLowerTable(), columnIndex, 1);
            exprValue = observationTable.getInconsistentColumn();
        }
        
        return isConsistent;
    }

    // return counter example for hypothesis
    @Override
    public void refineHypothesis(Query<HashableValue> ceQuery) {
        Word ceWord = ceQuery.getQueriedWord();
        
        // get all prefixes that are not in upper table
        LinkedHashSet<Word> prefixes = getAllValidPrefixes(ceWord);
        // get lower prefix row and new rows
        List<ObservationRow> existRows = new ArrayList<>();
        List<ObservationRow> newRows = new ArrayList<>();
        for(Word prefix : prefixes) {
            ObservationRow lowerRow = observationTable.getLowerTableRow(prefix);
            if(lowerRow == null) { // check existence of current prifix
                newRows.add(observationTable.addUpperRow(prefix));
            }else {
                existRows.add(lowerRow);
            }
        }
        
        // move existing rows from lower table to upper table
        for(ObservationRow row : existRows) {
            observationTable.moveRowFromLowerToUpper(row);
        }
        
        // ask membership for new added rows
        List<ObservationRow> newLowerRows = addLowerRowsFromRowsWithExtension(existRows);
        newLowerRows.addAll(addLowerRowsFromRowsWithExtension(newRows));
        processMembershipQueries(observationTable, newRows, 0, observationTable.getColumns().size());
        processMembershipQueries(observationTable, newLowerRows, 0, observationTable.getColumns().size());
        
        makeTableClosed();
    }
    
    // input S, add S.A
    private List<ObservationRow> addLowerRowsFromRowsWithExtension(List<ObservationRow> rows) {
        List<ObservationRow> lowerRows = new LinkedList<>();
        for(ObservationRow row : rows) {
            for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
                Word word = row.getWord().append(letter);
                ObservationRow tableRow = observationTable.getTableRow(word); 
                if(tableRow == null) { // check existence
                    tableRow = observationTable.addLowerRow(word);
                    lowerRows.add(tableRow);
                }
            }
        }
        return lowerRows;
    }
    
    // get all prefixes of counter example which are not in the upper table
    private LinkedHashSet<Word> getAllValidPrefixes(Word word) {
        LinkedHashSet<Word> prefixes = new LinkedHashSet<>();
        for(int length = 1; length <= word.length() ; length ++) {
            Word prefix = word.getSubWord(0, length);
            boolean valid = true;
            //search upper table
            for(ObservationRow row : observationTable.getUpperTable()) {
                Word upperWord = row.getWord();
                if(upperWord.equals(prefix)) {
                    valid = false;
                    break;
                }
            }
            if(valid) prefixes.add(prefix);
        }
        return prefixes;
    }
    
    private List<HashableValue> getSuccessorRowValues(int state, int letter) {
        ObservationRow stateRow = observationTable.getUpperTable().get(state);
        Word succWord = stateRow.getWord().append(letter);

        // search in upper table
        for(int succ = 0; succ < observationTable.getUpperTable().size(); succ ++) {
            ObservationRow succRow = observationTable.getUpperTable().get(succ);
            if(succRow.getWord().equals(succWord)) {
                return succRow.getValues();
            }
        }
        // search in lower table
        ObservationRow succRow = observationTable.getLowerTableRow(succWord);
        assert succRow != null;
        for(int succ = 0; succ < observationTable.getUpperTable().size(); succ ++) {
            ObservationRow upperRow = observationTable.getUpperTable().get(succ);
            if(succRow.valuesEqual(upperRow)) {
                return succRow.getValues();
            }
        }
        assert false : "successor values not found";
        return null;
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.DFA_LSTAR;
    }

    @Override
    protected CeAnalyzer getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
        return null;
    }
    
    @Override
    protected ObservationTableAbstract getTableInstance() {
        return new ObservationTableDFALStar(alphabet);
    }

}
