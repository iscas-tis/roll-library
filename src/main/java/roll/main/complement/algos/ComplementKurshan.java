package roll.main.complement.algos;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.automata.NBA;
import roll.main.complement.Complement;

/**
 * Complementing deterministic Büchi automata in polynomial time
 * by R.P.Kurshan
 * in Journal of Computer and System Sciences
 * */
public class ComplementKurshan extends Complement {
	
    private TObjectIntMap<StateKurshan> stateIndices;


	public ComplementKurshan(NBA operand) {
		super(operand);
	}

    @Override
    protected void computeInitialState() {
        if(! operand.isDeterministic()) {
            throw new RuntimeException("ComplementKurshan only supports deterministic BA");
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
            if (id != index) {
                throw new RuntimeException("ComplementKurshan state index error");
            }
            stateIndices.put(newState, id);
            if(!operand.isFinal(st) && label) setFinal(id);
            return newState;
        }
    }
	
}
