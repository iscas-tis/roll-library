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
import roll.automata.operations.NBAOperations;
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
    private final StateSetKVMH finalStates;
    
    public NBAUniversalityCheck(NBA nba) {
        nba = NBAOperations.removeDeadStates(nba);
        this.nba = nba;
        this.acc = nba.getFinalStates();
        this.k = 2 * (nba.getStateSize() - acc.cardinality());
        this.infinity = k + 2;
        this.wholeRank = new LevelRankingUniversal(nba.getStateSize(), infinity);
        this.emptyRank = new LevelRankingEmpty(nba.getStateSize(), infinity);
        this.finalStates = new StateSetKVMH(wholeRank, emptyRank);
    }
    
    /**
     * Check the universality of the given Buchi automaton by a fixedpoint computation
     *      vY. uX (Pre(X) \/ (Pre(Y) /\ F))
     * */
    public boolean isUniversal() {
        // now we use fixed point computation to check whether given Buchi is universal
        StateSetKVMH initUniv = new StateSetKVMH(wholeRank, wholeRank);
        StateSetKVMH initEmpty = new StateSetKVMH(emptyRank, emptyRank);
        // vY. uX (Pre(X) \/ (Pre(Y) /\ F))
        Set<StateSetKVMH> y = new TreeSet<>();
        Set<StateSetKVMH> F = new TreeSet<>();
        F.add(finalStates);
        y.add(initUniv);
        //outer loop for Y
        while(true) {
            Set<StateSetKVMH> preY = y;
            // do a inner loop
            Set<StateSetKVMH> x = new TreeSet<>();
            x.add(initEmpty);
            while(true) {
                Set<StateSetKVMH> preX = x;
                Set<StateSetKVMH> pX = preKVMH(x);
                Set<StateSetKVMH> pY = preKVMH(y);
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
        StateSetKVMH init = new StateSetKVMH(lvlRank, emptyRank);
        for(StateSetKVMH s : y) {
            if(s.lessThan(init))
                return false;
        }
        
        return true;
    }
    
    // L1 /\ L2 = { maximal characteristic functions }
    private Set<StateSetKVMH> intersect(Set<StateSetKVMH> L1, Set<StateSetKVMH> L2) {
        Set<StateSetKVMH> result = new TreeSet<>();
        Set<StateSetKVMH> temp = new TreeSet<>();
        for(StateSetKVMH f1 : L1) {
            for(StateSetKVMH f2 : L2) {
                // first compute f1O f2O
                LevelRanking f1O = f1.o;
                LevelRanking f2O = f2.o;
                LevelRanking fOMax = f1O.max(f1O, f2O);
                LevelRanking fSMax = f1.s.max(f1.s, f2.s);
                if(! fOMax.isEmpty()) {
                    temp.add(new StateSetKVMH(fSMax, fOMax));
                }else if(f1O.isEmpty() && f2O.isEmpty()){
                    temp.add(new StateSetKVMH(fSMax, fOMax));
                }
            }
        }
        // 
        // get Max(L2)
        for(StateSetKVMH f : temp) {
            boolean hasCovered = false;
            for(StateSetKVMH s : result) {
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
    private Set<StateSetKVMH> union(Set<StateSetKVMH> L1, Set<StateSetKVMH> L2) {
        Set<StateSetKVMH> result = new TreeSet<>();
        Set<StateSetKVMH> temp = new TreeSet<>();
        // get Max(L1)
        for(StateSetKVMH f1 : L1) {
            boolean hasCovered = false;
            for(StateSetKVMH s : temp) {
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
        for(StateSetKVMH f2 : L2) {
            boolean hasCovered = false;
            for(StateSetKVMH s : temp) {
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
        for(StateSetKVMH f : temp) {
            boolean hasCovered = false;
            for(StateSetKVMH s : result) {
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
    
    
    private Set<StateSetKVMH> preKVMH(Set<StateSetKVMH> succs) {
        Set<StateSetKVMH> lPre = new TreeSet<>();
        for(int c = 0; c < nba.getAlphabetSize(); c ++) {
            for(StateSetKVMH succ : succs) {
                Set<StateSetKVMH> pre = preUniv(succ, c);
                lPre.addAll(pre);
            }
        }
        return lPre;
    }
    
    private Set<StateSetKVMH> preUniv(StateSetKVMH succ, int c) {
        Set<StateSetKVMH> lPre = new TreeSet<>();
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
                            , Integer.min(fOp.getRank(lp), UtilLevelRanking.getLeastOdd(fSp.getRank(lp), k))));
                }
            }
            if(nba.isFinal(l)) {
                fO.addRank(l, UtilLevelRanking.getLeastEven(fO.getRank(l), k));
            }
            if(fO.getRank(l) < fO.getInfinityValue()) {
                isEmptyO = false;
            }
        }
        // add <fO, fE>
        if(isEmptyO) {
            fO = emptyRank;
        }
        lPre.add(new StateSetKVMH(fO, emptyRank));
        if(!isEmptyO) {
            LevelRanking fS = new LevelRankingFunction(stateSize, infinity);
            for(int l = 0; l < nba.getStateSize(); l ++) {
                int max = -1;
                for(int lp : nba.getSuccessors(l, c)) {
                    max = Integer.max(max, fSp.getRank(lp));
                }
                fS.addRank(l, max);
                if(nba.isFinal(l)) {
                    fS.addRank(l, UtilLevelRanking.getLeastEven(fS.getRank(l), k));
                }
            }
            lPre.add(new StateSetKVMH(fS, fO));
        }
        return lPre;
    }

}
