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

import roll.automata.NBA;
import roll.learner.nba.ldollar.LearnerLDollar;
import roll.main.Options;
import roll.oracle.rabit.TeacherNBARABIT;
import roll.oracle.sampler.TeacherNBASampler;
import roll.query.Query;
import roll.table.HashableValue;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBALDollarTest {
    
    @Test
    public void testSampler() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        target.createState();
        target.createState();
        
        // a^w + ab^w
        int fst = 0, snd = 1, thd = 2;
        target.getState(fst).addTransition(alphabet.indexOf('a'), snd);
        target.getState(fst).addTransition(alphabet.indexOf('a'), thd);
        target.getState(snd).addTransition(alphabet.indexOf('a'), snd);
        target.getState(thd).addTransition(alphabet.indexOf('b'), thd);
        target.setInitial(fst);
        target.setFinal(snd);
        target.setFinal(thd);
        
        Options options = new Options();
        options.structure = Options.Structure.TABLE;
        options.epsilon = 0.00018;
        options.delta = 0.0001;
        TeacherNBASampler teacher = new TeacherNBASampler(options, target);
        LearnerLDollar learner = new LearnerLDollar(options, alphabet, teacher);
        System.out.println("starting learning");
        learner.startLearning();
        while(true) {
            System.out.println("Table is both closed and consistent\n" + learner.toString());
            NBA model = learner.getHypothesis();
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
        
    }
    
    @Test
    public void testRABIT() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        target.createState();
        target.createState();
        
        // a^w + ab^w
        int fst = 0, snd = 1, thd = 2;
        target.getState(fst).addTransition(alphabet.indexOf('a'), snd);
        target.getState(fst).addTransition(alphabet.indexOf('a'), thd);
        target.getState(snd).addTransition(alphabet.indexOf('a'), snd);
        target.getState(thd).addTransition(alphabet.indexOf('b'), thd);
        target.setInitial(fst);
        target.setFinal(snd);
        target.setFinal(thd);
        
        System.out.println(target.toString());
        
        Options options = new Options();
        options.structure = Options.Structure.TABLE;
        options.epsilon = 0.00018;
        options.delta = 0.0001;
        TeacherNBARABIT teacher = new TeacherNBARABIT(options, target);
        LearnerLDollar learner = new LearnerLDollar(options, alphabet, teacher);
        System.out.println("starting learning");
        learner.startLearning();
        while(true) {
            System.out.println("Table is both closed and consistent\n" + learner.toString());
            NBA model = learner.getHypothesis();
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
        
    }

}
