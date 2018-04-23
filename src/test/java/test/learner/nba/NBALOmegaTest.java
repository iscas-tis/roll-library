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

package test.learner.nba;


import org.junit.Test;

import automata.FiniteAutomaton;
import roll.automata.NBA;
import roll.automata.operations.NBAGenerator;
import roll.learner.nba.lomega.LearnerNBALOmega;
import roll.main.Options;
import roll.oracle.nba.rabit.TeacherNBARABIT;
import roll.oracle.nba.rabit.UtilRABIT;
import roll.oracle.nba.sampler.TeacherNBASampler;
import roll.query.Query;
import roll.table.HashableValue;
import roll.util.Timer;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBALOmegaTest {
    
    private boolean isIncluded(NBA A, NBA B) {
        Alphabet alphabet = A.getAlphabet();
        FiniteAutomaton rA = UtilRABIT.toRABITNBA(A);
        FiniteAutomaton rB = UtilRABIT.toRABITNBA(B);
        return UtilRABIT.isIncluded(alphabet, rA, rB) == null;
    }
    
    @Test
    public void testSampler() {
        NBA target = NBAStore.getNBA3();
        System.out.println("Target: " + target.toString());
        Options options = new Options();
        options.structure = Options.Structure.TABLE;
        options.approximation = Options.Approximation.UNDER;
        options.algorithm = Options.Algorithm.RECURRENT;
        options.epsilon = 0.00018;
        options.delta = 0.0001;
        Timer timer = new Timer();
        timer.start();
        TeacherNBASampler teacher = new TeacherNBASampler(options, target);
        LearnerNBALOmega learner = new LearnerNBALOmega(options, target.getAlphabet(), teacher);
        System.out.println("starting learning");
        learner.startLearning();
        NBA model = null;
        while(true) {
            System.out.println("Table is both closed and consistent\n" + learner.toString());
            model = learner.getHypothesis();
            System.out.println(model.toString());
            // along with ce
            Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(model);
            boolean isEq = ceQuery.getQueryAnswer().get();
            if(isEq) {
                System.out.println(model.toString());
                break;
            }
            ceQuery.answerQuery(null);
            learner.refineHypothesis(ceQuery);
        }
        timer.stop();
        System.out.println("Totoal used time " + timer.getTimeElapsed() + " ms");
        options.stats.print();
        System.out.println("L(H) <= L(B): " + isIncluded(model, target));
        System.out.println("L(B) <= L(H): " + isIncluded(target, model));
    }

    @Test
    public void testLearining() {
       testNBA(NBAStore.getNBA1());
    }
    
    @Test
    public void testRABIT() {
       final int numTests = 5;
       final int numStates = 3;
       
       for(int i = 0; i < numTests; i ++) {
           NBA nba = NBAGenerator.getRandomNBA(numStates, 3);
           testNBA(nba);
           System.out.println("Done for case " + (i + 1));
       }
        
    }
    
    private void testNBA(NBA target) {
        Options options = new Options();
        options.structure = Options.Structure.TREE;
        options.approximation = Options.Approximation.UNDER;
        options.algorithm = Options.Algorithm.SYNTACTIC;
        options.verbose = 1;
        Timer timer = new Timer();
        timer.start();
        TeacherNBARABIT teacher = new TeacherNBARABIT(options, target);
        LearnerNBALOmega learner = new LearnerNBALOmega(options, target.getAlphabet(), teacher);
        System.out.println("starting learning");
        learner.startLearning();
        while(true) {
            System.out.println("Table is both closed and consistent\n" + learner.toString());
            NBA model = learner.getHypothesis();
            System.out.println("Hypothesis:\n" + model.toString());
            System.out.println("FDFA:\n" + learner.getLearnerFDFA().getHypothesis());
            // along with ce
            System.out.println("Resolving equivalence query...");
            Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(model);
            boolean isEq = ceQuery.getQueryAnswer().get();
            if(isEq) {
                System.out.println(model.toString());
                break;
            }
            ceQuery.answerQuery(null);
            System.out.println(ceQuery.toString());
            System.out.println("Refining current hypothesis...");
            learner.refineHypothesis(ceQuery);
        }
        timer.stop();
        System.out.println("Totoal used time " + timer.getTimeElapsed() + " ms");
        options.stats.print();
    }
    
    

}
