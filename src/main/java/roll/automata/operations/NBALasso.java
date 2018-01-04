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

import roll.automata.NBA;
import roll.automata.StateFA;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBALasso {
    private final Word stem;
    private final Word loop;
    private NBA result;
    public NBALasso(Word stem, Word loop) {
        assert stem != null && loop != null;
        this.stem = stem;
        this.loop = loop;
        if(loop.isEmpty()) {
            // if loop is empty, so set it empty automaton
            setEmptyAutomaton();
        }
    }
    
    private void setEmptyAutomaton() {
        this.result = new NBA(stem.getAlphabet());
        final int state = 0;
        this.result.createState();
        for(int letter = 0; letter < result.getAlphabetSize(); letter ++) {
            this.result.getState(state).addTransition(letter, state);
        }
        this.result.setFinal(state);
    }
    
    public NBA getNBA() {
        if(result == null) {
            this.result = new NBA(stem.getAlphabet());
            initialize();
        }
        return result;
    }
    
    private void initialize() {
        for(int i = 0; i <= getSum(); i ++) {
            StateFA state = result.createState();
            assert state.getId() == i;
            result.getState(i).addTransition(getNextLetter(state.getId())
                    , getNextState(state.getId()));
            if(isFinalState(state.getId())) {
                result.setFinal(state.getId());
            }
        }
        result.setInitial(0);
    }


    // ------------- transition for lasso word -------------------------
    public int getSum() {
        return stem.length() + loop.length();
    }
    
    public int getNextLetter(int state) {
        if(state < stem.length()) {
            return stem.getLetter(state);
        }
        if(state < getSum()) {
            return loop.getLetter(state - stem.length());
        }
        return loop.getFirstLetter();
    }
    
    public int getNextState(int state) {
        assert state >= 0 && state <= getSum();
        if(state < getSum()) {
            return state + 1;
        }
        return stem.length() + 1;
    }
    
    public boolean isFinalState(int state) {
        assert state >= 0 && state <= getSum();
        return state > stem.length();
    }

}
