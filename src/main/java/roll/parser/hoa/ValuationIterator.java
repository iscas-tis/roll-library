package roll.parser.hoa;

import java.util.Iterator;

public class ValuationIterator implements Iterator<Valuation> {
	
	private Valuation valuation;
	
	public ValuationIterator(int n) {
		this.valuation = new Valuation(n);
	}

	@Override
	public boolean hasNext() {
		int index = valuation.nextSetBit(0); // whether we have got out of the array
		return index < valuation.size();
	}

	@Override
	public Valuation next() {
		assert hasNext();
		Valuation val = valuation.clone();
		valuation.increment();
		return val;
	}

}
