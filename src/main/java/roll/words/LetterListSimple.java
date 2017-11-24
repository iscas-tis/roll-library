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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
class LetterListSimple implements LetterList {

	private List<Object> letters = new ArrayList<>();
	private Map<Object, Integer> letterToId = new HashMap<>();
	private boolean isImmutable;
	
	LetterListSimple() {
		this.isImmutable = false;
	}
	
	@Override
	public boolean isEmpty() {
		return letters.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return letterToId.containsKey(o);
	}

	@Override
	public Iterator<Object> iterator() {
		return letters.iterator();
	}

	@Override
	public Object[] toArray() {
		return letters.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return letters.toArray(a);
	}

	@Override
	public boolean add(Object e) {
		assert !isImmutable();
		if(letterToId.containsKey(e)) {
			return false;
		}
		int size = size();
		letters.add(e);
		letterToId.put(e, size);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		assert false : "do not allow remove elements";
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return letterToId.keySet().containsAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = letters.removeAll(c);
		letterToId.keySet().removeAll(c);
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = letters.retainAll(c);
		letterToId.keySet().retainAll(c);
		return result;
	}

	@Override
	public void clear() {
		letters.clear();
		letterToId.clear();
	}

	@Override
	public int compare(Object o1, Object o2) {
		assert indexOf(o1) >=0 && indexOf(o2) >= 0;
		return indexOf(o1) - indexOf(o2);
	}

	@Override
	public int size() {
		return letters.size();
	}

	@Override
	public Object get(int index) {
		assert index < size() : index + ", " + size();
		return letters.get(index);
	}

	@Override
	public int indexOf(Object letter) {
		Integer id = letterToId.get(letter);
		if(id == null) return -1;
		return id;
	}

	@Override
	public void setImmutable() {
		this.isImmutable = true;
	}

	@Override
	public boolean isImmutable() {
		return isImmutable;
	}
	
	@Override
	public boolean addAll(Collection<? extends Object> c) {
		assert !isImmutable();
		boolean result = false;
		for(Object letter : c) {
			result = result || add(letter);
		}
		return false;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(letters.isEmpty()) return "[]";
		builder.append("[" + 0 + "=" + letters.get(0));
		for(int letterNr = 1; letterNr < letters.size() ; letterNr ++) {
			builder.append(", " + letterNr + "=" + letters.get(letterNr));
		}
		builder.append("]");
		return builder.toString();
	}

}
