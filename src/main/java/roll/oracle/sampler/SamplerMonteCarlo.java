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
 * Radu Grosu and Scott A.Smolka. 
 *  "Monte Carlo Model checking"
 *  
 *  This method cannot sample the omega words whose finite prefixes visit a loop
 *  but this is actually not needed to check emptiness of BA, but this maybe a problem
 *  for BA inclusion check
 *
 * */

public class SamplerMonteCarlo extends SamplerAbstract {
    

    public SamplerMonteCarlo(double epsilon, double delta) {
        super(epsilon, delta);
    }

    /**
     * Make sure that every state has at least one successor
     */
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

}
