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

package roll.oracle.nba;

import roll.automata.NBA;
import roll.automata.operations.NBAOperations;
import roll.main.Options;
import roll.oracle.Teacher;
import roll.query.Query;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Timer;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public abstract class TeacherNBA implements Teacher<NBA, Query<HashableValue>, HashableValue> {
    protected final Options options;
    protected final NBA target;
    
    public TeacherNBA(Options options, NBA target) {
        assert options != null && target != null;
        this.options = options;
        this.target = target;
    }

    @Override
    public HashableValue answerMembershipQuery(Query<HashableValue> query) {
        Timer timer = new Timer();
        timer.start();
        Word prefix = query.getPrefix();
        Word suffix = query.getSuffix();
        boolean answer = NBAOperations.accepts(target, prefix, suffix);
        timer.stop();
        options.stats.timeOfMembershipQuery += timer.getTimeElapsed();
        options.stats.numOfMembershipQuery ++;
        return new HashableValueBoolean(answer);
    }
    
    protected abstract Query<HashableValue> checkEquivalence(NBA hypothesis);

    @Override
    public Query<HashableValue> answerEquivalenceQuery(NBA hypothesis) {
        Timer timer = new Timer();
        timer.start();
        Query<HashableValue> result = checkEquivalence(hypothesis);
        timer.stop();
        options.stats.numOfEquivalenceQuery ++;
        options.stats.timeOfEquivalenceQuery += timer.getTimeElapsed();
        options.stats.timeOfLastEquivalenceQuery = timer.getTimeElapsed();
        return result;
    }

}
