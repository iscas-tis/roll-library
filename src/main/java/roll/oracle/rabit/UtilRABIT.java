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

package roll.oracle.rabit;

import automata.FAState;
import automata.FiniteAutomaton;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import mainfiles.RABIT;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class UtilRABIT {
    
    private UtilRABIT() {
        
    }
    
    private static FAState getOrAddState(FiniteAutomaton fa, TIntObjectMap<FAState> map, int s) {
        if(map.containsKey(s)) {
            return map.get(s);
        }
        FAState st = fa.createState();
        map.put(s, st);
        return st;
    }
    
    public static FiniteAutomaton toRABITNBA(NBA nba) {
        FiniteAutomaton rabitAut = new FiniteAutomaton();
        TIntObjectMap<FAState> map = new TIntObjectHashMap<>();  
        Alphabet alphabet = nba.getAlphabet();
        for(int s = 0; s < nba.getStateSize(); s ++) {
            FAState faState = getOrAddState(rabitAut, map, s);
            StateNFA state = nba.getState(s);
            TIntProcedure procedure = new TIntProcedure() {
                @Override
                public boolean execute(int letter) {
                    for(int succ : state.getSuccessors(letter)) {
                        FAState faSucc = getOrAddState(rabitAut, map, succ);
                        rabitAut.addTransition(faState, faSucc, alphabet.getLetter(letter) + "");
                    }
                    return true;
                }
            };
            state.forEachEnabledLetter(procedure);
            if(nba.isFinal(s)) {
                rabitAut.F.add(faState);
            }
        }
        FAState init = map.get(nba.getInitialState());
        rabitAut.setInitialState(init);
        return rabitAut;
    }
    
    public static Pair<Word, Word> isIncluded(Alphabet alphabet
            , FiniteAutomaton A, FiniteAutomaton B) {
        boolean inclusion = RABIT.isIncluded(A, B);
        if(inclusion) return null;
        String prefixStr = RABIT.getPrefix();
        String suffixStr = RABIT.getSuffix();
        Word prefix = alphabet.getWordFromString(prefixStr);
        Word suffix = null;
        if(!suffixStr.equals("")) {
            suffix = alphabet.getWordFromString(suffixStr);
        }else {
            suffix = alphabet.getEmptyWord();
        }
        return new Pair<>(prefix, suffix);
    }

}
