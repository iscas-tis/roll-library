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

package roll.learner.ffa;

import java.util.ArrayList;
import java.util.List;

import roll.automata.FFA;
import roll.automata.NFA;
import roll.jupyter.NativeTool;
import roll.learner.LearnerBase2;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.util.Timer;
import roll.words.Alphabet;

public abstract class LearnerFFA<M extends NFA, A extends NFA> extends LearnerBase2<FFA<M, A>> {

    protected LearnerLeading<M> learnerLeading;
    protected List<LearnerProgress<A>> learnerProgress;
    protected FFA<M, A> ffa;
    
    public LearnerFFA(Options options, Alphabet alphabet
            , MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        this.learnerProgress = new ArrayList<>();
    }
    
    @Override
    protected void initialize() {
        learnerLeading = getLearnerLeading();
        Timer timer = new Timer();
        timer.start();
        learnerLeading.startLearning();
        timer.stop();
        options.stats.timeOfLearnerLeading += timer.getTimeElapsed();
        for(int state = 0; state < learnerLeading.getStateSize(); state ++ ) {
            LearnerProgress<A> learner = getLearnerProgress(state);
            learnerProgress.add(learner);
            timer.start();
            learner.startLearning();
            timer.stop();
            options.stats.timeOfLearnerProgress += timer.getTimeElapsed();
        }
        constructHypothesis();
    }
    
    @Override
    protected void constructHypothesis() {
        M leadingFA = learnerLeading.getHypothesis();
        List<A> progressFAs = new ArrayList<>();
        for(LearnerProgress<A> learner : learnerProgress) {
            A progressFA = learner.getHypothesis();
            progressFAs.add(progressFA);
        }
        ffa = makeFFA(leadingFA, progressFAs);
    }
    
    protected abstract FFA<M, A> makeFFA(M leadingFA, List<A> progressFAs);
    
    protected abstract LearnerLeading<M> getLearnerLeading();
    
    protected abstract LearnerProgress<A> getLearnerProgress(int state);
    
    protected boolean isLeadingNFA() {
        return false;
    }
    
    protected boolean isPeriodic() {
        return false;
    }
 

    @Override
    public FFA<M, A> getHypothesis() {
        return ffa;
    }

    // refine FDFA by counterexample
    protected abstract int getLeaingState(Query<HashableValue> query, HashableValue resultCE);
    @Override
    public void refineHypothesis(Query<HashableValue> query) {
        // we assume that the counterexample returned are normalized w.r.t the leading DFA
        ExprValue expr = learnerLeading.getExprValueWord(query.getPrefix(), query.getSuffix());
        options.log.verbose("normalized factorization: " + expr.toString());
        HashableValue resultCE = query.getQueryAnswer();
        Query<HashableValue> queryCE = new QuerySimple<>(expr.getLeft(), expr.getRight());
        if(resultCE == null) {
            resultCE = membershipOracle.answerMembershipQuery(queryCE);
        }
        int s = getLeaingState(query, resultCE);
        if(s < 0) { // refine leading automaton
            Timer timer = new Timer();
            M leadingFA = learnerLeading.getHypothesis();
            timer.start();
            learnerLeading.refineHypothesis(query);
            timer.stop();
            options.stats.timeOfLearnerLeading += timer.getTimeElapsed();
            timer.start();
            if(! isLeadingNFA()) {
                if(! isPeriodic()) {
                    // Syntactic and Recurrent FDFA should restart progress learning
                    for(LearnerProgress<A> learner : learnerProgress) {
                        learner.startLearning();
                    }
                }
                // new states, not just one (for table-based leading automaton)
                for(int state = leadingFA.getStateSize(); state < learnerLeading.getStateSize(); state ++) {
                    LearnerProgress<A> learner = getLearnerProgress(state);
                    learner.startLearning();
                    learnerProgress.add(learner);
                }
            }else {
                // for leading nfa, probably prime upper rows have been changed
                for(int state = 0; state < learnerLeading.getStateSize(); state ++) {
                    LearnerProgress<A> learner = getLearnerProgress(state);
                    learner.startLearning();
                    learnerProgress.add(learner);
                }
            }
            timer.stop();
            options.stats.timeOfLearnerProgress += timer.getTimeElapsed();
        }else { // refine progress automaton
            Timer timer = new Timer();
            timer.start();
            LearnerProgress<A> learnerPro = null;
            for(LearnerProgress<A> learner : learnerProgress) {
                if(learner.getLeadingState() == s) {
                    learnerPro = learner;
                    break;
                }
            }
            HashableValue result = learnerPro.getCeAnalyzerHashableValue(resultCE.get(), alphabet.getEmptyWord(), queryCE.getSuffix());
            queryCE.answerQuery(result);
            learnerPro.refineHypothesis(queryCE);
            timer.stop();
            options.stats.timeOfLearnerProgress += timer.getTimeElapsed();
        }
        constructHypothesis();
        options.log.verbose("Finished counterexample analysis in the learner...");
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Leading Learner: \n" + learnerLeading.toString() + "\n");
        
        for(LearnerProgress<A> learner : learnerProgress) {
            builder.append("Progress Learner for " + learner.getLeadingLabel().toStringWithAlphabet() + ": \n");
            builder.append(learner.toString());
        }
        return builder.toString();
    }
    
    @Override
    public String toHTML() {
        if (options.structure == Options.Structure.TREE) {
               StringBuilder builder = new StringBuilder();
               builder.append("<p> Leading Learner :  </p> <br> "    
                            + NativeTool.dot2SVG(learnerLeading.toString()));
               for(LearnerProgress<A> learner : learnerProgress) {
                   builder.append("<p> Progress Learner for " + learner.getLeadingLabel().toStringWithAlphabet() + ": </p> <br>"
                           + NativeTool.dot2SVG(learner.toString()) + "<br>");
               }
               return builder.toString();
        }else {
            return "<pre> " + toString() + "</pre>";
        }
    }

}
