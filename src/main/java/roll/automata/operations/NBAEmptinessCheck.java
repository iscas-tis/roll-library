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
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class NBAEmptinessCheck {
    
    private int index = 0;
    private Stack<Integer> SCCs;
    private NBA nba;
    private TIntIntMap vIndex;
    private TIntIntMap vLowlink;
    private int fstF = -1;
    private int sndF = -1;
    private ISet scc;
    private ISet fstAcc;
    private ISet sndAcc;
    
    private LassoConstructor constructor;
    
    // input BA is generalized BA
    public NBAEmptinessCheck(NBA nba, ISet fstAcc, ISet sndAcc){
        this.nba  = nba;
        this.fstAcc = fstAcc;
        this.sndAcc = sndAcc;
        this.SCCs = new Stack<>();
        this.scc = UtilISet.newISet();
        this.vIndex = new TIntIntHashMap();
        this.vLowlink = new TIntIntHashMap();
    }
    
    public boolean isEmpty() {
        // only check the part where final states can reach
        // all final states are reachable from the initial state
        for(final int s : fstAcc) {
            if(!vIndex.containsKey(s)){
                if(tarjan(s))
                    return false;
            }
        }
        
        for(final int s : sndAcc) {
            if(!vIndex.containsKey(s)){
                if(tarjan(s))
                    return false;
            }
        }

        return true;
    }

    // terminate on the first accepting loop
    boolean tarjan(int v) {
        vIndex.put(v, index);
        vLowlink.put(v, index);
        index++;
        SCCs.push(v);

        boolean selfLoop = false;
        for(int c = 0; c < nba.getAlphabetSize(); c ++) {
            ISet vprimes = nba.getSuccessors(v, c);
            for(final int vp : vprimes) {
                if(vp == v) selfLoop = true;
                if(!vIndex.containsKey(vp)){
                    if(tarjan(vp)) return true;
                    vLowlink.put(v, Math.min(vLowlink.get(v), vLowlink.get(vp)));
                }else if(SCCs.contains(vp)){
                    vLowlink.put(v, Math.min(vLowlink.get(v), vIndex.get(vp)));
                }
            }
        }
        
        boolean isAcc = false;
        if(vLowlink.get(v) == vIndex.get(v)){
            int numStates = 0;
            scc.clear();
            boolean left = false, right = false;
            while(! SCCs.empty()){
                int t = SCCs.pop();
                ++ numStates;
                if(fstAcc.get(t)) {
                    fstF = t;
                    left = true;
                }
                if(sndAcc.get(t)) {
                    sndF = t;
                    right = true;
                }

                scc.set(t);
                if(t == v)
                    break;
            }
            
            if(numStates == 1 && !selfLoop){
                return false;
            }
            
            isAcc = left && right;
        }
        
        return isAcc;
    }
    
    public void findpath() {
        if(fstF == -1 || sndF == -1) return ;
        constructor = new LassoConstructor(nba, fstF, sndF, scc);
        constructor.computeLasso();
    }
    
    public Pair<Word, Word> getCounterexample() {
        return constructor.getCounterexample();
    }

}
