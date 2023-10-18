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
public class HashableValueImplBoolPair extends HashableValueBooleanPair implements HashableValue {
		
	public HashableValueImplBoolPair(boolean left, boolean right) {
		super(left, right);
	}
	
	// specialized for limit FDFAs
	@Override
	public boolean isAccepting() {
		return !valueLeft || valueRight ;
	}
	
	@Override
	public boolean valueEqual(HashableValue rvalue) {
		return this.isAccepting() == rvalue.isAccepting();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof HashableValueImplBoolPair) {
			HashableValueImplBoolPair pair = (HashableValueImplBoolPair)obj;
			return valueEqual(pair);
		}
		return false;
	}

}
