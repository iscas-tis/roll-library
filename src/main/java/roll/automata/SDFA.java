package roll.automata;

import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;

// separating DFA for two positive and negative languages
public class SDFA extends DFA {
	
	protected final ISet rejectStates;
	
	public SDFA(Alphabet alphabet) {
		super(alphabet);
		this.rejectStates = UtilISet.newISet();
	}
	
    @Override
    public AutType getAccType() {
        return AutType.DFA;
    }
    
    @Override
    public void setReject(int state) {
    	assert checkValidState(state);
    	this.rejectStates.set(state);
    }
    
    @Override
    public void clearReject(int state) {
    	assert checkValidState(state);
    	this.rejectStates.clear(state);
    }
    
    @Override
    public boolean isReject(int state) {
    	assert checkValidState(state);
    	return this.rejectStates.get(state);
    }
    
    @Override
    protected StateNFA makeState(int index) {
        return new StateSDFA(this, index);
    }

}
