package roll.main.inclusion.congr;

import roll.util.sets.ISet;
import roll.words.Word;

public class PrefixEquivClass {
	
	protected ISet setOfBStates;
	
	protected Word repWord;
	
	public PrefixEquivClass(ISet set, Word word) {
		this.setOfBStates = set;
		this.repWord = word;
	}
	
	
	@Override
	public int hashCode() {
		assert setOfBStates != null;
		return setOfBStates.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PrefixEquivClass) {
			PrefixEquivClass other = (PrefixEquivClass)obj;
			return this.setOfBStates.equals(other.setOfBStates);
		}
		return false;
	}
	
	public boolean subsetOf(PrefixEquivClass other) {
		return this.setOfBStates.subsetOf(other.setOfBStates);
	}
	
}
