package roll.automata;

public interface State {
    // getters 
    Automaton getAutomaton();
    int getIndex();
    
    // setters
    void addTransition(int letter, int state);

}
