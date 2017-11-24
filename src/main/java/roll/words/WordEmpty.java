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

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
class WordEmpty extends WordAbstract {
	
	WordEmpty(Alphabet alphabet) {
		super(alphabet);
	}

	public String toString() {
		return "ϵ";
	}

	@Override
	public Iterator<Integer> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public Word getEmptyWord() {
		return this;
	}

	@Override
	public int getLetter(int index) {
		return -1;
	}

	@Override
	public int length() {
		return 0;
	}

	@Override
	public Word getSubWord(int fromIdx, int length) {
		assert length == 0;
		return this;
	}

	@Override
	public Word getPrefix(int prefixLength) {
		return this;
	}

	@Override
	public Word getSuffix(int startIndex) {
		return this;
	}

	@Override
	public Word append(int letter) {
		return new WordLetter(alphabet, letter);
	}

	@Override
	public Word preappend(int letter) {
		return new WordLetter(alphabet, letter);
	}

	@Override
	public boolean isPrefixOf(Word letter) {
		return true;
	}

	@Override
	public boolean isSuffixOf(Word letter) {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public String toStringWithAlphabet() {
		return toString();
	}

	@Override
	public void write(int offsetWord, int[] letters, int offsetArr, int length) {
		assert length == 0 : "empty word write";
	}


}
