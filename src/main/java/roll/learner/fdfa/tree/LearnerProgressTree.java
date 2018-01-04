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

import roll.learner.fdfa.LearnerLeading;
import roll.learner.fdfa.LearnerProgress;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.table.HashableValue;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

abstract class LearnerProgressTree extends LearnerOmegaTree implements LearnerProgress {

    protected final LearnerLeading learnerLeading;
    protected int state;
    protected final Word label;
    
    public LearnerProgressTree(Options options, Alphabet alphabet
            , MembershipOracle<HashableValue> membershipOracle
            , LearnerLeading learnerLeading, int state) {
        super(options, alphabet, membershipOracle);
        this.learnerLeading = learnerLeading;
        this.state = state;
        this.label = learnerLeading.getStateLabel(state);
    }

    @Override
    public Word getLeadingLabel() {
        return label;
    }

    @Override
    public LearnerLeading getLearnerLeading() {
        return learnerLeading;
    }

    @Override
    public int getLeadingState() {
        return state;
    }

}
