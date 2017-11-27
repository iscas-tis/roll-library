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
 * Simple query implementation
 * */
public class QuerySimple<O> implements Query<O> {

	private final Word prefix;
	private final Word suffix;
	private final ObservationRow row;
	private final int columnIndex ;
	private O answer;
	
	public QuerySimple(ObservationRow row, Word prefix, Word suffix, int columnIdx) {
        assert prefix != null && suffix != null;
		this.row = row;
		this.prefix = prefix;
		this.suffix = suffix;
		this.columnIndex = columnIdx;
	}
	
	public QuerySimple(Word prefix, Word suffix) {
		this(null, prefix, suffix, -1);
	}
	
	public QuerySimple(Word prefix) {
		this(null, prefix, prefix.getEmptyWord(), -1);
	}

	@Override
	public void answerQuery(O answer) {
		this.answer = answer;
	}

	@Override
	public O getQueryAnswer() {
		return answer;
	}

	@Override
	public Word getPrefix() {
		return prefix;
	}

	@Override
	public Word getSuffix() {
		return suffix;
	}

	@Override
	public ObservationRow getPrefixRow() {
		return row;
	}

	@Override
	public int getSuffixColumn() {
		return columnIndex;
	}
	
	public String toString() {
		return prefix.toStringWithAlphabet() + ":" + suffix.toStringWithAlphabet();
	}

}
