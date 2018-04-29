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

package roll.automata;

import java.util.List;

import roll.main.IHTML;
import roll.words.Alphabet;

/**
 * Acceptor for regular (omega) language 
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public interface Acceptor extends IHTML {
    
    Alphabet getAlphabet();
	
	AccType getAccType();
	
	Acc getAcc(); // acceptance condition
	
	default FDFA asFDFA() {
	    assert this instanceof FDFA;
		return (FDFA)this;
	}
	
	default DFA asDFA() {
	    assert this instanceof DFA;
		return (DFA)this;
	}
	
	default NFA asNFA() {
	    assert this instanceof NFA;
		return (NFA)this;
	}
	
	default NBA asNBA() {
	    assert this instanceof NBA;
        return (NBA)this;
    }
	// and so on
	String toString(List<String> apList);
}
