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
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.automata.NBA;
import roll.automata.NFA;
import roll.automata.StateFA;
import roll.automata.StateNFA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

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
        visited.set(initNr);
        while(! queue.isEmpty()) {
            State currState = queue.remove();
            int stateNr = getState(nba, currState, map);
            for(Transition trans : currState.getTransitions()) {
                for(char label = trans.getMin(); label <= trans.getMax(); label ++) {
                    int succNr = getState(nba, trans.getDest(), map);
                    nba.getState(stateNr).addTransition(alphabet.indexOf(label), succNr);
                    if(! visited.get(succNr)) {
                        queue.add(trans.getDest());
                        visited.set(succNr);
                    }
                }
            }
        }
        
        return nba;
    }
    
    public static boolean accepts(NBA nba, Word stem, Word loop) {
        assert nba != null && stem != null && loop != null;
        NBALasso lasso = new NBALasso(stem, loop);
        if(loop.isEmpty()) return false;
        NBAIntersectionCheck checker = new NBAIntersectionCheck(nba, lasso.getNBA());
        return !checker.isEmpty();
    }
    
    private static int getState(NBA result, NBA input, int state, TIntIntMap map) {
        if(map.containsKey(state)) {
            return map.get(state);
        }
        StateFA nbaState = result.createState();
        map.put(state, nbaState.getId());
        if(input.isFinal(state)) {
            result.setFinal(nbaState.getId());
        }
        return nbaState.getId();
    }
    
    public static boolean isSemideterministic(NBA result) {
//        NBA result = removeDeadStates(input);
        ISet finalStates = result.getFinalStates();
        LinkedList<Integer> workList = new LinkedList<>();
        // add to list
        for(final int fin : finalStates) {
            workList.addFirst(fin);
        }
        
        ISet visited = UtilISet.newISet();
        while(! workList.isEmpty()) {
            int s = workList.remove();
            if(visited.get(s)) continue;
            visited.set(s);
            for(int c = 0; c < result.getAlphabetSize(); c ++) {
                ISet succs = result.getSuccessors(s, c);
                if(succs.isEmpty()) continue;
                if(succs.cardinality() > 1) return false;
                for(final int sp : succs) {
                    if(! visited.get(sp)) {
                        workList.addFirst(sp);
                    }
                }
            }
        }
        
        return true;
    }
    
    
    public static NBA removeDeadStates(NBA input) {
        NBA reach = new NBA(input.getAlphabet());
        TIntIntMap map = new TIntIntHashMap();
        TIntObjectMap<StateContainer> mapSC = new TIntObjectHashMap<>();
        int init = input.getInitialState();
        int rInit = getState(reach, input, init, map);
        reach.setInitial(rInit);
        LinkedList<Integer> queue = new LinkedList<>();
        queue.add(init);
        ISet visited = UtilISet.newISet();
        visited.set(init);
        while(! queue.isEmpty()) {
            int lState = queue.remove();
            int rState = getState(reach, input, lState, map);
            for(int c = 0; c < input.getAlphabetSize(); c ++) {
                for(int lSucc : input.getSuccessors(lState, c)) {
                    int rSucc = getState(reach, input, lSucc, map);
                    // record outgoing transitions
                    reach.getState(rState).addTransition(c, rSucc);
                    StateContainer scSucc = mapSC.get(rSucc);
                    if(scSucc == null) {
                        scSucc = new StateContainer(rSucc, reach);
                    }
                    // record incoming transitions
                    scSucc.addPredecessors(c, rState);
                    mapSC.put(rSucc, scSucc);
                    if(! visited.get(lSucc)) {
                        queue.add(lSucc);
                        visited.set(lSucc);
                    }
                }
            }
        }
        // now we have all reachable states, we need to remove those reachable states
        // which cannot reach final states
        ISet used = reach.getFinalStates();
        ISet backReached = UtilISet.newISet();
        while(true) {
            backReached.or(used);
            ISet prevs = UtilISet.newISet();
            for(final int s : used) {
                StateContainer sC = mapSC.get(s);
                if( sC == null && s == reach.getInitialState()) {
                    continue;
                }else if(sC == null){
                    assert false : "State " + s + " has no predecessors";
                }
                // one step predecessors
                for(int c = 0; c < reach.getAlphabetSize(); c ++) {
                    for(StateNFA spre : sC.getPredecessors(c)) {
                        prevs.set(spre.getId());
                    }
                }
            }
            // new one step predecessors
            prevs.andNot(backReached);
            if(prevs.isEmpty()) {
                // no more predecessors
                break;
            }
            used = prevs;
        }
        map.clear();
        
        NBA result = new NBA(input.getAlphabet());
        init = reach.getInitialState();
        rInit = getState(result, reach, init, map);
        result.setInitial(rInit);
        queue = new LinkedList<>();
        queue.add(init);
        visited.clear();
        visited.set(init);
        while(! queue.isEmpty()) {
            int lState = queue.remove();
            // ignore unused states
            if(! backReached.get(lState)) continue;
            int rState = getState(result, reach, lState, map);
            for(int c = 0; c < reach.getAlphabetSize(); c ++) {
                for(int lSucc : reach.getSuccessors(lState, c)) {
                    if(! backReached.get(lSucc)) continue;
                    int rSucc = getState(result, reach, lSucc, map);
                    // record outgoing transitions
                    result.getState(rState).addTransition(c, rSucc);
                    if(! visited.get(lSucc)) {
                        queue.add(lSucc);
                        visited.set(lSucc);
                    }
                }
            }
        }
        
        return result;
    }
    
    public static int getNumberOfTransitions(NFA nfa) {
        int result = 0;
        for(int i = 0; i < nfa.getStateSize(); i ++) {
            for(int c = 0; c < nfa.getAlphabetSize(); c ++) {
                result += nfa.getSuccessors(i, c).cardinality();
            }
        }
        return result;
    }
    

}
