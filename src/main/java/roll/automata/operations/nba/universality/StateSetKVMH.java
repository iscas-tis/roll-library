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
 * <S, O> function
 * */

public class StateSetKVMH implements Comparable<StateSetKVMH>{
    
    protected LevelRanking s;
    protected LevelRanking o;
    
    public StateSetKVMH(LevelRanking s, LevelRanking o) {
        this.s = s;
        this.o = o;
    }
    
    public LevelRanking getS() {
        return s;
    }
    
    public LevelRanking getO() {
        return o;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(obj instanceof StateSetKVMH) {
            StateSetKVMH other = (StateSetKVMH)obj;
            return s.equals(other.s)
                && o.equals(other.o);
        }
        return false;
    }
    
    public boolean lessThan(StateSetKVMH other) {
        return s.rankLessThan(other.s)
            && o.rankLessThan(other.o);   
    }

    @Override
    public int compareTo(StateSetKVMH other) {
        int f = s.compareTo(other.s);
        if(f != 0) return f;
        return o.compareTo(other.o);
    }
    
    @Override
    public String toString() {
        return "<" + s.toString() + "," + o.toString() + ">";
    }

}
