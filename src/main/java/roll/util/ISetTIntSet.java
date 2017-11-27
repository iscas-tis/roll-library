/*
 * Written by Yong Li (liyong@ios.ac.cn)
 * This file is part of the Buchi.
 * 
 * Buchi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Buchi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Buchi. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package roll.util;

import java.util.Iterator;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class ISetTIntSet implements ISet {
	
	private final TIntSet mSet;
	
	public ISetTIntSet() {
		mSet = new TIntHashSet();
	}
	
	public ISetTIntSet(TIntSet set) {
		mSet = set;
	}

	@Override
	public ISet clone() {
		ISetTIntSet copy = new ISetTIntSet();
		copy.mSet.addAll(mSet);
		return copy;
	}

	@Override
	public void andNot(ISet set) {
		if(! (set instanceof ISetTIntSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TIntSet");
		}
		ISetTIntSet temp = (ISetTIntSet)set;
		this.mSet.removeAll(temp.mSet);
	}

	@Override
	public void and(ISet set) {
		if(! (set instanceof ISetTIntSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TIntSet");
		}
		ISetTIntSet temp = (ISetTIntSet)set;
		this.mSet.retainAll(temp.mSet);
	}

	@Override
	public void or(ISet set) {
		if(! (set instanceof ISetTIntSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TIntSet");
		}
		ISetTIntSet temp = (ISetTIntSet)set;
		this.mSet.addAll(temp.mSet);
	}

	@Override
	public boolean get(int value) {
		return mSet.contains(value);
	}
	
	@Override
	public void set(int value) {
		mSet.add(value);
	}

	@Override
	public void clear(int value) {
		mSet.remove(value);
	}
	
	@Override
	public void clear() {
		mSet.clear();
	}
	
	@Override
	public String toString() {
		return mSet.toString();
	}

	@Override
	public boolean isEmpty() {
		return mSet.isEmpty();
	}

	@Override
	public int cardinality() {
		return mSet.size();
	}

	@Override
	public boolean subsetOf(ISet set) {
		if(! (set instanceof ISetTIntSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TIntSet");
		}
		ISetTIntSet temp = (ISetTIntSet)set;
		return temp.mSet.containsAll(this.mSet);
	}

	@Override
	public boolean contentEq(ISet set) {
		if(! (set instanceof ISetTIntSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TIntSet");
		}
		ISetTIntSet temp = (ISetTIntSet)set;
		return this.mSet.equals(temp.mSet);
	}

	@Override
	public Object get() {
		return mSet;
	}
	
	public boolean equals(Object obj) {
		if(! (obj instanceof ISetTIntSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TIntSet");
		}
		ISetTIntSet temp = (ISetTIntSet)obj;
		return this.contentEq(temp);
	}
	
	private static class TIntSetIterator implements Iterator<Integer> {
        
	    private final TIntIterator iter;
	    TIntSetIterator(TIntSet set) {
	        iter = set.iterator();
	    }
	    
        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Integer next() {
            return iter.next();
        }
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return new TIntSetIterator(mSet);
	}

}
