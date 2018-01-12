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
import roll.automata.StateNFA;
import roll.automata.operations.NBAOperations;
import roll.automata.operations.StateContainer;
import roll.util.sets.ISet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Laurent Doyen and Jean-Francois Raskin
 *   "Improved Algorithms for the Automata-Based Approach to Model-Checking"
 * in TACAS 2007
 * */

public class NBAInclusionCheckRank {
    private final NBA A;
    private final NBA B;
    private final int k;
    private final int infinity;
    private final StateContainer[] stCs; 
    private final LevelRanking emptyRank;
    private final LevelRanking wholeRank;
    
    public NBAInclusionCheckRank(NBA A, NBA B) {
        this.A = A;
        this.B = B;
        this.stCs = new StateContainer[A.getStateSize()];
        ISet acc = B.getFinalStates();
        boolean isSemiDet = NBAOperations.isSemideterministic(B);
        if(isSemiDet) {
            this.k = 4;  // for semideterministic, 4 (3) is enough 
        }else {
            this.k = 2 * (B.getStateSize() - acc.cardinality());
        }
        this.infinity = k + 2;
        this.wholeRank = new LevelRankingUniversal(B.getStateSize(), infinity);
        this.emptyRank = new LevelRankingEmpty(B.getStateSize(), infinity);
        initializePredecessors();
    }
    
    private void initializePredecessors() {
        for(int i = 0; i < A.getStateSize(); i ++) {
            for(int c = 0; c < A.getAlphabetSize(); c ++) {
                for(int succ : A.getSuccessors(i, c)) {
                    if(stCs[succ] == null) {
                        stCs[succ] = new StateContainer(succ, A);
                    }
                    stCs[succ].addPredecessors(c, i);
                }
            }
        }
    }
    
    public boolean isIncluded() {
        // vY (uX1.[Pre(X1) \/ (Pre(Y) /\ F1)] /\ uX2.[Pre(X2) \/ (Pre(Y) /\ F2)])
        Set<StateSetKVMHInclusion> y = new TreeSet<>();
        Set<StateSetKVMHInclusion> F1 = new TreeSet<>();
        Set<StateSetKVMHInclusion> F2 = new TreeSet<>();
        Set<StateSetKVMHInclusion> emp = new TreeSet<>();
        
        for(int i = 0; i < A.getStateSize(); i ++) {
            // whole * whole
            StateSetKVMHInclusion st = new StateSetKVMHInclusion(i, wholeRank, wholeRank);
            y.add(st);
            // F1 = A.F * whole
            if(A.isFinal(i)) {
                F1.add(st);
            }
            // F1 = whole * B^c.F
            F2.add(new StateSetKVMHInclusion(i, wholeRank, emptyRank));
            emp.add(new StateSetKVMHInclusion(i, emptyRank, emptyRank));
        }
        
        //outer loop for Y
        while(true) {
            Set<StateSetKVMHInclusion> preY = y;
            // do a inner loop
            Set<StateSetKVMHInclusion> x1 = new TreeSet<>();
            x1.addAll(emp);
            // first iteration for min fixed point
            while(true) {
                Set<StateSetKVMHInclusion> preX = x1;
                Set<StateSetKVMHInclusion> pX = preKVMH(x1);
                Set<StateSetKVMHInclusion> pY = preKVMH(y);
                pY = intersect(pY, F1);
                x1 = union(pX, pY);
                if(preX.equals(x1)) {
                    break;
                }
            }
            // outer loop
            Set<StateSetKVMHInclusion> x2 = new TreeSet<>();
            x2.addAll(emp);
            // first iteration for min fixed point
            while(true) {
                Set<StateSetKVMHInclusion> preX = x2;
                Set<StateSetKVMHInclusion> pX = preKVMH(x2);
                Set<StateSetKVMHInclusion> pY = preKVMH(y);
                pY = intersect(pY, F2);
                x2 = union(pX, pY);
                if(preX.equals(x2)) {
                    break;
                }
            }
            
            y = intersect(x1, x2);
            if(y.equals(preY)) {
                break;
            }
        }
        LevelRanking lvlRank = new LevelRankingFunction(B.getStateSize(), infinity);
        lvlRank.addRank(B.getInitialState(), k);
        StateSetKVMHInclusion init = new StateSetKVMHInclusion(A.getInitialState(), lvlRank, emptyRank);
        for(StateSetKVMH s : y) {
            if(s.lessThan(init))
                return false;
        }
        
        return true;
    }
    
    // L1 /\ L2 = { maximal characteristic functions }
    private Set<StateSetKVMHInclusion> intersect(Set<StateSetKVMHInclusion> L1, Set<StateSetKVMHInclusion> L2) {
        Set<StateSetKVMHInclusion> result = new TreeSet<>();
        Set<StateSetKVMHInclusion> temp = new TreeSet<>();
        for(StateSetKVMHInclusion f1 : L1) {
            for(StateSetKVMHInclusion f2 : L2) {
                if(f1.left == f2.left) {
                    // first compute f1O f2O
                    LevelRanking f1O = f1.o;
                    LevelRanking f2O = f2.o;
                    LevelRanking fOMax = f1O.max(f1O, f2O);
                    LevelRanking fSMax = f1.s.max(f1.s, f2.s);
                    if(! fOMax.isEmpty()) {
                        temp.add(new StateSetKVMHInclusion(f1.left, fSMax, fOMax));
                    }else if(f1O.isEmpty() && f2O.isEmpty()){
                        temp.add(new StateSetKVMHInclusion(f1.left, fSMax, fOMax));
                    }
                }
            }
        }
        // 
        // get Max(L2)
        for(StateSetKVMHInclusion f : temp) {
            boolean hasCovered = false;
            for(StateSetKVMHInclusion s : result) {
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
    public static Set<StateSetKVMHInclusion> union(Set<StateSetKVMHInclusion> L1, Set<StateSetKVMHInclusion> L2) {
        Set<StateSetKVMHInclusion> result = new TreeSet<>();
        Set<StateSetKVMHInclusion> temp = new TreeSet<>();
        // get Max(L1)
        for(StateSetKVMHInclusion f1 : L1) {
            boolean hasCovered = false;
            for(StateSetKVMHInclusion s : temp) {
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
        for(StateSetKVMHInclusion f2 : L2) {
            boolean hasCovered = false;
            for(StateSetKVMHInclusion s : temp) {
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
        for(StateSetKVMHInclusion f : temp) {
            boolean hasCovered = false;
            for(StateSetKVMHInclusion s : result) {
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
    
    private Set<StateSetKVMHInclusion> preKVMH(Set<StateSetKVMHInclusion> succs) {
        Set<StateSetKVMHInclusion> lPre = new TreeSet<>();
        for(int c = 0; c < A.getAlphabetSize(); c ++) {
            for(StateSetKVMHInclusion succ : succs) {
                // check the predecessors of A
                StateContainer stC = stCs[succ.left];
                if(stC == null) continue;
                Set<StateNFA> lPrevs = stC.getPredecessors(c);
                if(lPrevs.isEmpty()) continue;
                // we have to compute previous of B
                Set<StateSetKVMH> rPrevs = UtilLevelRanking.preUniv(succ, c, B, k, infinity, emptyRank);
                for(StateNFA lPrev : lPrevs) {
                    for(StateSetKVMH rPrev : rPrevs) {
                        lPre.add(new StateSetKVMHInclusion(lPrev.getId(), rPrev.s, rPrev.o));
                    }
                }
            }
        }
        return lPre;
    }
    

    private class StateSetKVMHInclusion extends StateSetKVMH {
        int left;        
        public StateSetKVMHInclusion(int left, LevelRanking s, LevelRanking o) {
            super(s, o);
            this.left = left;
        }
        
        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null) return false;
            if(obj instanceof StateSetKVMHInclusion) {
                StateSetKVMHInclusion other = (StateSetKVMHInclusion)obj;
                return left == other.left
                    && super.equals(other);
            }
            return false;
        }
        
        @Override
        public boolean lessThan(StateSetKVMH obj) {
            if(!(obj instanceof StateSetKVMHInclusion)) {
                throw new UnsupportedOperationException("Not StateSetKVMHInclusion");
            }
            StateSetKVMHInclusion other = (StateSetKVMHInclusion)obj;
            return left == other.left
                && super.lessThan(other);   
        }

        @Override
        public int compareTo(StateSetKVMH obj) {
            if(!(obj instanceof StateSetKVMHInclusion)) {
                throw new UnsupportedOperationException("Not StateSetKVMHInclusion");
            }
            StateSetKVMHInclusion other = (StateSetKVMHInclusion)obj; 
            if(left < other.left) return -1;
            if(left > other.left) return 1;
            return super.compareTo(other);
        }
        
        @Override
        public String toString() {
            return left + ":" + super.toString();
        }
        
    }

}
