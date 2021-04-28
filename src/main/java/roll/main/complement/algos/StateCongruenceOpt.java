package roll.main.complement.algos;

import java.util.List;

import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class StateCongruenceOpt extends StateNFA {
	
	protected CongruenceClassOpt congrClass;
	private ComplementCongruenceOpt complement;
	private NBA operand;

	public StateCongruenceOpt(ComplementCongruenceOpt complement, int id, CongruenceClassOpt cls) {
		super(complement, id);
		this.complement = complement;
	    this.operand = complement.getOperand();
	    this.congrClass = cls;
	}
	
	private ISet visitedLetters = UtilISet.newISet();
	
	@Override
	public ISet getSuccessors(int letter) {
		if(visitedLetters.get(letter)) {
			return super.getSuccessors(letter);
		}
		visitedLetters.set(letter);
        List<ISet> ordSets = congrClass.getOrderedSets();
        ISet leftSuccs = UtilISet.newISet();
        ISet finalStates = operand.getFinalStates();
        
        CongruenceClassOpt nextCongrClass = new CongruenceClassOpt(congrClass.isProgress());
        ISet result = UtilISet.newISet();
        for(int i = 0; i < ordSets.size(); i ++) {
            // compute successors
            ISet Si = ordSets.get(i);
            ISet finalSuccs = UtilISet.newISet();
            ISet nonFinalSuccs = UtilISet.newISet();
            for(final int p : Si) {
                for(final int q : operand.getState(p).getSuccessors(letter)) {
                    // ignore successors already have been visited
                    if(leftSuccs.get(q)) continue;
                    if(finalStates.get(q)) {
                        finalSuccs.set(q);
                    }else {
                        nonFinalSuccs.set(q);
                    }
                    leftSuccs.set(q);
                }
            }
            // now we will record every thing
            if(!finalSuccs.isEmpty()) {
                nextCongrClass.addSet(finalSuccs);
                // now we record the parent
                if(congrClass.isProgress()) {
                	Pair<Integer, Boolean> pair = congrClass.getMaxPres(i); 
                	nextCongrClass.setMaxPres(pair.getLeft(), true);
                }
            }
            
            if(!nonFinalSuccs.isEmpty()) {
                nextCongrClass.addSet(nonFinalSuccs);
             // now we record the parent
                if(congrClass.isProgress()) {
                	Pair<Integer, Boolean> pair = congrClass.getMaxPres(i); 
                	nextCongrClass.setMaxPres(pair.getLeft(), finalStates.overlap(Si) || pair.getRight());
                }
            }
        }
        
        StateCongruenceOpt nextState = complement.getOrAddState(nextCongrClass);
		super.addTransition(letter, nextState.getId());
		if(complement.debug) System.out.println(this.getId() + ": " + this + " -> " + nextState.getId() + " : " + nextState + " > " + letter );
		result.set(nextState.getId());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof StateCongruenceOpt)) {
			return false;
		}
		StateCongruenceOpt other = (StateCongruenceOpt)obj;
		return  congrClass.equals(other.congrClass);
	}
	
	@Override
	public String toString() {
		return congrClass.toString();
	}
	

	@Override
	public int hashCode() {
		return congrClass.hashCode();
	}

}
