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
 * */

public class HashableValueBoolean implements HashableValue {
	
	private boolean value ;
	
	public HashableValueBoolean(boolean val) {
		value = val;
	}

	@Override
	public boolean valueEqual(HashableValue rvalue) {
		return value == (Boolean)rvalue.get();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean get() {
		return value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof HashableValueBoolean) {
			HashableValueBoolean value = (HashableValueBoolean)obj;
			return valueEqual(value);
		}
		return false;
	}
	
	public String toString() {
		return value? "+" : "-";
	}
	
	public int hashCode() {
		return value? 0 : 1;
	}

	@Override
	public boolean isPair() {
		return false;
	}

	@Override
	public <T> T getLeft() {
		return null;
	}

	@Override
	public <T> T getRight() {
		return null;
	}

	@Override
	public boolean isAccepting() {
		return value;
	}

}
