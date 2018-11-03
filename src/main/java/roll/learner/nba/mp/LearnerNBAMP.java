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

package roll.learner.nba.mp;

import java.util.ArrayList;
import java.util.List;

import roll.automata.DFA;
import roll.automata.NBA;
import roll.automata.StateDFA;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWordPair;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.table.ObservationTableAbstract;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * On the Learnability of Infinitary Regular Sets
 * by Oded Maler and Amir Pnueli
 * 
 * Still in construction
 * */

public class LearnerNBAMP extends LearnerBase<NBA> {

    private boolean alreadyStarted = false;
    private NBA nba;
    private ObservationTableAbstract observationTable;
    private int[] marks;
    
    public LearnerNBAMP(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        observationTable = new ObservationTableNBAMP();
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.NBA_MP;
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
        
        // add every alphabet
        for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
            Word letterWord = alphabet.getLetterWord(letterNr);
            observationTable.addLowerRow(alphabet.getLetterWord(letterNr));
            observationTable.addColumn(new ExprValueWordPair(wordEmpty, letterWord));
        }
        
        // ask initial queries for upper table
        processMembershipQueries(observationTable.getUpperTable()
                , 0, observationTable.getColumns().size());
        // ask initial queries for lower table
        processMembershipQueries(observationTable.getLowerTable()
                , 0, observationTable.getColumns().size());
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
            processMembershipQueries(newLowerRows, 0, observationTable.getColumns().size());
            lowerRow = observationTable.getUnclosedLowerRow();
        }
        
        constructHypothesis();
    }

    protected void constructHypothesis() {
        DFA dfa = constructTransitionGraph();
        markOrDetect(dfa);
    }

    protected void markOrDetect(DFA dfa) {
        marks = new int[dfa.getStateSize()];
        
        List<ObservationRow> upperTable = observationTable.getUpperTable();
        List<ExprValue> columns = observationTable.getColumns();
        for(int i = 0; i < upperTable.size(); i ++) {
            Word s = upperTable.get(i).getWord();
            for(int j = 0; j < columns.size(); j ++) {
                // compute Inf(s . expr)
                ExprValue expr = columns.get(j);
                Word suffix = expr.getLeft();
                Word period = expr.getRight();
                Word prefix = s.concat(suffix);
                HashableValue mq = upperTable.get(i).getValues().get(j);
                ISet inf = getInfSet(dfa, prefix, period);
                
                int mark = 0;
                if(mq.isAccepting()) {
                    mark = 2;
                }else {
                    mark = 1;
                }
                for(int state : inf) {
                    marks[state] = marks[state] | mark;
                    // check if there is a state with conflict signs
                    if(marks[state] == 3) {
                        // found one conflict
                        
                    }
                }
            }
        }
    }

    protected DFA constructTransitionGraph() {
        DFA dfa = new DFA(alphabet);
        List<ObservationRow> upperTable = observationTable.getUpperTable();
        for(int i = 0; i < upperTable.size(); i ++) {
            dfa.createState();
        }
        // build transition system
        for(int currNr = 0; currNr < upperTable.size(); currNr ++) {
            StateDFA state = dfa.getState(currNr);
            Word currWord = upperTable.get(currNr).getWord();
            for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
                Word succWord = currWord.append(letter);
                int succNr = observationTable.getUpperTableRowIndex(succWord);
                state.addTransition(letter, succNr);
            }
            // now decide whether it is initial state
            if(currWord.isEmpty()) {
                dfa.setInitial(currNr);
            }
        }
        return dfa;
    }

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
    
    protected Query<HashableValue> getQuerySimple(ObservationRow row, Word prefix, Word suffix, int column) {
        return new QuerySimple<>(row, prefix, suffix, column);
    }

    protected HashableValue processMembershipQuery(ObservationRow row, Word prefix, Word suffix, int column) {
        return membershipOracle.answerMembershipQuery(getQuerySimple(row, prefix, suffix, column));
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

    @Override
    public NBA getHypothesis() {
        return nba;
    }

    @Override
    public void refineHypothesis(Query<HashableValue> query) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String toHTML() {
        // TODO Auto-generated method stub
        return null;
    }
    
    // get the states infinitely occurs on the run of prefix . period .. 
    // i + j < size + 1 and 0 <= i < j 
    private ISet getInfSet(DFA dfa, Word prefix, Word period) {
        ISet inf = UtilISet.newISet();
        int size = dfa.getStateSize();
        int first = dfa.getSuccessor(prefix), last;
        for(int i = 0; i < size / 2; i ++) {
            last = first;
            if(i > 0) {
                first = dfa.getSuccessor(last, period);
            }
            int second = first;
            for(int j = i + 1; j < size + 1 - i; j ++) {
                for(int k = 1; k <= j; k ++) {
                    second = dfa.getSuccessor(second, period);
                }
            }
            // check whether u.v^i = u.v^{i + j}
            if(first == second) {
                break;
            }
        }
        // collect all states in the loop
        inf.set(first);
        while(true) {
            last = first;
            for(int i = 0; i < period.length(); i ++) {
                int letter = period.getLetter(i);
                last = dfa.getSuccessor(last, letter);
                inf.set(last);
            }
            if(last == first) {
                break;
            }
        }
        return inf;
    }

}
