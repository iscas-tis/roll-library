package roll.main.complement.algos;

import java.util.TreeSet;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.automata.DFA;
import roll.automata.NBA;
import roll.main.inclusion.congr.IntBoolTriple;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class DFACongruence extends DFA {
	
	protected NBA operand;
	protected CongruenceClass initCongrCls;
	protected TObjectIntMap<StateDFA> localIndices;
	protected ISet inits;
	
	public DFACongruence(NBA operand, CongruenceClass initCongCls) {
		super(operand.getAlphabet());
		this.operand = operand;
		this.initCongrCls = initCongCls;
		this.localIndices = new TObjectIntHashMap<>();
		if(! initCongrCls.isSet) {
			this.inits = this.getReachSet(initCongrCls.level);
		}else {
			this.inits = UtilISet.newISet();
		}
	}
	
	protected NBA getOperand() {
		return this.operand;
	}
	
	protected void computeInitialState() {
		// map from global index to local index
		StateDFA state = this.getOrAddState(initCongrCls);
		this.setInitial(state.getId());
	}
	
	private ISet getReachSet(TreeSet<IntBoolTriple> set) {
		ISet reached = UtilISet.newISet();
		for(IntBoolTriple triple : set) {
			reached.set(triple.getRight());
		}
		return reached;
	}

	
	protected StateDFA getOrAddState(CongruenceClass congrCls) {
		StateDFA state = new StateDFA(this, 0, congrCls);
		if (localIndices.containsKey(state)) {
			// this StateDFA already computed
			int localIndex = localIndices.get(state);
			return getStateDFA(localIndex);
		} else {
			int localIndex = getStateSize();
			StateDFA newState = new StateDFA(this, localIndex, congrCls);
			int id = this.addState(newState);
			if (id != localIndex) {
				throw new RuntimeException("ComplementCongruence state index error");
			}
			localIndices.put(newState, localIndex);
			if (! congrCls.isSet && congrCls.isAccepted(this.inits)) {
				// now decide if it is subsumed by initCongr 
				setFinal(localIndex);
			}
			return newState;
		}
	}
	
	public StateDFA getStateDFA(int id) {
		return (StateDFA) getState(id);
	}

}
