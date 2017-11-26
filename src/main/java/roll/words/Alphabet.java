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

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public final class Alphabet {
	
	private final Word epsilon;
	private final LetterList letterList;
	private final Class<?> clazz; // the class type of input letters
	
	public Alphabet(Class<?> clazz) {
		assert clazz != null;
		this.clazz = clazz;
		this.epsilon = new WordEmpty(this);
		this.letterList = new LetterListSimple();
	}
	
	public LetterList getLetters() {
		return letterList;
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
	
	public void addLetter(Object obj) {
		assert clazz.isInstance(obj);
		assert !letterList.contains(obj);
		letterList.add(obj);
	}
	
	public Word getWordFromString(String string, String splitter) {
		assert string != null ;
		String[] wordStr = string.split(splitter);
		int[] word = new int[wordStr.length];
		for(int index = 0; index < wordStr.length; index ++) {
			int letter = letterList.indexOf(wordStr[index]);
			if(letter == -1) return null;
			word[index] = letter;
		}
		
		return getArrayWord(word);
	}


}
