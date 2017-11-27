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

public class HashableValueInt implements HashableValue {

	private final int value;
	
	public HashableValueInt(int value) {
		this.value = value;
	}
	
	@Override
	public boolean valueEqual(HashableValue rvalue) {
		// TODO Auto-generated method stub
		Integer rValue = rvalue.get(); 
		return value == rValue.intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof HashableValueInt) {
			HashableValueInt val = (HashableValueInt)obj;
			return val.value == value;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Integer get() {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public boolean isPair() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T getLeft() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getRight() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAccepting() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString() {
		return "" + value;
	}
	
	@Override
	public int hashCode() {
		return value;
	}

}
