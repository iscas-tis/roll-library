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

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.NFA;
import roll.automata.operations.DFAGenerator;
import roll.automata.operations.NFAOperations;
import roll.learner.nfa.nlstar.LearnerNFANLStar;
import roll.learner.nfa.table.LearnerNFATable;
import roll.main.Options;
import roll.oracle.nfa.TeacherNFA;
import roll.query.Query;
import roll.table.HashableValue;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NFATest {
    
    public static void main(String[] args) {
        
        //test();
        ramdomTest();
        
    }
    
    private static void test() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        
        NFA nfa = new NFA(alphabet);
        nfa.createState();
        nfa.createState();
        nfa.createState();
        nfa.createState();
        
        nfa.setFinal(3);
        nfa.setInitial(0);
        
        nfa.getState(0).addTransition(0, 0);
        nfa.getState(0).addTransition(1, 0);
        nfa.getState(0).addTransition(1, 1);
        
        nfa.getState(1).addTransition(0, 2);
        nfa.getState(1).addTransition(1, 2);
        
        nfa.getState(2).addTransition(0, 3);
        nfa.getState(2).addTransition(1, 3);
        
        testLearnerNFA2(nfa, alphabet);
        
    }
    
    private static void test1() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        
        NFA nfa = new NFA(alphabet);
        nfa.createState();
        nfa.createState();
        nfa.createState();
        nfa.createState();
        
        nfa.setFinal(3);
        nfa.setInitial(0);
        
        nfa.getState(0).addTransition(0, 1);
        nfa.getState(0).addTransition(1, 1);
       
        nfa.getState(1).addTransition(0, 2);
        nfa.getState(1).addTransition(1, 2);
        
        nfa.getState(2).addTransition(1, 3);
        
        nfa.getState(3).addTransition(0, 3);
        nfa.getState(3).addTransition(1, 3);
        
        testLearnerNFA2(nfa, alphabet);
        
    }
    
    private static void ramdomTest() {

        Alphabet input = new Alphabet();
        input.addLetter('a');
        input.addLetter('b');
//        input.addLetter('c');
        
        int numCases = 30;
        int numStates = 10;
        int numOK = 0;
        
        long start = System.currentTimeMillis();
        
        for(int i = 0; i < numCases; i ++) {
            DFA dfa = DFAGenerator.getRandomDFA(input, numStates);
            System.out.println("Case " + i );
            if(NFATest.testLearnerNFA2(dfa, input)) {
                numOK ++;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Tested " + numCases + " cases and " + numOK + " cases passed in "
                        + ((end-start) / 1000) + " secs !");
    }
    
    public static boolean testLearnerNFA(NFA machine, Alphabet alphabet) {
        System.out.println("Target: \n" + machine.toDot());
        Options options = new Options();
        TeacherNFA teacher = new TeacherNFA(options, machine);
        LearnerNFATable learner = new LearnerNFATable(options, alphabet, teacher);

        System.out.println("starting learning");
        learner.startLearning();

        while(true) {
            System.out.println("Table is both closed and consistent\n" + learner.toString());
            NFA model = learner.getHypothesis();
//            System.out.println("Hypothesis:\n" + model.toString());
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
        
        return true;
    }
    
    public static boolean testLearnerNFA2(NFA machine, Alphabet alphabet) {
//        System.out.println("Target: \n" + machine.toDot());
        Options options = new Options();
        options.structure = Options.Structure.TREE;
        options.binarySearch = true;
        TeacherNFA teacher = new TeacherNFA(options, machine);
        LearnerNFANLStar learner = new LearnerNFANLStar(options, alphabet, teacher);
//        LearnerNFAColumn learner = new LearnerNFAColumn(options, alphabet, teacher);

        System.out.println("starting learning");
        learner.startLearning();

        NFA hypo;
        while(true) {
            System.out.println("Table is both closed and consistent\n" + learner.toString());
            NFA model = learner.getHypothesis();
//            System.out.println("Hypothesis:\n" + model.toString());
            // along with ce
            Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(model);
            boolean isEq = ceQuery.getQueryAnswer().get();
            if(isEq) {
                System.out.println(model.toString());
                hypo = model;
                break;
            }
            ceQuery.answerQuery(null);
            System.out.println("CE:\n" + ceQuery.toString());
            System.out.println(machine);
            learner.refineHypothesis(ceQuery);
        }
        Automaton dk = NFAOperations.toDkNFA(hypo);
        dk.determinize();
        System.out.println(dk.toDot());
        
        return true;
    }

}
