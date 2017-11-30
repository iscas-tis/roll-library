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

import roll.util.Pair;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Usually we just use Class Character
 * */
public final class Alphabet {
	
	private final Word epsilon;
	private final LetterList letterList;
	public static final Character DOLLAR = '$';
	
	public Alphabet() {
		this.epsilon = new WordEmpty(this);
		this.letterList = new LetterListSimple();
	}
	
	public LetterList getLetters() {
		return letterList;
	}
	
	public Character getLetter(int index) {
	    return letterList.get(index);
	}
	
	public int indexOf(Character letter) {
	    return letterList.indexOf(letter);
	}
	
	public int getLetterSize() {
		return letterList.size();
	}
	
	public Word getEmptyWord() {
		return epsilon;
	}
	
	public Word getLetterWord(int letter) {
		return new WordLetter(this, letter);
	}
	
	public Word getArrayWord(int ... word) {
		return new WordArray(this, word);
	}
	
	public void addLetter(Character obj) {
		assert !letterList.contains(obj);
		assert !obj.equals(DOLLAR);
		letterList.add(obj);
	}
	
	public Word getWordFromString(String wordStr) {
		assert wordStr != null ;
		if(wordStr.isEmpty()) {
		    return epsilon;
		}
		if(wordStr.length() == 1) {
		    int letter = indexOf(wordStr.charAt(0));
		    return new WordLetter(this, letter);
		}
		int[] word = new int[wordStr.length()];
		for(int index = 0; index < wordStr.length(); index ++) {
			int letter = indexOf(wordStr.charAt(index));
			if(letter == -1) return null;
			word[index] = letter;
		}
		return getArrayWord(word);
	}
	
	public Pair<Word, Word> getWordPairFromString(String wordStr) {
        int index = wordStr.indexOf(DOLLAR);
        assert index != -1;
        String prefixStr = wordStr.substring(0, index);
        String suffixStr = wordStr.substring(index + 1);
        Word prefix = getWordFromString(prefixStr);
        Word suffix = getWordFromString(suffixStr);
        assert prefix != null  && suffix != null;
        return new Pair<>(prefix, suffix);
	}


}
