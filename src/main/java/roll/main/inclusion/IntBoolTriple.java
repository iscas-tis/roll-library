/* Copyright (c) since 2016                                               */
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

package roll.main.inclusion;

// In RABIT, this structure is called Arc
public class IntBoolTriple implements Comparable<IntBoolTriple> {
	
	protected int left;
	protected int right;
	protected boolean acc;
	
	public IntBoolTriple(int left, int right, boolean acc) {
		this.left = left;
		this.right = right;
		this.acc = acc;
	}
	
	public int getLeft() {
		return this.left;
	}
	
	public int getRight() {
		return this.right;
	}
	
	public boolean getBool() {
		return this.acc;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if(this == obj) return true;
	    if(obj == null) return false;
		if(obj instanceof IntBoolTriple) {
			@SuppressWarnings("unchecked")
			IntBoolTriple p = (IntBoolTriple)obj;
			return p.left == left 
				&& p.right == right
				&& p.acc == acc; 
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "(" + left + ", " + right + ": "+ acc +")";
	}

	@Override
	public int compareTo(IntBoolTriple other) {
		if(this.left != other.left) {
			return this.left - other.left;
		}
		assert (this.left == other.left);
		if(this.right != other.right) {
			return this.right - other.right;
		}
		assert (this.right == other.right);
		int lBool = this.acc ? 1 : 0;
		int rBool = other.acc ? 1 : 0;
		return lBool - rBool;
	}

}

