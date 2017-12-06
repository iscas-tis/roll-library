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

package roll.automata.operations;

import java.util.Random;

import roll.automata.DFA;
import roll.automata.StateDFA;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class DFAGenerator {
    
    public static DFA getRandomDFA(Alphabet alphabet, int numState) {

        DFA result = new DFA(alphabet);

        Random r = new Random(System.currentTimeMillis());
        
        final int apSize = alphabet.getLetterSize();

        for(int i = 0; i < numState; i ++) {
            result.createState();
        }

        // add self loops for those transitions
        for(int i = 0; i < numState; i ++) {
            StateDFA state = result.getState(i);
            for(int k=0 ; k < apSize; k++){
                state.addTransition(k, i);
            }
        }

        result.setInitial(0);

        // final states
        int numF = r.nextInt(numState-1);
        boolean hasF = false;
        numF = numF > 0 ? numF : 1;
        for(int n = 0; n < numF ; n ++) {
            int f = r.nextInt(numF);
            if(f != 0) {
                result.setFinal(f);
                hasF = true;
            }
        }

        if(! hasF) {
            result.setFinal(numF);
        }

        int numTrans = r.nextInt(numState * apSize);

        // transitions
        for(int k=0 ; k < apSize; k++){
            for(int n = 0; n < numTrans; n++ ){
                int i=r.nextInt(numState);
                int j=r.nextInt(numState);
                result.getState(i).addTransition(k, j);
            }
        }

        return result;
    }

}
