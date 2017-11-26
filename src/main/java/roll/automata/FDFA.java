package roll.automata;

import java.util.List;

public class FDFA implements Acceptor {
    
    private final DFA leadingDFA;
    private final List<DFA> progressDFAs;
    
    public FDFA(DFA m, List<DFA> ps) {
        leadingDFA = m;
        progressDFAs = ps;
    }
    
    public DFA getLeadingDFA() {
        return leadingDFA;
    }
    
    public DFA getProgressDFA(int state) {
        assert state >= 0 && state < progressDFAs.size(); 
        return progressDFAs.get(state);
    }

    @Override
    public AccType getAccType() {
        return AccType.FDFA;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // todo
        return sb.toString();
    }

    @Override
    public Acc getAcc() {
        // TODO Auto-generated method stub
        return null;
    }

}
