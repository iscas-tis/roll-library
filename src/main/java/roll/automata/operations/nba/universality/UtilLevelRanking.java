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

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class UtilLevelRanking {
    
    public static int getLeastEven(int b, int bound) {
        if(b > bound) {
            return b;
        }
        if(b % 2 == 0) {
            return b;
        }else {
            return b + 1;
        }
    }
    
    public static int getLeastOdd(int b, int bound) {
        if(b > bound) {
            return b;
        }
        if(b % 2 == 0) {
            return b + 1;
        }else {
            return b;
        }
    }
    
    
    
    public static Set<StateSetKVMH> preUniv(StateSetKVMH succ, int c, NBA B, int K, int infinity, LevelRanking emptyRank) {
        Set<StateSetKVMH> lPre = new TreeSet<>();
        LevelRanking fOp = succ.o;
        LevelRanking fSp = succ.s;
        int stateSize = B.getStateSize();
        LevelRanking fO = new LevelRankingFunction(stateSize, infinity);
        boolean isEmptyO = true;
        for(int l = 0; l < B.getStateSize(); l ++) {
            fO.addRank(l, 0);
            for(int lp : B.getSuccessors(l, c)) {
                if(B.isFinal(lp)) {
                    fO.addRank(l, Integer.max(fO.getRank(l), fOp.getRank(lp)));
                }else {
                    fO.addRank(l, Integer.max(fO.getRank(l)
                            , Integer.min(fOp.getRank(lp), getLeastOdd(fSp.getRank(lp), K))));
                }
            }
            if(B.isFinal(l)) {
                fO.addRank(l, getLeastEven(fO.getRank(l), K));
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
            for(int l = 0; l < B.getStateSize(); l ++) {
                int max = -1;
                for(int lp : B.getSuccessors(l, c)) {
                    max = Integer.max(max, fSp.getRank(lp));
                }
                fS.addRank(l, max);
                if(B.isFinal(l)) {
                    fS.addRank(l, UtilLevelRanking.getLeastEven(fS.getRank(l), K));
                }
            }
            lPre.add(new StateSetKVMH(fS, fO));
        }
        return lPre;
    }
    

}
