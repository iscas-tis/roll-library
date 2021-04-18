package roll.main.complement.algos;

import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class SuccessorResult {
	
	public ISet succs ;
	public ISet minusFSuccs ;
	public ISet interFSuccs ;
	public boolean hasSuccessor ;
	
	public SuccessorResult() {
		succs = UtilISet.newISet();
		minusFSuccs = UtilISet.newISet();
		interFSuccs = UtilISet.newISet();
		hasSuccessor = true;
	}
	
	@Override
	public String toString() {
		return "[" + succs.toString() + ":" + minusFSuccs.toString() + ":"
				   + interFSuccs.toString() + ":" + hasSuccessor + "]";
	}

}