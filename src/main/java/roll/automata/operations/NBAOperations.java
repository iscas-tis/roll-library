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

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.automata.NBA;
import roll.automata.StateFA;
import roll.automata.StateNFA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBAOperations {
    
    private static int getState(NBA nba, State state, TObjectIntMap<State> map) {
        if(map.containsKey(state)) {
            return map.get(state);
        }
        StateFA nbaState = nba.createState();
        map.put(state, nbaState.getId());
        if(state.isAccept()) {
            nba.setFinal(nbaState.getId());
        }
        return nbaState.getId();
    }
    public static NBA fromDkNBA(Automaton dkAut, Alphabet alphabet) {
        NBA nba = new NBA(alphabet);
        TObjectIntMap<State> map = new TObjectIntHashMap<>();
        State init = dkAut.getInitialState();
        int initNr = getState(nba, init, map);
        nba.setInitial(initNr);
        LinkedList<State> queue = new LinkedList<>();
        queue.add(init);
        ISet visited = UtilISet.newISet();
        while(! queue.isEmpty()) {
            State currState = queue.remove();
            int stateNr = getState(nba, init, map);
            if(visited.get(stateNr)) {
                continue;
            }
            visited.set(stateNr);
            // visiting successors
            for(Transition trans : currState.getTransitions()) {
                for(char label = trans.getMin(); label <= trans.getMax(); label ++) {
                    int succNr = getState(nba, trans.getDest(), map);
                    if(! visited.get(succNr)) {
                        StateNFA state = nba.getState(stateNr);
                        state.addTransition(alphabet.indexOf(label), succNr);
                        queue.add(trans.getDest());
                    }
                }
            }
        }
        return nba;
    }

}
