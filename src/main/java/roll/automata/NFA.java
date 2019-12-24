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

import java.util.ArrayList;
import java.util.List;

import roll.jupyter.NativeTool;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Nodeterministic Finite Automata (NFA)
 * */
public class NFA implements Acceptor {

    protected final ArrayList<StateNFA> states;
    protected final Alphabet alphabet;
    protected int initialState = -1;  // no initial state available at first
    protected final ISet finalStates; // final states
    protected Accept accept;
    
    public NFA(final Alphabet alphabet) {
        this.alphabet = alphabet;
        this.states = new ArrayList<>();
        this.finalStates = UtilISet.newISet();
        this.accept = new AcceptNFA(this);
    }
    
    // -------------------------------------------
    @Override
    public Accept getAcc() {
        return accept;
    }

    @Override
    public AutType getAccType() {
        return AutType.NFA;
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
    
    public StateNFA getState(int state) {
        assert checkValidState(state);
        return states.get(state);
    }
    
    public int getInitialState() {
        return initialState;
    }
    
    public ISet getFinalStates() {
        return finalStates.clone();
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
    
    // for NFA
    public ISet getSuccessors(int state, int letter) {
        return getState(state).getSuccessors(letter);
    }
    
    // -------------------------------------------
    
    public void setInitial(int state) {
        initialState = state;
    }
    
    public void setInitial(StateNFA state) {
        setInitial(state.getId());
    }
    
    public void setFinal(int state) {
        assert checkValidState(state);
        finalStates.set(state);
    }
    
    // -------------------------------------------
    public StateNFA createState() {
        StateNFA state = makeState(states.size());
        states.add(state);
        return state;
    }
    
    protected StateNFA makeState(int index) {
        return new StateNFA(this, index);
    }
    
    // -------------------------------------------
    public boolean isInitial(int state) {
        return state == initialState;
    }
    
    public boolean isFinal(int state) {
        assert checkValidState(state);
        return finalStates.get(state);
    }
    
    protected boolean checkValidState(int state) {
        return state >= 0 && state < states.size();
    }
    
    protected boolean checkValidLetter(int letter) {
        return letter >= 0 && letter < getAlphabetSize();
    }

    
    // -------------------------------------------
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph {\n");
        builder.append("  rankdir=LR;\n");
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
        builder.append("  rankdir=LR;\n");
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
        builder.append("[" + getInitialState() + "]");
        for (int node = 0; node < this.getStateSize(); node++) {
            builder.append(this.getState(node).toBA());
        }
        for (int acc : finalStates) {
            builder.append("\n[" + acc + "]");
        }
        return builder.toString();
    }
}
