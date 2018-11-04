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

package roll.query;

import roll.table.ObservationRow;
import roll.words.Word;

/**
 * This is for the interaction with the membership query oracle
 * and equivalence query oracle
 * @O oracle return type 
 * */
/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public interface Query<O> {
	
	
	Word getPrefix();
	
	Word getSuffix();
	
	default Word getQueriedWord() {
		return getPrefix().concat(getSuffix());
	}
	
	void answerQuery(O answer);
	
	O getQueryAnswer();
	
	// only for tables
	ObservationRow getPrefixRow();
	
	int getSuffixColumn();
	
	Query<O> clone();

}
