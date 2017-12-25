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

package roll.oracle.sampler;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.StateNFA;
import roll.util.Pair;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 *
 * */

public class SamplerIndexedMonteCarlo extends SamplerAbstract {
    
    public SamplerIndexedMonteCarlo(double epsilon, double delta) {
        super(epsilon, delta);
    }

    /**
     * Make sure that every state has at least one successor
     */
    @Override
    public Pair<Pair<Word, Word>, Boolean> getRandomLasso() {
        if(nba == null) {
            throw new UnsupportedOperationException("Set NBA first before sampling");
        }
        // start sampling
        int s = nba.getInitialState();
        int i = 0, f = -1;
        TIntIntMap hTable = new TIntIntHashMap();
        List<Integer> wList = new ArrayList<>();
        while (!hTable.containsKey(s)) {
            hTable.put(s, i);
            if (nba.isFinal(s)) {
                f = i;
            }
            Pair<Integer, StateNFA> pair = rNext(nba, s);
            wList.add(pair.getLeft());
            s = pair.getRight().getId();
            ++i;
        }

        int start = hTable.get(s); // the state repeat
        int[] preArr = new int[start];
        for (int j = 0; j < start; j++) {
            preArr[j]  = wList.get(j);
        }
        int[] sufArr = new int[wList.size() - start]; 
        for (int j = start; j < wList.size(); j++) {
            sufArr[j - start] = wList.get(j);
        }
        boolean accept;
        if (hTable.get(s) <= f) {
            accept = true;
        } else {
            accept = false;
        }
        Word prefix = nba.getAlphabet().getArrayWord(preArr);
        Word suffix = nba.getAlphabet().getArrayWord(sufArr);
        return new Pair<>(new Pair<>(prefix, suffix), accept);
    }
    
    private class Elem {
        int state;
        int count;
        
        Elem(int state, int count) {
            this.state = state;
            this.count = count;
        }
        
        boolean isLastTime() {
            return count == 4;
        }
        
        @Override
        public int hashCode() {
            return count * nba.getStateSize() + state;
        }
        
        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null) return false;
            if(obj instanceof Elem) {
                Elem other = (Elem)obj;
                return other.state == state
                    && other.count == count;
            }
            return false;
        }
        @Override
        public String toString() {
            return state + ":" + count;
        }
    }

}
