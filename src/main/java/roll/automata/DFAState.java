package roll.automata;

public class DFAState implements State {
    private final DFA dfa;
    private final int[] successors;
    private final int id;
    
    public DFAState(final DFA dfa, final int id) {
        assert dfa != null;
        this.dfa = dfa;
        this.id = id;
        this.successors = new int[dfa.getAlphabetSize()];
    }

    @Override
    public DFA getFA() {
        return dfa;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void addTransition(int letter, int state) {
        assert dfa.checkValidLetter(letter);
        successors[letter] = state;
    }
    
    public int getSuccessor(int letter) {
        assert dfa.checkValidLetter(letter);
        return successors[letter];
    }
    
    

}
