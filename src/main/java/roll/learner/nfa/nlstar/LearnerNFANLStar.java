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

package roll.learner.nfa.nlstar;

import java.util.ArrayList;
import java.util.List;

import roll.automata.NFA;
import roll.automata.StateNFA;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWord;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Benedikt Bollig, Peter Habermehl, Carsten Kern, and Martin Leucker
 * "Angluin-Style Learning of NFA" in IJCAI 2009.
 * 
 * */

public class LearnerNFANLStar extends LearnerBase<NFA> {

    private boolean alreadyStarted = false;
    protected NFA nfa;
    protected ObservationTableNLStar observationTable;
    
    public LearnerNFANLStar(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        observationTable = new ObservationTableNLStar(alphabet);
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.NFA_NLSTAR;
    }

    @Override
    public void startLearning() {
        if(alreadyStarted) {
            throw new UnsupportedOperationException("Learner can not start twice");
        }
        alreadyStarted = true;
        initialize();
    }
    
    protected void initialize() {
        observationTable.clear();
        Word wordEmpty = alphabet.getEmptyWord();
        observationTable.addUpperRow(wordEmpty);
        ExprValue exprValue = new ExprValueWord(wordEmpty);
        
        // add empty word column
        observationTable.addColumn(exprValue);
        // add every alphabet
        for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
            observationTable.addLowerRow(alphabet.getLetterWord(letterNr));
        }
        
        // ask initial queries for upper table
        processMembershipQueries(observationTable.getUpperTable()
                , 0, observationTable.getColumns().size());
        // ask initial queries for lower table
        processMembershipQueries(observationTable.getLowerTable()
                , 0, observationTable.getColumns().size());
        
        makeTableComplete();        
    }
    
    protected Query<HashableValue> processMembershipQuery(ObservationRow row, int offset, ExprValue valueExpr) {
        Query<HashableValue> query = new QuerySimple<>(row, row.getWord(), valueExpr.get(), offset);
        HashableValue result = membershipOracle.answerMembershipQuery(query);
        Query<HashableValue> queryResult = new QuerySimple<>(row, row.getWord(), valueExpr.get(), offset);
        queryResult.answerQuery(result);
        return queryResult;
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
            processMembershipQueries(newLowerRows, 0, observationTable.getColumns().size());
            lowerRow = observationTable.getUnclosedLowerRow();
        }
        return closed;
    }

    protected boolean makeTableConsistent() {
        Word suffix = observationTable.getInconsistentColumn();     
        boolean consistent = suffix == null;
        while(suffix != null) {
            // 1. add to columns
            int columnIndex = observationTable.addColumn(getExprValueWord(suffix));
            // 2. add result of new column to upper table
            processMembershipQueries(observationTable.getUpperTable(), columnIndex, 1);
            // 3. process membership queries
            processMembershipQueries(observationTable.getLowerTable(), columnIndex, 1);
            suffix = observationTable.getInconsistentColumn();
        }
        return consistent;
    }

    // -----------------------------------------------------------------------------------
    
    protected void constructHypothesis() {
        nfa = new NFA(alphabet);
        List<ObservationRow> upperPrimes = observationTable.getUpperPrimes();
        
        for(int rowNr = 0; rowNr < upperPrimes.size(); rowNr ++) {
            nfa.createState();
        }
        
        ISet inits = UtilISet.newISet();
        ObservationRow emptyRow = observationTable.getTableRow(alphabet.getEmptyWord());
        for(int rowNr = 0; rowNr < upperPrimes.size(); rowNr ++) {
            StateNFA state = nfa.getState(rowNr);
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
                nfa.setFinal(rowNr);
            }
        }
        
        if(inits.cardinality() <= 1) {
            for(int init : inits) {
                nfa.setInitial(init);
            }
        }else {
            nfa.createState();
            nfa.setInitial(upperPrimes.size());
            StateNFA initState = nfa.getState(upperPrimes.size());
            for(final int init : inits) {
                StateNFA state = nfa.getState(init);
                for(final int letter : state.getEnabledLetters()) {
                    for(final int succ : state.getSuccessors(letter)) {
                        initState.addTransition(letter, succ);
                    }
                }
            }
        }
    }
    
    // a state is accepting iff it accepts empty language
    protected boolean isAccepting(List<ObservationRow> upperPrimes, int state) {
        ObservationRow stateRow = upperPrimes.get(state);
        int emptyNr = observationTable.getColumnIndex(getExprValueWord(alphabet.getEmptyWord()));
        assert emptyNr != -1 : "index -> " + emptyNr;
        return stateRow.getValues().get(emptyNr).isAccepting();
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
    
    @Override
    public NFA getHypothesis() {
        return nfa;
    }

    @Override
    public void refineHypothesis(Query<HashableValue> query) {
        Word wordCE = query.getQueriedWord();
        int number = 0;
        int size = observationTable.getColumns().size();
        for(int offset = 0; offset < wordCE.length(); offset ++) {
            Word suffix = wordCE.getSuffix(offset);
            if(observationTable.isInColumn(suffix)) {
                continue;
            }
            ExprValue exprValue = new ExprValueWord(suffix);
            observationTable.addColumn(exprValue); // add new experiment
            number ++;
        }        
        processMembershipQueries(observationTable.getUpperTable(), size, number);
        processMembershipQueries(observationTable.getLowerTable(), size, number);
        
        makeTableComplete();
    }

    @Override
    public String toHTML() {
        return "<pre>" + toString() + "</pre>";
    }
    
    @Override
    public String toString() {
        return observationTable.toString();
    }

}
