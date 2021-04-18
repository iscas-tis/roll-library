package roll.util.sets;

import java.util.Iterator;

import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

class PowerSetEmpty implements Iterator<ISet> {
	
	private boolean hasNext;
	
	public PowerSetEmpty() {
		hasNext = true;
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public ISet next() {
		assert hasNext();
		hasNext = false;
		return UtilISet.newISet();
	}

}
