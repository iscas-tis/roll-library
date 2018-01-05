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
import roll.automata.FDFA;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.util.Timer;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Dana Angluin and Dana Fisman. "Learning Regular Omega Languages" in ALT 2014
 * Yong Li, Yu-Fang Chen, Lijun Zhang and Depeng Liu.
 *       "A Novel Learning Algorithm for  Büchi Automata based on Family of DFAs and Classification Trees"
 * in TACAS 2017
 * */

public abstract class LearnerFDFA extends LearnerBase<FDFA> {

    protected LearnerLeading learnerLeading;
    protected List<LearnerProgress> learnerProgress;
    private boolean alreadyStarted;
    protected FDFA fdfa;
    public LearnerFDFA(Options options, Alphabet alphabet
            , MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        this.learnerProgress = new ArrayList<>();
    }
    
    @Override
    public void startLearning() {
        if(alreadyStarted)
            try {
                throw new UnsupportedOperationException("Learner should not be started twice");
            } catch (Exception e) {
                e.printStackTrace();
            }
        alreadyStarted = true;
        learnerLeading = getLearnerLeading();
        Timer timer = new Timer();
        timer.start();
        learnerLeading.startLearning();
        timer.stop();
        options.stats.timeOfLearnerLeading += timer.getTimeElapsed();
        
        DFA dfa = learnerLeading.getHypothesis();
        for(int state = 0; state < dfa.getStateSize(); state ++ ) {
            LearnerProgress learner = getLearnerProgress(state);
            learnerProgress.add(learner);
            timer.start();
            learner.startLearning();
            timer.stop();
            options.stats.timeOfLearnerProgress += timer.getTimeElapsed();
        }
        constructHypothesis();
    }
    
    protected void constructHypothesis() {
        DFA leadDFA = learnerLeading.getHypothesis();
        List<DFA> proDFAs = new ArrayList<>();
        for(LearnerProgress learner : learnerProgress) {
            DFA progressDFA = (DFA)learner.getHypothesis();
            proDFAs.add(progressDFA);
        }
        fdfa = new FDFA(leadDFA, proDFAs);
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.FDFA;
    }
    
    protected abstract LearnerLeading getLearnerLeading();
    
    protected abstract LearnerProgress getLearnerProgress(int state);
    
    protected boolean isPeriodic() {
        return false;
    }
 

    @Override
    public FDFA getHypothesis() {
        return fdfa;
    }

    // refine FDFA by counterexample
    @Override
    public void refineHypothesis(Query<HashableValue> query) {
        // we assume that the counterexample returned are normalized w.r.t the leading DFA
        ExprValue expr = learnerLeading.getExprValueWord(query.getPrefix(), query.getSuffix());
        if(options.verbose) {
            System.out.println("normalized factorization: " + expr.toString());
        }
        DFA leadDFA = learnerLeading.getHypothesis();
        int s = leadDFA.getSuccessor(expr.getLeft());
        Word label = learnerLeading.getStateLabel(s);
        Query<HashableValue> queryLabel = new QuerySimple<HashableValue>(label, expr.getRight());
        HashableValue resultLabel = membershipOracle.answerMembershipQuery(queryLabel);
        Query<HashableValue> queryLeading = new QuerySimple<HashableValue>(expr.getLeft(), expr.getRight());
        options.log.verbose("Starting counterexample analysis in the learner ("
                +((Word)expr.getLeft()).length() + "," + ((Word)expr.getRight()).length() + ") ...");
        HashableValue resultCE = query.getQueryAnswer();
        if(resultCE == null) {
            resultCE = membershipOracle.answerMembershipQuery(queryLeading);
        }
        queryLeading.answerQuery(resultCE);
        if(! resultLabel.equals(resultCE)) { // refine leading automaton
            Timer timer = new Timer();
            timer.start();
            learnerLeading.refineHypothesis(queryLeading);
            timer.stop();
            options.stats.timeOfLearnerLeading += timer.getTimeElapsed();
            
            timer.start();
            if(! isPeriodic()) {
                // Syntactic and Recurrent FDFA should restart progress learning
                for(LearnerProgress learner : learnerProgress) {
                    learner.startLearning();
                }
            }
            DFA leadDFAPrime = learnerLeading.getHypothesis();
            // new states, not just one (for table-based leading automaton)
            for(int state = leadDFA.getStateSize(); state < leadDFAPrime.getStateSize(); state ++) {
                LearnerProgress learner = getLearnerProgress(state);
                learner.startLearning();
                learnerProgress.add(learner);
            }
            timer.stop();
            options.stats.timeOfLearnerProgress += timer.getTimeElapsed();
        }else { // refine progress automaton
            Timer timer = new Timer();
            timer.start();
            LearnerProgress learnerPro = null;
            for(LearnerProgress learner : learnerProgress) {
                if(learner.getLeadingState() == s) {
                    learnerPro = learner;
                    break;
                }
            }
            HashableValue result = learnerPro.getCeAnalyzerHashableValue(resultCE.get(), alphabet.getEmptyWord(), queryLeading.getSuffix());
            queryLeading.answerQuery(result);
            learnerPro.refineHypothesis(queryLeading);
            timer.stop();
            options.stats.timeOfLearnerProgress += timer.getTimeElapsed();
        }
        constructHypothesis();
        options.log.verbose("Finished counterexample analysis in the learner...");
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("T: \n" + learnerLeading.toString() + "\n");
        
        for(LearnerProgress learner : learnerProgress) {
            builder.append(learner.getLeadingLabel().toStringWithAlphabet() + ": \n");
            builder.append(learner.toString());
        }
        return builder.toString();
    }
    
    // -------------- some helper function
    // for FDFA learning, this function should not be visible
//    public Word getProgressStateLabel(int stateLeading, int stateProgress) {
//        return learnerProgress.get(stateLeading).getStateLabel(stateProgress);
//    }
//    
//    public Word getLeadingStateLabel(int stateLeading) {
//        return learnerLeading.getStateLabel(stateLeading);
//    }

}
