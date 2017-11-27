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

package roll.learner.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
// can not be instantiated
public abstract class ObservationRowAbstract implements ObservationRow {

	protected final Word word;
	protected List<HashableValue> values;
	
	protected ObservationRowAbstract(Word word) {
		assert word != null;
		this.word = word;
		this.values = new ArrayList<>();
	}
	
	@Override
	public Word getWord() {
		return word;
	}

	@Override
	public List<HashableValue> getValues() {
		return Collections.unmodifiableList(values);
	}
	
	public String toString() {
		return word.toString();
	}
	
	public void add(HashableValue value) {
		values.add(value);
	}
	
	public void set(int index, HashableValue value) {
		assert index >= 0;
		while(values.size() <= index) {
			values.add(null);
		}
		values.set(index, value);
	}

	public void clear() {
		values.clear();
	}

}
