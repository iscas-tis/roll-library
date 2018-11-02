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

import dk.brics.automaton.Automaton;
import roll.automata.NBA;
import roll.automata.operations.FDFAOperations;
import roll.learner.nba.lomega.LearnerNBALOmega;
import roll.main.Options;
import roll.oracle.nba.rabit.TeacherNBARABIT;
import roll.query.Query;
import roll.table.HashableValue;
import roll.util.Timer;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBATest {
    
    public static void main(String[] args) {
        Options options = new Options();
        options.structure = Options.Structure.TABLE;
        options.approximation = Options.Approximation.UNDER;
        options.algorithm = Options.Algorithm.SYNTACTIC;
        options.verbose = 2;
        Timer timer = new Timer();
        timer.start();
        
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        target.createState();
        
//        target.getState(0).addTransition(0, 1);
//        target.getState(1).addTransition(1, 0);
//        target.setFinal(0);
//        target.setFinal(1);
        target.getState(0).addTransition(1, 1);
        target.getState(1).addTransition(1, 0);
        target.getState(1).addTransition(0, 0);
        target.setInitial(0);
        target.setFinal(1);
        
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
        
        int[] pr = new int[]{0};
        Word p = alphabet.getArrayWord(pr);
        
        int[] qr = new int[]{0, 1};
        Word q = alphabet.getArrayWord(qr);
        
        Automaton dd = FDFAOperations.buildDDollar(p, q);
        System.out.println(dd.toDot());
    }

}
