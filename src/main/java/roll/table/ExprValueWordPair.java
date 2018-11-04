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
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * specialized for the leading DFA learning 
 * */

public class ExprValueWordPair implements ExprValue {
	
	private final Word wordLeft;
	private final Word wordRight;
	
	public ExprValueWordPair(Word left, Word right) {
		this.wordLeft = left;
		this.wordRight = right;
	}
	
	@Override
	public boolean valueEqual(ExprValue rvalue) {
		ExprValueWordPair pvalue = (ExprValueWordPair)rvalue;
		return wordLeft.equals(pvalue.wordLeft)
			&& wordRight.equals(pvalue.wordRight);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Pair<Word, Word> get() {
		return new Pair<Word, Word>(wordLeft, wordRight);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ExprValueWordPair) {
			ExprValueWordPair pair = (ExprValueWordPair)obj;
			return valueEqual(pair);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "(" + wordLeft.toStringWithAlphabet() 
		+ ", " + wordRight.toStringWithAlphabet() + ")";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean isPair() {
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Word getLeft() {
		return wordLeft;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Word getRight() {
		return wordRight;
	}

}
