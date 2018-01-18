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

import roll.automata.FDFA;
import roll.automata.NBA;
import roll.automata.operations.NBAGenerator;
import roll.learner.fdfa.LearnerFDFA;
import roll.learner.nba.lomega.UtilLOmega;
import roll.learner.nba.lomega.translator.TranslatorFDFA;
import roll.learner.nba.lomega.translator.TranslatorFDFAUnder;
import roll.main.Options;
import roll.main.complement.TeacherNBAComplement;
import roll.query.Query;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBAComplementTest {

    private boolean runComplement(Options options, NBA input) {
        try {
            TeacherNBAComplement teacher = new TeacherNBAComplement(options, input);
            LearnerFDFA learner = UtilLOmega.getLearnerFDFA(options, input.getAlphabet(), teacher);
            System.out.println("starting learning");
            learner.startLearning();
            FDFA model = null;
            while(true) {
                System.out.println("Table is both closed and consistent\n" + learner.toString());
                model = learner.getHypothesis();
                System.out.println(model.toString());
                // along with ce
                Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(model);
                boolean isEq = ceQuery.getQueryAnswer().getLeft();
                if(isEq) {
                    System.out.println(model.toString());
                    break;
                }
                System.out.println("target: \n" + input.toBA() + "counterexample: " + ceQuery + " in:"
                + ceQuery.getQueryAnswer().getRight());
                ceQuery.answerQuery(new HashableValueBoolean(ceQuery.getQueryAnswer().getRight()));
                TranslatorFDFA translator = new TranslatorFDFAUnder(learner);
                translator.setQuery(ceQuery);
                ceQuery = translator.translate();
                learner.refineHypothesis(ceQuery);
            }
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println(input.toBA());
        }
        return false;
    }
    
    @Test
    public void testComplement() {
        Options options = new Options();
        options.structure = Options.Structure.TABLE;
        options.approximation = Options.Approximation.UNDER;
        options.algorithm = Options.Algorithm.SYNTACTIC;
        final int numTests = 20;
        final int numStates = 4;
        for(int i = 0; i < numTests; i ++) {
            NBA input = NBAGenerator.getRandomNBA(numStates, 2);
            if(runComplement(options, input)) {
                break;
            }
            System.out.println("Done for case " + (i +1));
        }
    }
    
    @Test
    public void testNBA1Complement() {
        Options options = new Options();
        options.structure = Options.Structure.TABLE;
        options.approximation = Options.Approximation.UNDER;
        options.algorithm = Options.Algorithm.RECURRENT;
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA input = new NBA(alphabet);
        input.createState();
        input.createState();
        input.createState();
        
        input.getState(0).addTransition(alphabet.indexOf('a'), 2);
        input.getState(2).addTransition(alphabet.indexOf('b'), 1);
        input.getState(1).addTransition(alphabet.indexOf('b'), 2);
        input.getState(1).addTransition(alphabet.indexOf('a'), 2);
        
        input.setInitial(0);
        input.setFinal(1);
        
        runComplement(options, input);
    }
   
}
