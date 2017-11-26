/* Written by Yong Li, Depeng Liu                                       */
/* Copyright (c) 2016                  	                               */
/* This program is free software: you can redistribute it and/or modify */
/* it under the terms of the GNU General Public License as published by */
/* the Free Software Foundation, either version 3 of the License, or    */
/* (at your option) any later version.                                  */

/* This program is distributed in the hope that it will be useful,      */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of       */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        */
/* GNU General Public License for more details.                         */

/* You should have received a copy of the GNU General Public License    */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package roll.automata;

public enum AccType {

	DFA,
	NFA,
	FDFA,
	BUECHI,
	RABIN,
	STREET,
	PARITY;
	
	public boolean isDFA() {
		return this == DFA;
	}
	
	public boolean isNFA() {
		return this == NFA;
	}
	
	public boolean isFDFA() {
		return this == FDFA;
	}
	
	public boolean isBuechi() {
		return this == BUECHI;
	}
	
	public boolean isRabin() {
		return this == RABIN;
	}
	
	public boolean isStreet() {
		return this == STREET;
	}
	
	public boolean isParity() {
		return this == PARITY;
	}
	
	
	public String toString() {
		if(this == DFA) {
			return "DFA";
		}else if(this == NFA) {
			return "NFA";
		}else if(this == FDFA) {
			return "FDFA";
		}if(this == BUECHI) {
			return "BUECHI";
		}else if(this == RABIN) {
			return "RABIN";
		}else if(this == STREET) {
				return "STREET";
		}else {
			return "PARITY";
		}
	}
}
