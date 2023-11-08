package roll.automata.operations;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.automata.DFA;
import roll.automata.NFA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class NFADet extends DFA {
	
	protected NFA operand;
    private TObjectIntMap<StateNFADet> stateIndices;
    protected final ISet finalStates;
	
	public NFADet(NFA input) {
		super(input.getAlphabet());
		// TODO Auto-generated constructor stub
		this.operand = input;
		this.finalStates = input.getFinalStates();
		computeInitialState();
	}
	
	protected NFA getOperand() {
		return operand;
	}
	
    protected void computeInitialState() {
        stateIndices = new TObjectIntHashMap<>();
        ISet inits = UtilISet.newISet();
        inits.set(operand.getInitialState());
        StateNFADet state = getOrAddState(inits);
        this.setInitial(state.getId());
    }
    
    protected StateNFADet getStateNFADet(int id) {
        return (StateNFADet) getState(id);
    }
    
    
    protected StateNFADet getOrAddState(ISet states) {
    	StateNFADet state = new StateNFADet(this, 0, states);
        if(stateIndices.containsKey(state)) {
            return getStateNFADet(stateIndices.get(state));
        }else {
            int index = getStateSize();
            StateNFADet newState = new StateNFADet(this, index, states);
            int id = this.addState(newState);
            stateIndices.put(newState, id);
            if(finalStates.overlap(states)) setFinal(id);
            return newState;
        }
    }
	

}
