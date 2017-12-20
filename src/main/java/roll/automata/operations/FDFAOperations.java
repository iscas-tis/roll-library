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

import java.util.ArrayList;
import java.util.List;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.DFA;
import roll.automata.FDFA;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * FDFA Operations
 * 
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class FDFAOperations {
        
    private FDFAOperations() {
    }
    
    public static Word getSmallestPeriod(Word period) {
        // from the possible smallest length
        for(int i = 1; i <= period.length() / 2; i ++) {
            // can be divided
            if (period.length() % i == 0) {
                // repeat word candidate
                Word rep = period.getPrefix(i);
                // compute the number of repeat
                int num = period.length() / i;
                boolean repeated = true;
                for(int j = 0; j < num; j ++) {
                    int pNr = j * i;
                    Word p = period.getSubWord(pNr, i);
                    if(! p.equals(rep)) {
                        repeated = false;
                        break;
                    }
                }
                // reduce the period
                if(repeated) { 
                    period = rep;
                    break;
                }
            }
        }
        return period;
    }
    
    public static Automaton buildRepeatAutomaton(Word suffix) {
        assert suffix.length() >= 1;
        Automaton result = new Automaton();
        State fst = new State();
        State snd = new State();
        result.setInitialState(fst);
        result.setDeterministic(true);
        char letter = suffix.getAlphabet().getLetter(suffix.getFirstLetter());
        fst.addTransition(new Transition(letter, snd));
        State pre = snd;
        for(int i = 1; i < suffix.length(); i ++) {
            State curr = new State();
            char ch = suffix.getAlphabet().getLetter(suffix.getLetter(i));
            pre.addTransition(new Transition(ch, curr));
            pre = curr;
        }
        pre.addTransition(new Transition(letter, snd));
        pre.setAccept(true);
        return result;
    }
    
    // Given word prefix and suffix, we do the shifting operation
    // as well as return the corresponding dk.brics.automaton.
    public static Automaton buildDDollar(Word prefix, Word suffix) {
        // Finds the smallest period of suffix.
        suffix = getSmallestPeriod(suffix);
        // Shifts prefix to the smallest one.
        while (prefix.getLastLetter() == suffix.getLastLetter()) {
            prefix = prefix.getPrefix(prefix.length() - 1);
            suffix = suffix.getSuffix(suffix.length() - 1).concat(suffix.getPrefix(suffix.length() - 1));
        }
        System.out.println("Building Dollar automaton for counterexamples ("
                + prefix.length() + "," + suffix.length() +")...");
        // System.out.println(prefix.toStringWithAlphabet() + ',' +
        // suffix.toStringWithAlphabet());
        Automaton result = new Automaton();
        State fst = null, pre = null;
        // first computer prefix automaton
        for(int i = 0; i <= prefix.length(); i ++) {
            State curr = new State();
            if(i > 0) {
                char letter = prefix.getAlphabet().getLetter(prefix.getLetter(i-1));
                pre.addTransition(new Transition(letter, curr));
            }else {
                fst = curr;
            }
            pre = curr;
        }
        // set initial state
        assert fst != null && pre != null;
        result.setInitialState(fst);
        State[] suffixStates = new State[suffix.length()];
        suffixStates[0] = pre; // last state in the prefix
        for(int j = 0; j < suffix.length() - 1; j ++) {
            suffixStates[j + 1] = new State();
            char letter = prefix.getAlphabet().getLetter(suffix.getLetter(j));
            suffixStates[j].addTransition(new Transition(letter, suffixStates[j + 1]));
        }
        char letter = suffix.getAlphabet().getLetter(suffix.getLastLetter());
        suffixStates[suffix.length() - 1].addTransition(new Transition(letter, pre));
        int dollar = 0; // from the first state in repeat 
        int length = suffix.length();
        for(int i = 0; i < length; i ++) {
            Automaton suf = buildRepeatAutomaton(suffix);
            State dollarState = suffixStates[dollar];
            State init = suf.getInitialState();
            dollarState.addTransition(new Transition(Alphabet.DOLLAR, init));
            suffix = suffix.getSubWord(1, suffix.length() - 1).append(suffix.getFirstLetter());
            dollar ++;
        }
        return result;
    }
    
    
    public static  Automaton buildDOne(FDFA fdfa) {
        return buildDFAFromFDFA(fdfa, false);
    }
    
    public static  Automaton buildDTwo(FDFA fdfa) {
        return buildDFAFromFDFA(fdfa, true);
    }
    
    private static Automaton buildDFAFromFDFA(FDFA fdfa, boolean complement) {
        TIntObjectMap<State> map = new TIntObjectHashMap<>(); 
        Automaton dkAutL = DFAOperations.toDkDFA(map, fdfa.getLeadingDFA());
        for(int stateNr = 0; stateNr < fdfa.getLeadingDFA().getStateSize(); stateNr ++) {
            // M^a_a
            Automaton dkAutLOther = DFAOperations.toDkDFA(fdfa.getLeadingDFA(), stateNr, stateNr);
            // A^a
            Automaton dkAutP = DFAOperations.toDkDFA(fdfa.getProgressDFA(stateNr));
            if(complement) {
                // whether we need the complement of A^a
                dkAutP.complement();
            }
            Automaton product = dkAutLOther.intersection(dkAutP);
            product.minimize();
            if(! product.getAcceptStates().isEmpty()) {
                State u = map.get(stateNr); // make dollar transitions
                u.addTransition(new Transition(Alphabet.DOLLAR, product.getInitialState()));
            }
        }
        dkAutL.setDeterministic(true);
        return dkAutL;
    }
    
    // build NBA from FDFA
    public static Automaton buildDollarNFA(FDFA fdfa) {
        // L means Leading and P means Progress
        TIntObjectMap<State> map = new TIntObjectHashMap<>(); 
        Automaton dkAutL = DFAOperations.toDkDFA(map, fdfa.getLeadingDFA());
        for(int stateNr = 0; stateNr < fdfa.getLeadingDFA().getStateSize(); stateNr ++) {
            DFA autP = fdfa.getProgressDFA(stateNr);
            ISet finalStates = autP.getFinalStates();
            List<Automaton> autAccs = new ArrayList<>();
            int stateInitP = autP.getInitialState();
            for(final int finalStateNr : finalStates) {
                // A^a_f
                Automaton dkAutP = DFAOperations.toDkDFA(autP, stateInitP, finalStateNr);
                dkAutP.minimize();
                // M^a_a
                Automaton dkAutLOther = DFAOperations.toDkDFA(fdfa.getLeadingDFA(), stateNr, stateNr);
                dkAutLOther.minimize();
                // A^a_a * M^a_a
                Automaton product = dkAutP.intersection(dkAutLOther);
                product.minimize();
                
                if(! product.getAcceptStates().isEmpty()) {
                    assert product.getAcceptStates().size() == 1;
                    autAccs.add(product);
                }
            }
            State u = map.get(stateNr);
            
            for(dk.brics.automaton.Automaton aut : autAccs) {
                u.addTransition(new Transition(Alphabet.DOLLAR, aut.getInitialState()));
            }
        }
        dkAutL.setDeterministic(false);
        //dkAutL.minimize(); only for DFA
        return dkAutL;
    }
    
    public static Automaton buildUnderNBA(FDFA fdfa) {
        return buildNBA(fdfa, true, false);
    }
    
    public static Automaton buildOverNBA(FDFA fdfa) {
        return buildNBA(fdfa, false, false);
    }
    
    private static Automaton buildNBA(FDFA fdfa, boolean under, boolean dba) {
        // L means Leading and P means Progress
        TIntObjectMap<State> map = new TIntObjectHashMap<>();
        Automaton dkAutL = DFAOperations.toDkDFA(map, fdfa.getLeadingDFA());
        for (int stateNr = 0; stateNr < fdfa.getLeadingDFA().getStateSize(); stateNr++) {
            DFA autP = fdfa.getProgressDFA(stateNr);
            ISet finalStates = autP.getFinalStates();
            List<Automaton> finalAuts = new ArrayList<>();
            int initP = autP.getInitialState();
            for (final int finalStateNr : finalStates) {
                // A^a_f
                Automaton dkAutP = DFAOperations.toDkDFA(autP, initP, finalStateNr);
                dkAutP.minimize();
                // M^a_a
                Automaton dkAutLOther = DFAOperations.toDkDFA(fdfa.getLeadingDFA(), stateNr, stateNr);
                dkAutLOther.minimize();
                // M^a_a * A^a_f                
                dk.brics.automaton.Automaton product = dkAutP.intersection(dkAutLOther);
                product.minimize();
                
                if (under) {
                    //A^f_f
                    Automaton dkAutNq = DFAOperations.toDkDFA(autP, finalStateNr, finalStateNr);
                    dkAutNq.minimize();
                    // M^a_a * A^a_f * A^f_f
                    product = product.intersection(dkAutNq);
                    product.minimize();
                }

                if (! product.getAcceptStates().isEmpty()) {
                    if(product.getAcceptStates().size() > 1) {
                        throw new UnsupportedOperationException("FDFAOperations.buildNBA(): More than one accepting state...");
                    }
                    assert product.getAcceptStates().size() == 1 : "More than one accepting state...";
                    if(dba) {
                        product = DFAOperations.toDBA(product);
                    }else {
                        product =  DFAOperations.addEpsilon(product);
                    }
                    finalAuts.add(product);
                }
            }

            State u = map.get(stateNr); // make epsilon transitions
            for (Automaton dkAut : finalAuts) {
                State init = dkAut.getInitialState();
                for (Transition t : init.getTransitions())
                    u.addTransition(new Transition(t.getMin(), t.getMax(), t.getDest()));
            }
        }

        return dkAutL;
    }
    
    public static Automaton buildUnderLDBA(FDFA fdfa) {
        Automaton result = buildNBA(fdfa, true, true);
        result.removeDeadTransitions();
        return result;
    }
    
    public static Automaton buildOverLDBA(FDFA fdfa) {
        Automaton result = buildNBA(fdfa, false, true);
        result.removeDeadTransitions();
        return result;
    }
    
    
    public static Pair<Word, Word> isEmpty(FDFA fdfa) {
        Automaton d1 = buildDOne(fdfa);
        String ce = d1.getShortestExample(true);
        if(ce == null) {
            return null;
        }
        return fdfa.getAlphabet().getWordPairFromString(ce);
    }
    
    public static Pair<Word, Word> normalize(FDFA fdfa, Word prefix, Word suffix) {
        Automaton dDollar = buildDDollar(prefix, suffix);
        Automaton dOne =  buildDOne(fdfa);
        dOne = dOne.intersection(dDollar);
        String wordStr = dOne.getShortestExample(true);
        if(wordStr == null) return null;
        return fdfa.getAlphabet().getWordPairFromString(wordStr);
    }

}
