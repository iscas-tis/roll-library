package roll.automata;

import java.util.LinkedList;

import roll.table.HashableValue;
import roll.table.HashableValueEnum;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

// separating DFA for two positive and negative languages
public class SDFA extends DFA {
	
	protected final ISet rejectStates;
	
	public SDFA(Alphabet alphabet) {
		super(alphabet);
		this.rejectStates = UtilISet.newISet();
	}
	
    @Override
    public AutType getAccType() {
        return AutType.SDFA;
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
    
    public int getRejectSize() {
    	return this.rejectStates.cardinality();
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
    
    public SDFA reduce(LinkedList<Word> positives
    		, LinkedList<Word> negatives) {
    	SDFA res = new SDFA(this.alphabet);
    	// first, record the transitions that we will keep
    	for (int s = 0; s < this.getStateSize(); s ++) {
    		res.createState();
    		if (this.isFinal(s)) {
    			res.setFinal(s);
    		}else if (this.isReject(s)) {
    			res.setReject(s);
    		}
    	}
    	for (Word word : positives) {
    		int curr = this.getInitialState();
    		int index = 0;
    		while (index < word.length()) {
    			int letter = word.getLetter(index);
    			int succ = this.getSuccessor(curr, letter);
    			res.getState(curr).addTransition(letter, succ);
    			curr = succ;
    			++ index;
    		}
    	}
    	
    	for (Word word : negatives) {
    		int curr = this.getInitialState();
    		int index = 0;
    		while (index < word.length()) {
    			int letter = word.getLetter(index);
    			int succ = this.getSuccessor(curr, letter);
    			res.getState(curr).addTransition(letter, succ);
    			curr = succ;
    			++ index;
    		}
    	}
    	res.setInitial(getInitialState());
    	return res;
    }
    
    public HashableValue run(Word word) {
    	int state = this.getSuccessor(word);
    	if (this.isFinal(state)) {
    		return new HashableValueEnum(1);
    	}else if (this.isReject(state)) {
    		return new HashableValueEnum(-1);
    	}else {
    		return new HashableValueEnum(0);
    	}
    }
    
    public DFA getDFA(boolean accept) {
    	DFA dfa = new DFA(this.alphabet);
    	for (int s = 0; s < this.getStateSize(); s ++) {
    		dfa.createState();
    		if (accept && this.isFinal(s)) {
    			dfa.setFinal(s);
    		}else if (!accept && this.isReject(s)) {
    			dfa.setFinal(s);
    		}
    	}
    	for (int s = 0; s < this.getStateSize(); s ++) {
    		for (int a = 0; a < this.getAlphabetSize(); a ++) {
    			dfa.getState(s).addTransition(a, this.getSuccessor(s, a));
    		}
    	}
    	dfa.setInitial(this.getInitialState());
    	return dfa;
    }

}
