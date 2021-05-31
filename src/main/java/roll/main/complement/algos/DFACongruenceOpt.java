package roll.main.complement.algos;

import java.util.TreeSet;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.automata.DFA;
import roll.automata.NBA;
import roll.main.inclusion.congr.IntBoolTriple;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class DFACongruenceOpt extends DFA {
	
	protected NBA operand;
	protected CongruenceClassOpt initCongrCls;
	protected TObjectIntMap<StateDFAOpt> localIndices;
	
	public DFACongruenceOpt(NBA operand, CongruenceClassOpt initCongCls) {
		super(operand.getAlphabet());
		this.operand = operand;
		this.initCongrCls = initCongCls;
		this.localIndices = new TObjectIntHashMap<>();
	}
	
	protected NBA getOperand() {
		return this.operand;
	}
	
	protected void computeInitialState() {
		// map from global index to local index
		StateDFAOpt state = this.getOrAddState(initCongrCls);
		this.setInitial(state.getId());
	}
	
	private ISet getReachSet(TreeSet<IntBoolTriple> set) {
		ISet reached = UtilISet.newISet();
		for(IntBoolTriple triple : set) {
			reached.set(triple.getRight());
		}
		return reached;
	}

	
	protected StateDFAOpt getOrAddState(CongruenceClassOpt congrCls) {
		StateDFAOpt state = new StateDFAOpt(this, 0, congrCls);
		if (localIndices.containsKey(state)) {
			// this StateDFA already computed
			int localIndex = localIndices.get(state);
			return getStateDFAOpt(localIndex);
		} else {
			int localIndex = getStateSize();
			StateDFAOpt newState = new StateDFAOpt(this, localIndex, congrCls);
			int id = this.addState(newState);
			if (id != localIndex) {
				throw new RuntimeException("ComplementCongruence state index error");
			}
			localIndices.put(newState, localIndex);
			if (congrCls.isProgress() && congrCls.isAccepted(initCongrCls, operand.getFinalStates())) {
				// now decide if it is subsumed by initCongr 
				setFinal(localIndex);
//				System.out.println("Acc state = " + congrCls + ", init = " + this.inits);
			}
			return newState;
		}
	}
	
	public StateDFAOpt getStateDFAOpt(int id) {
		return (StateDFAOpt) getState(id);
	}

}
