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

import java.util.Arrays;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * represent a set of states in Qk of KVMH automaton
 * */

public class LevelRankingFunction extends LevelRankingUniversal {
    
    private int[] ranks;
    
    public LevelRankingFunction(int stateSize, int infinityValue) {
        super(stateSize, infinityValue);
        ranks = new int[stateSize];
        // initialize it to be empty set
        Arrays.fill(ranks, getInfinityValue());
    }
    
    @Override
    public void addRank(int state, int rank) {
        assert checkStateConsistency(state);
        ranks[state] = rank;
    }
    
    @Override
    public int getRank(int state) {
        assert checkStateConsistency(state);
        return ranks[state];
    }
    
    @Override
    public boolean rankLessThan(LevelRanking levelRanks) {
        if(levelRanks.isUniversal()) {
            return false;
        }
        if(levelRanks.isEmpty()) {
            return true;
        }
        LevelRankingFunction other = (LevelRankingFunction)levelRanks;
        for(int i = 0; i < stateSize; i ++) {
            if(ranks[i] <= other.ranks[i]
            || other.ranks[i] >= getInfinityValue()) {
                continue;
            }else {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(obj instanceof LevelRankingFunction) {
            LevelRankingFunction other = (LevelRankingFunction)obj;
            return compareTo(other) == 0;
        }
        return false;
    }
    
    @Override
    public boolean isUniversal() {
        return false;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(int i = 0; i < stateSize; i ++) {
            if(i != 0) builder.append(",");
            if(ranks[i] >= infinityValue) {
                builder.append(i + "->inf");
            }else {
                builder.append(i + "->" + ranks[i]);
            }
        }
        builder.append("]");
        return builder.toString();
    }

}
