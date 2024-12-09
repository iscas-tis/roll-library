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

import java.util.ArrayList;
import java.util.List;

import roll.automata.DFA;
import roll.query.Query;
import roll.table.HashableValue;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public interface LearnerProgressLimit extends LearnerProgressRecurrent {
    
	@Override
    default HashableValue prepareRowHashableValue(boolean mqResult, Word x, Word e) {
        DFA leadDFA = getLearnerLeading().getHypothesis();
        int stateUX = leadDFA.getSuccessor(getLeadingState(), x);
        int stateUXE = leadDFA.getSuccessor(stateUX, e);
        boolean recur = stateUXE == getLeadingState();
        return getHashableValueImplBoolPair(recur, mqResult);
    }    
	
	// we need to find two states x and y in the progress DFA
	// such that u = M(uxv1) and u(xv1) in L
	//           u = M(uyv2) and u(yv2) not in L
	// Note that xv1 and yv2 cannot be empty words
	List<Query<HashableValue>> computeMark();


}
