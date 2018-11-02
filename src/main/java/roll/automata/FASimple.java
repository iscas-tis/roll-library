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
import java.util.List;

import roll.jupyter.NativeTool;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;

/**
 * simple FA like DFA, NFA and NBA
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public abstract class FASimple implements FA {

    protected final ArrayList<StateFA> states;
    protected final Alphabet alphabet;
    protected int initialState = -1; // no initial state available at first
    protected final ISet finalStates;
    protected Acc acceptance;
    
    public FASimple(final Alphabet alphabet) {
        this.alphabet = alphabet;
        this.states = new ArrayList<>();
        this.finalStates = UtilISet.newISet();
    }
    
    public Alphabet getAlphabet() {
        return alphabet;
    }
    
    public int getStateSize() {
        return states.size();
    }
    
    public int getAlphabetSize() {
        return alphabet.getLetterSize();
    }
    
    public StateFA createState() {
        StateFA state = makeState(states.size());
        states.add(state);
        return state;
    }
    
    public void setInitial(int state) {
        initialState = state;
    }
    
    public boolean isInitial(int state) {
        return state == initialState;
    }
    
    public void setInitial(State state) {
        setInitial(state.getId());
    }
    
    public StateFA getState(int state) {
        assert checkValidState(state);
        return states.get(state);
    }
    
    public int getInitialState() {
        return initialState;
    }
    
    public abstract ISet getSuccessors(int state, int letter);
    
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
    
    protected abstract StateFA makeState(int index);
    
    protected boolean checkValidState(int state) {
        return state >= 0 && state < states.size();
    }
    
    protected boolean checkValidLetter(int letter) {
        return letter >= 0 && letter < getAlphabetSize();
    }
    
    @Override
    public Acc getAcc() {
        return acceptance;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph {\n");
        int startNode = this.getStateSize();
        for (int node = 0; node < this.getStateSize(); node++) {
            builder.append(this.getState(node).toString());
        }   
        builder.append("  " + startNode + " [label=\"\", shape = plaintext];\n");
        builder.append("  " + startNode + " -> " + this.getInitialState() + " [label=\"\"];\n");
        builder.append("}\n");
        return builder.toString();
    }
    
    @Override
    public String toString(List<String> apList) {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph {\n");
        int startNode = this.getStateSize();
        for (int node = 0; node < this.getStateSize(); node++) {
            builder.append(this.getState(node).toString(apList));
        }   
        builder.append("  " + startNode + " [label=\"\", shape = plaintext];\n");
        builder.append("  " + startNode + " -> " + this.getInitialState() + " [label=\"\"];\n");
        builder.append("}\n");
        return builder.toString();
    }
    
    public String toDot() {
        List<String> apList = new ArrayList<>();
        for(int i = 0; i < alphabet.getLetterSize(); i ++) {
            apList.add("" + alphabet.getLetter(i));
        }
        return toString(apList);
    }
    
    @Override
    public String toHTML() {
        return NativeTool.dot2SVG(toDot());
    }
    
    public String toBA() {
        StringBuilder builder = new StringBuilder();
        builder.append("[" + getInitialState() + "]\n");
        for (int node = 0; node < this.getStateSize(); node++) {
            builder.append(this.getState(node).toBA());
        }
        for (int acc : finalStates) {
            builder.append("[" + acc + "]\n");
        }
        return builder.toString();
    }

}
