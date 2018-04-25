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

import roll.automata.NBA;
import roll.learner.LearnerBase;
import roll.main.IHTML;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * A wrapper for NBA learner
 * 
 * @author Yong Li
 * */

public class NBALearner implements JupyterLearner<NBA>, IHTML {

    private LearnerBase<NBA> learner;
    private MembershipOracle<HashableValue> mqOracle;
    private Alphabet alphabet;
    public NBALearner(Alphabet alphabet
            , LearnerBase<NBA> learner
            , MembershipOracle<HashableValue> mqOracle) {
        assert learner != null;
        assert alphabet != null;
        assert mqOracle != null;
        this.alphabet = alphabet;
        this.learner = learner;
        this.mqOracle = mqOracle;
    }
    
    @Override
    public NBA getHypothesis() {
        return learner.getHypothesis();
    }
    
    @Override
    public String toString() {
        if(isTable()) {
            return learner.toString();
        }else {
            return null;
//            return learner.toSVG();
        }
    }
    
    public void refineHypothesis(String stem, String loop) {

        Word prefix = alphabet.getWordFromString(stem);
        Word suffix = alphabet.getWordFromString(loop);
        // now verify counterexample
        NBA hypothesis = (NBA) learner.getHypothesis();
        boolean isInHypo = hypothesis.getAcc().isAccepting(prefix, suffix);
        Query<HashableValue> ceQuery = new QuerySimple<>(prefix, suffix);
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
    public boolean isTable() {
        return learner.getOptions().structure == Options.Structure.TABLE;
    }

    @Override
    public String toHTML() {
        return learner.toHTML();
    }

}
