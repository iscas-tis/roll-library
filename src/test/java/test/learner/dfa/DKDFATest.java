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
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class DKDFATest {
    
    public static void main(String[] args) {
        
        Automaton A = new Automaton();
        State q0 = new State();
        State q1 = new State();
        State q2 = new State();
        
        A.setDeterministic(true);
        A.setInitialState(q0);
        
        q0.addTransition(new Transition('a', q1));        
        q1.addTransition(new Transition('b', q2));
        q2.addTransition(new Transition('a', q1));
        
        q2.setAccept(true);
        
        System.out.println(A.toDot());
        
//        Automaton A1 = A.clone();
//        
//        State p = A1.getInitialState();
//        p = p.step('a');
//        p = p.step('b');
//        A1.setInitialState(p);
//        
//        A1 = A.intersection(A1);
        
        Automaton A1 = new Automaton();
        State a0 = new State();
        State a1 = new State();
        
        A1.setDeterministic(true);
        a0.addTransition(new Transition('a', a1));
        a0.addTransition(new Transition('b', a1));
        
        a1.addTransition(new Transition('a', a1));
        a1.addTransition(new Transition('b', a0));
        A1.setInitialState(a0);
        a0.setAccept(true);
        
        System.out.println(A1.toDot());
        
//        A1 = A1.complement();
        A1 = A1.intersection(A);
//        
        System.out.println(A1.toDot());
        
        
        
    }

}
