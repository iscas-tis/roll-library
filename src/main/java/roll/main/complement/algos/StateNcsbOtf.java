package roll.main.complement.algos;

import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class StateNcsbOtf extends StateNFA {

	private final NCSB ncsb;
	private final NBA operand;
	private final ComplementNcsbOtf complement;
	
	public StateNcsbOtf(ComplementNcsbOtf complement, int id, NCSB ncsb) {
		super(complement, id);
		this.complement = complement;
		this.operand = complement.getOperand();
		this.ncsb = ncsb;
	}
	
	public NCSB getNCSB() {
		return  ncsb;
	}
	
	private ISet visitedLetters = UtilISet.newISet();
	
	@Override
	public ISet getSuccessors(int letter) {
		if(visitedLetters.get(letter)) {
			return super.getSuccessors(letter);
		}
		visitedLetters.set(letter);
		// B
		SuccessorResult succResult = UtilNcsb.collectSuccessors(operand, ncsb.getBSet(), letter, true);
		if(!succResult.hasSuccessor) return UtilISet.newISet();
		ISet BSuccs = succResult.succs;
		ISet minusFSuccs = succResult.minusFSuccs;
		ISet interFSuccs = succResult.interFSuccs;

		// C\B
		ISet cMinusB = ncsb.copyCSet();
		cMinusB.andNot(ncsb.getBSet());
		boolean lazys = false;
		succResult = UtilNcsb.collectSuccessors(operand, cMinusB, letter, !lazys);
		if(!succResult.hasSuccessor) return UtilISet.newISet();
		ISet CSuccs = succResult.succs;
		CSuccs.or(BSuccs);
		minusFSuccs.or(succResult.minusFSuccs);
		interFSuccs.or(succResult.interFSuccs);
		
		// N
		succResult = UtilNcsb.collectSuccessors(operand, ncsb.getNSet(), letter, false);
		if(!succResult.hasSuccessor) return UtilISet.newISet();
		ISet NSuccs = succResult.succs;

		// S
		succResult = UtilNcsb.collectSuccessors(operand, ncsb.getSSet(), letter, false);
		if(!succResult.hasSuccessor) return UtilISet.newISet();
		ISet SSuccs = succResult.succs;
		
		return computeSuccessors(new NCSB(NSuccs, CSuccs, SSuccs, BSuccs), minusFSuccs, interFSuccs, letter);
	}
	
    @Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof StateNcsbOtf)) {
			return false;
		}
		StateNcsbOtf other = (StateNcsbOtf)obj;
		return  ncsb.equals(other.ncsb);
	}
	
	@Override
	public String toString() {
		return ncsb.toString();
	}
	

	@Override
	public int hashCode() {
		return ncsb.hashCode();
	}
	// -------------------------------------------------
	
	private ISet computeSuccessors(NCSB succNCSB, ISet minusFSuccs
			, ISet interFSuccs, int letter) {
		// check d(S) and d(C)
		if(succNCSB.getSSet().overlap(operand.getFinalStates())
		|| minusFSuccs.overlap(succNCSB.getSSet())) {
			return UtilISet.newISet();
		}
		SuccessorGenerator generator = new SuccessorGenerator(complement.getOptions()
				                                            , ncsb.getBSet().isEmpty()
															, succNCSB
															, minusFSuccs
															, interFSuccs
															, operand.getFinalStates());
		ISet succs = UtilISet.newISet();
		while(generator.hasNext()) {
		    NCSB ncsb = generator.next();
		    if(ncsb == null) continue;
			StateNcsbOtf succ = complement.getOrAddState(ncsb);
			super.addTransition(letter, succ.getId());
			succs.set(succ.getId());
			if(complement.debug) {
				System.out.println(this.getId() + ": " + this + " - " + letter + " -> " + succ.getId() + ": " + succ);
			}
		}

		return succs;
	}

}
