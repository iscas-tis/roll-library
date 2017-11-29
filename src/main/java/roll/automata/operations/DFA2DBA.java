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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.util.Pair;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

class DFA2DBA {
    
    private Automaton dfa;
    
    public DFA2DBA(Automaton dfa) {
        this.dfa = dfa;
    }

    // build buechi for a given DFA, which has only one accepting state
    // and L(aut) = N without N+ = N, this is for over approximation method
    // there four kinds of states, namely q, [q], (q, q') and <q>
    public Automaton buildDBA() {
        // remove all unreachable states and transitions
        dfa.removeDeadTransitions();
        Set<State> accs = dfa.getAcceptStates();
        if(accs.size() != 1) {
            throw new UnsupportedOperationException("Not one accepting state");
        }
        TObjectIntMap<State> map = new TObjectIntHashMap<>();
        int num = 0;
        // numbering every state
        for(State s : dfa.getStates()) {
            map.put(s, num);
            num ++;
        }
        // state q is -2 * n + q, [q] is -1 * n + q 
        // (p, q) is p * n + q, while <q> is (n+1)* n + q
        
        State qf = accs.iterator().next(); // qf
        State init = dfa.getInitialState();
        LinkedList<PairState> worklist = new LinkedList<PairState>();
        Automaton result = new Automaton();
        HashMap<PairState, State> stateMap = new HashMap<>();
        PairState q0 = new PairState(init, map); // q0
        worklist.add(q0); // q0
        
        State initc = new State();
        stateMap.put(q0, initc);
        result.setInitialState(initc);
        
        // collect all letters from q0 and qf
        Set<Character> letterInQ0Qf = new HashSet<>();
        for(Transition tr : qf.getTransitions()) {
            for(char letter = tr.getMin(); letter <= tr.getMax(); letter ++) {
                letterInQ0Qf.add(letter);
            }
        }
        for(Transition tr : init.getTransitions()) {
            for(char letter = tr.getMin(); letter <= tr.getMax(); letter ++) {
                letterInQ0Qf.add(letter);
            }
        }
    
        // collect final states
        Set<State> finals = new HashSet<>();
        
        while(worklist.size() > 0) {
            PairState w = worklist.removeFirst();
            // q
            if(w.getMiddle() != null) {
                State q = w.getMiddle();   // get the state q
                State qc = stateMap.get(w);    // get the corresponding state in C
                Set<Transition> trs = q.getTransitions();
                if(q != qf) { // if q is not accepting state
                    for(Transition tr : trs) {
                        State succ = tr.getDest();
                        PairState sp = new PairState(succ, map); // new q successors
                        State sc = stateMap.get(sp);
                        if(sc == null) {
                            sc = new State();
                            worklist.add(sp);
                            stateMap.put(sp, sc);
                        }
                        qc.addTransition(new Transition(tr.getMin(), tr.getMax(), sc));
                    }
                }else {
                    // we know that q is an accepting state
                    for(Character letter : letterInQ0Qf) {
                        State sua = init.step(letter); // successor of initial state
                        State suf = qf.step(letter);  // successor of final state
                        
                        if(sua == null && suf == null) {
                            System.err.println("sua == null && suf == null");
                            System.exit(-1);
                        }
                        PairState sp =  new PairState(sua, suf, map);
                        State sc = stateMap.get(sp);
                        if(sc == null) {
                            sc = new State();
                            worklist.add(sp);
                            stateMap.put(sp, sc);
                        }
                        qc.addTransition(new Transition(letter, sc));
                    }
                }
            }else       
            //[q], mimic behaviors from q0
            if(w.getRight() == null) {
                State q = w.getLeft();
                State qc = stateMap.get(w);
                Set<Transition> trs = q.getTransitions();
                if(q != qf) {
                    for(Transition tr : trs) {
                        State succ = tr.getDest();
                        PairState sp = new PairState(succ, null, map);
                        State sc = stateMap.get(sp);
                        if(sc == null) {
                            sc = new State();
                            worklist.add(sp);
                            stateMap.put(sp, sc);
                        }
                        qc.addTransition(new Transition(tr.getMin(), tr.getMax(), sc));
                    }
                }else {
                    // we know that q is accepting state
                    finals.add(qc); // [qf]
                    for(Character letter : letterInQ0Qf) {
                        State sua = init.step(letter); // successor of initial state
                        State suf = qf.step(letter);  // successor of final state
                        
                        if(sua == null && suf == null) {
                            System.err.println("sua == null && suf == null");
                            System.exit(-1);
                        }
                        PairState sp =  new PairState(sua, suf, map);
                        State sc = stateMap.get(sp);
                        if(sc == null) {
                            sc = new State();
                            worklist.add(sp);
                            stateMap.put(sp, sc);
                        }
                        qc.addTransition(new Transition(letter, sc));
                    }
                }
            }else
            // <q>, mimic behaviors of qf
            if(w.getLeft() == null) {
                State q = w.getRight();
                State qc = stateMap.get(w);
                Set<Transition> trs = q.getTransitions();
                if(q != qf) {
                    for(Transition tr : trs) {
                        State succ = tr.getDest();
                        PairState sp = new PairState(null, succ, map);
                        State sc = stateMap.get(sp);
                        if(sc == null) {
                            sc = new State();
                            worklist.add(sp);
                            stateMap.put(sp, sc);
                        }
                        qc.addTransition(new Transition(tr.getMin(), tr.getMax(), sc));
                    }
                }else {
                    // we know that q is accepting state
                    for(Character letter : letterInQ0Qf) {
                        State sua = init.step(letter); // successor of initial state
                        State suf = qf.step(letter);  // successor of final state
                        
                        if(sua == null && suf == null) {
                            System.err.println("sua == null && suf == null");
                            System.exit(-1);
                        }
                        PairState sp =  new PairState(sua, suf, map);
                        State sc = stateMap.get(sp);
                        if(sc == null) {
                            sc = new State();
                            worklist.add(sp);
                            stateMap.put(sp, sc);
                        }
                        qc.addTransition(new Transition(letter, sc));
                    }
                }
            }else 
            // (p, q), mimic behaviors of (q0, qf)
            if(w.getLeft() != null && w.getRight() != null) {
                
                State p = w.getLeft();
                State q = w.getRight();
                
                State qc = stateMap.get(w);
                
                Set<Character> commonLetters = new HashSet<>();
                Set<Transition> trSets = null;
                if(p != qf) {
                    trSets = p.getTransitions();
                }else {
                    trSets = init.getTransitions();
                }
                for(Transition tr : trSets) {
                    for(char letter = tr.getMin(); letter <= tr.getMax(); letter ++) {
                        commonLetters.add(letter);
                    }
                }
                
                for(Transition tr : q.getTransitions()) {
                    for(char letter = tr.getMin(); letter <= tr.getMax(); letter ++) {
                        commonLetters.add(letter);
                    }
                }
                
                for(Character letter : commonLetters) {

                    if(p != qf) { // common letters for p and q
                        State sup = p.step(letter);
                        State suq = q.step(letter);
                        if(sup == null && suq == null) {
                            System.err.println("sua == null && suf == null");
                            System.exit(-1);
                        }
                        PairState sp =  new PairState(sup, suq, map);
                        State sc = stateMap.get(sp);
                        if(sc == null) {
                            sc = new State();
                            worklist.add(sp);
                            stateMap.put(sp, sc);
                        }
                        qc.addTransition(new Transition(letter, sc));
                    }else { // p is accepting state
                        // // common letters for init and q
                        State sui = init.step(letter);
                        State suq = q.step(letter);
                        finals.add(qc); // [qf]
                        if(sui == null && suq == null) {
                            System.err.println("sua == null && suf == null");
                            System.exit(-1);
                        }
                        PairState sp =  new PairState(sui, suq, map);
                        State sc = stateMap.get(sp);
                        if(sc == null) {
                            sc = new State();
                            worklist.add(sp);
                            stateMap.put(sp, sc);
                        }
                        qc.addTransition(new Transition(letter, sc));
                    }
                }
    
            }
        }
        
        for(State f : finals) {
            f.setAccept(true);
        }
        return result;
    }
    
    // Product state in product automaton
    private class PairState extends Pair<State, State> {

        private final TObjectIntMap<State> map;
        
        public PairState(State left, State right, TObjectIntMap<State> map) {
            super(left, right);
            this.middle = null;
            this.map = map;
        }
        
        private State middle; // -2*n + s.id
        
        public PairState(State s, TObjectIntMap<State> map) {
            super(null, null);
            this.middle = s;
            this.map = map;
        }
        
        public State getMiddle() {
            return this.middle;
        }
        
        public boolean equals(Object obj) {
            if(obj instanceof PairState) {
                PairState pair = (PairState)obj;
                if(getMiddle() != pair.getMiddle()) return false;
                if(getLeft() != pair.getLeft()) return false;
                if(getRight() != pair.getRight()) return false;
                return true;
            }
            return false;
        }
        
        public String toString() {

            if(getMiddle() != null) {
                return "" + map.get(getMiddle());
            }
            if(getLeft() != null && getRight() == null) {
                return "[" + map.get(getLeft()) + "]";
            } // left is successor for q0
            if(getLeft() == null && getRight() != null) {
                return "<" + map.get(getRight()) + ">";
            } // right is successor for qf
            if(getLeft() != null && getRight() != null) {
                return "(" + map.get(getLeft()) + ", " + map.get(getRight()) + ")";
            }
            return "";
        }
        
        public int hashCode() {
            
            int num = map.size();
            
            // state q is -2 * n + q, [q] is -1 * n + q 
            // , (p, q) is p * n + q, while <q> is (n+1)* n + q

            //q
            if(getMiddle() != null) {
                return map.get(getMiddle()) - 2*num;
            }
            // [q]
            if(getLeft() != null && getRight() == null) {
                return map.get(getLeft()) - num;
            }
            //<q>
            if(getLeft() == null && getRight() != null) {
                return num * (num + 1) + map.get(getRight());
            }
            //(q, q')
            if(getLeft() != null && getRight() != null) {
                return num * map.get(getLeft()) +  map.get(getRight());
            }
            
            return 0;

        }
        
    }
    
    private static Automaton aut1() {
        Automaton aut = new Automaton();
        State a = new State();
        State b = new State();
        a.addTransition(new Transition('a', b));
        b.addTransition(new Transition('b', b));
        b.setAccept(true);
        aut.setInitialState(a);
        aut.setDeterministic(true);
        return aut;
    }
    
    private static Automaton aut2() {
        Automaton aut = new Automaton();
        State a = new State();
        State b = new State();
        a.addTransition(new Transition('a', a));
        a.addTransition(new Transition('b', b));
        b.addTransition(new Transition('b', b));
        b.addTransition(new Transition('c', b));
        b.setAccept(true);
        aut.setInitialState(a);
        aut.setDeterministic(true);
        return aut;
    }
    
    
    public static void main(String[] args) {
        Automaton aut1 = aut2();
        DFA2DBA builder = new DFA2DBA(aut1);
        Automaton ba1 = builder.buildDBA();
        System.out.println(aut1.toDot());
        System.out.println("\nba:\n" + ba1.toDot());
    }


}
