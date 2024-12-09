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
import roll.learner.fdfa.LearnerFDFA;
import roll.learner.fdfa.LearnerLeading;
import roll.learner.fdfa.LearnerProgress;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LearnerFDFATreeLimit extends LearnerFDFA {

    public LearnerFDFATreeLimit(Options options, Alphabet alphabet,
            MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
    }

    @Override
    protected LearnerLeading createLearnerLeading() {
        return new LearnerLeadingTree(options, alphabet, membershipOracle);
    }

    @Override
    protected LearnerProgress createLearnerProgress(int state) {
        return new LearnerProgressTreeLimit(options, alphabet, membershipOracle, learnerLeading, state);
    }
    
    @Override
    public boolean checkLeadingConsistency() {
    	LearnerLeadingTree learnerLeading = (LearnerLeadingTree) this.learnerLeading;
    	DFA dfa = learnerLeading.getHypothesis();
    	for (int state = 0; state < dfa.getStateSize(); state ++) {
    		if (state == dfa.getInitialState()) continue;
    		Word repr = this.getLeadingStateLabel(state);
    		int reachState = dfa.getSuccessor(repr);
    		if (reachState != state) {
    			// we can obtain the experiments of this two
    			ExprValue expr = learnerLeading.getExperiment(state, reachState);
    			// now we input the word for refinement
    			Word prefix = expr.getLeft();
    			Word loop = expr.getRight();
    			Query<HashableValue> query = new QuerySimple<>(repr.concat(prefix), loop);
    			this.refineLeadingDFA(query);
    			return true;
    		}
    	}
		return false;
    }

}