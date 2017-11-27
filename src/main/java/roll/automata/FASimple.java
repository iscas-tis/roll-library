package roll.automata;

import java.util.ArrayList;
import java.util.BitSet;

import roll.util.ISet;
import roll.util.UtilISet;

/**
 * simple fa like DFA, NFA and NBA
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public abstract class FASimple implements FA {

    protected final ArrayList<State> states;
    protected final int alphabetSize;
    protected int initialState;
    protected final ISet finalStates;
    
    public FASimple(final int alphabetSize) {
        this.alphabetSize = alphabetSize;
        this.states = new ArrayList<>();
        this.finalStates = UtilISet.newISet();
    }
    
    public int getStateSize() {
        return states.size();
    }
    
    public int getAlphabetSize() {
        return alphabetSize;
    }
    
    public abstract State createState();
    
    public void setInitial(int state) {
        initialState = state;
    }
    
    public void setInitial(State state) {
        setInitial(state.getId());
    }
    
    public State getState(int state) {
        assert checkValidState(state);
        return states.get(state);
    }
    
    public int getInitialState() {
        return initialState;
    }
    
    public void setFinal(int state) {
        assert checkValidState(state);
        finalStates.set(state);
    }
    
    public boolean isFinal(int state) {
        assert checkValidState(state);
        return finalStates.get(state);
    }
    
    protected abstract State makeState(int index);
    
    protected boolean checkValidState(int state) {
        return state >= 0 && state < states.size();
    }
    
    protected boolean checkValidLetter(int letter) {
        return letter >= 0 && letter < alphabetSize;
    }

}
