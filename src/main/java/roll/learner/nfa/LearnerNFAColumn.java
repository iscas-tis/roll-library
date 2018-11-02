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

package roll.learner.nfa;

import roll.automata.DFA;
import roll.automata.NFA;
import roll.automata.StateNFA;
import roll.learner.LearnerBase;
import roll.learner.LearnerDFA;
import roll.learner.LearnerType;
import roll.learner.dfa.table.LearnerDFATableColumn;
import roll.learner.dfa.tree.LearnerDFATreeColumn;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * Learning a NFA of L by learning the DFA which accepts L^c = { x^c | x in L}
 * */

public class LearnerNFAColumn extends LearnerBase<NFA> {
    private boolean alreadyStarted = false;
    private final LearnerDFA dfaLearner;
    private NFA nfa;
    
    public LearnerNFAColumn(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        MembershipOracleNFAReverse nfaMembershipOracle = new MembershipOracleNFAReverse(membershipOracle);
        if(options.structure.isTable()) {
            dfaLearner = new LearnerDFATableColumn(options, alphabet, nfaMembershipOracle);
        }else {
            dfaLearner = new LearnerDFATreeColumn(options, alphabet, nfaMembershipOracle);
        }
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.NFA_RDFA;
    }

    @Override
    public void startLearning() {
        if(alreadyStarted) {
            throw new UnsupportedOperationException("Learner can not start twice");
        }
        alreadyStarted = true;
        dfaLearner.startLearning();
        constructHypothesis();
    }

    private void constructHypothesis() {
        nfa = new NFA(alphabet);
        
        DFA dfa = dfaLearner.getHypothesis();
        // now we get the reverse NFA
        ISet inits = UtilISet.newISet();
        for(int i = 0; i < dfa.getStateSize(); i ++) {
            nfa.createState();
            if(dfa.isInitial(i)) {
                nfa.setFinal(i);
            }else if(dfa.isFinal(i)) {
                inits.set(i);
            }
        }
        
        for(int curr = 0; curr < dfa.getStateSize(); curr ++) {
            for(int c = 0; c < dfa.getAlphabetSize(); c ++) {
                int succ = dfa.getState(curr).getSuccessor(c);
                // reverse the transition curr -- c --> succ
                nfa.getState(succ).addTransition(c, curr);
            }
        }
        
        if(inits.cardinality() <= 1) {
            for(final int init : inits) {
                nfa.setInitial(init);
            }
        }else {
            // |inits| > 1
            nfa.createState();
            nfa.setInitial(dfa.getStateSize());
            StateNFA initState = nfa.getState(dfa.getStateSize());
            for(final int init : inits) {
                StateNFA state = nfa.getState(init);
                for(final int letter : state.getEnabledLetters()) {
                    for(final int succ : state.getSuccessors(letter)) {
                        initState.addTransition(letter, succ);
                    }
                }
            }
        }
        
    }

    @Override
    public NFA getHypothesis() {
        return nfa;
    }

    @Override
    public void refineHypothesis(Query<HashableValue> query) {
        Word word = query.getQueriedWord().reverse();
        dfaLearner.refineHypothesis(new QuerySimple<>(word));
        constructHypothesis();
    }

    @Override
    public String toString() {
        return dfaLearner.toString();
    }

    @Override
    public String toHTML() {
        return "<pre>" + toString() + "</pre>";
    }

}
