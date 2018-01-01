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

package roll.words;

import java.util.Iterator;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
class WordLetter extends WordAbstract {

	private final int letter ;
	
	WordLetter(Alphabet alphabet, int letter) {
		super(alphabet);
		this.letter = letter;
	}
	
	private class ListItr implements Iterator<Integer> {

		private int cursor = 0;
		@Override
		public boolean hasNext() {
			return cursor != 1;
		}

		@Override
		public Integer next() {
			++ cursor; 
			return letter;
		}
		
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return new ListItr();
	}

	@Override
	public int getLetter(int index) {
		assert index == 0;
		return letter;
	}

	@Override
	public int length() {
		return 1;
	}

	@Override
	public Word getSubWord(int fromIdx, int length) {
		assert fromIdx <= 1;
		if(length <= 0) return getEmptyWord();
		return this;
	}

	@Override
	public Word getPrefix(int prefixLength) {
		assert prefixLength <= 1;
		if(prefixLength <= 0) return getEmptyWord();
		return this;
	}

	@Override
	public Word getSuffix(int startIndex) {
		assert startIndex <= 1 : "word: " + letter + " index: " + startIndex;
		if(startIndex >= 1) return getEmptyWord();
		return this;
	}

	@Override
	public Word append(int letter) {
		int[] data = new int[2];
		data[0] = this.letter;
		data[1] = letter;
		return new WordArray(alphabet, data);
	}

	@Override
	public Word preappend(int letter) {
		int[] data = new int[2];
		data[0] = letter;
		data[1] = this.letter;
		return new WordArray(alphabet, data);
	}

	@Override
	public int getFirstLetter() {
		return letter;
	}

	@Override
	public int getLastLetter() {
		return letter;
	}

	@Override
	public boolean isPrefixOf(Word word) {
		return word.getFirstLetter() == letter;
	}

	@Override
	public boolean isSuffixOf(Word word) {
		return word.getLastLetter() == letter;
	}

	@Override
	public void write(int offsetWord, int[] letters, int offsetArr, int length) {
		assert length <= 1 && offsetWord == 0;
		assert offsetArr < letters.length;
		if(length == 0) return ;
		letters[offsetArr] = letter;
	}
	
	public String toString() {
		return letter + "";
	}

}
