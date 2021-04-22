package roll.util.sets;

import java.util.Iterator;

import roll.util.sets.ISet;
import roll.util.sets.ISetBits;

public class PowerSet implements Iterator<ISet> {
	
	private Iterator<ISet> iterator;
	public PowerSet(ISet set) {
		if(set.isEmpty()) {
			iterator = new PowerSetEmpty();
		}else {
			iterator = new PowerSetPositive(set);
		}
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public ISet next() {
		assert hasNext();
		return iterator.next();
	}
	
	
	public static void main(String[] args) {
		ISetBits bits = new ISetBits();
		bits.set(2);
		bits.set(3);
		bits.set(6);
//		bits.set(7);
		bits.set(9);
		System.out.println(bits);
		PowerSet ps = new PowerSet(bits);
		int i = 0;
		while(ps.hasNext()) {
			ISet subset = ps.next();
			i ++;
			System.out.println(" " + subset + ", hashCode=" + subset.hashCode());
		}
		System.out.println("number "+ i);
		
		
		bits.clear();
		System.out.println(bits);
		ps = new PowerSet(bits);
		i = 0;
		while(ps.hasNext()) {
			ISet subset = ps.next();
			i ++;
			System.out.println(" " + subset + ", hashCode=" + subset.hashCode());
		}
		System.out.println("number "+ i);
	}

}
