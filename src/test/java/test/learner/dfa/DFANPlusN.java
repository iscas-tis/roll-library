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

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.operations.DFAGenerator;
import roll.automata.operations.DFAOperations;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class DFANPlusN {
    
    @Test
    public void test() {
        Alphabet input = new Alphabet();
        input.addLetter('a');
        input.addLetter('b');
        input.addLetter('c');
        
        final int numCases = 20;
        for(int i = 1; i <= numCases; i ++) {
            DFA dfa = DFAGenerator.getRandomDFA(input, 4);
            Automaton dkAut = DFAOperations.toDkDFA(dfa);
            Automaton dkCopy = dkAut.clone();
            dkAut = dkAut.concatenate(dkAut.repeat()); // N+
            dkAut.minimize();
            if(dkAut.getAcceptStates().size() > 1) {
                dkCopy.minimize();
                System.out.println(dkCopy.toDot());
                System.out.println("---------------------");
                System.out.println(dkAut.toDot());
            }
            //check whether there exists some DFA with more than 
            // one accepting states and N^+ = N
            assert dkAut.getAcceptStates().size() <= 1;
        }
    }

}
