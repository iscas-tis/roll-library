package roll.automata;

public class StateSDFA extends StateNFA {
	
	final SDFA sdfa;
	
	public StateSDFA(SDFA fa, int id) {
		super(fa, id);
		this.sdfa = fa;
	}
	
	@Override
	protected String toStringShape() {
    	StringBuilder builder = new StringBuilder();
        if(nfa.isFinal(getId())) builder.append(", shape = doublecircle");
        else if (sdfa.isReject(getId())) builder.append(", shape = circle");
        else builder.append(", shape = square");
        return builder.toString();
    }
	
}
