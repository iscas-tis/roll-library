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

package roll.oracle.dfa;

import roll.automata.DFA;
import roll.main.Options;
import roll.oracle.Teacher;
import roll.query.Query;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Timer;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public abstract class TeacherDFA implements Teacher<DFA, Query<HashableValue>, HashableValue>{

    protected final DFA target;
    protected final Alphabet alphabet;
    protected final Options options;
    
    public TeacherDFA(Options options, DFA dfa) {
        this.options = options;
        this.target = dfa;
        this.alphabet = dfa.getAlphabet();
    }
    
    @Override
    public HashableValue answerMembershipQuery(Query<HashableValue> query) {
        Timer timer = new Timer();
        timer.start();
        Word word = query.getQueriedWord();
        boolean answer = target.isFinal(target.getSuccessor(word));
        timer.stop();
        options.stats.timeOfMembershipQuery += timer.getTimeElapsed();
        options.stats.numOfMembershipQuery ++;
        return new HashableValueBoolean(answer);
    }
    
    @Override
    public Query<HashableValue> answerEquivalenceQuery(DFA hypothesis) {
        Timer timer = new Timer();
        timer.start();
        Query<HashableValue> result = checkEquivalence(hypothesis);
        timer.stop();
        options.stats.numOfEquivalenceQuery ++;
        options.stats.timeOfEquivalenceQuery += timer.getTimeElapsed();
        options.stats.timeOfLastEquivalenceQuery = timer.getTimeElapsed();
        return result;
    }
    
    protected abstract Query<HashableValue> checkEquivalence(DFA hypothesis);

}
