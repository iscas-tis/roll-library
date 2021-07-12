package roll.main.inclusion.congr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roll.util.Pair;
import roll.util.sets.ISet;
import roll.words.Word;

public class CongruenceClassProfile {
	/**
	 * left most are successors which visit final states earlier/maximal equivalence class
	 */
	protected final ArrayList<ISet> orderedSets;
	/**
	 * the set of 1-colored sets which are waiting to be moved to breakpoint
	 */
	protected final ArrayList<Pair<Integer, Boolean>> maxPres;
	
	/**
	 * representative word of this class
	 */
	protected Word representative;

	/**
	 * flag to indicate whether the state is in the accepting component
	 */
	protected ArrayList<ISet> prevOrderedSets = null;

	public CongruenceClassProfile() {
		this.orderedSets = new ArrayList<>();
		this.maxPres = new ArrayList<>();
	}
	
	public void setPrevOrderedSets(ArrayList<ISet> prev) {
		this.prevOrderedSets = prev;
	}

	public int addSet(ISet oset) {
		int index = orderedSets.size();
		orderedSets.add(oset);
		return index;
	}
	
	public void setWord(Word word) {
		this.representative = word;
	}

	public void setMaxPres(int index, boolean b) {
		maxPres.add(new Pair<>(index, b));
	}
	
	public Word getWord() {
		return this.representative;
	}

	public List<ISet> getOrderedSets() {
		return Collections.unmodifiableList(this.orderedSets);
	}

	public boolean isProgress() {
		return this.prevOrderedSets != null;
	}

	public ISet getSet(int index) {
		assert index < orderedSets.size();
		return this.orderedSets.get(index);
	}
	
	public int getSetSize() {
		return this.orderedSets.size();
	}
	
	public boolean isEmpty() {
		return this.orderedSets.isEmpty();
	}
	
	public Pair<Integer, Boolean> getMaxPres(int index) {
		assert index < maxPres.size();
		return this.maxPres.get(index);
	}
	
	public Pair<ISet, Boolean> getPrevSet(int index) {
		assert index < maxPres.size();
		Pair<Integer, Boolean> pair = this.maxPres.get(index);
		return new Pair<>(this.prevOrderedSets.get(pair.getLeft()), pair.getRight());
	}
	
	public CongruenceClassProfile getProgress(ISet finals) {
		CongruenceClassProfile progress = new CongruenceClassProfile();
		for(int i = 0; i < this.getSetSize(); i ++) {
			ISet set = this.getSet(i);
			progress.addSet(set);
			progress.setMaxPres(i, set.overlap(finals));
		}
		progress.setPrevOrderedSets(progress.orderedSets);
		return progress;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof CongruenceClassProfile)) {
			return false;
		}
		CongruenceClassProfile otherClass = (CongruenceClassProfile) other;
		if (this.prevOrderedSets == null && otherClass.prevOrderedSets != null 
			|| this.prevOrderedSets != null && otherClass.prevOrderedSets == null) {
			return false;
		}
		if (this.prevOrderedSets != null && otherClass.prevOrderedSets != null 
			&& !this.prevOrderedSets.equals(otherClass.prevOrderedSets)) {
			return false;
		}
		return this.maxPres.equals(otherClass.maxPres) && this.orderedSets.equals(otherClass.orderedSets);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + (prevOrderedSets != null ? 1 : 0);
		for (int i = 0; i < orderedSets.size(); i++) {
			result = prime * result + orderedSets.get(i).hashCode();
		}
		for (int i = 0; i < maxPres.size(); i++) {
			result = prime * result + maxPres.get(i).hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (prevOrderedSets != null) {
			builder.append("<" + orderedSets + ", " + maxPres + ", " + prevOrderedSets + ">");
		} else {
			builder.append(orderedSets + "");
		}
		return builder.toString();
	}

}

