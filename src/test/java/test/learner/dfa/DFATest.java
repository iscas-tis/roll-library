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

package test.learner.dfa;

import org.junit.Test;

import roll.automata.DFA;
import roll.learner.LearnerDFA;
import roll.learner.dfa.table.LearnerDFATableColumn;
import roll.main.Options;
import roll.oracle.dfa.dk.TeacherDFADK;
import roll.query.Query;
import roll.table.HashableValue;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class DFATest {
    
    private DFA getDFA() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        
        DFA dfa = new DFA(alphabet);
        dfa.createState();
        dfa.createState();
        dfa.createState();
        dfa.createState();
        int fst = 0, snd = 1, thd = 2, fur = 3;
        dfa.getState(fst).addTransition(0, fst);
        dfa.getState(fst).addTransition(1, snd);
        dfa.getState(snd).addTransition(0, snd);
        dfa.getState(snd).addTransition(1, thd);
        dfa.getState(thd).addTransition(0, thd);
        dfa.getState(thd).addTransition(1, fur);
        dfa.getState(fur).addTransition(0, fur);
        dfa.getState(fur).addTransition(1, fst);
        dfa.setInitial(fst);
        dfa.setFinal(fur);
        return dfa;
    }
    
    @Test
    public void test4nPlus3() {
        Options options = new Options();
        DFA machine = getDFA();
        Alphabet alphabet = machine.getAlphabet();
        TeacherDFADK teacher = new TeacherDFADK(options, machine);
        LearnerDFA learner = null;
        
        learner = new LearnerDFATableColumn(options, alphabet, teacher);
//        else if(algo == LearnerType.DFA_KV) {
//            learner = new LearnerDFATreeKV(options, alphabet, teacher); 
//        }else if(algo == LearnerType.DFA_COLUMN_TREE){
//            learner = new LearnerDFATreeColumn(options, alphabet, teacher); 
//        }else {
//            learner = new LearnerDFATableLStar(options, alphabet, teacher); 
//        }
        System.out.println("starting learning");
        learner.startLearning();

        while(true) {
            System.out.println("Table is both closed and consistent\n" + learner.toString());
            DFA model = learner.getHypothesis();
            // along with ce
            Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(model);
            boolean isEq = ceQuery.getQueryAnswer().get();
            if(isEq) {
                System.out.println(model.toString());
                break;
            }
            ceQuery.answerQuery(null);
//          HashableValue val = teacher.answerMembershipQuery(ceQuery);
//          ceQuery.answerQuery(val);
            learner.refineHypothesis(ceQuery);
        }
    }

}
