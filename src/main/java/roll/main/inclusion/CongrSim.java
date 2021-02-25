package roll.main.inclusion;

import java.util.HashSet;
import java.util.Set;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.StateContainer;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.util.sets.ISetTreeSet;

// This algorithm is inspired by simulation relation and the work 
//	Congruence Relations for B\"uchi Automata submitted to ICALP'21

public class CongrSim {
	
	NBA A;
	NBA B;
	
	// Simulation relation between A and complement B (Congruence relations for B)
	/**
	 * for each u, i_A - u -> q, i_B - u -> q', then we have q' simulates q for prefix
	 * Here we actually define congruence relations for states in B with respect to states in A
	 * 
	 * That is, for prefix, we have (s_A, Set_B_states)
	 * if s_A = i_A, then Set_B_states = {i_B}
	 * otherwise, for each state s_A in A, if i_A - u -> s_A, then there exists a state t in Set_B_states,
	 *  such that i_B - u -> t.
	 * */
	TIntObjectMap<ISet> prefSim;
	StateContainer[] bStates;
	
	/**
	 * for each v and final state q, then we have q' simulates q for period in B as follows:
	 * 
	 * q_A - v - > q'_A in A, then we need to have a path q_B - v -> q'_B
	 * q_A = v => q'_A in A (visiting accepting states), then we have q_B = v => q'_B
	 *
	 * */
	// periodSim;

	
	CongrSim(NBA A, NBA B) {
		this.A = A;
		this.B = B;
		prefSim = new TIntObjectHashMap<ISet>();
		bStates = new StateContainer[B.getStateSize()];
		// compute the predecessors and successors
		for(int i = 0; i < B.getStateSize(); i ++) {
          bStates[i] = new StateContainer(i, B);
		}
		for (int i = 0; i < B.getStateSize(); i++) {
			StateNFA st = bStates[i].getState();
			for (int letter = 0; letter < B.getAlphabetSize(); letter++) {
				for (int succ : st.getSuccessors(letter)) {
					bStates[i].addSuccessors(letter, succ);
					bStates[succ].addPredecessors(letter, i);
				}
			}
		}
	}
	
	
	public void compute_prefix_simulation() {
		// initialization
		for(int s = 0; s < A.getStateSize(); s ++)
		{
			prefSim.put(s, new ISetTreeSet());
			// only i_B simulates i_A at first
			if(s == A.getInitialState()) {
				prefSim.get(s).set(B.getInitialState());
			}
		}
		// compute simulation relation
		while(true) {
			// copy the first one
			boolean changed = false;
			TIntObjectMap<ISet> copy = new TIntObjectHashMap<ISet>();
			for(int s = 0; s < A.getStateSize(); s ++) {
				copy.put(s, prefSim.get(s).clone());
			}
			// compute relations 
			for(int s = 0; s < A.getStateSize(); s++) {
				// tried to update successors
				ISet letters = A.getState(s).getEnabledLetters();
				for(int a : letters) {
					for(int t : A.getState(s).getSuccessors(a)) {
						// s - a - > t in A
						// f(s) - a -> P'
						// p \in f(s), then P' \subseteq f(t) in B
						// compute mapping relations to B
						for(int p : copy.get(s)) {
							for(int q : B.getSuccessors(p, a)) {
								// update the states for t
								if(! copy.get(t).get(q)) {
									changed = true;
									prefSim.get(t).set(q);
								}
							}
						}
					}
				}
			}
			// changed or not
			if(! changed ) {
				break;
			}
		}
	}
	
	public void compute_period_simulation() {
		
	}
	
	public static void main(String[] args) {
		
	}
	

}
