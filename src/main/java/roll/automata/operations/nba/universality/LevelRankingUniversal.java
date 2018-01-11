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

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * represent the universal states in Qk of KVMH automaton
 * */

public class LevelRankingUniversal implements LevelRanking {
    
    protected final int stateSize;
    protected final int infinityValue;
    
    public LevelRankingUniversal(int stateSize, int infinityValue) {
        this.stateSize = stateSize;
        this.infinityValue = infinityValue; // larger than k
    }
    
    @Override
    public int compareTo(LevelRanking other) {
        assert stateSize == other.getRankArrarySize();
        for(int i = 0; i < stateSize; i ++) {
            if(getRank(i) > other.getRank(i)) {
                return 1;
            }else if(getRank(i) < other.getRank(i)) {
                return -1;
            }
        }
        return 0;
    }

    @Override
    public void addRank(int state, int rank) {
        throw new UnsupportedOperationException("LevelRankingAll does not support addLevelRank");
    }

    @Override
    public int getRank(int state) {
        assert checkStateConsistency(state);
        return 0;
    }

    @Override
    public boolean rankLessThan(LevelRanking levelRanks) {
        return true;
    }

    @Override
    public int getRankArrarySize() {
        return stateSize;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(obj instanceof LevelRanking) {
            LevelRanking other = (LevelRanking)obj;
            return other.isUniversal();
        }
        return false;
    }

    @Override
    public boolean isUniversal() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getInfinityValue() {
        return infinityValue;
    }

    @Override
    public LevelRanking max(LevelRanking lvlRank1, LevelRanking lvlRank2) {
        if(lvlRank1.isUniversal()) return lvlRank2;
        if(lvlRank1.isEmpty()) return lvlRank1;
        LevelRanking result = new LevelRankingFunction(stateSize, infinityValue);
        boolean allInfinity = true;
        for(int i = 0; i < stateSize; i ++) {
            int maxValue = Integer.max(lvlRank1.getRank(i), lvlRank2.getRank(i));
            if(maxValue < infinityValue) {
                allInfinity = false;
            }
            result.addRank(i, maxValue);
        }
        if(allInfinity) {
            result = new LevelRankingEmpty(stateSize, infinityValue);
        }
        return result;
    }

    @Override
    public LevelRanking min(LevelRanking lvlRank1, LevelRanking lvlRank2) {
        if(lvlRank1.isUniversal()) return lvlRank1;
        if(lvlRank1.isEmpty()) return lvlRank2;
        LevelRanking result = new LevelRankingFunction(stateSize, infinityValue);
        boolean allZero = true;
        for(int i = 0; i < stateSize; i ++) {
            int minValue = Integer.min(lvlRank1.getRank(i), lvlRank2.getRank(i));
            if(minValue > 0) {
                allZero = false;
            }
            result.addRank(i, minValue);
        }
        if(allZero) {
            result = new LevelRankingUniversal(stateSize, infinityValue);
        }
        return result;
    }
    
    protected boolean checkStateConsistency(int state) {
        return state >= 0 && state < stateSize;
    }
    
    @Override
    public String toString() {
        return "tt";
    }
    

}
