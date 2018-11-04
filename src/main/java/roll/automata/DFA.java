/* Copyright (c) 2018 -                                                   */
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

package roll.automata;

import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class DFA extends NFA {
    
    public DFA(final Alphabet alphabet) {
        super(alphabet);
    }

    @Override
    public AutType getAccType() {
        return AutType.DFA;
    }
    
    // specialized for DFA
    public int getSuccessor(Word word) {
        return getSuccessor(getInitialState(), word);
    }
    
    public int getSuccessor(int state, int letter) {
        return getState(state).getSuccessor(letter);
    }
    
    public int getSuccessor(int state, Word word) {
        int index = 0;
        int currState = state;
        while(index < word.length()) {          
            currState = getSuccessor(currState, word.getLetter(index));
            ++ index;
        }
        return currState;
    }

}
