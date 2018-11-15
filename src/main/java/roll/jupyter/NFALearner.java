/* Copyright (c) 2016, 2017, 2018                                         */
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

package roll.jupyter;

import roll.automata.NFA;
import roll.learner.LearnerBase;
import roll.main.IHTML;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * A wrapper for NFA learner
 * @author Yong Li
 * */

public class NFALearner implements JupyterLearner<NFA>, IHTML {

    private LearnerBase<NFA> learner;
    private MembershipOracle<HashableValue> mqOracle;
    private Alphabet alphabet;
    public NFALearner(Alphabet alphabet
            , LearnerBase<NFA> learner
            , MembershipOracle<HashableValue> mqOracle) {
        assert learner != null;
        assert alphabet != null;
        assert mqOracle != null;
        this.alphabet = alphabet;
        this.learner = learner;
        this.mqOracle = mqOracle;
    }
    
    @Override
    public NFA getHypothesis() {
        return learner.getHypothesis();
    }
    
    @Override
    public String toString() {
        return learner.toString();
    }
    
    public void refineHypothesis(String counterexample) {
        Word word = alphabet.getWordFromString(counterexample);
        // now verify counterexample
        NFA hypothesis = learner.getHypothesis();
        boolean isInHypo = hypothesis.getAcc().accept(word, alphabet.getEmptyWord());
        Query<HashableValue> ceQuery = new QuerySimple<>(word);
        HashableValue isInTarget = mqOracle.answerMembershipQuery(ceQuery);
        if(isInHypo && isInTarget.isAccepting()) {
            System.err.println("Invalid counterexample, both in hypothesis and target");
            return ;
        }
        
        if(!isInHypo && !isInTarget.isAccepting()) {
            System.err.println("Invalid counterexample, neither in hypothesis or target");
            return ;
        }
        ceQuery.answerQuery(null);
        learner.refineHypothesis(ceQuery);
    }

    @Override
    public String toHTML() {
        return learner.toHTML();
    }

}
