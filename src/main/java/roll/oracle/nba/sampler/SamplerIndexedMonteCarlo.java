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

package roll.oracle.nba.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 *
 * */

public class SamplerIndexedMonteCarlo extends SamplerAbstract {
    // the number of visiting times, should be the number of
    // states in the minimal deterministic omega automaton recognizing
    // L(A) where A is the given Buchi automaton
    // the upper bound of K is the 2^n where n is the number of states
    // in the second automaton B for inclusion checking
    public int K = -1; 
    public SamplerIndexedMonteCarlo(double epsilon, double delta) {
        super(epsilon, delta);
    }

    // only for 1 and 2, only allowed three apearacnces for one state
    private boolean terminate(int index) {
        if(index >= K) return true;
        // the probability whether to stop right now or not
        int sNr = ThreadLocalRandom.current().nextInt(0, 2);
        return sNr == 1;
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
        if(K == -1) K = nba.getStateSize(); // set it as default
        int i = 0, f = -1;
        TIntIntMap hTable = new TIntIntHashMap();
        TIntIntMap countTable = new TIntIntHashMap();
        List<Integer> wList = new ArrayList<>();
        while (true) {
            boolean occured = hTable.containsKey(s);
            if(occured) {
                // already occured before
                assert countTable.containsKey(s);
                int index = countTable.get(s);
                if(terminate(index)) {
                    break;
                }else {
                    index ++;
                    countTable.put(s, index);
                }
            }else {
                // next time, it should be one
                countTable.put(s, 1);
            }
            // record last appearance
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
        Pair<Word, Word> normForm = Alphabet.getNormalForm(prefix, suffix);
        return new Pair<>(new Pair<>(normForm.getLeft(), normForm.getRight()), accept);
    }
    
    public static void main(String[] args) {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        
        NBA nba = new NBA(alphabet);
        nba.createState();
        nba.createState();
        nba.createState();
        nba.createState();
        nba.createState();
        
        final int zero = 0, one = 1, two = 2, three = 3, four = 4;
        // 
        nba.setInitial(zero);
        nba.getState(zero).addTransition(0, one);
        nba.getState(one).addTransition(0, zero);
        nba.getState(one).addTransition(1, two);
        nba.getState(two).addTransition(1, three);
        nba.getState(three).addTransition(1, two);
        nba.getState(three).addTransition(0, four);
        nba.getState(four).addTransition(0, four);
        nba.getState(four).addTransition(1, three);
        nba.setFinal(two);
        
        System.out.println(nba.toString());
        
        Sampler sampler = new SamplerIndexedMonteCarlo(0.01, 0.01);
        sampler.setNBA(nba);
        Pair<Pair<Word, Word>, Boolean> result = sampler.getRandomLasso();
        System.out.println("prefix: " + result.getLeft().getLeft());
        System.out.println("prefix: " + result.getLeft().getRight());
        System.out.println("membership: " + result.getRight());
    }

}
