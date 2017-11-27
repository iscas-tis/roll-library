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
import java.util.TreeSet;


public class ISetTreeSet implements ISet {
	
	private final TreeSet<Integer> mSet;
	
	public ISetTreeSet() {
		mSet = new TreeSet<>();
	}

	@Override
	public ISet clone() {
		ISetTreeSet copy = new ISetTreeSet();
		copy.mSet.addAll(mSet);
		return copy;
	}

	@Override
	public void andNot(ISet set) {
		if(! (set instanceof ISetTreeSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TreeSet");
		}
		ISetTreeSet temp = (ISetTreeSet)set;
		this.mSet.removeAll(temp.mSet);
	}

	@Override
	public void and(ISet set) {
		if(! (set instanceof ISetTreeSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TreeSet");
		}
		ISetTreeSet temp = (ISetTreeSet)set;
		this.mSet.retainAll(temp.mSet);
	}

	@Override
	public void or(ISet set) {
		if(! (set instanceof ISetTreeSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TreeSet");
		}
		ISetTreeSet temp = (ISetTreeSet)set;
		this.mSet.addAll(temp.mSet);
	}

	@Override
	public boolean get(int value) {
		return mSet.contains(value);
	}

	@Override
	public void clear(int value) {
		mSet.remove(value);
	}
	
	@Override
	public String toString() {
		return mSet.toString();
	}
	
	@Override
	public void clear() {
		mSet.clear();
	}
	
	@Override
	public void set(int value) {
		mSet.add(value);
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
		if(! (set instanceof ISetTreeSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TreeSet");
		}
		ISetTreeSet temp = (ISetTreeSet)set;
		return temp.mSet.containsAll(this.mSet);
	}

	@Override
	public boolean contentEq(ISet set) {
		if(! (set instanceof ISetTreeSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TreeSet");
		}
		ISetTreeSet temp = (ISetTreeSet)set;
		return this.mSet.equals(temp.mSet);
	}

	@Override
	public Object get() {
		return mSet;
	}
	
	public boolean equals(Object obj) {
		if(! (obj instanceof ISetTreeSet)) {
		    throw new UnsupportedOperationException("OPERAND should be TreeSet");
		}
		ISetTreeSet temp = (ISetTreeSet)obj;
		return this.contentEq(temp);
	}

    @Override
    public Iterator<Integer> iterator() {
        return mSet.iterator();
    }
}
