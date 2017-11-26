package roll.automata;

/**
 * Acceptor for regular (omega) language 
 * */
public interface Acceptor {
	
	AccType getAccType();
	
	Acc getAcc(); // acceptance condition
	
	default FDFA asFDFA() {
	    assert this instanceof FDFA;
		return (FDFA)this;
	}
	
	default DFA asDFA() {
	    assert this instanceof DFA;
		return (DFA)this;
	}
	
	default NFA asNFA() {
	    assert this instanceof NFA;
		return (NFA)this;
	}
	
	default Buchi asBuchi() {
	    assert this instanceof Buchi;
        return (Buchi)this;
    }
	// and so on

}
