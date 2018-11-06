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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class StateNFA {
    
    private final int id;
    private final NFA nfa;
    private final TIntObjectMap<ISet> successors; // Alphabet -> 2^Q
    
    public StateNFA(final NFA fa, final int id) {
        assert fa != null;
        this.nfa = fa;
        this.id = id;
        this.successors = new TIntObjectHashMap<>();
    }

    public NFA getFA() {
        return nfa;
    }
    
    public int getId() {
        return id;
    }
    
    public void addTransition(int letter, int state) {
        assert nfa.checkValidLetter(letter);
        ISet succs = successors.get(letter);
        if(succs == null) {
            succs = UtilISet.newISet();
        }
        if(nfa instanceof DFA) {
            // remove all previous states
            succs.clear(); 
        }
        succs.set(state);
        successors.put(letter, succs);
    }
    
    public ISet getSuccessors(int letter) {
        assert nfa.checkValidLetter(letter);
        ISet succs = successors.get(letter);
        if(succs == null) {
            return UtilISet.newISet();
        }
        return succs;
    }
    
    // get first successor
    public int getSuccessor(int letter) {
        ISet succs = getSuccessors(letter);
        if(succs.isEmpty()) {
            return -1;
        }else {
            return succs.iterator().next();
        }
    }
    
    public ISet getEnabledLetters() {
        ISet letters = UtilISet.newISet();
        TIntProcedure procedure = new TIntProcedure() {
            @Override
            public boolean execute(int letter) {
                letters.set(letter);
                return true;
            }
        };
        successors.forEachKey(procedure);
        return letters;
    }
    
    public void forEachEnabledLetter(TIntProcedure procedure) {
        successors.forEachKey(procedure);
    }
    
    @Override
    public String toString() {
        List<String> apList = new ArrayList<>();
        for(int i = 0; i < nfa.getAlphabetSize(); i ++) {
            apList.add("" + i);
        }
        return toString(apList);
    }
    
    
    public String toString(List<String> apList) {
        StringBuilder builder = new StringBuilder();
        builder.append("  " + getId() + " [label=\"" + getId() + "\"");
        if(nfa.isFinal(getId())) builder.append(", shape = doublecircle");
        else builder.append(", shape = circle");
        builder.append("];\n");
        // transitions
        TIntObjectProcedure<ISet> procedure = new TIntObjectProcedure<ISet> () {
            @Override
            public boolean execute(int letter, ISet succs) {
                for(int succ : succs) {
                    builder.append("  " + getId() + " -> " + succ
                            + " [label=\"" + apList.get(letter) + "\"];\n");
                }
                return true;
            }
        };
        successors.forEachEntry(procedure);
        return builder.toString();
    }
    
    public String toBA() {
        StringBuilder builder = new StringBuilder();
        // transitions
        TIntObjectProcedure<ISet> procedure = new TIntObjectProcedure<ISet> () {
            @Override
            public boolean execute(int letter, ISet succs) {
                for(int succ : succs) {
                    builder.append("a" + letter + ",[" + getId() + "]->[" + succ + "]\n");
                }
                return true;
            }
        };
        successors.forEachEntry(procedure);
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(obj instanceof StateNFA) {
            StateNFA other = (StateNFA)obj;
            return getId() == other.getId()
                && nfa == other.nfa;
        }
        return false;
    }

}
