package roll.main.inclusion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.StateContainer;
import roll.main.Options;
import roll.parser.ba.PairParserBA;
import roll.util.Timer;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;

// This algorithm is inspired by simulation relation and the work 
//	Congruence Relations for B\"uchi Automata submitted to ICALP'21
// We actually can define congruence relations for language inclusion checking

public class CongruenceSimulation {
	
	NBA A;
	NBA B;
	
	// Simulation relation between A and B based on the congruence relations defined for B
	/**
	 * for each u, i_A - u -> q, i_B - u -> q', then we have q' simulates q for prefix u
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
	StateContainer[] aStates;
	
	/**
	 * for each v and final state q, then we have q' simulates q for period in B as follows:
	 * 
	 * q_A - v - > q'_A in A, then we need to have a path q_B - v -> q'_B
	 * q_A = v => q'_A in A (visiting accepting states), then we have q_B = v => q'_B
	 *
	 * */
	// only care about reachable states from q_A
	TIntObjectMap<HashSet<TreeSet<IntBoolTriple>>> periodSim;
	
	boolean antichain = false;
	
	CongruenceSimulation(NBA A, NBA B) {
		this.A = A;
		this.B = B;
		prefSim = new ArrayList<>();
		for(int s = 0; s < A.getStateSize(); s ++)
		{
			prefSim.add(new HashSet<>());
		}
		aStates = new StateContainer[A.getStateSize()];
		// compute the predecessors and successors
		for(int i = 0; i < A.getStateSize(); i ++) {
          aStates[i] = new StateContainer(i, A);
		}
		// initialize the information for B
		for (int i = 0; i < A.getStateSize(); i++) {
			StateNFA st = aStates[i].getState();
			for (int letter = 0; letter < A.getAlphabetSize(); letter++) {
				for (int succ : st.getSuccessors(letter)) {
					//aStates[i].addSuccessors(letter, succ);
					aStates[succ].addPredecessors(letter, i);
				}
			}
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
//					bStates[i].addSuccessors(letter, succ);
					bStates[succ].addPredecessors(letter, i);
				}
			}
		}
		periodSim = new TIntObjectHashMap<>();
	}
	
	public void outputPrefixSimulation() {
		for(int s = 0; s < A.getStateSize(); s ++)
		{
			// only i_B simulates i_A at first
			System.out.print("State " + s + "\n");
			for(ISet set : prefSim.get(s)) {
				System.out.println(set + ", ");
			}
		}
	}
		
	public void computePrefixSimulation() {
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
								//TODO: Antichain, only keep the set that are a subset of another
								if(antichain) {
									HashSet<ISet> curr = prefSim.get(t);
									HashSet<ISet> result = new HashSet<>();
									boolean contained = false;
									for(ISet sts: curr) {
										if(update.subsetOf(sts)) {
											// ignore sets that subsume update
											continue;
										}else if(sts.subsetOf(update)){
											contained = true;
											result.add(sts);
										}else {
											result.add(sts);
										}
									}
									if(! contained) {
										changed = true;
										result.add(update);
									}
									prefSim.set(t, result);
								}else {
									prefSim.get(t).add(update);
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
	
	// 
	HashSet<ISet> addSetToPrefixAntichain(HashSet<ISet> orig, ISet update, boolean[] changed) {
		boolean contained = false;
		HashSet<ISet> result = new HashSet<>();
		changed[0] = false;
		// a set corresponds to a class of finite prefixes to an accepting state in A
		for(ISet sts: orig) {
			if(update.subsetOf(sts)) {
				// ignore sets that subsume update
				continue;
			}else if(sts.subsetOf(update)){
				// updated should not be added into the hashset
				contained = true;
				result.add(sts);
			}else {
				// update and sts are incomparable
				result.add(sts);
			}
		}
		if(! contained) {
			changed[0] = true;
			result.add(update);
		}
		return result;
	}
	
	/**
	 * Only compute the states that can reach accState
	 * */
	public void computePrefixSimulation(int accState, ISet reachSet) {
		
		prefSim.clear();
		// initialization
		for(int s = 0; s < A.getStateSize(); s ++)
		{
			prefSim.add(new HashSet<>());
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
			for(int s : reachSet) {
				// tried to update successors
				ISet letters = A.getState(s).getEnabledLetters();
				// the letter is changed
				for(int a : letters) {
					for(int t : A.getState(s).getSuccessors(a)) {
						if(! reachSet.get(t)) continue;
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
								//TODO: Antichain, only keep the set that are not a subset of another
								if(antichain) {
									HashSet<ISet> orig = prefSim.get(t);
									boolean[] modified = new boolean[1];
									HashSet<ISet> result = addSetToPrefixAntichain(orig, update, modified);
									changed = modified[0];
									prefSim.set(t, result);
								}else {
									prefSim.get(t).add(update);
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
	
	private ISet getPredSet(int state, StateContainer[] states, NBA nba) {
		LinkedList<Integer> queue = new LinkedList<>();
        queue.add(state);
        ISet visited = UtilISet.newISet();
        visited.set(state);
        while(! queue.isEmpty()) {
        	int lState = queue.remove();
            // ignore unused states
            for(int c = 0; c < nba.getAlphabetSize(); c ++) {
                for(StateNFA lPred : states[lState].getPredecessors(c)) {
                    if(! visited.get(lPred.getId())) {
                        queue.add(lPred.getId());
                        visited.set(lPred.getId());
                    }
                }
            }
        }
        return visited;
	}
	
	private ISet getReachSet(int state, NBA nba) {
		LinkedList<Integer> queue = new LinkedList<>();
        queue.add(state);
        ISet visited = UtilISet.newISet();
        visited.set(state);
        while(! queue.isEmpty()) {
        	int lState = queue.remove();
            // ignore unused states
            for(int c = 0; c < nba.getAlphabetSize(); c ++) {
                for(int lSucc : nba.getSuccessors(lState, c)) {
                    if(! visited.get(lSucc)) {
                        queue.add(lSucc);
                        visited.set(lSucc);
                    }
                }
            }
        }
        return visited;
	}
	
	private ISet getReachSet(ISet states) {
		LinkedList<Integer> queue = new LinkedList<>();
        ISet visited = UtilISet.newISet();
        for(int state: states) {
        	queue.add(state);
        	visited.set(state);
        }
        while(! queue.isEmpty()) {
        	int lState = queue.remove();
            for(int c = 0; c < B.getAlphabetSize(); c ++) {
                for(int lSucc : B.getSuccessors(lState, c)) {
                	// ignore states that are already in the queue
                    if(! visited.get(lSucc)) {
                        queue.add(lSucc);
                        visited.set(lSucc);
                    }
                }
            }
        }
        return visited;
	}
	
	boolean containTriples(HashSet<TreeSet<IntBoolTriple>> sets, TreeSet<IntBoolTriple> set) {
		for(TreeSet<IntBoolTriple> s : sets) {
			if(s.equals(set)) {
				return true;
			}
		}
		return false;
	}
	
	HashSet<TreeSet<IntBoolTriple>> addSetToPeriodAntichain(HashSet<TreeSet<IntBoolTriple>> orig, TreeSet<IntBoolTriple> update, boolean[] changed) {
		HashSet<TreeSet<IntBoolTriple>> result = new HashSet<TreeSet<IntBoolTriple>>();
		boolean contained = false;
		for(TreeSet<IntBoolTriple> triples: orig) {
			if(triples.containsAll(update)) {
				// ignore sets that subsume update
				continue;
			}else if(update.containsAll(triples)) {
				// must add triples
				contained = true;
				result.add(triples);
			}else {
				result.add(triples);
			}
		}
		if(!contained) {
			changed[0] = true;
			result.add(update);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void computePeriodSimulation(int accState, ISet reachStatesInB) {
		periodSim.clear();
		// now compute every state that can be reached by accState
		ISet reachSet = getReachSet(accState);
		// only keep those state that can go back to accState
		ISet predSet = getPredSet(accState, aStates, A);
		reachSet.and(predSet);
		System.out.println("States for A: " + reachSet);
		System.out.println("States for B: " + reachStatesInB);
		// those can not be reached should corresponds to empty set
		for(int s : reachSet)
		{
			// only i_B simulates i_A at first
			periodSim.put(s, new HashSet<TreeSet<IntBoolTriple>>());
		}
		{
			// only care about reachable states from reachStatesInB
			int s = accState;
			// v must not be empty word
			for (int a : A.getState(s).getEnabledLetters()) {
				for (int t : A.getSuccessors(s, a)) {
					if(!reachSet.get(t)) continue;
					TreeSet<IntBoolTriple> set = new TreeSet<>();
					// s - a -> t
					for (int p : reachStatesInB) {
						for (int q : B.getSuccessors(p, a)) {
							// put every p - a -> q in f(t)
							boolean acc = B.isFinal(p) || B.isFinal(q);
							set.add(new IntBoolTriple(p, q, acc));
						}
					}
					//TODO: Antichain, only keep the set that are a subset of another
					if(antichain) {
						// keep subsets
						HashSet<TreeSet<IntBoolTriple>> curr = periodSim.get(t);
						boolean[] modified = new boolean[1];
						HashSet<TreeSet<IntBoolTriple>> result = addSetToPeriodAntichain(curr, set, modified);
						periodSim.put(t, result);
					}else {
						periodSim.get(t).add(set);	
					}
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
						if(!reachSet.get(t)) continue;
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
							if(! containTriples(copy.get(t), update)) {
								//TODO: Antichain, only keep the set that are a subset of another
								if(antichain) {
									HashSet<TreeSet<IntBoolTriple>> curr = periodSim.get(t);
									boolean[] modified = new boolean[1];
									HashSet<TreeSet<IntBoolTriple>> result = addSetToPeriodAntichain(curr, update, modified);
									changed = modified[0];
									periodSim.put(t, result);
								}else {
									periodSim.get(t).add(update);
								}
							}
						}
					}
				}
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
//		// checking accepting
//		// i_A -> q
//		HashSet<ISet> simPrefix = prefSim.get(accState);
//		for(ISet set : simPrefix) {
//			// q - u -> q
//			System.out.println("Simulated sets for A_state " + accState + ": " + set);
//			HashSet<TreeSet<IntBoolTriple>> simPeriod = periodSim.get(accState);
//			// decide whether there exists one accepting run in B
//			// must satisfy every set
//			System.out.println("Loop arrows for " + accState + " -> " + accState);
//			for(TreeSet<IntBoolTriple> setPeriod : simPeriod) {
//				System.out.println(setPeriod);
//			}
//		}
		
	}
	
	public boolean isEquvalent() {
		return false;
	}
	
	public boolean isIncluded() {
		
		// for each accepting state (should be reachable from the initial state and can reach itself)
		for(int accState : A.getFinalStates()) {
			
			System.out.println("Testing for accepting state " + accState);
			ISet reachSet = getReachSet(A.getInitialState(), A);
			// only keep those state that can go back to accState
			ISet predSet = getPredSet(accState, aStates, A);
			reachSet.and(predSet);
			if(!reachSet.get(A.getInitialState())) {
				continue;
			}
			computePrefixSimulation(accState, reachSet);
			//outputPrefixSimulation();
			
			// obtain the necessary part for accState
			HashSet<ISet> prefSims = prefSim.get(accState);
			ISet allSimulatedStatesInB = UtilISet.newISet();
			for(ISet sim: prefSims) {
				if(sim.isEmpty()) {
					// empty means some word to accState cannot be simulated
					return false;
				}
				allSimulatedStatesInB.or(sim);
			}
			allSimulatedStatesInB = getReachSet(allSimulatedStatesInB);
			System.out.println("Prefix simulated sets: " + prefSims);
			System.out.println("Necessary states for B: " + allSimulatedStatesInB);
			// now we compute the simulation for periods from accState
			computePeriodSimulation(accState, allSimulatedStatesInB);
			// now decide whether there is one word accepted by A but not B
			for(ISet pref: prefSims) {
				System.out.println("Simulated set in B: " + pref);
				for(TreeSet<IntBoolTriple> period: periodSim.get(accState)) {
					// decide whether this pref (period) is accepting in B
					if(! decideAcceptance(pref, period)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private TreeSet<IntBoolTriple> compose(TreeSet<IntBoolTriple> first, TreeSet<IntBoolTriple> second) {
		TreeSet<IntBoolTriple> result = new TreeSet<>();
		for(IntBoolTriple fstTriple: first) {
			for(IntBoolTriple sndTriple: second) {
				if(fstTriple.getRight() == sndTriple.getRight()) {
					result.add(new IntBoolTriple(fstTriple.getLeft()
							, sndTriple.getRight()
							, fstTriple.getBool() || sndTriple.getBool()));
				}
			}
		}
		return result;
	}
	
	// decide whether there exists an accepting run in B from states in sim
	private boolean decideAcceptance(ISet pref, TreeSet<IntBoolTriple> period) {
		for(int state: pref) {
			// iteratively check whether there exists a triple (q, q: true) reachable from state
			TreeSet<IntBoolTriple> reachSet = new TreeSet<>();
			reachSet.add(new IntBoolTriple(state, state, false));
			while(true) {
				// compute update
				int origSize = reachSet.size();
				TreeSet<IntBoolTriple> update = compose(reachSet, period);
				reachSet.addAll(update);
				// if we reach a fixed point
				if(origSize == reachSet.size()) {
					break;
				}
				// a triple (q, q: true) means that we have found an accepting run
				for(IntBoolTriple triple: reachSet) {
					if(triple.getLeft() == triple.getRight() && triple.getBool()) {
						return true;
					}
				}
			}	
		}
		return false;
	}

	public static void main(String[] args) {
		
//		Alphabet alphabet = new Alphabet();
//		alphabet.addLetter('a');
//		alphabet.addLetter('b');
//		NBA A = new NBA(alphabet);
//		A.createState();
//		A.createState();
//		A.getState(0).addTransition(0, 0);
//		A.getState(0).addTransition(1, 1);
//		A.getState(1).addTransition(1, 1);
//		A.setFinal(1);
//		A.setInitial(0);
//		
//		NBA B = new NBA(alphabet);
//		B.createState();
//		B.createState();
////		B.createState();
////		B.getState(0).addTransition(0, 0);
//		B.getState(0).addTransition(1, 1);
////		B.getState(0).addTransition(0, 2);
//		B.getState(1).addTransition(1, 1);
//		B.getState(1).addTransition(0, 1);
////		B.getState(2).addTransition(0, 2);
////		B.getState(2).addTransition(1, 2);
//
//		B.setFinal(1);
//		B.setInitial(0);
		
		System.out.println(args.length + " " + args[0]);
		
		Options options = new Options();
		PairParserBA pairParser = new PairParserBA(options, args[0], args[1]);
		NBA A = pairParser.getA();
		NBA B = pairParser.getB();
		System.out.println("#A = " + A.getStateSize() + ", #B = " + B.getStateSize());
		Timer timer = new Timer();
		timer.start();
		CongruenceSimulation sim = new CongruenceSimulation(A, B);
		sim.antichain = true;
		boolean included = sim.isIncluded();
		System.out.println(included ? "Included" : "Not included");
		timer.stop();
		System.out.println("Time elapsed " + timer.getTimeElapsed());
		
	}
	
	// In RABIT, this structure is called Arc
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
