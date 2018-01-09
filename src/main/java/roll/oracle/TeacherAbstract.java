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

package roll.oracle;

import roll.main.Options;
import roll.query.Query;
import roll.table.HashableValue;
import roll.util.Timer;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public abstract class TeacherAbstract<M> implements Teacher<M, Query<HashableValue>, HashableValue> {
    
    protected final Options options;
    
    public TeacherAbstract(Options options) {
        this.options = options;
    }
    
    protected abstract HashableValue checkMembership(Query<HashableValue> query);
    
    @Override
    public HashableValue answerMembershipQuery(Query<HashableValue> query) {
        Timer timer = new Timer();
        timer.start();
        HashableValue answer = checkMembership(query);
        timer.stop();
        options.stats.timeOfMembershipQuery += timer.getTimeElapsed();
        options.stats.numOfMembershipQuery ++;
        return answer;
    }

    protected abstract Query<HashableValue> checkEquivalence(M hypothesis);
    
    @Override
    public Query<HashableValue> answerEquivalenceQuery(M hypothesis) {
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
