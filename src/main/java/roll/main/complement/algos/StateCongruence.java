package roll.main.complement.algos;

import java.util.TreeSet;

import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.main.inclusion.congr.IntBoolTriple;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class StateCongruence extends StateNFA {
	
	protected CongruenceClass congrClass;
	private ComplementCongruence complement;
	private NBA operand;

	public StateCongruence(ComplementCongruence complement, int id, CongruenceClass cls) {
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
		ISet result = UtilISet.newISet();
		if(congrClass.isSet && congrClass.guess.isEmpty()) {
			super.addTransition(letter, this.getId());
			System.out.println(this.getId() + ": " + this + " -> " + this.getId() + " : " + this + " > " + letter );
			result.set(this.getId());
			return result;
		}
		TreeSet<IntBoolTriple> preLevel = new TreeSet<>();
		if(congrClass.isSet) {
			ISet nextGuess = UtilISet.newISet();
			for(int curr : congrClass.guess) {
				for(int succ : operand.getState(curr).getSuccessors(letter)) {
					nextGuess.set(succ);
				}
			}
			CongruenceClass nextCongrCls = new CongruenceClass(nextGuess);
			// add one state
			StateCongruence nextState = complement.getOrAddState(nextCongrCls);
			super.addTransition(letter, nextState.getId());
			System.out.println(this.getId() + ": " + this + " -> " + nextState.getId() + " : " + nextState + " > " + letter );
			result.set(nextState.getId());
		}else {
			// now move onto level
			preLevel = congrClass.level;
			TreeSet<IntBoolTriple> nextLevel = computeNextLevel(preLevel, letter);
			CongruenceClass nextCongrCls = new CongruenceClass(nextLevel);
			// add one state
			StateCongruence nextState = complement.getOrAddState(nextCongrCls);
			super.addTransition(letter, nextState.getId());
			System.out.println(this.getId() + ": " + this + " -> " + nextState.getId() + " : " + nextState + " > " + letter);
			result.set(nextState.getId());
		}
		return result;
	}
	
	protected TreeSet<IntBoolTriple> computeNextLevel(TreeSet<IntBoolTriple> preLevel, int letter) {
		TreeSet<IntBoolTriple> nextLevel = new TreeSet<>();
		for(IntBoolTriple currTriple : preLevel) {
			for(int succ : operand.getState(currTriple.getRight()).getSuccessors(letter)) {
				UtilCongruence.addTriple(nextLevel, new IntBoolTriple(currTriple.getLeft(), succ, currTriple.getBool() || operand.isFinal(succ)));
			}
		}
		return nextLevel;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof StateCongruence)) {
			return false;
		}
		StateCongruence other = (StateCongruence)obj;
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
