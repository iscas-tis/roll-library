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

package roll.automata;

import java.util.Arrays;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

// all DFA state will be complete in the sense that
// it has successors for every letter
public class StateDFA extends StateFA {
    private final DFA dfa;
    private final int[] successors; // // Alphabet -> Q
    
    public StateDFA(final DFA dfa, final int id) {
        super(id);
        assert dfa != null;
        this.dfa = dfa;
        this.successors = new int[dfa.getAlphabetSize()];
        Arrays.fill(successors, -1); // with initial value -1
    }

    @Override
    public DFA getFA() {
        return dfa;
    }

    @Override
    public void addTransition(int letter, int state) {
        assert dfa.checkValidLetter(letter);
        successors[letter] = state;
    }
    
    public int getSuccessor(int letter) {
        assert dfa.checkValidLetter(letter);
        return successors[letter];
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("  " + getId() + " [label=\"" + getId() + "\"");
        if(dfa.isFinal(getId())) builder.append(", shape = doublecircle");
        else builder.append(", shape = circle");
        builder.append("];\n");
        // transitions
        for(int i = 0; i < successors.length; i ++) {
            builder.append("  " + getId() + " -> " + successors[i]
                    + " [label=\"" + i + "\"];\n");
        }
        return builder.toString();
    }
    
}
