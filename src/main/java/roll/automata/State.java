package roll.automata;

public interface State {
    // getters 
    FA getFA();
    int getId();
    // setters
    void addTransition(int letter, int state);

}
