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

package roll.learner;

import java.util.ArrayList;
import java.util.List;

import roll.main.IHTML;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWord;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.table.ObservationRow;
import roll.table.ObservationTable;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public abstract class LearnerBase2<M> implements Learner<M, HashableValue>, IHTML {
    
    private boolean alreadyStarted = false;
    
    protected final Alphabet alphabet;
    protected final MembershipOracle<HashableValue> membershipOracle;
    protected final Options options;
    protected M hypothesis;
    
    public LearnerBase2(Options options, Alphabet alphabet
            , MembershipOracle<HashableValue> membershipOracle) {
        assert options != null && alphabet != null && membershipOracle != null;
        this.options = options;
        this.alphabet = alphabet;
        this.membershipOracle = membershipOracle;
    }
    
    @Override
    public Options getOptions() {
        return options;
    }
    
    @Override
    public void startLearning() {
        if (alreadyStarted)
            try {
                throw new Exception("Learner should not be started twice");
            } catch (Exception e) {
                e.printStackTrace();
            }
        alreadyStarted = true;
        initialize();
    }
    
    @Override
    public M getHypothesis() {
        return hypothesis;
    }
    
    // ---------------------------------------------------------------------
    // only for the finite automata learning
    protected ExprValue getCounterExampleWord(Query<HashableValue> query) {
        assert query != null;
        Word word = query.getQueriedWord();
        assert word != null;
        return new ExprValueWord(word);
    }
    
    protected ExprValue getExprValueWord(Word word) {
        return new ExprValueWord(word);
    }
    
    protected HashableValue getHashableValueBoolean(boolean result) {
        return new HashableValueBoolean(result);
    }
    
    protected HashableValue processMembershipQuery(Word prefix, Word suffix) {
        Query<HashableValue> query = new QuerySimple<>(null, prefix, suffix, -1);
        return membershipOracle.answerMembershipQuery(query);
    }
    
    protected HashableValue processMembershipQuery(Query<HashableValue> query) {
        return membershipOracle.answerMembershipQuery(query);
    }
    // ---------------------------------------------------------------------
    // specialized for learning algorithm based on observation table
    
    protected abstract Query<HashableValue> makeMembershipQuery(ObservationRow row, int offset, ExprValue exprValue);
    
    protected Query<HashableValue> processMembershipQuery(ObservationRow row, int offset, ExprValue exprValue) {
        Query<HashableValue> query = makeMembershipQuery(row, offset, exprValue);
        HashableValue result = membershipOracle.answerMembershipQuery(query);
        Query<HashableValue> queryResult = makeMembershipQuery(row, offset, exprValue);
        queryResult.answerQuery(result);
        return queryResult;
    }
    
    protected void processMembershipQueries(ObservationTable observationTable, List<ObservationRow> rows
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
    
    protected abstract ExprValue getInitialColumnExprValue();
    
    protected void initializeTable(ObservationTable observationTable) {
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
    }
    // ---------------------------------------------------------------------
    // designed for omega word
    protected abstract Query<HashableValue> makeMembershipQuery(Word prefix, ExprValue exprValue);
    
    protected abstract void initialize();
        
    protected abstract void constructHypothesis();
    
    protected abstract Word getLabelWord(int state);
    
}
