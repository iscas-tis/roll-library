package roll.util.sets;

import java.util.BitSet;

/**
 * This is a helper class for enumerate all possible subsets in a given full BitSet
 * */
class EnumeratorBitSet extends BitSet implements Comparable<EnumeratorBitSet>{

	private static final long serialVersionUID = 1L;
	/**
	 * should keep the size
	 */
	private int size ;
	
	public EnumeratorBitSet(int size) {
		super(size);
		if(size <= 0) {
			throw new RuntimeException("Input size should be positive number");
		}
		this.size = size;  // very important to know the size
	}
	
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null || !(obj instanceof EnumeratorBitSet)) {
			return false;
		}
		EnumeratorBitSet other = (EnumeratorBitSet)obj;
		return this.compareTo(other) == 0;
	}
	
	
	@Override
	public EnumeratorBitSet clone() {
		EnumeratorBitSet copy = new EnumeratorBitSet(this.size());
		copy.or(this);
		return copy;
	}

	@Override
	public int compareTo(EnumeratorBitSet bits) {
		if(bits.size() > this.size()) return -1;
		if(bits.size() < this.size()) return 1;
		for(int i = 0; i < size(); i ++) {
			if(get(i) && ! bits.get(i)) return 1;
			else if(!get(i) && bits.get(i)) return -1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
	/** increase a bit */
	protected void nextBitSet() {
		int i = this.nextClearBit(0);
		this.clear(0,i);
		this.set(i);
	}
	
	// since this is modifiable bitset, we donot supprt hashCode
	@Override
	public int hashCode() {
		throw new RuntimeException("EnumeratorBitSet doesnot support hashCode");
	}
}
