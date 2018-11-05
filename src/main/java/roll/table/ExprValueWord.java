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

import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class ExprValueWord implements ExprValue {
	private Word word;
	
	public ExprValueWord(Word col) {
		word = col;
	}
	
	@Override
	public boolean valueEqual(ExprValue rvalue) {
		return word.equals(rvalue.get());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Word get() {
		return word;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ExprValueWord) {
			ExprValueWord col = (ExprValueWord)obj;
			return valueEqual(col);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return word.toStringWithAlphabet();
	}
	
	@Override
	public boolean isPair() {
		return false;
	}
	
	@Override
	public <T> T getLeft() {
		return null;
	}
	
	@Override
	public <T> T getRight() {
		return null;
	}
	
	@Override
	public int hashCode() {
		return word.hashCode();
	}

    @Override
    public int compareTo(ExprValue arg) {
        assert arg instanceof ExprValueWord;
        ExprValueWord other = (ExprValueWord)arg;
        return word.compareTo(other.word);
    }
}
