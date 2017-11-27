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

import java.util.Stack;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.NBA;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBAAccept {
    
    private final NBA nba;
    private final Word stem;
    private final Word loop;
    private boolean accepting;
    
    public NBAAccept(NBA nba, Word stem, Word loop) {
        assert nba != null && stem != null && loop != null;
        this.nba = nba;
        this.stem = stem;
        this.loop = loop;
        this.accepting = false;
        if(! loop.isEmpty()) {
            new AsccExplore();
        }
    }
    
    public boolean isAccepting() {
        return accepting;
    }
    
    private class AsccExplore {
        
        private int depth;
        private final Stack<Integer> sccs;           // C99 's root stack
        private final Stack<Integer> act;            // tarjan's stack
        private final TIntIntMap dfsNum;        
                       
        public AsccExplore() {
            this.sccs = new Stack<>();
            this.act = new Stack<>();
            this.dfsNum = new TIntIntHashMap();
            this.depth = 0;
            strongConnect(nba.getInitialState(), 0);
        }

        void strongConnect(int s, int snd) {
            
            ++ depth;
            dfsNum.put(s, depth);
            sccs.push(s);
            act.push(s);

            for (int t : nba.getSuccessors(s, getNextLetter(snd))) {
                if (!dfsNum.containsKey(t)) {
                    strongConnect(t, getNextState(snd));
                    if(accepting) return ;
                } else if (act.contains(t)) {
                    // we have already seen it before, there is a loop
                    // probably there is one final state without self-loop
                    while (true) {
                        // pop element u
                        int u = sccs.pop();
                        // found one accepting scc
                        if (nba.isFinal(u) && isFinalState(snd)) {
                            accepting = true;
                            return ;
                        }
                        if (dfsNum.get(u) <= dfsNum.get(t)) {
                            sccs.push(u); // push back
                            break;
                        }
                    }
                }
            }
            
            // if current number is done, 
            // then we should remove all 
            // active states in the same scc
            if(sccs.peek().intValue() == s) {
                sccs.pop();
                while(true) {
                    int u = act.pop(); // Tarjan' Stack
                    if(u == s) break;
                }
            }
        }
    }
    
    // ------------- transition for lasso word -------------------------
    private int getSum() {
        return stem.length() + loop.length();
    }
    
    private int getNextLetter(int state) {
        if(state < stem.length()) {
            return stem.getLetter(state);
        }
        if(state < getSum()) {
            return loop.getLetter(state - stem.length());
        }
        return loop.getFirstLetter();
    }
    
    private int getNextState(int state) {
        assert state >= 0 && state <= getSum();
        if(state < getSum()) {
            return state + 1;
        }
        return stem.length() + 1;
    }
    
    private boolean isFinalState(int state) {
        assert state >= 0 && state <= getSum();
        return state > stem.length();
    }

}
