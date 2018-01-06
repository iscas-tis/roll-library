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

import automata.FAState;
import automata.FiniteAutomaton;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
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
}
