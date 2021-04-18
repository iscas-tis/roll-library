package roll.main.complement.algos;

import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.util.sets.ISet;

public class UtilNcsb {
    
    private UtilNcsb() {
        
    }
    
    /**
     * If q in C\F or (B\F), then tr(q, a) should not be not empty
     * */
    public static boolean noTransitionAssertionMinusF(NBA buchi, int state, ISet succs) {
        return !buchi.isFinal(state) && succs.isEmpty();
    }
    
    /**
     * compute the successor of a set of states, in particular, d(C\F) should not contain be empty
     * **/
    public static SuccessorResult collectSuccessors(NBA buchi, ISet states, int letter, boolean testTrans) {
        SuccessorResult result = new SuccessorResult();
        for(final int stateId : states) {
            StateNFA state = buchi.getState(stateId);
            ISet succs = state.getSuccessors(letter);
            if (testTrans && noTransitionAssertionMinusF(buchi, stateId, succs)) {
                result.hasSuccessor = false;
                return result;
            }
            result.succs.or(succs);
            if(testTrans) {
                if(buchi.isFinal(stateId)) {
                    result.interFSuccs.or(succs);
                }else {
                    result.minusFSuccs.or(succs);
                }
            }
        }
        return result;
    }
    
    public static boolean hasFinalStates(NBA buchi, ISet states) {
        return states.overlap(buchi.getFinalStates());
    }
}

