package roll.util.sets;

import java.util.Iterator;

import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

class PowerSetPositive implements Iterator<ISet> {

	private EnumeratorBitSet enumerator;
	
	private final int[] intArr;
	
	public PowerSetPositive(ISet set) {
		assert ! set.isEmpty();
		intArr = new int[set.cardinality()];
		int index = 0;
		for(int elem : set) {
			intArr[index ++] = elem;
		}
		this.enumerator = new EnumeratorBitSet(set.cardinality());
	}

	@Override
	public boolean hasNext() {
		int index = enumerator.nextSetBit(0); // whether we have got out of the array
		return index < enumerator.size();
	}

	@Override
	public ISet next() {
		assert hasNext();
		EnumeratorBitSet val = enumerator.clone();
		enumerator.nextBitSet();
		ISet bits = UtilISet.newISet();
		for(int n = val.nextSetBit(0); n >= 0 ; n = val.nextSetBit(n + 1)) {
			bits.set(intArr[n]);
		}
		return bits;
	}

}
