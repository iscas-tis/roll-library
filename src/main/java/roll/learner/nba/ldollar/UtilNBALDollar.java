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

package roll.learner.nba.ldollar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class UtilNBALDollar {
    
    private UtilNBALDollar() {
        
    }
    /**
     * Get a DFA accepts E*$E+
     * **/
    public static Automaton getAllUPWords(Alphabet alphabet, int dollarLetter) {
        Automaton all = new Automaton();
        State fst = new State();
        for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
            if(letter == dollarLetter) continue;
            fst.addTransition(new Transition(alphabet.getLetter(letter), fst));
        }
        State snd = new State();
        fst.addTransition(new Transition(Alphabet.DOLLAR, snd));
        State thd = new State();
        thd.setAccept(true);
        for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
            if(letter == dollarLetter) continue;
            Transition tr = new Transition(alphabet.getLetter(letter), thd);
            snd.addTransition(tr);
            thd.addTransition(tr);
        }
        all.setInitialState(fst);
        all.setDeterministic(true);
        return all;
    }
    
    
    public static LinkedList<State> getLeadingStates(Automaton aut, Map<State, State> spairs) {
        LinkedList<State> worklist = new LinkedList<State>();
        LinkedList<State> leadingStates = new LinkedList<State>();
        Set<State> visited = new HashSet<>();
        worklist.add(aut.getInitialState());
        visited.add(aut.getInitialState());
        while (worklist.size() > 0) {
            State s = worklist.removeFirst();
            for (Transition trans : s.getTransitions()) {
                State t = trans.getDest();
                if(trans.getMin() <= Alphabet.DOLLAR 
                 && trans.getMax() >= Alphabet.DOLLAR  ) {
                    spairs.put(s, t);
                    leadingStates.add(s);
                }
                // add states into list
                if (!visited.contains(t)) {
                    visited.add(t);
                    worklist.add(t);
                }
            }
        }
        return leadingStates;
    }
    
    
    private static State getOrAddState(Map<State, State> map, State key) {
        State val = map.get(key);
        if(val == null) {
            val = new State();
            map.put(key, val);
        }
        return val;
    }
    
    public static Automaton copyDkFA(Automaton aut, State init, Set<State> fins) {
        Automaton result = new Automaton();
        Map<State, State> map = new HashMap<>();
        LinkedList<State> worklist = new LinkedList<State>();
        Set<State> visited = new HashSet<>();
        worklist.add(init);
        visited.add(init);
        State rState = getOrAddState(map, init); 
        result.setInitialState(rState);
        while (worklist.size() > 0) {
            State curr = worklist.removeFirst();
            State rCurr = getOrAddState(map, curr);
            if(fins.contains(curr)) {
                rCurr.setAccept(true);
            }
            for (Transition trans : curr.getTransitions()) {
                State succ = trans.getDest();
                State rSucc = getOrAddState(map, succ);
                // copy the transitions
                rCurr.addTransition(new Transition(trans.getMin(), trans.getMax(), rSucc));
                // add states into list
                if (!visited.contains(succ)) {
                    visited.add(succ);
                    worklist.add(succ);
                }
            }
        }
        return result;
    }
    
    //add specific(not general) epsilon transition in an NFA.
    public static Automaton addEpsilon (Automaton A) {
        State epsilon = new State();
        epsilon.setAccept(true);
        Set<State> acc = A.getAcceptStates();
        //Only one accepted state.
        if(acc.size() > 1)  {
            throw new UnsupportedOperationException("multiple final states while add epsilon transitions");
        }
        
        State accept = acc.iterator().next();
        accept.setAccept(false);
        //Records transitions to be added to epsilon state.
        Set<Transition> transToAcc = new HashSet<Transition>();
        
        for (State s: A.getStates()) {
            for (Transition t: s.getTransitions()){
                if (t.getDest() == accept)
                    transToAcc.add(new Transition(t.getMin(), t.getMax(), s));
            }
        }
        
        // first add transitions from epsilon state
        State ini = A.getInitialState();
        for (Transition t : ini.getTransitions())
            epsilon.addTransition(new Transition(t.getMin(), t.getMax(), t.getDest()));
        
        // add transition to epsilon
        for(Transition t: transToAcc)
            t.getDest().addTransition(new Transition(t.getMin(), t.getMax(), epsilon));
        
        return A;
    }

    public static Automaton dkDFAToBuchi(Automaton dollarAut) {
        Map<State, State> spairs = new HashMap<>();
        LinkedList<State> leadingStates = getLeadingStates(dollarAut, spairs);
        // now we construct Buchi from fdfa
        while (! leadingStates.isEmpty()){
            State q = leadingStates.removeFirst();
            // first remove the dollar transitions from q
            List<Transition> trs = new ArrayList<>();
            for(Transition tr : q.getTransitions()) {
                for(char c = tr.getMin(); c <= tr.getMax(); c ++) {
                    if(c != Alphabet.DOLLAR) {
                        trs.add(new Transition(c, tr.getDest()));
                    }
                }
            }
            q.getTransitions().clear();
            q.getTransitions().addAll(trs);
            //progress part in DFA
            Automaton Aq = new Automaton();
            Aq.setInitialState(spairs.get(q));
            for(State f : Aq.getAcceptStates()) {
                Automaton Mqq = copyDkFA(dollarAut, q, Collections.singleton(q));
//                Mqq.minimize();
                Mqq.removeDeadTransitions();
                Automaton product = Mqq;
                Automaton Aqf = copyDkFA(Aq, Aq.getInitialState(), Collections.singleton(f));
//                Aqf.minimize();
                Aqf.removeDeadTransitions();
                product = product.intersection(Aqf);
//                Mqq.minimize();
                Automaton Aff = copyDkFA(Aq, f, Collections.singleton(f));
//                Aff.minimize();
                Aff.removeDeadTransitions();
                product = product.intersection(Aff);
                product.minimize();
                
                if (! product.getAcceptStates().isEmpty()) {
                    assert product.getAcceptStates().size() == 1;
                    if(product.getAcceptStates().size() > 1) {
                        throw new UnsupportedOperationException("More than one accepting state...");
                    }
                    product = addEpsilon(product);
                    State init = product.getInitialState();
                    // add epsilon transitions to accepting parts
                    for (Transition t : init.getTransitions())
                        q.addTransition(new Transition(t.getMin(), t.getMax(), t.getDest()));
                }
            }
        }
        
        return dollarAut;
    }

}
