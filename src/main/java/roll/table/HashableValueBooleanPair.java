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

import roll.util.Pair;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class HashableValueBooleanPair implements HashableValue {
	
	private boolean valueLeft, valueRight ;
	
	public HashableValueBooleanPair(boolean left, boolean right) {
		this.valueLeft  = left;
		this.valueRight = right;
	}

	@Override
	public boolean valueEqual(HashableValue rvalue) {
	    boolean left = valueLeft && valueRight;
	    boolean right = (Boolean)rvalue.getLeft() && (Boolean)rvalue.getRight();
		return left == right;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Pair<Boolean, Boolean> get() {
		return new Pair<>(valueLeft, valueRight);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof HashableValueBooleanPair) {
			HashableValueBooleanPair pair = (HashableValueBooleanPair)obj;
			return valueEqual(pair);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "(" +  (valueLeft ? "+" : "-")
				+ ", " + (valueRight ? "+" : "-") + ")";
	}
	
	@Override
	public int hashCode() {
		int value = valueLeft ? 0 : 1;
		value = value * 2 + (valueRight ? 0 : 1);
		return value;
	}

	@Override
	public boolean isPair() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean getLeft() {
		return valueLeft;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean getRight() {
		return valueRight;
	}

	@Override
	public boolean isAccepting() {
		return valueLeft && valueRight ;
	}

}
