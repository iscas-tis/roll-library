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

import java.util.Set;
import java.util.TreeSet;

import algorithms.Minimization;
import algorithms.Options;
import algorithms.Simulation;
import automata.AutomatonPreprocessingResult;
import automata.FAState;
import automata.FiniteAutomaton;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class UtilInclusion {

    private static FAState getOrAddState(FiniteAutomaton result,TIntObjectMap<FAState> map, int state) {
        FAState rState = map.get(state);
        if(rState == null) {
            rState = result.createState();
            map.put(state, rState);
        }
        return rState;
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
