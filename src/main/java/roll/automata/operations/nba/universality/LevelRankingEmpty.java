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
 * represent the empty set of states Qk of KVMH automaton
 * 
 * */

public class LevelRankingEmpty extends LevelRankingUniversal {
    public LevelRankingEmpty(int stateSize, int infinityValue) {
        super(stateSize, infinityValue);
    }

    @Override
    public int getRank(int state) {
        assert checkStateConsistency(state);
        return infinityValue;
    }

    @Override
    public boolean rankLessThan(LevelRanking levelRanks) {
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(obj instanceof LevelRanking) {
            LevelRanking other = (LevelRanking)obj;
            return other.isEmpty();
        }
        return false;
    }
    
    @Override
    public boolean isUniversal() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
    
    @Override
    public String toString() {
        return "ff";
    }
}
