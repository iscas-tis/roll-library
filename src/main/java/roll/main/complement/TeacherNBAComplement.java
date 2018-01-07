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

package roll.main.complement;

import automata.FiniteAutomaton;
import roll.automata.FDFA;
import roll.automata.NBA;
import roll.automata.operations.NBAOperations;
import roll.main.Options;
import roll.main.inclusion.UtilInclusion;
import roll.oracle.Teacher;
import roll.query.Query;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Timer;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class TeacherNBAComplement implements Teacher<FDFA, Query<HashableValue>, HashableValue> {

    private final NBA nba;
    private final Options options;
    private final FiniteAutomaton rB;
    
    public TeacherNBAComplement(Options options, NBA nba) {
        assert options != null && nba != null;
        this.options = options;
        this.nba = nba;
        this.rB = UtilInclusion.toRABITNBA(nba);
    }
    
    @Override
    public HashableValue answerMembershipQuery(Query<HashableValue> query) {
        Timer timer = new Timer();
        timer.start();
        
        boolean result;
        Word prefix = query.getPrefix();
        Word suffix = query.getSuffix();
        
        if(suffix.isEmpty()) {
            return new HashableValueBoolean(false);
        }else {
            result = NBAOperations.accepts(nba, prefix, suffix);
        }
        
        timer.stop();
        options.stats.timeOfMembershipQuery += timer.getTimeElapsed();
        ++ options.stats.numOfMembershipQuery; 
        return new HashableValueBoolean(!result); // reverse the result for Buechi automaton
    }

    @Override
    public Query<HashableValue> answerEquivalenceQuery(FDFA hypothesis) {
        return null;
    }

}
