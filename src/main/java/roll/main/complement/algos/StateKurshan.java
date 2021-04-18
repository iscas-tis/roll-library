package roll.main.complement.algos;

import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class StateKurshan extends StateNFA {
	
	 private int opState;     // state id in original automaton
	 private boolean label; //label to indicate whether it is in accepting component or the original automaton
	 private final ComplementKurshan complement;
	 private final NBA operand;
	   

	public StateKurshan(ComplementKurshan complement, int id, int origState, boolean label) {
		super(complement, id);
		this.complement = complement;
	    this.operand = complement.getOperand();
	    this.opState = origState;
	    this.label = label;
	}
    
    private ISet visitedLetters = UtilISet.newISet();
    
    @Override
    public ISet getSuccessors(int letter) {
        if(visitedLetters.get(letter)) {
            return super.getSuccessors(letter);
        }
        visitedLetters.set(letter);
        // computing successors
        ISet succs = operand.getState(opState).getSuccessors(letter);
        if(succs.cardinality() > 1) {
            throw new RuntimeException("Not a DBA: state " + opState 
                    + " has more than one successors " + succs.toString());
        }
        if(succs.cardinality() == 0) {
        	throw new RuntimeException("Not a complete DBA: state " + opState 
                    + " has no successors " + succs.toString());
        }
        int succ = succs.iterator().next();
        StateKurshan qP;
        succs = UtilISet.newISet();
        if(label) {
            // c = 1
            if(operand.isFinal(succ)) {
                //empty
            }else {
                // q' \notin F
                qP = complement.getOrAddState(succ, true);
                super.addTransition(letter, qP.getId());
                succs.set(qP.getId());
            }
        }else {
            // c = 0
            if(operand.isFinal(succ)) {
                // q' \in F
                qP = complement.getOrAddState(succ, false);
                super.addTransition(letter, qP.getId());
                succs.set(qP.getId());
            }else {
                // q' \notin F
                qP = complement.getOrAddState(succ, false);
                super.addTransition(letter, qP.getId());
                succs.set(qP.getId());
                qP = complement.getOrAddState(succ, true);
                super.addTransition(letter, qP.getId());
                succs.set(qP.getId());
            }
        }
        return succs;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof StateKurshan)) {
            return false;
        }
        StateKurshan other = (StateKurshan)obj;
        return  opState == other.opState
              && label == other.label;
    }
    
    @Override
    public int hashCode() {
        return opState * 31 + (label ? 1 : 0);
    }

}
