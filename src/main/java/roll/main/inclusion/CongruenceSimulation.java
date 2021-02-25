package roll.main.inclusion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.StateContainer;
import roll.util.sets.ISet;
import roll.util.sets.ISetTreeSet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;

// This algorithm is inspired by simulation relation and the work 
//	Congruence Relations for B\"uchi Automata submitted to ICALP'21

public class CongruenceSimulation {
	
	NBA A;
	NBA B;
	
	// Simulation relation between A and B based on the congruence relations defined for B
	// Note that B must be complete
	/**
	 * for each u, i_A - u -> q, i_B - u -> q', then we have q' simulates q for prefix
	 * Here we actually define congruence relations for states in B with respect to states in A
	 * 
	 * That is, for prefix, we have (s_A, Set_B_states) for an equivalence class [u]
	 * if s_A = i_A, then Set_B_states = {i_B}
	 * otherwise, for each state s_A in A, if i_A - u -> s_A, then there must exist a state t in Set_B_states,
	 *  such that i_B - u -> t.
	 *  
	 * */
	ArrayList<HashSet<ISet>> prefSim;
	StateContainer[] bStates;
	
	/**
	 * for each v and final state q, then we have q' simulates q for period in B as follows:
	 * 
	 * q_A - v - > q'_A in A, then we need to have a path q_B - v -> q'_B
	 * q_A = v => q'_A in A (visiting accepting states), then we have q_B = v => q'_B
	 *
	 * */
	// only care about reachable states from q_A
	TIntObjectMap<HashSet<TreeSet<IntBoolTriple>>> periodSim;
	
	CongruenceSimulation(NBA A, NBA B) {
		this.A = A;
		this.B = B;
		prefSim = new ArrayList<>();
		for(int s = 0; s < A.getStateSize(); s ++)
		{
			prefSim.add(new HashSet<>());
		}
		bStates = new StateContainer[B.getStateSize()];
		// compute the predecessors and successors
		for(int i = 0; i < B.getStateSize(); i ++) {
          bStates[i] = new StateContainer(i, B);
		}
		// initialize the information for B
		for (int i = 0; i < B.getStateSize(); i++) {
			StateNFA st = bStates[i].getState();
			for (int letter = 0; letter < B.getAlphabetSize(); letter++) {
				for (int succ : st.getSuccessors(letter)) {
					bStates[i].addSuccessors(letter, succ);
					bStates[succ].addPredecessors(letter, i);
				}
			}
		}
		periodSim = new TIntObjectHashMap<>();
	}
	
	public void output_prefix_simulation() {
		for(int s = 0; s < A.getStateSize(); s ++)
		{
			// only i_B simulates i_A at first
			System.out.print("State " + s + "\n");
			for(ISet set : prefSim.get(s)) {
				System.out.println(set + ", ");
			}
		}
	}
		
	public void compute_prefix_simulation() {
		// initialization
		for(int s = 0; s < A.getStateSize(); s ++)
		{
			// only i_B simulates i_A at first
			if(s == A.getInitialState()) {
				ISet set = UtilISet.newISet();
				set.set(B.getInitialState());
				prefSim.get(s).add(set);
			}
		}
		// compute simulation relation
		while(true) {
			// copy the first one
			boolean changed = false;
			ArrayList<HashSet<ISet>> copy = new ArrayList<HashSet<ISet>>();
			for(int s = 0; s < A.getStateSize(); s ++) {
				HashSet<ISet> sets = prefSim.get(s);
				copy.add(new HashSet<>());
				for(ISet set : sets) {
					// now sets will not be changed anymore
					copy.get(s).add(set);
				}
			}
			// compute relations 
			for(int s = 0; s < A.getStateSize(); s++) {
				// tried to update successors
				ISet letters = A.getState(s).getEnabledLetters();
				// the letter is changed
				for(int a : letters) {
					for(int t : A.getState(s).getSuccessors(a)) {
						// s - a - > t in A
						// f(s) - a -> P'
						// p \in f(s), then P' \subseteq f(t) in B
						// compute mapping relations to B
						for(ISet set : copy.get(s)) {
							// for every set, we update the sets
							ISet update = UtilISet.newISet();
							for (int p : set) {
								for (int q : B.getSuccessors(p, a)) {
									// update the states for t
									update.set(q);
								}
							}
							// check whether we need to update
							if (!copy.get(t).contains(update)) {
								changed = true;
								prefSim.get(t).add(update);
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
	
	private ISet getReachSet(int state) {
		
		LinkedList<Integer> queue = new LinkedList<>();
        queue.add(state);
        ISet visited = UtilISet.newISet();
        visited.set(state);
        while(! queue.isEmpty()) {
        	int lState = queue.remove();
            // ignore unused states
            for(int c = 0; c < B.getAlphabetSize(); c ++) {
                for(int lSucc : B.getSuccessors(lState, c)) {
                    if(! visited.get(lSucc)) {
                        queue.add(lSucc);
                        visited.set(lSucc);
                    }
                }
            }
        }
        return visited;
	}
	
	boolean containsTriples(HashSet<TreeSet<IntBoolTriple>> sets, TreeSet<IntBoolTriple> set) {
		for(TreeSet<IntBoolTriple> s : sets) {
			if(s.equals(set)) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public void compute_period_simulation(int accState) {
		
		periodSim.clear();
		// now compute every state that can be reached by accState
		ISet reachSet = getReachSet(accState);
		// those can not be reached should corresponds to empty set
		for(int s : reachSet)
		{
			// only i_B simulates i_A at first
			periodSim.put(s, new HashSet<TreeSet<IntBoolTriple>>());
		}
		{
			int s = accState;
			// v must not be empty word
			for (int a : A.getState(s).getEnabledLetters()) {
				for (int t : A.getSuccessors(s, a)) {
					TreeSet<IntBoolTriple> set = new TreeSet<>();
					// s - a -> t
					for (int p = 0; p < B.getStateSize(); p++) {
						for (int q : B.getSuccessors(p, a)) {
							// put every p - a -> q in f(t)
							boolean acc = B.isFinal(p) || B.isFinal(q);
							set.add(new IntBoolTriple(p, q, acc));
						}
					}
					periodSim.get(t).add(set);
				}
			}
		}
		// compute simulation relation
		while(true) {
			// copy the first one
			boolean changed = false;
			TIntObjectMap<HashSet<TreeSet<IntBoolTriple>>> copy = new TIntObjectHashMap<>();
			for(int s : reachSet) {
				copy.put(s, new HashSet<TreeSet<IntBoolTriple>>());
				for(TreeSet<IntBoolTriple> set: periodSim.get(s)) {
					copy.get(s).add(set);
				}
			}
			for(int s : reachSet)
			{
				for(int a : A.getState(s).getEnabledLetters()) {
					for(int t : A.getSuccessors(s, a)) {
						// s - a -> t
						for(TreeSet<IntBoolTriple> set: copy.get(s)) {
							TreeSet<IntBoolTriple> update = new TreeSet<>();
							// put sets
							for(IntBoolTriple triple : set) {
								int p = triple.getLeft();
								int q = triple.getRight();
								for(int qr : B.getSuccessors(q, a)) {
									boolean acc = B.isFinal(qr) || triple.getBool();
									IntBoolTriple newTriple  = new IntBoolTriple(p, qr, acc);
									update.add(newTriple);
								}
							}
							// we have extended for set
							if(! containsTriples(copy.get(t), update)) {
								changed = true;
								periodSim.get(t).add(update);
							}
						}
					}
				}
			}
			boolean eq = false;
			for(int s : reachSet) {
				
			}
			if(! changed) {
				break;
			}
		}
		
		System.out.println("Period for state " + accState);
		for(int s : reachSet)
		{
			// only i_B simulates i_A at first
			System.out.print("State " + s + "\n");
			
			System.out.println(periodSim.get(s));
		}
		// checking accepting
		// i_A -> q
		HashSet<ISet> simPrefix = prefSim.get(accState);
		for(ISet set : simPrefix) {
			// q - u -> q
			System.out.println("Simulated sets for A_state " + accState + ": " + set);
			HashSet<TreeSet<IntBoolTriple>> simPeriod = periodSim.get(accState);
			// decide whether there exists one accepting run in B
			// must satisfy every set
			System.out.println("Loop arrows for " + accState + " -> " + accState);
			for(TreeSet<IntBoolTriple> setPeriod : simPeriod) {
				System.out.println(setPeriod);
			}
		}
		
	}
	
	public static void main(String[] args) {
		
		Alphabet alphabet = new Alphabet();
		alphabet.addLetter('a');
		alphabet.addLetter('b');
		NBA A = new NBA(alphabet);
		A.createState();
		A.createState();
		A.getState(0).addTransition(0, 0);
		A.getState(0).addTransition(1, 1);
		A.getState(1).addTransition(1, 1);
		A.setFinal(1);
		A.setInitial(0);
		
		NBA B = new NBA(alphabet);
		B.createState();
		B.createState();
		B.createState();
		B.getState(0).addTransition(1, 1);
		B.getState(0).addTransition(0, 2);
		B.getState(1).addTransition(1, 1);
		B.getState(1).addTransition(0, 1);
		B.getState(2).addTransition(0, 2);
		B.getState(2).addTransition(1, 2);

		B.setFinal(1);
		B.setInitial(0);
		
		CongruenceSimulation sim = new CongruenceSimulation(A, B);
		sim.compute_prefix_simulation();
		sim.output_prefix_simulation();
		
		for(int f : A.getFinalStates()) {
			sim.compute_period_simulation(f);
			// check the accepting words for (f, f)
		}
		
	}
	
	
	public class IntBoolTriple implements Comparable<IntBoolTriple> {
		
		private int left;
		private int right;
		private boolean acc;
		
		public IntBoolTriple(int left, int right, boolean acc) {
			this.left = left;
			this.right = right;
			this.acc = acc;
		}
		
		public int getLeft() {
			return this.left;
		}
		
		public int getRight() {
			return this.right;
		}
		
		public boolean getBool() {
			return this.acc;
		}
		
		@Override
		public boolean equals(Object obj) {
		    if(this == obj) return true;
		    if(obj == null) return false;
			if(obj instanceof IntBoolTriple) {
				@SuppressWarnings("unchecked")
				IntBoolTriple p = (IntBoolTriple)obj;
				return p.left == left 
					&& p.right == right
					&& p.acc == acc; 
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "(" + left + ", " + right + ": "+ acc +")";
		}

		@Override
		public int compareTo(IntBoolTriple other) {
			if(this.left != other.left) {
				return this.left - other.left;
			}
			assert (this.left == other.left);
			if(this.right != other.right) {
				return this.right - other.right;
			}
			assert (this.right == other.right);
			int lBool = this.acc ? 1 : 0;
			int rBool = other.acc ? 1 : 0;
			return lBool - rBool;
		}

	}

	

}
