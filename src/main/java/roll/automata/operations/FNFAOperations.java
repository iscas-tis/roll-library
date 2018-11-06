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
import roll.automata.FNFA;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * FDFA Operations
 * 
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class FNFAOperations {
        
    private FNFAOperations() {
    }
    
    public static  Automaton buildDOne(FNFA fnfa) {
        return buildNFAFromFNFA(fnfa, false);
    }
    
    public static  Automaton buildDTwo(FNFA fnfa) {
        return buildNFAFromFNFA(fnfa, true);
    }
    
    private static Automaton buildNFAFromFNFA(FNFA fnfa, boolean complement) {
        TIntObjectMap<State> map = new TIntObjectHashMap<>(); 
        Automaton dkAutL = NFAOperations.toDkFA(map, fnfa.getLeadingFA(), false);
        for(int stateNr = 0; stateNr < fnfa.getLeadingFA().getStateSize(); stateNr ++) {
            // M^a_a
            Automaton dkAutLOther = NFAOperations.toDkNFA(fnfa.getLeadingFA(), stateNr, stateNr);
            // A^a
            Automaton dkAutP = DFAOperations.toDkDFA(fnfa.getProgressFA(stateNr));
            if(complement) {
                // whether we need the complement of A^a
                dkAutP = dkAutP.complement();
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
    public static Automaton buildDollarNFA(FDFA fnfa) {
        // L means Leading and P means Progress
        TIntObjectMap<State> map = new TIntObjectHashMap<>(); 
        Automaton dkAutL = DFAOperations.toDkDFA(map, fnfa.getLeadingFA());
        for(int stateNr = 0; stateNr < fnfa.getLeadingFA().getStateSize(); stateNr ++) {
            DFA autP = fnfa.getProgressFA(stateNr);
            ISet finalStates = autP.getFinalStates();
            List<Automaton> autAccs = new ArrayList<>();
            int stateInitP = autP.getInitialState();
            for(final int finalStateNr : finalStates) {
                // A^a_f
                Automaton dkAutP = DFAOperations.toDkDFA(autP, stateInitP, finalStateNr);
                dkAutP.minimize();
                // M^a_a
                Automaton dkAutLOther = DFAOperations.toDkDFA(fnfa.getLeadingFA(), stateNr, stateNr);
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
    
    public static Automaton buildUnderNBA(FNFA fnfa) {
        return buildNBA(fnfa, true, false);
    }
    
    public static Automaton buildOverNBA(FNFA fnfa) {
        return buildNBA(fnfa, false, false);
    }
    
    private static Automaton buildNBA(FNFA fnfa, boolean under, boolean dba) {
        // L means Leading and P means Progress
        TIntObjectMap<State> map = new TIntObjectHashMap<>();
        Automaton dkAutL = NFAOperations.toDkFA(map, fnfa.getLeadingFA(), false);
        for (int stateNr = 0; stateNr < fnfa.getLeadingFA().getStateSize(); stateNr++) {
            DFA autP = fnfa.getProgressFA(stateNr);
            ISet finalStates = autP.getFinalStates();
            List<Automaton> finalAuts = new ArrayList<>();
            int initP = autP.getInitialState();
            for (final int finalStateNr : finalStates) {
                // A^a_f
                Automaton dkAutP = DFAOperations.toDkDFA(autP, initP, finalStateNr);
                dkAutP.minimize();
                // M^a_a
                Automaton dkAutLOther = NFAOperations.toDkNFA(fnfa.getLeadingFA(), stateNr, stateNr);
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
    
    public static Automaton buildUnderLDBA(FNFA fnfa) {
        Automaton result = buildNBA(fnfa, true, true);
        result.removeDeadTransitions();
        return result;
    }
    
    public static Automaton buildOverLDBA(FNFA fnfa) {
        Automaton result = buildNBA(fnfa, false, true);
        result.removeDeadTransitions();
        return result;
    }
    
    
    public static Pair<Word, Word> isEmpty(FNFA fnfa) {
        Automaton d1 = buildDOne(fnfa);
        String ce = d1.getShortestExample(true);
        if(ce == null) {
            return null;
        }
        return fnfa.getAlphabet().getWordPairFromString(ce);
    }
    
    public static Pair<Word, Word> normalize(FNFA fnfa, Word prefix, Word suffix) {
        Automaton dDollar = FDFAOperations.buildDDollar(prefix, suffix);
        Automaton dOne =  buildDOne(fnfa);
        dOne = dOne.intersection(dDollar);
        String wordStr = dOne.getShortestExample(true);
        if(wordStr == null) return null;
        return fnfa.getAlphabet().getWordPairFromString(wordStr);
    }
    
    public static Automaton buildNegNBA(FDFA fnfa) {
        DFA autL = fnfa.getLeadingFA();
        TIntObjectMap<State> map = new TIntObjectHashMap<>();
        Automaton dkAutL = DFAOperations.toDkDFA(map, autL);
        for (int stateNr = 0; stateNr < autL.getStateSize(); stateNr++) {
            DFA autP = fnfa.getProgressFA(stateNr);
            List<Automaton> accAuts = new ArrayList<>();
            int stateInitP = autP.getInitialState();
            for (int accNr = 0; accNr < autP.getStateSize(); accNr ++) {
                // reverse all the states here
                if(autP.isFinal(accNr)) continue;
                Automaton dkAutP = DFAOperations.toDkDFA(autP, stateInitP, accNr);
                dkAutP.minimize();
                Automaton dkAutLOther = DFAOperations.toDkDFA(autL, stateNr, stateNr);
                dkAutLOther.minimize();
                
                Automaton product = dkAutP.intersection(dkAutLOther);                
                product.minimize();
                
                Automaton dkAutNq = DFAOperations.toDkDFA(autP, accNr, accNr);
                dkAutNq.minimize();

                product = product.intersection(dkAutNq);
                product.minimize();

                if (! product.getAcceptStates().isEmpty()) {
                    assert product.getAcceptStates().size() == 1;
                    if(product.getAcceptStates().size() > 1) {
                        throw new UnsupportedOperationException("More than one accepting state...");
                    }
                    product = DFAOperations.addEpsilon(product);
                    accAuts.add(product);
                }
            }

            State u = map.get(stateNr); // make epsilon or Dollar transitions
            for (Automaton dkAut : accAuts) {
                State init = dkAut.getInitialState();
                for (Transition t : init.getTransitions())
                    u.addTransition(new Transition(t.getMin(), t.getMax(), t.getDest()));
            }
        }
//      dkAutL.removeDeadTransitions();
        return dkAutL;
    }

}
