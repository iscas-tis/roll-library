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

package roll.main.inclusion;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import algorithms.Minimization;
import algorithms.Options;
import algorithms.Simulation;
import automata.AutomatonPreprocessingResult;
import automata.Buchi;
import automata.FAState;
import automata.FiniteAutomaton;
import automata.IBuchi;
import automata.IState;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class UtilInclusion {
    
    private static int getOrAddState(NBA nba, FAState left, TIntIntMap map) {
        int right = -1;
        if(map.containsKey(left.id)) {
            right = map.get(left.id);
        }
        if(right == -1) {
            StateNFA rtSt = nba.createState();
            right = rtSt.getId();
            map.put(left.id, right);
        }
        return right;
    }
    
    public static NBA toNBA(FiniteAutomaton aut, Alphabet alphabet) {
        NBA result = new NBA(alphabet);
        TIntIntMap map = new TIntIntHashMap();
        for(FAState st : aut.states) {
            int rSt = getOrAddState(result, st, map);
            // all successors
            Iterator<String> nextIt = st.nextIt();
            while(nextIt.hasNext()) {
                String symb = nextIt.next();
                Set<FAState> succs = st.getNext(symb);
                int ch = alphabet.getLetter(symb.charAt(0));
                if(succs == null) continue;
                for(FAState succ : succs) {
                    int rSucc = getOrAddState(result, succ, map);
                    result.getState(rSt).addTransition(ch, rSucc);
                }
            }
            if(st.id == aut.getInitialState().id) {
                result.setInitial(rSt);
            }
            if(aut.F.contains(st)) {
                result.setFinal(rSt);
            }
        }
        return result;
    }

    private static FAState getOrAddState(FiniteAutomaton result,TIntObjectMap<FAState> map, int state) {
        FAState rState = map.get(state);
        if(rState == null) {
            rState = result.createState();
            map.put(state, rState);
        }
        return rState;
    }
    
    //Buchi lib not used any more
    private static IState getOrAddState(IBuchi result,TIntObjectMap<IState> map, int state) {
        IState rState = map.get(state);
        if(rState == null) {
            rState = result.addState();
            map.put(state, rState);
        }
        return rState;
    }
    
    public static IBuchi toBuchiNBA(NBA nba) {
        TIntObjectMap<IState> map = new TIntObjectHashMap<>();
        IBuchi result = new Buchi(nba.getAlphabetSize());
        for(int state = 0; state < nba.getStateSize(); state ++) {
            IState rState = getOrAddState(result, map, state);
            for(int letter = 0; letter < nba.getAlphabetSize(); letter ++) {
                ISet succs = nba.getSuccessors(state, letter);
                for(final int succ : succs) {
                    IState rSucc = getOrAddState(result, map, succ);
                    rState.addSuccessor(letter, rSucc.getId());
                }
            }
            if(nba.isInitial(state)) {
                result.setInitial(rState.getId());
            }
            if(nba.isFinal(state)) {
                result.setFinal(rState.getId());
            }
        }
        
        return result;
    }
    
    public static FiniteAutomaton toRABITNBA(NBA nba) {
        TIntObjectMap<FAState> map = new TIntObjectHashMap<>();
        FiniteAutomaton result = new FiniteAutomaton();
        Alphabet alphabet = nba.getAlphabet();
        
        for(int state = 0; state < nba.getStateSize(); state ++) {
            FAState rState = getOrAddState(result, map, state);
            for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
                ISet succs = nba.getSuccessors(state, letter);
                for(final int succ : succs) {
                    FAState rSucc = getOrAddState(result, map, succ);
                    result.addTransition(rState, rSucc, alphabet.getLetter(letter) + "");
                }
            }
            if(nba.isInitial(state)) {
                result.setInitialState(rState);
            }
            if(nba.isFinal(state)) {
                result.F.add(rState);
            }
        }
        
        return result;
    }
    
    public static boolean removeDeadStates(FiniteAutomaton automaton) {
        // preprocessing, make sure every state in automaton has successors
        Set<FAState> states = new TreeSet<>();
        states.addAll(automaton.states);
        while (true) {
            boolean changed = false;
            Set<FAState> temp = new TreeSet<>();
            for (FAState st : states) {
                if (st.getNext().isEmpty()) {
                    automaton.removeState(st);
                    temp.add(st);
                    changed = true;
                }
            }
            if (!changed)
                break;
            else {
                states.removeAll(temp);
            }
        }
        states = null;
        if (automaton.F.isEmpty())
            return true;
        // start sampling
        FAState s = automaton.getInitialState();
        if (s.getNext().isEmpty())
            return true;
        return false;
    }
    
    public static FiniteAutomaton copyAutomaton(FiniteAutomaton aut) {
        FiniteAutomaton result = new FiniteAutomaton();
        FAState init = aut.getInitialState();
        TIntObjectMap<FAState> stMap = new TIntObjectHashMap<>();
        FAState rInit = result.createState();
        result.setInitialState(rInit);
        stMap.put(init.id, rInit);
        if(aut.F.contains(init)) {
            result.F.add(rInit);
        }
        
        Queue<FAState> queue = new LinkedList<>();
        queue.add(init);
        while(! queue.isEmpty()) {
            FAState state = queue.poll();
            FAState rState = stMap.get(state.id);
            Iterator<String> strIt = state.nextIt();
            while(strIt.hasNext()) {
                String label = strIt.next();
                Set<FAState> succs = state.getNext(label);
                for(FAState succ : succs) {
                    FAState rSucc = stMap.get(succ.id);
                    if(rSucc == null) {
                        rSucc = result.createState();
                        stMap.put(succ.id, rSucc);
                        queue.add(succ);
                    }
                    result.addTransition(rState, rSucc, label);
                    if(aut.F.contains(succ)) {
                        result.F.add(rSucc);
                    }
                }
            }
        }
        
        return result;
    }
    
    
    public static Pair<Boolean, Pair<FiniteAutomaton, FiniteAutomaton>>
          lightPrepocess(FiniteAutomaton system, FiniteAutomaton spec) {
        Options.debug = false;
        Options.fast=true;
        Options.backward=true;
        Options.rd=true;
        Options.fplus=true;
        Options.SFS=true;
        Options.qr=true;
        Options.C1=true;
        Options.EB=false; // difference to fast. EB must be false to report counterexamples
        Options.CPT=true;
        Options.superpruning=true;
        Options.delayed=true;
        Options.blamin=true;
        Options.blasub=true;
        Options.transient_pruning=true;
        Options.jumpsim_quotienting=true;
        Options.verbose=false; // set verbose to true to report counterexample
        Minimization minimizer = new Minimization();
        Simulation simulation = new Simulation();
        Set<datastructure.Pair<FAState, FAState>> frel, drel;
        frel = simulation.ForwardSimRelNBW(system, spec);
        if(frel.contains(new datastructure.Pair<FAState, FAState>(system.getInitialState(), spec.getInitialState())))
            return new Pair<Boolean, Pair<FiniteAutomaton, FiniteAutomaton>>(true, null);
        system = minimizer.quotient(system, frel);
        spec = minimizer.quotient(spec, frel);
        drel = simulation.DelayedSimRelNBW(system, spec);
        if(drel.contains(new datastructure.Pair<FAState, FAState>(system.getInitialState(), spec.getInitialState())))
            return new Pair<Boolean, Pair<FiniteAutomaton, FiniteAutomaton>>(true, null);
        system = minimizer.quotient(system, drel);
        spec = minimizer.quotient(spec, drel);
        return new Pair<Boolean, Pair<FiniteAutomaton, FiniteAutomaton>>(false, new Pair<>(system, spec));
    }

    public static Pair<Boolean, Pair<FiniteAutomaton, FiniteAutomaton>> prepocess(FiniteAutomaton system,
            FiniteAutomaton spec) {
        Options.debug = false;
        Options.fast=true;
        Options.backward=true;
        Options.rd=true;
        Options.fplus=true;
        Options.SFS=true;
        Options.qr=true;
        Options.C1=true;
        Options.EB=false; // difference to fast. EB must be false to report counterexamples
        Options.CPT=true;
        Options.superpruning=true;
        Options.delayed=true;
        Options.blamin=true;
        Options.blasub=true;
        Options.transient_pruning=true;
        Options.jumpsim_quotienting=true;
        Options.verbose=false; // set verbose to true to report counterexample
        Minimization minimizer = new Minimization();
        AutomatonPreprocessingResult x = minimizer.Preprocess_Buchi(system, spec);
        system = x.system;
        spec = x.spec;
        if (x.result) {
            return new Pair<Boolean, Pair<FiniteAutomaton, FiniteAutomaton>>(true, null);
        }
        return new Pair<Boolean, Pair<FiniteAutomaton, FiniteAutomaton>>(false, new Pair<>(system, spec));
    }
    
}
