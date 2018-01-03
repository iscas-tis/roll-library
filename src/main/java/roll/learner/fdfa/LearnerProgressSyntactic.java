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

package roll.learner.fdfa;

import roll.automata.DFA;
import roll.table.HashableValue;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public interface LearnerProgressSyntactic extends LearnerProgress {
    
    @Override
    default HashableValue prepareRowHashableValue(boolean mqResult, Word x, Word e) {
        DFA leadDFA = getLearnerLeading().getHypothesis();
        int stateUX = leadDFA.getSuccessor(getLeadingState(), x);
        int stateUXE = leadDFA.getSuccessor(stateUX, e);
        boolean recur = stateUXE == getLeadingState();
        return getHashableValueIntBoolPair(stateUX, recur, mqResult);
    }
    
    // pairs (m1, c1) and (m2, c2), 1. c1 != c2 or (m1 != m2)
    // c1 != c2 means that M(x1) != M(x2) since M(ux1ae1) = M(u) but M(ux2e2) != M(u)
    // m1 != m2 means that c1 = c2 = true but m1 != m2 so x1 and x2 are distinguished
    // first pair must be A or B since c1 should be true at first
    @Override
    default HashableValue getCeAnalyzerHashableValue(boolean mqResult, Word x, Word e) {
        DFA leadDFA = getLearnerLeading().getHypothesis();
        int stateUX = leadDFA.getSuccessor(getLeadingState(), x);
        int stateUXE = leadDFA.getSuccessor(stateUX, e);
        boolean recur = stateUXE == getLeadingState();
        final int stateNotUsed = -1;
        return getHashableValueIntBoolPair(stateNotUsed, recur, mqResult);
    }

}
