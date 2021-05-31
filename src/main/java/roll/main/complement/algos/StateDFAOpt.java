package roll.main.complement.algos;

import roll.automata.NBA;
import roll.automata.StateNFA;

public class StateDFAOpt extends StateNFA {

	protected CongruenceClassOpt congrClass;
	protected DFACongruenceOpt dfa;
	protected NBA operand;
	
	public StateDFAOpt(DFACongruenceOpt fa, int id, CongruenceClassOpt cls) {
		super(fa, id);
		this.dfa = fa;
		this.operand = fa.getOperand();
	    this.congrClass = cls;
	}
	
//	private ISet visitedLetters = UtilISet.newISet();
//	
//	@Override
//	public int getSuccessor(int letter) {
//		if(visitedLetters.get(letter)) {
//			super.getSuccessor(letter);
//		}
//		visitedLetters.set(letter);
//		if(congrClass.isSet && congrClass.guess.isEmpty()) {
//			super.addTransition(letter, this.getId());
//			return this.getId();
//		}
//		if(congrClass.isSet) {
//			ISet nextGuess = UtilISet.newISet();
//			for(int curr : congrClass.guess) {
//				for(int succ : operand.getState(curr).getSuccessors(letter)) {
//					nextGuess.set(succ);
//				}
//			}
//			CongruenceClass nextCongrCls = new CongruenceClass(nextGuess);
//			// add one state
//			StateDFA nextState = dfa.getOrAddState(nextCongrCls);
//			super.addTransition(letter, nextState.getId());
//			System.out.println(this.getId() + ": " + this + " -> " + nextState.getId() + " : " + nextState + " > " + letter );
//			return nextState.getId();
//		}else {
//			// here it is progress DFA
//			// now move onto level
//			TreeSet<IntBoolTriple> nextLevel = computeNextLevel(congrClass.level, letter);
//			CongruenceClass nextCongrCls = new CongruenceClass(nextLevel);
//			// add one state
//			StateDFA nextState = dfa.getOrAddState(nextCongrCls);
//			super.addTransition(letter, nextState.getId());
//			System.out.println(this.getId() + ": " + this + " -> " + nextState.getId() + " : " + nextState + " > " + letter);
//			return nextState.getId();
//		}
//	}
//	
//	protected TreeSet<IntBoolTriple> computeNextLevel(TreeSet<IntBoolTriple> preLevel, int letter) {
//		TreeSet<IntBoolTriple> nextLevel = new TreeSet<>();
//		for(IntBoolTriple currTriple : preLevel) {
//			for(int succ : operand.getState(currTriple.getRight()).getSuccessors(letter)) {
//				addTriple(nextLevel, new IntBoolTriple(currTriple.getLeft(), succ, currTriple.getBool() || operand.isFinal(succ)));
//			}
//		}
//		return nextLevel;
//	}
//	
//	
//	// Always guarantte that if there is (q, r: true), then no (q, r: false) appears
//	private void addTriple(TreeSet<IntBoolTriple> set, IntBoolTriple triple) {
//		IntBoolTriple revTriple = new IntBoolTriple(triple.getLeft(), triple.getRight(), !triple.getBool());
//		boolean containedRev = set.contains(revTriple);
//		if (containedRev) {
//			if (triple.getBool()) {
//				set.remove(revTriple);
//				set.add(triple);
//			} else {
//				// do nothing, keep the original one
//			}
//		} else {
//			set.add(triple);
//		}
//	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof StateDFAOpt)) {
			return false;
		}
		StateDFAOpt other = (StateDFAOpt)obj;
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