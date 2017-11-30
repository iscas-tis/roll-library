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

package roll.parser.hoa;

import java.util.BitSet;
import java.util.Iterator;

/**
 * this is an evaluation of APSet
 * */
public class Valuation extends BitSet implements Comparable<Valuation>, Iterable<Integer> {

	/**
	 * should keep the size
	 */
	private static final long serialVersionUID = 1L;
	private int size ;
	
	public Valuation(int size) {
		super(size);
		if(size <= 0) {
			throw new UnsupportedOperationException("valuation size should be positive number");
		}
		this.size = size;  // very important to know the size
	}
	
	@Override
	public int size() {
		return size;
	}
	
	public boolean contains(Valuation other) {
		for(int i = other.nextSetBit(0); i >= 0; i ++) {
			if(! get(i)) return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Valuation) {
			return this.compareTo((Valuation)obj) == 0;
		}
		return false;
	}
	
	@Override
	public Valuation clone() {
		Valuation copy = new Valuation(this.size());
		copy.or(this);
		return copy;
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return new ValuationIterator(this);
	}

	@Override
	public int compareTo(Valuation bits) {
		if(bits.size() > this.size()) return -1;
		if(bits.size() < this.size()) return 1;
		for(int i = 0; i < size(); i ++) {
			if(get(i) && ! bits.get(i)) return 1;
			else if(!get(i) && bits.get(i)) return -1;
		}
		return 0;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		Iterator<Integer> iter = iterator();
		builder.append("{");
		while(iter.hasNext()) {
			builder.append(iter.next());
			if(iter.hasNext()) {
				builder.append(", ");
			}
		}
		builder.append("}");
		return builder.toString();
	}
	
	public String toString(APSet ap) {
		StringBuilder builder = new StringBuilder();

		for(int i = 0; i < size(); i ++) {
			if(! get(i)) {
				builder.append("!");
			}
			builder.append(ap.getAP(i));
			if(i != size() - 1) {
				builder.append("&");
			}
		}
		
		return builder.toString();
	}
	
	/** increase a bit */
	protected void increment() {
		int i = this.nextClearBit(0);
		this.clear(0,i);
		this.set(i);
	}
	
	public int toInt() {
		int n = 0;
		for(int i = 0; i < size(); i ++) {
			n += get(i) ? (1 << i) : 0;
		}
		return n;
	}
	
	@Override
	public int hashCode() {
		return toInt();
	}

	public static class ValuationIterator implements Iterator<Integer> {

		private Valuation valuation;
		private int index;
		
		public ValuationIterator(Valuation val) {
			this.valuation = val;
			index = val.nextSetBit(0);
		}
		
		public boolean hasNext() {
			return (index >= 0);
		}
		
		public Integer next() {
			Integer rv = new Integer(index);
			index = valuation.nextSetBit(index + 1);
			return rv;
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
