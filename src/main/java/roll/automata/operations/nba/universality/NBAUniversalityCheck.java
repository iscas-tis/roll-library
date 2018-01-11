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

package roll.automata.operations.nba.universality;

import java.util.Set;
import java.util.TreeSet;

import roll.automata.NBA;
import roll.util.sets.ISet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Laurent Doyen and Jean-Francois Raskin
 *   "Improved Algorithms for the Automata-Based Approach to Model-Checking"
 * in TACAS 2007
 *  
 * */

public class NBAUniversalityCheck {
    
    private final NBA nba;
    private final int k;
    private final int infinity;
    private final ISet acc;
    private final LevelRanking emptyRank;
    private final LevelRanking wholeRank;
    private final KVMHState finalStates;
    
    public NBAUniversalityCheck(NBA nba) {
        this.nba = nba;
        this.acc = nba.getFinalStates();
        this.k = 2 * (nba.getStateSize() - acc.cardinality());
        this.infinity = k + 2;
        this.wholeRank = new LevelRankingUniversal(nba.getStateSize(), infinity);
        this.emptyRank = new LevelRankingEmpty(nba.getStateSize(), infinity);
        this.finalStates = new KVMHState(wholeRank, emptyRank);
    }
    
    /**
     * Check the universality of the given Buchi automaton by a fixedpoint computation
     *      vY. uX (Pre(X) \/ (Pre(Y) /\ F))
     * */
    public boolean isUniversal() {
        // now we use fixed point computation to check whether given Buchi is universal
        KVMHState initUniv = new KVMHState(wholeRank, wholeRank);
        KVMHState initEmpty = new KVMHState(emptyRank, emptyRank);
        // vY. uX (Pre(X) \/ (Pre(Y) /\ F))
        Set<KVMHState> y = new TreeSet<>();
        Set<KVMHState> F = new TreeSet<>();
        F.add(finalStates);
        y.add(initUniv);
        //outer loop for Y
        while(true) {
            Set<KVMHState> preY = y;
            // do a inner loop
            Set<KVMHState> x = new TreeSet<>();
            x.add(initEmpty);
            while(true) {
                Set<KVMHState> preX = x;
                Set<KVMHState> pX = preKVMH(x);
                Set<KVMHState> pY = preKVMH(y);
                pY = intersect(pY, F);
                x = union(pX, pY);
                if(preX.equals(x)) {
                    break;
                }
            }
            // outer loop
            y = x;
            if(y.equals(preY)) {
                break;
            }
        }
        LevelRanking lvlRank = new LevelRankingFunction(nba.getStateSize(), infinity);
        lvlRank.addRank(nba.getInitialState(), k);
        KVMHState init = new KVMHState(lvlRank, emptyRank);
        for(KVMHState s : y) {
            if(s.lessThan(init))
                return false;
        }
        
        return true;
    }
        
    private int getLeastEven(int b) {
        if(b > k) {
            return b;
        }
        if(b % 2 == 0) {
            return b;
        }else {
            return b + 1;
        }
    }
    
    private int getLeastOdd(int b) {
        if(b > k) {
            return b;
        }
        if(b % 2 == 0) {
            return b + 1;
        }else {
            return b;
        }
    }
    
    // L1 /\ L2 = { maximal characteristic functions }
    private Set<KVMHState> intersect(Set<KVMHState> L1, Set<KVMHState> L2) {
        Set<KVMHState> result = new TreeSet<>();
        Set<KVMHState> temp = new TreeSet<>();
        for(KVMHState f1 : L1) {
            for(KVMHState f2 : L2) {
                // first compute f1O f2O
                LevelRanking f1O = f1.o;
                LevelRanking f2O = f2.o;
                LevelRanking fOMax = f1O.max(f1O, f2O);
                LevelRanking fSMax = f1.s.max(f1.s, f2.s);
                if(! fOMax.isEmpty()) {
                    temp.add(new KVMHState(fSMax, fOMax));
                }else if(f1O.isEmpty() && f2O.isEmpty()){
                    temp.add(new KVMHState(fSMax, fOMax));
                }
            }
        }
        // 
        // get Max(L2)
        for(KVMHState f : temp) {
            boolean hasCovered = false;
            for(KVMHState s : result) {
                if(s.lessThan(f)) {
                    hasCovered = true;
                    break;
                }
            }
            if(! hasCovered) {
                result.add(f);
            }
        }
        return result;
    }
    
    // L1 \/ L2 = Max{Max{L1} \/ Max{L2}}
    private Set<KVMHState> union(Set<KVMHState> L1, Set<KVMHState> L2) {
        Set<KVMHState> result = new TreeSet<>();
        Set<KVMHState> temp = new TreeSet<>();
        // get Max(L1)
        for(KVMHState f1 : L1) {
            boolean hasCovered = false;
            for(KVMHState s : temp) {
                if(s.lessThan(f1)) {
                    hasCovered = true;
                    break;
                }
            }
            if(! hasCovered) {
                temp.add(f1);
            }
        }
        // get Max(L2)
        for(KVMHState f2 : L2) {
            boolean hasCovered = false;
            for(KVMHState s : temp) {
                if(s.lessThan(f2)) {
                    hasCovered = true;
                    break;
                }
            }
            if(! hasCovered) {
                temp.add(f2);
            }
        }
        
        // get Max(Max(L1) \/ Max(L2))
        for(KVMHState f : temp) {
            boolean hasCovered = false;
            for(KVMHState s : result) {
                if(s.lessThan(f)) {
                    hasCovered = true;
                    break;
                }
            }
            if(! hasCovered) {
                result.add(f);
            }
        }
        return result;
    }
    
    
    private Set<KVMHState> preKVMH(Set<KVMHState> succs) {
        Set<KVMHState> lPre = new TreeSet<>();
        for(int c = 0; c < nba.getAlphabetSize(); c ++) {
            for(KVMHState succ : succs) {
                Set<KVMHState> pre = preUniv(succ, c);
                lPre.addAll(pre);
            }
        }
        return lPre;
    }
    
    private Set<KVMHState> preUniv(KVMHState succ, int c) {
        Set<KVMHState> lPre = new TreeSet<>();
        LevelRanking fOp = succ.o;
        LevelRanking fSp = succ.s;
        int stateSize = nba.getStateSize();
        LevelRanking fO = new LevelRankingFunction(stateSize, infinity);
        boolean isEmptyO = true;
        for(int l = 0; l < nba.getStateSize(); l ++) {
            fO.addRank(l, 0);
            for(int lp : nba.getSuccessors(l, c)) {
                if(nba.isFinal(lp)) {
                    fO.addRank(l, Integer.max(fO.getRank(l), fOp.getRank(lp)));
                }else {
                    fO.addRank(l, Integer.max(fO.getRank(l)
                            , Integer.min(fOp.getRank(lp), getLeastOdd(fSp.getRank(lp)
                                    ))));
                }
            }
            if(nba.isFinal(l)) {
                fO.addRank(l, getLeastEven(fO.getRank(l)));
            }
            if(fO.getRank(l) < fO.getInfinityValue()) {
                isEmptyO = false;
            }
        }
        // add <fO, fE>
        if(isEmptyO) {
            fO = emptyRank;
        }
        lPre.add(new KVMHState(fO, emptyRank));
        if(!isEmptyO) {
            LevelRanking fS = new LevelRankingFunction(stateSize, infinity);
            for(int l = 0; l < nba.getStateSize(); l ++) {
                int max = -1;
                for(int lp : nba.getSuccessors(l, c)) {
                    max = Integer.max(max, fSp.getRank(lp));
                }
                fS.addRank(l, max);
                if(nba.isFinal(l)) {
                    fS.addRank(l, getLeastEven(fS.getRank(l)));
                }
            }
            lPre.add(new KVMHState(fS, fO));
        }
        return lPre;
    }

    private class KVMHState implements Comparable<KVMHState> {
        LevelRanking s;
        LevelRanking o;
        public KVMHState(LevelRanking s, LevelRanking o) {
            this.s = s;
            this.o = o;
        }
        
        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null) return false;
            if(obj instanceof KVMHState) {
                KVMHState other = (KVMHState)obj;
                return s.equals(other.s)
                    && o.equals(other.o);
            }
            return false;
        }
        
        public boolean lessThan(KVMHState other) {
            return s.rankLessThan(other.s)
                && o.rankLessThan(other.o);   
        }

        @Override
        public int compareTo(KVMHState other) {
            int f = s.compareTo(other.s);
            if(f != 0) return f;
            return o.compareTo(other.o);
        }
        
        @Override
        public String toString() {
            return "<" + s.toString() + "," + o.toString() + ">";
        }
        
    }

}
