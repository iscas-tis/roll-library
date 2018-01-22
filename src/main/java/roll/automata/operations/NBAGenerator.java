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

import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBAGenerator {
    
    public static NBA getRandomNBA(int numState, int numLetter) {

        if (numLetter > 5) {
            throw new UnsupportedOperationException("only allow a,b,c,d,e letters in generated NBA");
        }
        Alphabet alphabet = new Alphabet();
        char[] letters = { 'a', 'b', 'c', 'd', 'e' };

        for (int i = 0; i < numLetter && i < letters.length; i++) {
            alphabet.addLetter(letters[i]);
        }

        NBA result = new NBA(alphabet);
        Random r = new Random(System.currentTimeMillis());

        for (int i = 0; i < numState; i++) {
            result.createState();
        }

        result.setInitial(0);

        // final states
        int numF = r.nextInt(numState - 1);
        numF = numF > 0 ? numF : 1;
        boolean acc = false;
        for (int n = 0; n < numF; n++) {
            int f = r.nextInt(numF);
            if (f != 0) {
                acc = true;
                result.setFinal(f);
            }
        }

        if (!acc) {
            result.setFinal(numF);
        }

        int numTrans = r.nextInt(numState * numLetter);

        // transitions
        for (int k = 0; k < numLetter && k < 5; k++) {
            for (int n = 0; n < numTrans; n++) {
                int i = r.nextInt(numState);
                int j = r.nextInt(numState);
                result.getState(i).addTransition(k, j);
            }
        }

        return result;
    }
        
    public static NBA getRandomLDBA(
            final int numState,
            final int numDetState,
            final int numLetter, 
            final int numFinals, 
            final double density) {

        if (numLetter > 5) {
            throw new UnsupportedOperationException("only allow a,b,c,d,e letters in generated LDBA");
        }
        
        Alphabet alphabet = new Alphabet();
        char[] letters = { 'a', 'b', 'c', 'd', 'e' };

        for (int i = 0; i < numLetter && i < letters.length; i++) {
            alphabet.addLetter(letters[i]);
        }

        NBA result = new NBA(alphabet);
        Random r = new Random(System.currentTimeMillis());

        for (int i = 0; i < numState; i++) {
            result.createState();
        }

        final int numNondetState = numState - numDetState;
        final int numTrans = (int) (numState * density);

        result.setInitial(0);

        // the deterministic states are all in the range 
        // [ numNondetState, numState)
        // so the accepting states should be all in this range 
        for (int i = 0; i < numFinals; i++) {
            result.setFinal(numNondetState + r.nextInt(numDetState));
        }
        
        for (int i = 0; i < numTrans; i++) {
            final StateNFA source = result.getState(r.nextInt(numState));
            final int action = r.nextInt(numLetter);
            if (source.getId() >= numNondetState) {
                // source is a deterministic state, so it can't enable again the same letter 
                if (source.getEnabledLetters().get(action)) {
                    // the transition is not created, so we have to try again
                    i--;
                } else {
                    // the deterministic states are all in the range 
                    // [ numNondetState, numState)
                    // so the target state should be all in this range 
                    source.addTransition(action, numNondetState + r.nextInt(numDetState));
                }
            } else {
                source.addTransition(action, r.nextInt(numState));
            }
        }
    
        return result;
    }

}
