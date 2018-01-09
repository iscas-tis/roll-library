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

package roll.util;

public class Pair<L, R> {
	
	private final L left;
	private final R right;
	
	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}
	
	public L getLeft() {
		return this.left;
	}
	
	public R getRight() {
		return this.right;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if(this == obj) return true;
	    if(obj == null) return false;
		if(obj instanceof Pair<?, ?>) {
			@SuppressWarnings("unchecked")
			Pair<L, R> p = (Pair<L, R>)obj;
			return p.left.equals(left) 
				&& p.right.equals(right); 
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "(" + left.toString() + ", " + right.toString() + ")";
	}

}
