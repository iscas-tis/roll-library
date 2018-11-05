/* Copyright (c) 2018 -                                                   */
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

package roll.learner.nfa.nlstar;

import java.util.ArrayList;
import java.util.List;

import roll.automata.NFA;
import roll.automata.StateNFA;
import roll.learner.LearnerBase2;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * uniform NL* learning framework for NFA
 * */

public abstract class LearnerNFATable extends LearnerBase2<NFA> {

    protected ObservationTableNLStar observationTable;
    
    public LearnerNFATable(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        observationTable = new ObservationTableNLStar(this, alphabet);
    }
    
    @Override
    protected void initialize() {
        observationTable.clear();
        Word wordEmpty = alphabet.getEmptyWord();
        observationTable.addUpperRow(wordEmpty);
        ExprValue exprValue = getInitialColumnExprValue();
        
        // add empty word column
        observationTable.addColumn(exprValue);
        // add every alphabet
        for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
            observationTable.addLowerRow(alphabet.getLetterWord(letterNr));
        }
        
        // ask initial queries for upper table
        processMembershipQueries(observationTable, observationTable.getUpperTable()
                , 0, observationTable.getColumns().size());
        // ask initial queries for lower table
        processMembershipQueries(observationTable, observationTable.getLowerTable()
                , 0, observationTable.getColumns().size());
        
        makeTableComplete();        
    }
    

    
    // ----------------------- make table closed and consistent ---------------------
    protected void makeTableComplete() {
        boolean ready = false;
        while(! ready) {
            ready = makeTableClosed();
            ready = ready && makeTableConsistent();  
        }
        constructHypothesis();
    }

    protected boolean makeTableClosed() {
        ObservationRow lowerRow = observationTable.getUnclosedLowerRow();
        boolean closed = lowerRow == null;
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
        return closed;
    }

    protected boolean makeTableConsistent() {
        ExprValue newColumn = observationTable.getInconsistentColumn();     
        boolean consistent = newColumn == null;
        while(newColumn != null) {
            // 1. add to columns
            int columnIndex = observationTable.addColumn(newColumn);
            // 2. add result of new column to upper table
            processMembershipQueries(observationTable, observationTable.getUpperTable(), columnIndex, 1);
            // 3. process membership queries
            processMembershipQueries(observationTable, observationTable.getLowerTable(), columnIndex, 1);
            newColumn = observationTable.getInconsistentColumn();
        }
        return consistent;
    }

    // -----------------------------------------------------------------------------------
    
    @Override
    protected void constructHypothesis() {
        hypothesis = new NFA(alphabet);
        List<ObservationRow> upperPrimes = observationTable.getUpperPrimes();
        
        for(int rowNr = 0; rowNr < upperPrimes.size(); rowNr ++) {
            hypothesis.createState();
        }
        ISet inits = UtilISet.newISet();
        ObservationRow emptyRow = observationTable.getTableRow(alphabet.getEmptyWord());
        for(int rowNr = 0; rowNr < upperPrimes.size(); rowNr ++) {
            StateNFA state = hypothesis.getState(rowNr);
            for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
                for(int succNr : getSuccessorRows(upperPrimes, rowNr, letterNr)) {
                    state.addTransition(letterNr, succNr);
                }
            }
            ObservationRow row = upperPrimes.get(rowNr);
            if(emptyRow.covers(row)) {
                inits.set(rowNr);
            }
            if(isAccepting(upperPrimes, rowNr)) {
                hypothesis.setFinal(rowNr);
            }
        }
        
        if(inits.cardinality() <= 1) {
            for(int init : inits) {
                hypothesis.setInitial(init);
            }
        }else {
            hypothesis.createState();
            hypothesis.setInitial(upperPrimes.size());
            StateNFA initState = hypothesis.getState(upperPrimes.size());
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
    
    protected ISet getSuccessorRows(List<ObservationRow> upperPrimes, int state, int letter) {
        ObservationRow stateRow = upperPrimes.get(state);
        // get the predecessor
        Word word = stateRow.getWord().append(letter);
        ISet succs = UtilISet.newISet();
        ObservationRow succRow = observationTable.getTableRow(word);
        for(int index = 0; index < upperPrimes.size(); index ++) {
            ObservationRow upperPrime = upperPrimes.get(index);
            if(succRow.covers(upperPrime)) {
                succs.set(index);
            }
        }
        return succs;
    }
    
    // a state is accepting iff it accepts empty language
    protected abstract boolean isAccepting(List<ObservationRow> upperPrimes, int state);
    
    protected abstract int addNewColumnsToTable(Query<HashableValue> query);

    @Override
    public void refineHypothesis(Query<HashableValue> query) {
        int size = observationTable.getColumns().size();
        int number = addNewColumnsToTable(query);
        processMembershipQueries(observationTable, observationTable.getUpperTable(), size, number);
        processMembershipQueries(observationTable, observationTable.getLowerTable(), size, number);
        
        makeTableComplete();
    }
    
    // ----------------------------------------------------------------------
    // we have e and a and make a. e for the new column
    protected abstract ExprValue makeInconsistencyColumn(ExprValue exprValue, int preletter);
    // ----------------------------------------------------------------------

    @Override
    public String toHTML() {
        return "<pre>" + toString() + "</pre>";
    }
    
    @Override
    public String toString() {
        return observationTable.toString();
    }
    

    @Override
    public Word getLabelWord(int state) {
        List<ObservationRow> primeRows = observationTable.getUpperPrimes();
        if(state >= 0 && state < primeRows.size()) {
            return primeRows.get(state).getWord();
        }
        return null;
    }
    
    public int getLabelNum() {
        return observationTable.getUpperPrimes().size();
    }


}
