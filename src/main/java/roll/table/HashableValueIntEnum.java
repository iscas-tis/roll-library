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

package roll.table;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * a data structure specialized for the syntactic FDFA learning
 * */

public class HashableValueIntEnum implements HashableValue {
    
    public static enum RValue {
        A,
        B,
        C;
    }
	
	private RValue value;
	private int state;
	
	public HashableValueIntEnum(int state, boolean recur, boolean mq) {
		this.state = state;
		if(recur && mq) {
		    value = RValue.A;
		}else if(recur && !mq) {
		    value = RValue.B;
		}else if(!recur) {
		    value = RValue.C;
		}
	}

	@Override
	public boolean valueEqual(HashableValue rvalue) {
	    if(this == rvalue) return true;
	    if( rvalue instanceof HashableValueIntEnum) {
	        HashableValueIntEnum other = (HashableValueIntEnum)rvalue;
	        return other.state == state
	            && other.value == value;
	    }
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean get() {
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if(obj == null) return false;
	    if(this == obj) return true;
		if(obj instanceof HashableValueIntEnum) {
			HashableValueIntEnum row = (HashableValueIntEnum)obj;
			return valueEqual(row);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return  "(" + state + ", " + value + ")";
	}
	
	@Override
	public int hashCode() {
	    switch(value) {
	    case A:
	        return state;
	    case B:
	        return state + 1;
	    case C:
	        return state + 2;
	    default:
	            throw new UnsupportedOperationException("No such value for right component");
	    }
	}

	@Override
	public boolean isPair() {
		return true;
	}

	@SuppressWarnings("unchecked")
    @Override
	public Integer getLeft() {
		return state;
	}

	@SuppressWarnings("unchecked")
    @Override
	public RValue getRight() {
		return value;
	}

	@Override
	public boolean isAccepting() {
		return value == RValue.A;
	}

}
