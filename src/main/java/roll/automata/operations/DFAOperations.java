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

import java.util.LinkedList;
import java.util.Queue;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.DFA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class DFAOperations {
    
    private static State getState(TIntObjectMap<State> map, int stateNr) {
        State state = map.get(stateNr);
        if(state == null) {
            state = new State();
            map.put(stateNr, state);
        }
        return state;
    }
    
    public static Automaton toDkDFA(DFA dfa) {
        TIntObjectMap<State> map = new TIntObjectHashMap<>();
        return toDkDFA(map, dfa);
    }
    
    public static Automaton toDkDFA(TIntObjectMap<State> map, DFA dfa) {  
        Automaton dkAut = new Automaton();
        
        for(int stateNr = 0; stateNr < dfa.getStateSize(); stateNr ++) {
            State state = getState(map, stateNr);
            // initial states
            if(dfa.isInitial(stateNr)) {
                dkAut.setInitialState(state);
            }
            // final states
            if(dfa.isFinal(stateNr)) {
                state.setAccept(true);
            }

            for (int letter = 0; letter < dfa.getAlphabetSize(); letter ++) {
                int succNr = dfa.getSuccessor(stateNr, letter);
                State stateSucc = getState(map, succNr);
                state.addTransition(new Transition(dfa.getAlphabet().getLetter(letter),
                        stateSucc));
            }
        }
        
        dkAut.setDeterministic(true);
        // should not restore invariant, it may contain no final states
        //dkAut.restoreInvariant();
        //automaton.minimize();
        return dkAut;
    }
    
    // Transfers a DFA into a dk.brics.automaton
    // with specific initial and final state.
    public static Automaton toDkDFA(DFA dfa, int init, int fin){
        Automaton dkAut = new Automaton();
        TIntObjectMap<State> map = new TIntObjectHashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        State initState = getState(map, init);
        dkAut.setInitialState(initState);
        queue.add(init);
        ISet visited = UtilISet.newISet();
        
        while(! queue.isEmpty()) {
            int stateNr = queue.poll();
            if(visited.get(stateNr)) continue;
            visited.set(stateNr);
            State state = getState(map, stateNr);
            for (int letter = 0; letter < dfa.getAlphabetSize(); letter ++) {
                int succNr =  dfa.getSuccessor(stateNr, letter);
                State stateSucc = getState(map, succNr);
                state.addTransition(new Transition(dfa.getAlphabet().getLetter(letter),
                        stateSucc));
                if(! visited.get(succNr)) {
                    queue.add(succNr);
                }
            }
        }
        
        getState(map, fin).setAccept(true);
        dkAut.setDeterministic(true);
        dkAut.restoreInvariant();
        dkAut.minimize();
        return dkAut;
    }

}
