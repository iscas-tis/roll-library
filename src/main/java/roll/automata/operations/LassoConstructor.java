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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.NBA;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LassoConstructor {
    
    private final NBA result;
    private int fstF;
    private int sndF;
    private ISet scc;
    
    // word information
    private Word wordPrefix;
    private Word wordSuffix;
    
    // run information
    private List<Integer> runPrefix;
    private List<Integer> runSuffix;
    
    public LassoConstructor(NBA result, int fstF, int sndF, ISet scc) {
        this.result = result;
        this.fstF = fstF;
        this.sndF = sndF;
        this.scc = scc;
        this.wordPrefix = result.getAlphabet().getEmptyWord();
        this.wordSuffix = result.getAlphabet().getEmptyWord();
        this.runPrefix = new ArrayList<>();
        this.runSuffix = new ArrayList<>();
    }
    
    public void computeLasso() {
        computePrefix();
        computeSuffix();
    }
    

    private void computePrefix() {
        int initial = result.getInitialState();
        runPrefix.add(initial);
        // compute a path to the first final state
        findPath(initial, fstF, true);
    }
    
    public Pair<Word, Word> getCounterexample() {
        return new Pair<>(wordPrefix, wordSuffix);
    }

    private void computeSuffix() {
        // add state in runSuffix
        runSuffix.add(fstF);        
        if(fstF == sndF) {
            // we have a self-loop
            int state = -1;
            int letter = -1;
            for(int c = 0; c < result.getAlphabetSize(); c ++) {
                ISet succs = result.getSuccessors(fstF, c);
                boolean found = false;
                for(final int succ : succs) {
                    if(fstF == succ) {
                        wordSuffix = wordSuffix.append(c);
                        found = true;
                        break;
                    }else if(scc.get(succ)){
                        state = succ;
                        letter = c;
                    }
                }
                if(found) break;
            }
            // it does not have self-loop and occurred in both sets
            if(! wordSuffix.isEmpty()) return ;
            // we have to set them differently
            assert state != -1 : "successor " + state;
            assert letter != -1 : "letter " + letter;
            wordSuffix = wordSuffix.append(letter);
            runSuffix.add(state);
            findPath(state, sndF, false);
        }else {
            // set fstF and sndF are two different states
            findPath(fstF, sndF, false);
            findPath(sndF, fstF, false); 
        }
        
    }

    // usually s is not the same as t
    private void findPath(int s, int t, boolean prefix) {
        // store the predecessors (value) of the specific states (key)
        TIntIntMap predStates = new TIntIntHashMap();
        TIntIntMap predLabels = new TIntIntHashMap();
        Alphabet alphabet = result.getAlphabet();
        ISet visited = UtilISet.newISet();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(s);
        visited.set(s);
        while(! queue.isEmpty()) {
            if(visited.get(t)) break; // already found it
            int cur = queue.poll();
            for(int c = 0; c < alphabet.getLetterSize(); c ++) {
                ISet succs = result.getSuccessors(cur, c);
                if(succs.isEmpty()) continue;
                for (final int succ : succs) {
                    if (!visited.get(succ)) {// in states allowed and not visited
                        queue.add(succ); // add in queue
                        predStates.put(succ, cur); // record predecessors
                        predLabels.put(succ, c); // record previous letter
                        visited.set(succ);
                    }
                }
            }
        }
        // must have a path from s to t
        LinkedList<Integer> run = new LinkedList<>();
        LinkedList<Integer> word = new LinkedList<>();
        int cur = t;
        while(cur != s) {
            run.addFirst(cur);
            word.addFirst(predLabels.get(cur));
            cur = predStates.get(cur);
        }
        List<Integer> r;
        Word w;
        if(prefix) {
            r = runPrefix;
            w = wordPrefix;
        }else {
            r = runSuffix;
            w = wordSuffix;         
        }
        
        for(Integer state : run) {
            r.add(state);
        }
        
        for(Integer letter : word) {
            w = w.append(letter);
        }
        
        if(prefix) {
             wordPrefix = w;
        }else {
             wordSuffix = w;         
        }
        
    }

    

}
