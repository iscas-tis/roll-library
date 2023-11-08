package roll.automata.operations;

import java.util.List;

import roll.automata.NFA;
import roll.automata.StateNFA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class StateNFADet extends StateNFA {
	
	protected NFADet det;
	protected final NFA operand;
	protected ISet setOfStates;

	public StateNFADet(NFADet fa, int id, ISet set) {
		super(fa, id);
		this.operand = fa.getOperand();
		this.det = fa;
		this.setOfStates = set.clone();
	}
	
	private ISet visitedLetters = UtilISet.newISet();

	@Override
	public int getSuccessor(int letter) {
		if (visitedLetters.get(letter)) {
			return super.getSuccessors(letter).iterator().next();
		}
		visitedLetters.set(letter);
		// computing successors
		if (setOfStates.isEmpty()) {
			return this.getId();
		}else {
			// it is not empty
			ISet succSetOfStates = UtilISet.newISet();
			for (int state : setOfStates) {
				ISet succs = operand.getState(state).getSuccessors(letter);
				succSetOfStates.or(succs);
			}
			// we create a new one
			StateNFADet succ = det.getOrAddState(succSetOfStates);
			super.addTransition(letter, succ.getId());
			return succ.getId();
		}
	}
	
	@Override
	public ISet getSuccessors(int letter) {
		ISet succIds = UtilISet.newISet();
		succIds.set(getSuccessor(letter));
		return succIds;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof StateNFADet)) {
			return false;
		}
		StateNFADet other = (StateNFADet) obj;
		return setOfStates.contentEq(other.setOfStates);
	}

	@Override
	public int hashCode() {
		return setOfStates.hashCode();
	}
	
	@Override
	protected String toStringLabel() {
    	return setOfStates.toString();
    }

}
