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

package roll.learner.nba.mp;

import java.util.Stack;

import gnu.trove.map.TIntIntMap;
import roll.automata.DFA;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * found an MSCC which has a conflict state otherwise mark the rest of states in the MSCC
 * */

public class MSCCMarkOrDetect {
    
    private final DFA dfa;
    private int index = 0;
    private Stack<Integer> SCCs;
    private TIntIntMap vIndex;
    private TIntIntMap vLowlink;
    private final int[] marks;
    Pair<Integer, Integer> pair;
    
    public MSCCMarkOrDetect(DFA dfa, int[] marks) {
        this.dfa = dfa;
        this.marks = marks;
    }
    
    public boolean markOrdetect() {
        // only check the states reachable by the dfa
        final int s = dfa.getInitialState();
        if(!vIndex.containsKey(s)){
            if(tarjan(s))
               return false;
        }

        return true;
    }

    // terminate on the first accepting loop
    boolean tarjan(int v) {
        vIndex.put(v, index);
        vLowlink.put(v, index);
        index++;
        SCCs.push(v);

        for(int c = 0; c < dfa.getAlphabetSize(); c ++) {
            int w = dfa.getSuccessor(v, c);
            // to check whether there is a loop
            if(!vIndex.containsKey(w)){
                if(tarjan(w)) return true;
                vLowlink.put(v, Math.min(vLowlink.get(v), vLowlink.get(w)));
            }else if(SCCs.contains(w)){
                vLowlink.put(v, Math.min(vLowlink.get(v), vIndex.get(w)));
            }
        }
        if(vLowlink.get(v) == vIndex.get(v)){
            int num = 0;
            int s1 = -1, s2 = -2;
            ISet scc = UtilISet.newISet();
            int mark = 0;
            while(! SCCs.empty()){
                int t = SCCs.pop();
                if((marks[t] | 1) != 0 ) {
                    s1 = t;
                }
                if((marks[t] | 2) != 0 ) {
                    s2 = t;
                }
                if(s1 >= 0 && s2 >= 0) {
                    pair = new Pair<>(s1, s2);
                    return true;
                }
                mark = marks[t];
                scc.set(t);
                ++ num;
                if(t == v)
                    break;
            }
            if(num > 1) {
                //mark all other states, including those have been marked
                for(int s : scc) {
                    marks[s] = mark;
                }
            }
        }
        return false;
    }
    
    
    

}
