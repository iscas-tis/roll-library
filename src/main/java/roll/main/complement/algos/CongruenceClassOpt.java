package roll.main.complement.algos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roll.util.Pair;
import roll.util.sets.ISet;

/**
 * Optimal congruence relation for Buchi automata
 * 
 */
public class CongruenceClassOpt {
	/**
	 * left most are successors which visit final states earlier/maximal equivalence class
	 */
	protected final ArrayList<ISet> orderedSets;
	/**
	 * the set of 1-colored sets which are waiting to be moved to breakpoint
	 */
	protected final ArrayList<Pair<Integer, Boolean>> maxPres;

	/**
	 * flag to indicate whether the state is in the accepting component
	 */
	private final boolean period;

	public CongruenceClassOpt(boolean period) {
		this.orderedSets = new ArrayList<>();
		this.maxPres = new ArrayList<>();
		this.period = period;
	}

	public int addSet(ISet oset) {
		int index = orderedSets.size();
		orderedSets.add(oset);
		return index;
	}

	public void setMaxPres(int index, boolean b) {
		maxPres.add(new Pair<>(index, b));
	}

	public List<ISet> getOrderedSets() {
		return Collections.unmodifiableList(this.orderedSets);
	}

	public boolean isProgress() {
		return period;
	}

	public ISet getSet(int index) {
		assert index < orderedSets.size();
		return this.orderedSets.get(index);
	}
	
	public int getSetSize() {
		return this.orderedSets.size();
	}
	
	public Pair<Integer, Boolean> getMaxPres(int index) {
		assert index < maxPres.size();
		return this.maxPres.get(index);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof CongruenceClassOpt)) {
			return false;
		}
		CongruenceClassOpt otherClass = (CongruenceClassOpt) other;
		if (this.period != otherClass.period) {
			return false;
		}
		return this.maxPres.equals(otherClass.maxPres) && this.orderedSets.equals(otherClass.orderedSets);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + (period ? 1 : 0);
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
		if (period) {
			builder.append("<" + orderedSets + ", " + maxPres + ">");
		} else {
			builder.append(orderedSets + "");
		}
		return builder.toString();
	}
	
	public boolean isAccepted(CongruenceClassOpt initCongrCls, ISet finals) {
		if(! this.orderedSets.equals(initCongrCls.orderedSets)) {
			return false;
		}
		return !UtilCongruence.decideAcceptanceOpt(initCongrCls.orderedSets, this, finals);
	}

}
