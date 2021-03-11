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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeSet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.NBALasso;
import roll.automata.operations.StateContainer;
import roll.automata.operations.TarjanSCCs;
import roll.automata.operations.TarjanSCCsNonrecursive;
import roll.main.Options;
import roll.main.inclusion.run.SuccessorInfo;
import roll.parser.ba.PairParserBA;
import roll.util.Pair;
import roll.util.Timer;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Word;

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
	
	Word prefix = null;
	Word period = null;
	
	// p - a -> q cannot be simulated with transitions in B
	int aPState = -1;
	int aQState = -1;
	int aLetter = -1;
	boolean earlyTerminated = false;
	ISet bSetForP;
	
	boolean computeCounterexample = false;
	
	HashMap<Pair<Integer, ISet>, Word> prefWordMap;
	
	HashMap<Pair<Integer, TreeSet<IntBoolTriple>>, Word> periodWordMap;
		
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
	
	
	public Pair<Word, Word> getCounterexample() {
		assert prefix != null && period != null;
		return new Pair<>(prefix, period);
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
			
	// 
	HashSet<ISet> addSetToPrefixAntichain(HashSet<ISet> orig, ISet update, HashSet<ISet> subsetOfUpdate, boolean[] changed) {
		boolean contained = false;
		HashSet<ISet> result = new HashSet<>();
		changed[0] = false;
		// a set corresponds to a class of finite prefixes to an accepting state in A
		for(ISet sts: orig) {
			if(update.subsetOf(sts)) {
				// ignore sets that subsume update
				if(computeCounterexample) {
					subsetOfUpdate.add(sts);
				}
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
		if(computeCounterexample) {
			this.prefWordMap = new HashMap<>();
		}
		Timer timer = new Timer();
		timer.start();
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
				if(computeCounterexample) {
					this.prefWordMap.put(new Pair<>(s,  set), A.getAlphabet().getEmptyWord());
				}
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
			LinkedList<Pair<Integer, ISet>> removedPairs = new LinkedList<>();
			for(int a : A.getState(s).getEnabledLetters()) {
				for(int t : A.getState(s).getSuccessors(a)) {
					if(! reachSet.get(t)) continue;
					// s - a - > t in A
					// f(s) - a -> P'
					// p \in f(s), then P' \subseteq f(t) in B
					// compute mapping relations to B
					// a set corresponds to a word u
					HashSet<ISet> copy = new HashSet<>();
					for(ISet set : prefSim.get(s)) {
						copy.add(set);
					}
					for(ISet set : copy) {
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
								HashSet<ISet> subsetsOfUpdate = new HashSet<>();
								HashSet<ISet> result = addSetToPrefixAntichain(orig, update, subsetsOfUpdate, modified);
								changed = modified[0];
								prefSim.set(t, result);
								if(changed && computeCounterexample) {
									Pair<Integer, ISet> pair = new Pair<>(s, set);
									
									Word pref = this.prefWordMap.get(pair);
						
									if(pref == null) {
										for(Entry<Pair<Integer, ISet>, Word> entry : this.prefWordMap.entrySet()) {
											System.out.println();
										}
									}
									Word newPref = pref.append(a);
									this.prefWordMap.put(new Pair<>(t,  update), newPref);
									for(ISet subset : subsetsOfUpdate) {
										removedPairs.add(new Pair<>(t, subset));
									}
								}
							}else {
								prefSim.get(t).add(update);
								changed = true;
							}
							if(changed && !inWorkList.get(t)) {
								workList.addLast(t);
								inWorkList.set(t);
							}
						}
						// detected that update is empty for the first time
						// now need to update 
						if(update.isEmpty()) {
							aPState = s;
							aQState = t;
							aLetter = a;
							earlyTerminated = true;
							bSetForP = set;
							break;
						}
					}
					if(earlyTerminated) {
						break;
					}
				}
				if(earlyTerminated) {
					break;
				}
			}
			if(earlyTerminated) {
				prefSim.get(accState).add(UtilISet.newISet());
				break;
			}
			for(Pair<Integer, ISet> pair : removedPairs) {
				prefWordMap.remove(pair);
			}
		}
		timer.stop();
		this.timeForPrefixSim += timer.getTimeElapsed();
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
	
	HashSet<TreeSet<IntBoolTriple>> addSetToPeriodAntichain(HashSet<TreeSet<IntBoolTriple>> orig, TreeSet<IntBoolTriple> update
			, HashSet<TreeSet<IntBoolTriple>> subsetOfUpdate, boolean[] changed) {
		HashSet<TreeSet<IntBoolTriple>> result = new HashSet<TreeSet<IntBoolTriple>>();
		boolean contained = false;
		for(TreeSet<IntBoolTriple> triples: orig) {
			if(triples.containsAll(update)) {
				// ignore sets that subsume update
				if(computeCounterexample) {
					subsetOfUpdate.add(triples);
				}
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
	
	private long timeForPrefixSim = 0;
	private long timeForPeriodSim = 0;
	
	
	
	// Always guarantte that if there is (q, r: true), then no (q, r: false) appears
	private void addTriple(TreeSet<IntBoolTriple> set, IntBoolTriple triple) {
		IntBoolTriple revTriple = new IntBoolTriple(triple.left, triple.right, !triple.acc);
		boolean containedRev = set.contains(revTriple);
		if(containedRev) {
			if(triple.acc) {
				set.remove(revTriple);
				set.add(triple);
			}else {
				// do nothing, keep the original one
			}
		}else {
			set.add(triple);
		}
	}
	
	// the Input simulatedStatesInB can simulate accState
	@SuppressWarnings("unchecked")
	public void computePeriodSimulation(int accState, ISet simulatedStatesInB, ISet bReachSet) {
		if(computeCounterexample) {
			this.periodWordMap = new HashMap<>();
		}
		Timer timer = new Timer();
		timer.start();
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
		System.out.println("Computing the congruence representation of periods for accepting state " + accState + " ...");
		// 1. initialization
		{
			// only care about states from simulatedStatesInB
			int s = accState;
			LinkedList<Pair<Integer, TreeSet<IntBoolTriple>>> removedPairs = new LinkedList<>();
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
							if(!bReachSet.get(q)) continue;
							// put every p - a -> q in f(t)
							boolean acc = B.isFinal(p) || B.isFinal(q);
							addTriple(set, new IntBoolTriple(p, q, acc));
						}
					}
					//TODO: Antichain, only keep the set that are a subset of another
					if(antichain) {
						// keep subsets
						HashSet<TreeSet<IntBoolTriple>> curr = periodSim.get(t);
						boolean[] modified = new boolean[1];
						HashSet<TreeSet<IntBoolTriple>> subsetOfUpdate = new HashSet<>();
						HashSet<TreeSet<IntBoolTriple>> result = addSetToPeriodAntichain(curr, set, subsetOfUpdate, modified);
						periodSim.put(t, result);
						if(modified[0] && computeCounterexample) {
							Word pref = A.getAlphabet().getLetterWord(a);
							this.periodWordMap.put(new Pair<>(t,  set), pref);
							for(TreeSet<IntBoolTriple> key : subsetOfUpdate) {
								removedPairs.add(new Pair<>(t, key));
							}
						}
					}else {
						periodSim.get(t).add(set);	
					}
				}
			}
			for(Pair<Integer, TreeSet<IntBoolTriple>> pair : removedPairs) {
				this.periodWordMap.remove(pair);
			}
		}
		// 2. computation of simulated relations
		while(! workList.isEmpty()) {
			int s = workList.removeFirst();
			inWorkList.clear(s);
			LinkedList<Pair<Integer, TreeSet<IntBoolTriple>>> removedPairs = new LinkedList<>();
			// update for successors
			for(int a : A.getState(s).getEnabledLetters()) {
				for(int t : A.getSuccessors(s, a)) {
					// Again, ignore states that cannot reach accState
					if(!reachSet.get(t)) continue;
					// s - a -> t
					HashSet<TreeSet<IntBoolTriple>> copy = new HashSet<>();
					for(TreeSet<IntBoolTriple> set: periodSim.get(s)) {
						copy.add(set);
					}
					for(TreeSet<IntBoolTriple> set: copy) {
						TreeSet<IntBoolTriple> update = new TreeSet<>();
						// put sets
						for(IntBoolTriple triple : set) {
							int p = triple.getLeft();
							int q = triple.getRight();
							for(int qr : B.getSuccessors(q, a)) {
								if(!bReachSet.get(q)) continue;
								boolean acc = B.isFinal(qr) || triple.getBool();
								IntBoolTriple newTriple  = new IntBoolTriple(p, qr, acc);
								addTriple(update, newTriple);
							}
						}
						// we have extended for set
						if(! containTriples(periodSim.get(t), update)) {
							//TODO: Antichain, only keep the set that are a subset of another
							boolean changed = false;
							if(antichain) {
								HashSet<TreeSet<IntBoolTriple>> curr = periodSim.get(t);
								boolean[] modified = new boolean[1];
								HashSet<TreeSet<IntBoolTriple>> subsetOfUpdate = new HashSet<>();
								HashSet<TreeSet<IntBoolTriple>> result = addSetToPeriodAntichain(curr, update, subsetOfUpdate, modified);
								changed = modified[0];
								periodSim.put(t, result);
								if(modified[0] && computeCounterexample) {
									Word pref = this.periodWordMap.get(new Pair<>(s, set));
									Word newPref = pref.append(a);
									this.periodWordMap.put(new Pair<>(t,  update), newPref);
									for(TreeSet<IntBoolTriple> key : subsetOfUpdate) {
										removedPairs.add(new Pair<>(t, key));
									}
								}
							}else {
								changed = true;
								periodSim.get(t).add(update);
							}
							if(changed && !inWorkList.get(t)) {
								workList.add(t);
								inWorkList.set(t);
							}
//							if(t == accState) {
//								System.out.println("AccState Sim: \n" + periodSim.get(accState));
//							}
						}
						// not possible
						if(update.isEmpty()) {
							aPState = s;
							aQState = t;
							aLetter = a;
							earlyTerminated = true;
							break;
						}
					}
					if(earlyTerminated) {
						break;
					}
				}
				if(earlyTerminated) {
					break;
				}
			}
			for(Pair<Integer, TreeSet<IntBoolTriple>> pair : removedPairs) {
				this.periodWordMap.remove(pair);
			}
			if(earlyTerminated) {
				periodSim.get(accState).add(new TreeSet<>());
				System.out.println("Early termination in computing representation of periods.");
				break;
			}
		}
		timer.stop();
		this.timeForPeriodSim += timer.getTimeElapsed();
//		System.out.println("Finished computing the congruence representation for accepting state " + accState + " ...");		
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
				//System.out.println("Ignored the accepting state " + accState + "");
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
				// any word that reaches accState will do
				assert false;
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
					System.out.println("Computing counterexample ...");
					if(computeCounterexample) computeCounterexamplePrefix(accState, necessaryStates);
					System.out.println("Prefix: " + prefix);
					System.out.println("Period: " + period);
					return false;
				}
				simulatedStatesInB.or(sim);
			}
			//simulatedStatesInB = getReachSet(simulatedStatesInB);
			if(debug) System.out.println("Prefix simulated sets: " + antichainPrefix);
			if(debug) System.out.println("Necessary states for B: " + simulatedStatesInB);
			// now we compute the simulation for periods from accState
			System.out.println("pref rep: " + antichainPrefix + " -> " + simulatedStatesInB);
//			TarjanSCCs sccs = new TarjanSCCs(B, simulatedStatesInB);
			ISet allowSccs = UtilISet.newISet();
			ISet bFinals = B.getFinalStates();			
			System.out.println("Final states: " + bFinals);
//			for(ISet scc : sccs.getSCCs()) {
//				System.out.println("SCC: " + scc);
//				if(scc.overlap(simulatedStatesInB) && scc.overlap(bFinals)) {
//					allowSccs.or(scc);
//				}
//			}
			System.out.println("Nonrecursive: ");
			TarjanSCCsNonrecursive sccNonrecur = new TarjanSCCsNonrecursive(B, simulatedStatesInB);;
			for(ISet scc : sccNonrecur.getSCCs()) {
				System.out.println("SCC: " + scc);
				if(scc.overlap(simulatedStatesInB) && scc.overlap(bFinals)) {
					allowSccs.or(scc);
				}
			}
			System.out.println("pref rep: " + antichainPrefix);
			System.out.println("pref bReach: " + allowSccs);
			simulatedStatesInB.and(allowSccs);
			System.out.println("simulated states in B: " + simulatedStatesInB);
//			System.out.println("Final states: " + B.getFinalStates());
			computePeriodSimulation(accState, simulatedStatesInB, allowSccs);
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
					computeCounterexamplePeriod(accState, necessaryStates);
					return false;
				}
				for(TreeSet<IntBoolTriple> period: antichainPeriod) {
					// decide whether this pref (period) is accepting in B
					if(! decideAcceptance(pref, period)) {
						// we need to construct a counterexample here
						if(computeCounterexample) computeCounterexample(accState, pref, period, necessaryStates);
						return false;
					}
				}
			}
			timer.stop();
			timeForAcceptance += timer.getTimeElapsed();
//			antichainFinals.add(new Pair(prefSim.get(accState), periodSim.get(accState)));
		}
		System.out.println("Time for deciding acceptance: " + timeForAcceptance);
		System.out.println("Time for computing prefix simulation: " + this.timeForPrefixSim);
		System.out.println("Time for computing period simulation: " + this.timeForPeriodSim);

		return true;
	}

	private void computeCounterexample(int accState, ISet pref, TreeSet<IntBoolTriple> period, ISet aReachSet) {
		// first, compute the word to this pref
		// construct prefix
		this.prefix = this.prefWordMap.get(new Pair<>(accState, pref));
		this.period = this.periodWordMap.get(new Pair<>(accState, period));
	}

	// goal must be reachable from start
	private Word computeWordInA(int start, int goal) {
		PriorityQueue<SuccessorInfo<Integer>> queue = new PriorityQueue<SuccessorInfo<Integer>>();
		SuccessorInfo<Integer> info = new SuccessorInfo<Integer>(start);
		info.word = A.getAlphabet().getEmptyWord();
		queue.add(info);
		ISet visited = UtilISet.newISet();
		visited.set(start);
		
		while(! queue.isEmpty()) {
			SuccessorInfo<Integer> curr = queue.remove();
            // trace back to the initial state
            for(int letter : A.getState(curr.state).getEnabledLetters()) {
            	// for sure current state has empty set
            	Word word = curr.word.append(letter);
                for(int succId : A.getState(curr.state).getSuccessors(letter)) {
                	// now add those states into it
                	if(! visited.get(succId)) {
                		SuccessorInfo<Integer> succInfo = new SuccessorInfo<Integer>(succId);
                		succInfo.word = word;
                		queue.add(succInfo);
                		visited.set(succId);
                	}
                	if(succId == goal) {
                		return word;
                	}
                }
            }
		}
		assert false;
		return null;
	}
	
	private void computeCounterexamplePrefix(int accState, ISet reachSet) {
		assert (aPState >= 0  && aQState >= 0);
		System.out.println("Computing counterexample for the accepting state " + accState);
		//System.out.println("Computing counterexample prefix: " + accState + " bSetForP: " + bSetForP);
		ISet initSet = UtilISet.newISet();
		initSet.set(B.getInitialState());
		// construct prefix
//		 Word p1 = computeWordInProduct(initSet, bSetForP, reachSet);
		Word p1 = this.prefWordMap.get(new Pair<>(aPState, bSetForP));
		this.prefix = p1.append(this.aLetter);
		Word p2 = computeWordInA(aQState, accState);
		this.prefix = this.prefix.concat(p2);
		
		// construct loop
		this.period = A.getAlphabet().getEmptyWord();
        int start = -1;
        for(int letter : A.getState(accState).getEnabledLetters()) {
        	// just choose this one
        	for(int succ : A.getState(accState).getSuccessors(letter)) {
        		if(reachSet.get(succ)) {
        			this.period = this.period.append(letter);
//        			System.out.println("Start = " + succ + " letter = " + letter);
        			start = succ;
        			break;
        		}
        	}
        	if(start >= 0) {
        		break;
        	}
        }
//        System.out.println("Start = " + start + " accState = " + accState);
        Word suffix = computeWordInA(start, accState);
//        System.out.println("pref = " + this.period + "suffix = " + suffix);
        assert(suffix != null);
        this.period = this.period.concat(suffix);
	}
	
	// This function should not be called
	private void computeCounterexamplePeriod(int accState, ISet reachSet) {
		assert (aPState >= 0  && aQState >= 0);
		System.out.println("Computing counterexample period: " + accState + " reachSet: " + reachSet);
		this.prefix = computeWordInA(A.getInitialState(), accState);
		// construct period
		Word p1 = computeWordInA(accState, aPState);
		Word p2 = computeWordInA(aQState, accState);
		this.period = p1.append(this.aLetter);
		this.period = this.period.concat(p2);
	}
//
	private TreeSet<IntBoolTriple> compose(TreeSet<IntBoolTriple> first
			, TreeSet<IntBoolTriple> second) { //, ISet sndStates
		TreeSet<IntBoolTriple> result = new TreeSet<>();
		for(IntBoolTriple fstTriple: first) {
			for(IntBoolTriple sndTriple: second) {
				// (p, q, ) + (q, r) -> (p, r)
				if(fstTriple.getRight() == sndTriple.getLeft()) {
					addTriple(result, new IntBoolTriple(fstTriple.getLeft()
							, sndTriple.getRight()
							, fstTriple.getBool() || sndTriple.getBool()));
				}
			}
		}
		return result;
	}
	
	private TreeSet<IntBoolTriple> extend(TreeSet<IntBoolTriple> first, int letter) {
		TreeSet<IntBoolTriple> result = new TreeSet<>();
		for(IntBoolTriple triple: first) {
			int state = triple.getRight();
			for(int succ: B.getState(state).getSuccessors(letter)) {
				addTriple(result, new IntBoolTriple(triple.getLeft()
							, succ
							, triple.getBool() || B.isFinal(succ)));
			}
		}
		return result;
	}
	
	private TreeSet<IntBoolTriple> compose(ISet preds, TreeSet<IntBoolTriple> triples) {
		TreeSet<IntBoolTriple> result = new TreeSet<>();
		for(IntBoolTriple triple: triples) {
			if(preds.get(triple.getLeft())) {
				addTriple(result, triple);
			}
		}
		return result;
	}
	
	// decide whether there exists an accepting run in B from states in sim
	// all states on the left are from pref
	private boolean decideAcceptance(ISet pref, TreeSet<IntBoolTriple> period) {
//		System.out.println("pref: " + pref);
		boolean foundLoop = false;
		for(int state: pref) {
			// iteratively check whether there exists a triple (q, q: true) reachable from state
			TreeSet<IntBoolTriple> reachSet = new TreeSet<>();
			for(IntBoolTriple triple: period) {
				if(state == triple.getLeft()) {
					reachSet.add(triple);
				}
			}
			while(true) {
				// compute update
				int origSize = reachSet.size();
				TreeSet<IntBoolTriple> update = compose(reachSet, period);
				// reachable states from pref can also be first states
				reachSet.addAll(update);
				// a triple (q, q: true) means that we have found an accepting run
				for(IntBoolTriple triple: reachSet) {
					if(triple.getLeft() == triple.getRight() && triple.getBool()) {
						foundLoop = true;
						break;
					}
				}
				// reach a fixed point
				if(foundLoop || origSize == reachSet.size()) {
					break;
				}
			}
//			System.out.println("state = " + state + ", reachSet :\n" + reachSet);
			if(foundLoop) {
				break;
			}
		}
		
		return foundLoop;
	}

	public static void main(String[] args) {
		
		TreeSet<IntBoolTriple> set1 = new TreeSet<>();
		set1.add(new IntBoolTriple(0, 0, true));
		set1.add(new IntBoolTriple(24, 0, true));
		
		TreeSet<IntBoolTriple> set2 = new TreeSet<>();
		set2.add(new IntBoolTriple(0, 0, true));
		
		System.out.println(set1.containsAll(set1));
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
		sim.computeCounterexample = true;
		boolean included = sim.isIncluded();
		System.out.println(included ? "Included" : "Not included");
		if(!included && sim.computeCounterexample) {
			NBALasso lasso = new NBALasso(sim.prefix, sim.period);
			pairParser.print(lasso.getNBA(), options.log.getOutputStream());
		}
		timer.stop();
		System.out.println("Time elapsed " + timer.getTimeElapsed());
		
	}
	
}

//private Word computeWordInProduct(int accState, TreeSet<IntBoolTriple> start, TreeSet<IntBoolTriple> goal, ISet aReachSet) {
//PriorityQueue<SuccessorInfo<Pair<Integer, TreeSet<IntBoolTriple>>>> queue = new PriorityQueue<SuccessorInfo<Pair<Integer, TreeSet<IntBoolTriple>>>>();
//Pair<Integer, TreeSet<IntBoolTriple>> pair = new Pair<>(accState, start);
//SuccessorInfo<Pair<Integer, TreeSet<IntBoolTriple>>> info = new SuccessorInfo<>(pair);
//info.word = A.getAlphabet().getEmptyWord();
//queue.add(info);
//HashSet<Pair<Integer, TreeSet<IntBoolTriple>>> visited = new HashSet<>();
//visited.add(pair);
//
//while(! queue.isEmpty()) {
//	SuccessorInfo<Pair<Integer, TreeSet<IntBoolTriple>>> curr = queue.remove();
//    // trace back to the initial state
//    for(int letter = 0; letter < B.getAlphabetSize(); letter ++) {
//    	// for sure current state has empty set
//    	Word word = curr.word.append(letter);
//    	TreeSet<IntBoolTriple> succTriple = extend(curr.state.getRight(), letter);
//    	for(int aSucc: A.getState(curr.state.getLeft()).getSuccessors(letter)) {
//    		if(! aReachSet.get(aSucc)) continue;
//    		// found the word in the product
//    		if(aSucc == accState && succTriple.equals(goal)) {
//        		//System.out.println("Found a word to " + goal);
//        		return word;
//        	}
//    		Pair<Integer, TreeSet<IntBoolTriple>> newPair = new Pair<>(aSucc, succTriple);
//        	if(! visited.contains(newPair)) {
//        		SuccessorInfo<Pair<Integer, TreeSet<IntBoolTriple>>> succInfo = new SuccessorInfo<>(newPair);
//        		succInfo.word = word;
//        		queue.add(succInfo);
//        		visited.add(newPair);
//        	}
//    	}
//    }
//}
//throw new RuntimeException("Exception happened in computing words in B: " + start + " -> " + goal);
//}
//

//private Word computeWordInProduct(ISet start, ISet goal, ISet aReachSet) {
//if(start.equals(goal) && A.getInitialState() == aPState) {
//	//System.out.println("Computing words in B from " + start + " to " + goal);
//	return A.getAlphabet().getEmptyWord();
//}
//PriorityQueue<SuccessorInfo<Pair<Integer, ISet>>> queue = new PriorityQueue<SuccessorInfo<Pair<Integer, ISet>>>();
//Pair<Integer, ISet> pair = new Pair<>(A.getInitialState(), start);
//SuccessorInfo<Pair<Integer, ISet>> info = new SuccessorInfo<>(pair);
//info.word = A.getAlphabet().getEmptyWord();
//queue.add(info);
//HashSet<Pair<Integer, ISet>> visited = new HashSet<>();
//visited.add(pair);
//
//while(! queue.isEmpty()) {
//	SuccessorInfo<Pair<Integer, ISet>> curr = queue.remove();
//    // trace back to the initial state
//    for(int letter = 0; letter < B.getAlphabetSize(); letter ++) {
//    	// for sure current state has empty set
//    	Word word = curr.word.append(letter);
//    	ISet succs = UtilISet.newISet();
//    	for(int currId : curr.state.getRight()) {
//    		for(int succId : B.getState(currId).getSuccessors(letter)) {
//            	// now add those states into it
//            	succs.set(succId);
//            }
//    	}
//    	for(int aSucc: A.getState(curr.state.getLeft()).getSuccessors(letter)) {
//    		if(! aReachSet.get(aSucc)) continue;
//    		// found the word in the product
//    		if(aSucc == aPState && succs.equals(goal)) {
//        		//System.out.println("Found a word to " + goal);
//        		return word;
//        	}
//    		// ignore representation that are not in the prefSim
//    		//if(! prefSim.get(aSucc).contains(succs)) {
//    		//	continue;
//    		//}
//    		Pair<Integer, ISet> newPair = new Pair<>(aSucc, succs);
//    		// only add those words that are in prefSim
//        	if(! visited.contains(newPair)) {
//        		SuccessorInfo<Pair<Integer, ISet>> succInfo = new SuccessorInfo<>(newPair);
//        		succInfo.word = word;
//        		queue.add(succInfo);
//        		visited.add(newPair);
//        	}
//    	}
//    }
//}
//throw new RuntimeException("Exception happened in computing words in B: " + start + " -> " + goal);
//}
//
//private Word computeWordInProductBackward(ISet start, ISet goal, ISet aReachSet) {
//if(start.equals(goal) && A.getInitialState() == aPState) {
//	//System.out.println("Computing words in B from " + start + " to " + goal);
//	return A.getAlphabet().getEmptyWord();
//}
//PriorityQueue<SuccessorInfo<Pair<Integer, ISet>>> queue = new PriorityQueue<SuccessorInfo<Pair<Integer, ISet>>>();
//Pair<Integer, ISet> pair = new Pair<>(aPState, goal);
//SuccessorInfo<Pair<Integer, ISet>> info = new SuccessorInfo<>(pair);
//info.word = A.getAlphabet().getEmptyWord();
//queue.add(info);
//HashSet<Pair<Integer, ISet>> visited = new HashSet<>();
//visited.add(pair);
//
//while(! queue.isEmpty()) {
//	SuccessorInfo<Pair<Integer, ISet>> curr = queue.remove();
//    // trace back to the initial state
//    for(int letter = 0; letter < B.getAlphabetSize(); letter ++) {
//    	// for sure current state has empty set
//    	Word word = curr.word.preappend(letter);
//    	ISet preds = UtilISet.newISet();
//    	for(int currId : curr.state.getRight()) {
//    		for(StateNFA predNfa : bStates[currId].getPredecessors(letter)) {
//            	// now add those states into it
//    			preds.set(predNfa.getId());
//            }
//    	}
//    	// preds
//    	for(StateNFA aPredNfa: aStates[curr.state.getLeft()].getPredecessors(letter)) {
//    		if(! aReachSet.get(aPredNfa.getId())) continue;
//    		// found the word in the product
//    		if(aPredNfa.getId() == A.getInitialState() && preds.equals(start)) {
//        		//System.out.println("Found a word to " + goal);
//        		return word;
//        	}
//    		// now check the prefSim, not the one in computation of representation
//    		if(! prefSim.get(aPredNfa.getId()).contains(preds)) {
//    			continue;
//    		}
//    		System.out.println("");
//    		Pair<Integer, ISet> newPair = new Pair<>(aPredNfa.getId(), preds);
//        	if(! visited.contains(newPair)) {
//        		SuccessorInfo<Pair<Integer, ISet>> predInfo = new SuccessorInfo<>(newPair);
//        		predInfo.word = word;
//        		queue.add(predInfo);
//        		visited.add(newPair);
//        	}
//    	}
//    }
//}
//throw new RuntimeException("Exception happened in computing words in B: " + start + " -> " + goal);
//}

