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

import java.util.List;

import roll.words.Word;


/**
 * A row consists of its strings, namely its labels 
 * and corresponding row values
 * 
 * @author Yong Li (liyong@ios.ac.cn)
*/
// maybe later replace O with Value type
public interface ObservationRow {

	Word getWord();
	
	// can not modified copy of values
	List<HashableValue> getValues();
	
	// do not know whether it is necessary
	boolean equals(Object obj);
		
	void add(HashableValue value);
	
	default boolean valuesEqual(ObservationRow other) {
		List<HashableValue> thisValues = getValues();
		List<HashableValue> otherValues = other.getValues();
		assert thisValues.size() == otherValues.size();
		for(int valNr = 0; valNr < thisValues.size(); valNr ++) {
			if(! thisValues.get(valNr).valueEqual(otherValues.get(valNr))) {
				return false;
			}
		}
		return true;
	}
	
	void set(int index, HashableValue value);
	
	void clear();
	
}
