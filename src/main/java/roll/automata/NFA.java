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

import roll.util.ISet;
import roll.util.UtilISet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class NFA extends FASimple {

    public NFA(int alphabetSize) {
        super(alphabetSize);
        this.acceptance = new AccNFA(this);
    }

    @Override
    public AccType getAccType() {
        return AccType.NFA;
    }

    @Override
    public State makeState(int index) {
        return new NFAState(this, index);
    }
    
    @Override
    public NFAState getState(int state) {
        return (NFAState) super.getState(state);
    }
    
    public ISet getSuccessors(ISet states, Word word) {
        ISet currentStates = states;
        int index = 0;
        while(index < word.length()) {
            ISet nextStates = UtilISet.newISet();
            for(final int state : currentStates) {
                nextStates.or(getSuccessors(state, word.getLetter(index)));
            }
            currentStates = nextStates;
            ++ index;
        }
        return currentStates;
    }
    
    public ISet getSuccessors(int state, Word word) {
        ISet states = UtilISet.newISet();
        states.set(state);
        return getSuccessors(states, word);
    }
    
    public ISet getSuccessors(Word word) {
        return getSuccessors(getInitialState(), word);
    }

    public ISet getSuccessors(int state, int letter) {
        return getState(state).getSuccessors(letter);
    }
    
    private class AccNFA extends AccFA {

        public AccNFA(FASimple fa) {
            super(fa);
        }

        @Override
        public boolean isAccepting(Word prefix, Word suffix) {
            Word word = prefix.concat(suffix);
            return isAccepting(getSuccessors(word));
        }
        
    }

}
