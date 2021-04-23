package roll.main.complement.algos;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.automata.NBA;
import roll.main.Options;
import roll.main.complement.Complement;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

/**
 * Only valid for Semi-deterministic Buchi automata
 * NCSB On-the-fly version: input Buchi automaton is constructed during its complementation
 * <br>
 * "Complementing Semi-deterministic Büchi Automata" 
 * by František Blahoudek, Matthias Heizmann, Sven Schewe, Jan Strejček and Ming-Hsien Tsai
 * in TACAS 2016 (NCSB)
 * <br>
 * "Advanced Automata-based Algorithms for Program Termination Checking"
 * by Yu-Fang Chen, Matthias Heizmann, Ondra Lengál, Yong Li, Ming-Tsien Tsai, Andrea Turrini and Lijun Zhang.
 * In PLDI 2018 (NCSB + Lazy-S version)
 * <br>
 * NCSB + Lazy-B has not been published yet
 * 
 * */
public class ComplementNcsbOtf extends Complement {

    protected TObjectIntMap<StateNcsbOtf> stateIndices;
    
    public ComplementNcsbOtf(Options options, NBA nba) {
        super(options, nba);
    }

    @Override
    protected void computeInitialState() {
    	if(! operand.isLimitdeterministic()) {
    		throw new RuntimeException("ComplementNcsbOtf only supports limit deterministic BA");
    	}
    	// should be on-the-fly
        stateIndices = new TObjectIntHashMap<>();
        int init = operand.getInitialState();
        ISet C = UtilISet.newISet();
        ISet N = UtilISet.newISet();
        if(operand.isFinal(init)) {
        	C.set(init); // goto C
        }else {
        	N.set(init);
        }
        NCSB ncsb = new NCSB(N, C, UtilISet.newISet(), C);
        StateNcsbOtf state = new StateNcsbOtf(this, 0, ncsb);
        int id = this.addState(state);
        stateIndices.put(state, id);
        if(C.isEmpty()) this.setFinal(0);
        this.setInitial(0);
    }
    

    protected StateNcsbOtf getOrAddState(NCSB ncsb) {
        
        StateNcsbOtf state = new StateNcsbOtf(this, 0, ncsb);
        
        if(stateIndices.containsKey(state)) {
            return getStateNCSB(stateIndices.get(state));
        }else {
            int index = getStateSize();
            StateNcsbOtf newState = new StateNcsbOtf(this, index, ncsb);
            int id = this.addState(newState);
            if (id != index) {
                throw new RuntimeException("ComplementNcsbOtf state index error");
            }
            stateIndices.put(newState, id);
            if(ncsb.getBSet().isEmpty()) setFinal(index);
            return newState;
        }
    }
    
    public StateNcsbOtf getStateNCSB(int id) {
        return (StateNcsbOtf) getState(id);
    }
    
    public void testLemma() {
        for(int i = 0; i < getStateSize(); i ++) {
            StateNcsbOtf s = (StateNcsbOtf) getState(i);
            ISet N = s.getNCSB().copyNSet();
            ISet C = s.getNCSB().copyCSet();
            ISet B = s.getNCSB().copyBSet();
            C.andNot(B);
            C.andNot(getFinalStates());
            for(int n : C) {
                ISet Cp = s.getNCSB().copyCSet();
                Cp.clear(n);
                ISet S = s.getNCSB().copySSet();
                S.set(n);
                NCSB ncsb = new NCSB(N, Cp, S, B);
                StateNcsbOtf nn = getOrAddState(ncsb);
                System.out.println(s.getNCSB() + " : " + ncsb);
                assert nn != null : "Not reachable " + s.getNCSB() + " -> " + ncsb;
            }

        }
    }

}

