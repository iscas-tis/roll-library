/* Copyright (c) 2018 -                                                   */
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

package roll.automata;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public enum AutType {

	DFA,
	NFA,
	FDFA,
	FNFA,
	NBA,
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
	
	public boolean isFRFSA() {
	    return this == FNFA;
	}
	
	public boolean isNBA() {
		return this == NBA;
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
		}else if(this == FNFA) {
            return "FNFA";
        }if(this == NBA) {
			return "NBA";
		}else if(this == RABIN) {
			return "RABIN";
		}else if(this == STREET) {
				return "STREET";
		}else {
			return "PARITY";
		}
	}
}
