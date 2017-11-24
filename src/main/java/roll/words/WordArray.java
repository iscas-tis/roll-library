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
import java.util.List;

// begin at offset 
/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
class WordArray extends WordAbstract {

	private final int[] elementData;
	private int offset;
	private int length; // exclusive
	
	
	WordArray(Alphabet context, int[] data) {
		this(context, data, 0, data.length);
	}
	
	WordArray(Alphabet context, int[] data, int length) {
		this(context, data, 0, length);
	}
	
	WordArray(Alphabet context, int[] data, int offset, int length) {
		super(context);
		assert data != null;
		this.elementData = data;
		this.offset = offset;
		this.length = length;
	}
	
	WordArray(Alphabet context, List<Integer> word) {
		super(context);
		assert word != null;
		this.elementData = new int[word.size()];
		this.offset = 0;
		this.length = word.size();
		for(int letterNr = 0; letterNr < length; letterNr ++) {
			this.elementData[letterNr] = word.get(letterNr);
		}
	}

	@Override
	public boolean isEmpty() {
		return length == 0;
	}
	
	// write letters starting from offsetArr with length len
	// so offsetWord is relative with respect to offset
	public void write(int offsetWord, int[] letters, int offsetArr, int len) {
		assert offsetWord >= 0 && offsetWord < length;
		assert letters.length >= offsetArr + len;
		int size = len + offsetArr; // end
		for(int index = offsetWord + offset, indexArr = offsetArr;
				indexArr < size; index ++, indexArr ++) {
			letters[indexArr] = elementData[index];
		}
	}

	@Override
	public int getLetter(int index) {
		assert index < length : index + " : " + length;
		assert offset + index < elementData.length : offset + " : " + index + ": " + elementData.length;
		return elementData[offset + index];
	}

	@Override
	public Word getSubWord(int fromIdx, int length) {
		assert fromIdx >= 0 && length <= this.length;
		if(length == 0 || fromIdx >= this.length) return alphabet.getEmptyWord();
		if(length == 1) return alphabet.getLetterWord(getLetter(fromIdx));
		return new WordArray(alphabet, elementData, fromIdx + offset, length);
	}

	@Override
	public Word getPrefix(int prefixLength) {
		assert prefixLength <= length;
		if(prefixLength <= 0) return alphabet.getEmptyWord();
		if(prefixLength == 1) return alphabet.getLetterWord(getLetter(0));
		return new WordArray(alphabet, elementData, offset, prefixLength);
	}

	@Override
	public Word getSuffix(int startIndex) {
		assert startIndex >= 0;
		assert startIndex <= length;
		if(startIndex >= length) return alphabet.getEmptyWord();
		if(startIndex == length - 1) return alphabet.getLetterWord(getLetter(startIndex));
		return new WordArray(alphabet, elementData, startIndex + offset, length - startIndex);
	}

	@Override
	public Word append(int letter) {
		int newLength = length + 1; 
		int candidateIndex = offset + length; 
		if(candidateIndex < elementData.length && elementData[candidateIndex] == letter) { 
			return new WordArray(alphabet, elementData, offset, newLength);
		}
		// get new array
		int[] newElementData = new int[newLength];
		write(0, newElementData, 0, length);
		newElementData[length] = letter;
		return new WordArray(alphabet, newElementData);
	}

	@Override
	public Word preappend(int letter) {
		int candidateIndex = offset - 1; 
		int newLength = length + 1;
		if(candidateIndex >= 0 && elementData[candidateIndex] == letter) { 
			return new WordArray(alphabet, elementData, candidateIndex, newLength);
		}
		// get new array
		int[] newElementData = new int[newLength];
		write(0, newElementData, 1, length);
		newElementData[0] = letter;
		return new WordArray(alphabet, newElementData);
	}

	@Override
	public int getFirstLetter() {
		return elementData[offset];
	}

	@Override
	public int getLastLetter() {
		return elementData[offset + length - 1];
	}

	@Override
	public boolean isPrefixOf(Word word) {
		if(length > word.length()) return false;
		for(int letterNr = 0; letterNr < length ; letterNr ++) {
			if(getLetter(letterNr) != word.getLetter(letterNr))
				return false;
		}
		return true;
	}

	@Override
	public boolean isSuffixOf(Word word) {
		if(length > word.length()) return false;
		int otherNr = word.length() - 1;
		for(int letterNr = length - 1; letterNr >= 0 ; letterNr --, otherNr --) {
			if(getLetter(letterNr) != word.getLetter(otherNr))
				return false;
		}
		return true;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new ListItr();
	}

	@Override
	public int length() {
		return length;
	}
	
    private class ListItr implements Iterator<Integer> {

    	private int cursor = 0;
		@Override
		public boolean hasNext() {
			return cursor != length;
		}

		@Override
		public Integer next() {
			int i = cursor;
			if(i >= length)
				try {
					throw new Exception("No such element");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			++ cursor; 
			return getLetter(i);
		}

		@Override
		public void remove() {
			try {
				throw new Exception("Can not remove letters");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    public String toString() {
    	StringBuilder builder = new StringBuilder();
    	builder.append(getLetter(0));
    	for(int letterNr = 1; letterNr < length; letterNr ++) {
    		builder.append("." + getLetter(letterNr));
    	}
    	return builder.toString();
    }
}
