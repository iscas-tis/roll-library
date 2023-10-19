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

package roll.learner.fdfa.tree;

import roll.automata.DFA;
import roll.learner.LearnerType;
import roll.learner.fdfa.LearnerLeading;
import roll.learner.fdfa.LearnerProgressLimit;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.table.HashableValue;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LearnerProgressTreeLimit extends LearnerProgressTree implements LearnerProgressLimit {

    public LearnerProgressTreeLimit(Options options, Alphabet alphabet,
            MembershipOracle<HashableValue> membershipOracle, LearnerLeading learnerLeading, int state) {
        super(options, alphabet, membershipOracle, learnerLeading, state);
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.FDFA_LIMIT_TREE;
    }
    
    @Override
    public void startLearning() {
        initialize();
    }
    
    @Override
    public HashableValue prepareRowHashableValue(boolean mqResult, Word x, Word e) {
        DFA leadDFA = getLearnerLeading().getHypothesis();
        int stateUX = leadDFA.getSuccessor(getLeadingState(), x);
        int stateUXE = leadDFA.getSuccessor(stateUX, e);
        boolean recur = stateUXE == getLeadingState();
        return getHashableValueBool(!recur || mqResult);
    }

}
