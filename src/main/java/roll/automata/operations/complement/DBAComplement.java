package roll.automata.operations.complement;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.automata.NBA;

public class DBAComplement extends NBA {
	protected NBA operand;
    private TObjectIntMap<StateKurshan> stateIndices;
	
	public DBAComplement(NBA input) {
		super(input.getAlphabet());
		this.operand = input;
		computeInitialState();
	}
	
	protected NBA getOperand() {
		return operand;
	}
	
    protected void computeInitialState() {
        if(!operand.isDeterministic()) {
            throw new RuntimeException("DBAComplement only supports deterministic BA");
        }
        // should make sure input automaton is complete
        operand.makeComplete();
        stateIndices = new TObjectIntHashMap<>();
        int init = operand.getInitialState();
        StateKurshan state = getOrAddState(init, false);
        this.setInitial(state.getId());
    }
    
    public StateKurshan getStateKurshan(int id) {
        return (StateKurshan) getState(id);
    }
    
    protected StateKurshan getOrAddState(int st, boolean label) {
        StateKurshan state = new StateKurshan(this, 0, st, label);
        
        if(stateIndices.containsKey(state)) {
            return getStateKurshan(stateIndices.get(state));
        }else {
            int index = getStateSize();
            StateKurshan newState = new StateKurshan(this, index, st, label);
            int id = this.addState(newState);
            stateIndices.put(newState, id);
            if(!operand.isFinal(st) && label) setFinal(id);
            return newState;
        }
    }
	
}
