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

package roll.automata;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * NFA is allowed to be incomplete
 * */

public class StateNFA implements State {
    private final NFA nfa;
    private final TIntObjectMap<ISet> successors; // Alphabet -> 2^Q
    private final int id;
    
    public StateNFA(final NFA nfa, final int id) {
        assert nfa != null;
        this.nfa = nfa;
        this.id = id;
        this.successors = new TIntObjectHashMap<>();
    }

    @Override
    public NFA getFA() {
        return nfa;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void addTransition(int letter, int state) {
        assert nfa.checkValidLetter(letter);
        ISet succs = successors.get(letter);
        if(succs == null) {
            succs = UtilISet.newISet();
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
    
}
