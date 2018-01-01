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

package roll.learner.nba.lomega;

import roll.automata.FDFA;
import roll.automata.NBA;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.learner.fdfa.LearnerFDFA;
import roll.learner.nba.lomega.translator.TranslatorFDFA;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.table.HashableValue;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 *  * This class implements the BA learning algorithm from the paper 
 * Yong Li, Yu-Fang Chen, Lijun Zhang and Depeng Liu
 *       "A Novel Learning Algorithm for  Büchi Automata based on Family of DFAs and Classification Trees"
 * In TACAS 2017
 * */

public class LearnerNBALOmega extends LearnerBase<NBA>{

    private boolean alreadyStarted = false;
    private NBA nba;
    private final LearnerFDFA fdfaLearner;
    public LearnerNBALOmega(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        fdfaLearner = UtilLOmega.getLearnerFDFA(options, alphabet, membershipOracle);
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.NBA_FDFA;
    }

    @Override
    public void startLearning() {
        if(alreadyStarted) {
            throw new UnsupportedOperationException("Learner can not start twice");
        }
        alreadyStarted = true;
        fdfaLearner.startLearning();
        constructHypothesis();
    }

    protected void constructHypothesis() {
        // construct BA from FDFA
        FDFA fdfa = fdfaLearner.getHypothesis();
        nba = UtilLOmega.constructNBA(options, fdfa);
    }

    @Override
    public NBA getHypothesis() {
        return nba;
    }

    @Override
    public void refineHypothesis(Query<HashableValue> query) {
        if(options.verbose) {
            System.out.println(fdfaLearner.getHypothesis().toString());
        }
        TranslatorFDFA translator = UtilLOmega.getTranslator(options, fdfaLearner, membershipOracle);
        // lazy equivalence check is implemented here
        HashableValue mqResult = query.getQueryAnswer();
        if(mqResult == null) {
            mqResult = membershipOracle.answerMembershipQuery(query);
        }
        query.answerQuery(mqResult);
        translator.setQuery(query);
        while(translator.canRefine()) {
            Query<HashableValue> ceQuery = translator.translate();
            fdfaLearner.refineHypothesis(ceQuery);
            // usually lazyeq is not very useful
            if(options.optimization != Options.Optimization.LAZY_EQ) break;
        }
        constructHypothesis();
        
    }
    
    @Override
    public String toString() {
        return fdfaLearner.toString();
    }

}
