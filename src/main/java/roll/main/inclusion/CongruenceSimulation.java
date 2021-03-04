/* Copyright (c) since 2016                                               */
/*       Institute of Software, Chinese Academy of Sciences               */
/* This file is part of ROLL, a Regular Omega Language Learning library.  */
/* ROLL is free software: you can redistribute it and/or modify           */
/* it under the terms of the GNU General Public License as published by   */
/* the Free Software Foundation, either version 3 of the License, or      */
/* (at your option) any later version.                                    */

/* This program is distributed in the hope that it will be useful,        */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          */
/* GNU General Public License for more details.                           */

/* You should have received a copy of the GNU General Public License      */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package roll.main.inclusion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.TreeSet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.StateContainer;
import roll.main.Options;
import roll.main.inclusion.run.SuccessorInfo;
import roll.parser.ba.PairParserBA;
import roll.util.Pair;
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
	 *  Assume that for a state q in A that can reach an accepting state f on u' and
	 *  currently we encode q as [S1, S2] such that S1 \subseteq S2 over states of B,
	 *  If S1 - u -> [], it means that for some word uu' from i_A to f where u corresponds to S1,
	 *  no states in B can simulate the word uu' from the initial state i_B. 
	 *  Therefore, for the state q, we can just remove S2 and only represent q with [S1].
	 *  
	 *  Similarly, we can do the same to period computation
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
	boolean debug = false;
		
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
				if(!set.isEmpty()) System.out.println(set + ", ");
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
									changed = true;
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
		if(debug) System.out.println("Reachable size: " + reachSet.cardinality());
		// compute simulation relation
		LinkedList<Integer> workList = new LinkedList<>();
		workList.add(A.getInitialState());
		ISet inWorkList = UtilISet.newISet();
		inWorkList.set(A.getInitialState());
		while(! workList.isEmpty()) {
			// take out one state
			int s = workList.removeFirst();
			inWorkList.clear(s);
			// the letter is changed
			for(int a : A.getState(s).getEnabledLetters()) {
				for(int t : A.getState(s).getSuccessors(a)) {
					if(! reachSet.get(t)) continue;
					// s - a - > t in A
					// f(s) - a -> P'
					// p \in f(s), then P' \subseteq f(t) in B
					// compute mapping relations to B
					// a set corresponds to a word u
					for(ISet set : prefSim.get(s)) {
						// for every set, we update the sets
						ISet update = UtilISet.newISet();
						for (int p : set) {
							for (int q : B.getSuccessors(p, a)) {
								// update the states for t
								update.set(q);
							}
						}
						// update is the word ua and check whether we need to update
						if (! prefSim.get(t).contains(update)) {
							//TODO: Antichain, only keep the set that are not a subset of another
							boolean changed = false;
							if(antichain) {
								HashSet<ISet> orig = prefSim.get(t);
								boolean[] modified = new boolean[1];
								HashSet<ISet> result = addSetToPrefixAntichain(orig, update, modified);
								changed = modified[0];
								prefSim.set(t, result);
							}else {
								prefSim.get(t).add(update);
								changed = true;
							}
							if(changed && !inWorkList.get(t)) {
								workList.addLast(t);
								inWorkList.set(t);
							}
						}
					}
				}
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
		ISet visited = UtilISet.newISet();
        queue.add(state);
        visited.set(state);
        boolean accLoop = false;
        while(! queue.isEmpty()) {
        	int lState = queue.remove();
            // ignore unused states
            for(int c = 0; c < nba.getAlphabetSize(); c ++) {
                for(StateNFA lPred : states[lState].getPredecessors(c)) {
                    if(! visited.get(lPred.getId())) {
                        queue.add(lPred.getId());
                        visited.set(lPred.getId());
                    }else if(lPred.getId() == state) {
                    	// the state can reach itself
                    	if(debug) System.out.println("Found loop from state " + state);
                    	accLoop = true;
                    }
                }
            }
        }
        if(! accLoop) {
        	visited.clear(state);
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
	
	// the Input simulatedStatesInB can simulate accState
	@SuppressWarnings("unchecked")
	public void computePeriodSimulation(int accState, ISet simulatedStatesInB) {
		periodSim.clear();
		// now compute every state that can be reached by accState
		ISet reachSet = getReachSet(accState);
		// only keep those state that can go back to accState
		ISet predSet = getPredSet(accState, aStates, A);
		reachSet.and(predSet);
		if(debug) System.out.println("States for A: " + reachSet);
		if(debug) System.out.println("States for B: " + simulatedStatesInB);
		// those can not be reached should corresponds to empty set
		for(int s : reachSet)
		{
			// only i_B simulates i_A at first
			periodSim.put(s, new HashSet<TreeSet<IntBoolTriple>>());
		}
		LinkedList<Integer> workList = new LinkedList<>();
		ISet inWorkList = UtilISet.newISet();
		System.out.println("Start computing the congruence representation for accepting state " + accState + " ...");
		// 1. initialization
		{
			// only care about states from simulatedStatesInB
			int s = accState;
			// v must not be empty word
			for (int a : A.getState(s).getEnabledLetters()) {
				for (int t : A.getSuccessors(s, a)) {
					if(!reachSet.get(t)) continue;
					// add to worklist
					workList.add(t);
					inWorkList.set(t);
					// compute the simulation relations
					TreeSet<IntBoolTriple> set = new TreeSet<>();
					// s - a -> t
					for (int p : simulatedStatesInB) {
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
		// 2. computation of simulated relations
		while(! workList.isEmpty()) {
			int s = workList.removeFirst();
			inWorkList.clear(s);
			// update for successors
			for(int a : A.getState(s).getEnabledLetters()) {
				for(int t : A.getSuccessors(s, a)) {
					// Again, ignore states that cannot reach accState
					if(!reachSet.get(t)) continue;
					// s - a -> t
					for(TreeSet<IntBoolTriple> set: periodSim.get(s)) {
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
						if(! containTriples(periodSim.get(t), update)) {
							//TODO: Antichain, only keep the set that are a subset of another
							boolean changed = false;
							if(antichain) {
								HashSet<TreeSet<IntBoolTriple>> curr = periodSim.get(t);
								boolean[] modified = new boolean[1];
								HashSet<TreeSet<IntBoolTriple>> result = addSetToPeriodAntichain(curr, update, modified);
								changed = modified[0];
								periodSim.put(t, result);
							}else {
								changed = true;
								periodSim.get(t).add(update);
							}
							if(changed && !inWorkList.get(t)) {
								workList.add(t);
								inWorkList.set(t);
							}
						}
					}
				}
			}
		}
		System.out.println("Finished computing the congruence representation for accepting state " + accState + " ...");		
	}
	
	public boolean isEquvalent() {
		return false;
	}
	
	private boolean coveredBy(Pair<HashSet<ISet>, HashSet<TreeSet<IntBoolTriple>>> left
			, Pair<HashSet<ISet>, HashSet<TreeSet<IntBoolTriple>>> right) {
		// check whether left is coveredby right
		// first, check prefix
		for(ISet lSet : left.getLeft()) {
			boolean covered = false;
			for(ISet rSet : right.getLeft()) {
				if(rSet.subsetOf(lSet)) {
					covered = true;
					break;
				}
			}
			if(! covered) {
				return false;
			}
		}
		for(TreeSet<IntBoolTriple> lTriples : left.getRight()) {
			boolean covered = false;
			for(TreeSet<IntBoolTriple> rTriples : right.getRight()) {
				if(lTriples.containsAll(rTriples)) {
					covered = true;
					break;
				}
			}
			if(! covered) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isIncluded() {
		
		// for each accepting state (should be reachable from the initial state and can reach itself)
		ISet reachSet = getReachSet(A.getInitialState(), A);
		int countStates = 0;
//		LinkedList<Pair<HashSet<ISet>, HashSet<TreeSet<IntBoolTriple>>>> antichainFinals = new LinkedList<>();
		long timeForAcceptance = 0;
		for(int accState : A.getFinalStates()) {
			countStates ++;
			System.out.println("Checking for "+ countStates + "-th accepting state " + accState + " out of " + A.getFinalStates().cardinality() + " states");
			// reachable states from the initial state
			ISet necessaryStates = reachSet.clone();
			// only keep those state that can go back to accState
			ISet predSet = getPredSet(accState, aStates, A);
			necessaryStates.and(predSet);
			// if the initial state cannot reach the accepting state or the accepting state cannot reach itself
			//, then language is empty
			if(!necessaryStates.get(A.getInitialState()) || !necessaryStates.get(accState)) {
				continue;
			}
			if(debug) System.out.println("Necessary states in A: " + necessaryStates + " #size = " + necessaryStates.cardinality());
			computePrefixSimulation(accState, necessaryStates);
			//outputPrefixSimulation();
			for(int i = 0; i < A.getStateSize() && debug; i ++) {
				System.out.println("state " + i + " -> " + prefSim.get(i));
			}
//			System.exit(-1);
			// obtain the necessary part for accState
			HashSet<ISet> prefSims = prefSim.get(accState);
			if(debug) System.out.println("Acc simulated sets: " + prefSims);
			if(prefSims.isEmpty()) {
				return false;
			}
			// only keep the sets that are subset of another
			HashSet<ISet> antichainPrefix = new HashSet<>();
			// compute antichain
			for(ISet sim1: prefSims) {
				boolean subsumes = false;
				for(ISet sim2: prefSims) {
					// ignore itself
					if(sim1 == sim2) continue;
					// only keep those that do not subsume others
					if(sim2.subsetOf(sim1)) {
						subsumes = true;
						break;
					}
				}
				if(! subsumes) {
					// not subsume others
					antichainPrefix.add(sim1);
				}
			}
			ISet simulatedStatesInB = UtilISet.newISet();
			for(ISet sim: antichainPrefix) {
				if(sim.isEmpty()) {
					// empty means some word to accState cannot be simulated
					//computeCounterexamplePrefix(accState);
					return false;
				}
				simulatedStatesInB.or(sim);
			}
			//simulatedStatesInB = getReachSet(simulatedStatesInB);
			if(debug) System.out.println("Prefix simulated sets: " + antichainPrefix);
			if(debug) System.out.println("Necessary states for B: " + simulatedStatesInB);
			// now we compute the simulation for periods from accState
			computePeriodSimulation(accState, simulatedStatesInB);
			// now decide whether there is one word accepted by A but not B
			System.out.println("Deciding the language inclusion between L(A^i_f) (A^f_f)^w and L(B) ...");
			Timer timer = new Timer();
			timer.start();
			for(ISet pref: antichainPrefix) {
				if(debug) System.out.println("Simulated set in B: " + pref);
//				computePeriodSimulation(accState, pref);
				// compute antichain
				HashSet<TreeSet<IntBoolTriple>> antichainPeriod = new HashSet<>();
				for(TreeSet<IntBoolTriple> period1: periodSim.get(accState)) {
					boolean subsumes = false;
					for(TreeSet<IntBoolTriple> period2: periodSim.get(accState)) {
						if(period1 == period2) continue;
						if(period1.containsAll(period2)) {
							subsumes = true;
							break;
						}
					}
					if(!subsumes) {
						antichainPeriod.add(period1);
					}
				}
				if(antichainPeriod.contains(new TreeSet<>())) {
					// empty means some word from accState to itself cannot be simulated
					return false;
				}
				for(TreeSet<IntBoolTriple> period: antichainPeriod) {
					// decide whether this pref (period) is accepting in B
					if(! decideAcceptance(pref, period)) {
						return false;
					}
				}
			}
			timer.stop();
			timeForAcceptance += timer.getTimeElapsed();
//			antichainFinals.add(new Pair(prefSim.get(accState), periodSim.get(accState)));
		}
		System.out.println("Time elapsed for deciding acceptance: " + timeForAcceptance);
		return true;
	}
	
	private SuccessorInfo getSuccessorInfo(TIntObjectMap<SuccessorInfo> map, int state) {
		if(map.containsKey(state)) {
            return map.get(state);
        }
        SuccessorInfo succInfo = new SuccessorInfo(state);
        map.put(state, succInfo);
        return succInfo;
	}
	
	private void computeCounterexamplePrefix(int accState) {
		LinkedList<Integer> word = new LinkedList<Integer>();
		// we make use of aStates to compute a word that can not be simulated by B
		//1. First, find the word that precessor has empty set
		PriorityQueue<SuccessorInfo> workList = new PriorityQueue<>();
        // input source states
		TIntObjectMap<SuccessorInfo> map = new TIntObjectHashMap<>();
		SuccessorInfo accSuccInfo = getSuccessorInfo(map, accState);
        workList.add(accSuccInfo);
        accSuccInfo.distance = 0;
        
        ISet visited = UtilISet.newISet();
        while(! workList.isEmpty()) {
            SuccessorInfo currInfo = workList.remove(); 
            if(visited.get(currInfo.state)) {
                continue;
            }
            if(currInfo.state == A.getInitialState()) {
                break;
            }
            if(currInfo.unreachable()) {
                assert false : "Unreachable state";
            }
            // update distance of successors
            for(int letter = 0; letter < A.getAlphabetSize(); letter ++) {
            	HashSet<ISet> currSims = prefSim.get(currInfo.state);
                for(final StateNFA pred : aStates[currInfo.state].getPredecessors(letter)) {
                	// first check whether it also contains empty set
                	HashSet<ISet> prefSims = prefSim.get(pred.getId());
                	
                    SuccessorInfo predInfo = getSuccessorInfo(map, pred.getId());
                    int distance = currInfo.distance + 1;
                    if(!visited.get(pred.getId()) && predInfo.distance > distance) {
                        // update predInfo
                    	predInfo.letter = letter;
                    	predInfo.distance = distance;
                    	predInfo.predState = currInfo.state;
                        workList.remove(predInfo);
                        workList.add(predInfo);
                    }
                }
            }
        }
        
        // construct loop
        
		
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
