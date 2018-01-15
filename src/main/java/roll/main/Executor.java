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

package roll.main;

import roll.automata.DFA;
import roll.automata.FDFA;
import roll.automata.NBA;
import roll.learner.LearnerBase;
import roll.learner.nba.ldollar.LearnerNBALDollar;
import roll.learner.nba.lomega.LearnerNBALOmega;
import roll.learner.nba.lomega.UtilLOmega;
import roll.oracle.Teacher;
import roll.oracle.nba.TeacherNBA;
import roll.oracle.nba.rabit.TeacherNBARABIT;
import roll.oracle.nba.sampler.TeacherNBASampler;
import roll.query.Query;
import roll.table.HashableValue;
import roll.util.Timer;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class Executor {
    
    public static void executeRABIT(Options options, NBA target) {
        TeacherNBARABIT teacher = new TeacherNBARABIT(options, target);
        Executor.execute(options, target, teacher);
    }
    
    public static void executeSampler(Options options, NBA target) {
        TeacherNBASampler teacher = new TeacherNBASampler(options, target);
        Executor.execute(options, target, teacher);
    }
    
    private static void prepareStats(Options options, LearnerBase<NBA> learner, NBA hypothesis) {
        options.stats.numOfStatesInHypothesis = hypothesis.getStateSize();
        if(learner instanceof LearnerNBALOmega) {
            LearnerNBALOmega learnerLOmega = (LearnerNBALOmega)learner;
            FDFA fdfa = learnerLOmega.getLearnerFDFA().getHypothesis();
            options.stats.numOfStatesInLeading = fdfa.getLeadingDFA().getStateSize();
            for(int state = 0; state < fdfa.getLeadingDFA().getStateSize(); state ++) {
                options.stats.numOfStatesInProgress.add(fdfa.getProgressDFA(state).getStateSize());
            }
            if(options.automaton.isLDBA()) {
                hypothesis = UtilLOmega.constructLDBA(options, fdfa);
            }
        }else if(learner instanceof LearnerNBALDollar) {
            LearnerNBALDollar learnerLDollar = (LearnerNBALDollar)learner;
            DFA dfa = learnerLDollar.getLearnerDFA().getHypothesis();
            options.stats.numOfStatesInLeading = dfa.getStateSize();
        }else {
            throw new UnsupportedOperationException("Unsupported BA Learner");
        }
        options.stats.hypothesis = hypothesis;
        options.stats.numOfStatesInHypothesis = hypothesis.getStateSize();
    }
    
    private static void execute(Options options, NBA target,
            TeacherNBA teacher) {
        LearnerBase<NBA> learner = getLearner(options, target, teacher);
        Timer timer = new Timer();
        options.log.println("Initializing learner...");
        timer.start();
        learner.startLearning();
        timer.stop();
        options.stats.timeOfLearner += timer.getTimeElapsed();
        NBA hypothesis = null;
        while(true) {
            options.log.verbose("Table is both closed and consistent\n" + learner.toString());
            hypothesis = learner.getHypothesis();
            // along with ce
            options.log.println("Resolving equivalence query...");
            Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(hypothesis);
            boolean isEq = ceQuery.getQueryAnswer().get();
            if(isEq) {
                // store statistics
                prepareStats(options, learner, hypothesis);
                break;
            }
            ceQuery.answerQuery(null);
            options.log.verbose("Counterexample is: " + ceQuery.toString());
            timer.start();
            options.log.println("Refining current hypothesis...");
            learner.refineHypothesis(ceQuery);
            timer.stop();
            options.stats.timeOfLearner += timer.getTimeElapsed();
        }
        options.log.println("Learning completed...");
    }

    private static LearnerBase<NBA> getLearner(Options options, NBA nba,
            Teacher<NBA, Query<HashableValue>, HashableValue> teacher) {
        LearnerBase<NBA> learner = null;
        if(options.algorithm == Options.Algorithm.NBA_LDOLLAR) {
            learner = new LearnerNBALDollar(options, nba.getAlphabet(), teacher);
        }else if(options.algorithm == Options.Algorithm.PERIODIC
             || options.algorithm == Options.Algorithm.SYNTACTIC
             || options.algorithm == Options.Algorithm.RECURRENT) {
            learner = new LearnerNBALOmega(options, nba.getAlphabet(), teacher);
        }else {
            throw new UnsupportedOperationException("Unsupported BA Learner");
        }
        
        return learner;
    }
}
