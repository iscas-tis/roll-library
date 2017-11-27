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

import java.util.ArrayList;

import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

/**
 * simple fa like DFA, NFA and NBA
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public abstract class FASimple implements FA {

    protected final ArrayList<State> states;
    protected final int alphabetSize;
    protected int initialState;
    protected final ISet finalStates;
    protected Acc acceptance;
    
    public FASimple(final int alphabetSize) {
        this.alphabetSize = alphabetSize;
        this.states = new ArrayList<>();
        this.finalStates = UtilISet.newISet();
    }
    
    public int getStateSize() {
        return states.size();
    }
    
    public int getAlphabetSize() {
        return alphabetSize;
    }
    
    public State createState() {
        State state = makeState(states.size());
        states.add(state);
        return state;
    }
    
    public void setInitial(int state) {
        initialState = state;
    }
    
    public void setInitial(State state) {
        setInitial(state.getId());
    }
    
    public State getState(int state) {
        assert checkValidState(state);
        return states.get(state);
    }
    
    public int getInitialState() {
        return initialState;
    }
    
    public void setFinal(int state) {
        assert checkValidState(state);
        finalStates.set(state);
    }
    
    public ISet getFinalStates() {
        return finalStates.clone();
    }
    
    public boolean isFinal(int state) {
        assert checkValidState(state);
        return finalStates.get(state);
    }
    
    protected abstract State makeState(int index);
    
    protected boolean checkValidState(int state) {
        return state >= 0 && state < states.size();
    }
    
    protected boolean checkValidLetter(int letter) {
        return letter >= 0 && letter < alphabetSize;
    }
    
    @Override
    public Acc getAcc() {
        return acceptance;
    }

}
