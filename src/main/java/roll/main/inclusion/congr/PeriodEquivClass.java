package roll.main.inclusion.congr;

import java.util.TreeSet;

import roll.words.Word;

public class PeriodEquivClass {
	
	protected TreeSet<IntBoolTriple> setOfArcs;
	protected Word repWord;
	
	public PeriodEquivClass() {
		this.setOfArcs = new TreeSet<>();
		this.repWord = null;
	}
	
	public void addTriple(IntBoolTriple triple) {
		IntBoolTriple revTriple = new IntBoolTriple(triple.left, triple.right, !triple.acc);
		boolean containedRev = this.setOfArcs.contains(revTriple);
		if(containedRev) {
			if(triple.acc) {
				this.setOfArcs.remove(revTriple);
				this.setOfArcs.add(triple);
			}else {
				// do nothing, keep the original one
			}
		}else {
			this.setOfArcs.add(triple);
		}
	}
	
	
	@Override
	public int hashCode() {
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PeriodEquivClass) {
			PeriodEquivClass other = (PeriodEquivClass)obj;
			return this.setOfArcs.equals(other.setOfArcs);
		}
		return false;
	}
	
	public boolean subsetOf(PeriodEquivClass other) {
		return false;
	}
	

}
