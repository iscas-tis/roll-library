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

package roll.main.inclusion.congr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.AcceptNBA;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.NBALasso;
import roll.automata.operations.NBAOperations;
import roll.automata.operations.StateContainer;
import roll.automata.operations.TarjanSCCsNonrecursive;
import roll.main.Options;
import roll.main.complement.IsIncluded;
import roll.main.complement.algos.CongruenceClassOpt;
import roll.main.complement.algos.UtilCongruence;
import roll.main.inclusion.run.SuccessorInfo;
import roll.parser.ba.PairParserBA;
import roll.util.Pair;
import roll.util.Timer;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Word;

// This algorithm is inspired by simulation relation and the work 
//Congruence Relations for B\"uchi Automata 
// We actually can define congruence relations for language inclusion checking

public class CongruenceSimulationOpt implements IsIncluded {

	NBA A;
	NBA B;
	
	ArrayList<HashSet<CongruenceClassProfile>> prefSim;
	StateContainer[] bStates;
	StateContainer[] aStates;

	TIntObjectMap<HashSet<CongruenceClassProfile>> periodSim;
	// antichain is used by default
	boolean debug = false;

	// counterexample (prefix, period) if exists
	Word prefix = null;
	Word period = null;

	// p - a -> q cannot be simulated with transitions in B
//	int aPState = -1;
	int aQState = -1;
	int aLetter = -1;
	// I_A -> p or f -> p
	Word cePrefix = null;

	boolean computeCounterexample = true;

	boolean useSimulationAB = false;
	boolean useSimulation = false;
	boolean fwSimB[][]; // forward simulation on B
	boolean bwSimB[][]; // backward simulation on B
	boolean fwSimA[][]; // forward simulation on A, didn't use this one
	boolean fwSimAB[][]; // simulation between A and B
	
	Options options;
	boolean minimizePrefix = false;
	boolean minimizePeriod = false;
	private long timeForPrefixSim;
	private long timeForPeriodSim;

	public CongruenceSimulationOpt(NBA A, NBA B, Options options) {
		this.A = A;
		this.B = B;
		this.options = options;
		prefSim = new ArrayList<>();
		for (int s = 0; s < A.getStateSize(); s++) {
			prefSim.add(new HashSet<>());
		}
		aStates = new StateContainer[A.getStateSize()];
		// compute the predecessors and successors
		for (int i = 0; i < A.getStateSize(); i++) {
			aStates[i] = new StateContainer(i, A);
		}
		// initialize the information for B
		for (int i = 0; i < A.getStateSize(); i++) {
			StateNFA st = aStates[i].getState();
			for (int letter = 0; letter < A.getAlphabetSize(); letter++) {
				for (int succ : st.getSuccessors(letter)) {
					// aStates[i].addSuccessors(letter, succ);
					aStates[succ].addPredecessors(letter, i);
				}
			}
		}
		bStates = new StateContainer[B.getStateSize()];
		// compute the predecessors and successors
		for (int i = 0; i < B.getStateSize(); i++) {
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

		this.useSimulation = options.simulation || options.minimization;
		this.useSimulationAB = options.simulation || options.minimization;
		this.minimizePrefix = options.simulation || options.minimization;
		this.minimizePeriod = options.minimization;
	}

	@Override
	public Pair<Word, Word> getCounterexample() {
		assert prefix != null && period != null;
		return new Pair<>(prefix, period);
	}

	public void outputPrefixSimulation() {
		for (int s = 0; s < A.getStateSize(); s++) {
			// only i_B simulates i_A at first
			System.out.print("State " + s + "\n");
			for (CongruenceClassProfile set : prefSim.get(s)) {
					System.out.println(set + ", ");
			}
		}
	}
	
	// test whether set can be simulated by update
	// either *left* is a subset of *right* or obey simulation relation
	boolean isPrefSimulated(CongruenceClassProfile set, CongruenceClassProfile update) {
		ISet leftSet = UtilISet.newISet();
		for(ISet states : set.orderedSets) {
			leftSet.or(states);
		}
		ISet rightSet = UtilISet.newISet();
		for(ISet states : update.orderedSets) {
			rightSet.or(states);
		}
		for (int p : leftSet) {
			boolean simulated = false;
			for (int q : rightSet) {
				if (fwSimB[p][q]) {
					simulated = true;
					break;
				}
			}
			if (!simulated) {
				return false;
			}
		}
		// if two sets are not equal..
//		if(! leftSet.contentEq(rightSet)) {
//			return true;
//		}else {
//			
//		}
//		System.out.println(set + " is simulated by " + update);
		return true;
	}
	
	// keep the least one
	boolean canAddToSet(HashSet<CongruenceClassProfile> orig, CongruenceClassProfile update) {
		for(CongruenceClassProfile set : orig) {
			// some set in orig can be simulated by update
			if(isPrefSimulated(set, update)) {
				if(debug) System.out.println("Ignore " + update);
				return false;
			}
		}
		return true;
	}

	// PRECONDITION: we know that update can not simulate any set in orig
	HashSet<CongruenceClassProfile> addSetToPrefixAntichain(HashSet<CongruenceClassProfile> orig
			, CongruenceClassProfile update
			, HashSet<CongruenceClassProfile> subsetOfUpdate) {
		HashSet<CongruenceClassProfile> result = new HashSet<>();
		if(debug) System.out.println("Current set = " + orig + " update = " + update);
		// a set corresponds to a class of finite prefixes to an accepting state in A
		// check whether there is set that can simulate update
		for (CongruenceClassProfile sts : orig) {
			if (isPrefSimulated(update, sts)) {
				// ignore set that simulates update
				if (computeCounterexample) {
					if(debug) System.out.println("Need to remove " + sts);
					subsetOfUpdate.add(sts);
				}
				continue;
			} else {
				// update and sts are incomparable
				result.add(sts);
			}
		}
		if(debug) System.out.println("Need to add " + update);
		result.add(update);
		return result;
	}
	
	@Override
	public Boolean isIncluded() {
		// TODO Auto-generated method stub
		if (useSimulationAB) {
//			fwSimA = Simulation.computeForwardSimilation(A, aStates);
			fwSimAB = Simulation.computeForwardSimulation(A, B);
		} else {
//			System.out.println("Hello");
			int num = A.getStateSize() + B.getStateSize();
			fwSimAB = new boolean[num][num];
			for (int s = 0; s < num; s++) {
				for (int t = 0; t < num; t++) {
					fwSimAB[s][t] = (s == t);
				}
			}
		}
		if (fwSimAB[0][A.getStateSize()]) {
			options.log.println("Inclusion proved by forward simulation between initial states.");
			return true;
		}
		if(useSimulation) {
			fwSimB = Simulation.computeForwardSimilation(B, bStates);
			bwSimB = Simulation.computeBackwardSimilation(B, bStates);
		}else {
//			System.out.println("Hello");
			fwSimB = new boolean[B.getStateSize()][B.getStateSize()];
			bwSimB = new boolean[B.getStateSize()][B.getStateSize()];
			for (int s = 0; s < B.getStateSize(); s++) {
				for (int t = 0; t < B.getStateSize(); t++) {
					fwSimB[s][t] = (s == t);
					bwSimB[s][t] = (s == t);
				}
			}
		}
		// for each accepting state (should be reachable from the initial state and can
		// reach itself)
		ISet reachSet = UtilCongruenceClassProfile.getReachSet(A.getInitialState(), A);
		int countStates = 0;
//		LinkedList<Pair<HashSet<ISet>, HashSet<TreeSet<IntBoolTriple>>>> antichainFinals = new LinkedList<>();
		long timeForAcceptance = 0;
		TIntObjectMap<ISet> mustStates = new TIntObjectHashMap<>();
		ISet aMustStates = UtilISet.newISet();
//		ISet bFinals = B.getFinalStates();
		for (int accState : A.getFinalStates()) {
			countStates++;
			options.log.println(countStates + "-th accepting state " + accState + " out of "
					+ A.getFinalStates().cardinality() + " states");
			// reachable states from the initial state
			ISet necessaryStates = reachSet.clone();
			// only keep those state that can go back to accState
			ISet predSet = getPredSet(accState, aStates, A);
			necessaryStates.and(predSet);
			// if the initial state cannot reach the accepting state or the accepting state
			// cannot reach itself
			// , then language is empty
			if (!necessaryStates.get(A.getInitialState()) || !necessaryStates.get(accState)) {
				// System.out.println("Ignored the accepting state " + accState + "");
				continue;
			}
			mustStates.put(accState, necessaryStates);
			aMustStates.or(necessaryStates);
		}
		// now compute the representative for prefixes
		this.computePrefixSimulation(aMustStates);
		outputPrefixSimulation();
		for (int accState : A.getFinalStates()) {
			this.computePeriodSimulation(accState);
			System.out.println(this.periodSim.get(accState));
		}
		return true;
	}


	/**
	 * Only compute the states that can reach accState
	 */
	public void computePrefixSimulation(ISet reachSet) {
		Timer timer = new Timer();
		timer.start();
		prefSim.clear();
		// initialization
		for (int s = 0; s < A.getStateSize(); s++) {
			prefSim.add(new HashSet<>());
			// only i_B simulates i_A at first
			if (s == A.getInitialState()) {
				ISet set = UtilISet.newISet();
				set.set(B.getInitialState());
				CongruenceClassProfile congrCls = new CongruenceClassProfile();
				congrCls.addSet(set);
				prefSim.get(s).add(congrCls);
				congrCls.setWord(A.getAlphabet().getEmptyWord());
				if (debug) {
					System.out.println("t = " + s + " set = " + set);
				}
			}
		}
		if (debug)
			System.out.println("Reachable size: " + reachSet.cardinality());
		// compute simulation relation
		LinkedList<Integer> workList = new LinkedList<>();
		workList.add(A.getInitialState());
		ISet inWorkList = UtilISet.newISet();
		inWorkList.set(A.getInitialState());
		while (!workList.isEmpty()) {
			// take out one state
			int s = workList.removeFirst();
			inWorkList.clear(s);
			// the letter is changed
			for (int a : A.getState(s).getEnabledLetters()) {
				for (int t : A.getState(s).getSuccessors(a)) {
					if (!reachSet.get(t))
						continue;
					// s - a - > t in A
					// f(s) - a -> P'
					// p \in f(s), then P' \subseteq f(t) in B
					// compute mapping relations to B
					// a set corresponds to a word u
					HashSet<CongruenceClassProfile> copy = new HashSet<>();
					for (CongruenceClassProfile set : prefSim.get(s)) {
						copy.add(set);
					}
					for (CongruenceClassProfile set : copy) {
						// if the successor is itself and some set has been removed
						// for every set, we update the sets
						CongruenceClassProfile update = UtilCongruenceClassProfile.getNext(B, a, set, false);
						if (computeCounterexample) {
							Word newPref = set.representative.append(a);
							update.setWord(newPref);
						}
						HashSet<CongruenceClassProfile> orig = prefSim.get(t);
						// update is the word ua and check whether we need to update
						if (canAddToSet(orig, update)) {
							// POSTCONDITION: update can not simulate any set in orig
							HashSet<CongruenceClassProfile> subsetsOfUpdate = new HashSet<>();
							if (debug)
								System.out.println("Next state is " + t);
							HashSet<CongruenceClassProfile> result = addSetToPrefixAntichain(orig, update, subsetsOfUpdate);
							prefSim.set(t, result);
							if (debug)
								System.out.println("Updated sim " + t + " is " + prefSim.get(t));
							
							if (!inWorkList.get(t)) {
								workList.addLast(t);
								inWorkList.set(t);
								if (debug) {
									System.out.println("t = " + t + " set = " + update);
								}
							}
						}
						// detected that update is empty for the first time
						// now need to update
						if (update.isEmpty()) {
							aQState = t;
							aLetter = a;
							cePrefix = set.representative;
							if(debug) System.out.println("Hello " + cePrefix + ", " + new Pair<>(s, set));
							return;
						}
					}
				}
			}
		}
		timer.stop();
		this.timeForPrefixSim += timer.getTimeElapsed();
	}
	
	private HashSet<CongruenceClassProfile> computePeriodNext(HashSet<CongruenceClassProfile> currSet, int letter, boolean word) {
		ISet finals = B.getFinalStates();
		HashSet<CongruenceClassProfile> result = new HashSet<CongruenceClassProfile>();
		for(CongruenceClassProfile curr : currSet) {
			CongruenceClassProfile currProgress = curr.getProgress(finals);
			CongruenceClassProfile nextProgress = UtilCongruenceClassProfile.getNext(B, letter, currProgress, true);
			if(word) {
				assert curr.representative != null;
				nextProgress.representative = curr.representative.append(letter);
			}else {
				nextProgress.representative = B.getAlphabet().getLetterWord(letter);
			}
			result.add(nextProgress);
		}
		return result;
	}
	
	public void computePeriodSimulation(int accState) {
		Timer timer = new Timer();
		timer.start();
		periodSim = new TIntObjectHashMap<>();
		// now compute every state that can be reached by accState
		ISet reachSet = UtilCongruenceClassProfile.getReachSet(accState, A);
		// only keep those state that can go back to accState
		ISet predSet = getPredSet(accState, aStates, A);
		reachSet.and(predSet);
		if (debug)
			System.out.println("States for A: " + reachSet);
		// those can not be reached should corresponds to empty set
		for (int s : reachSet) {
			// only i_B simulates i_A at first
			periodSim.put(s, new HashSet<CongruenceClassProfile>());
		}
		LinkedList<Integer> workList = new LinkedList<>();
		ISet inWorkList = UtilISet.newISet();
		options.log
				.println("Computing the congruence representation of periods for accepting state " + accState + " ...");
		// 1. initialization
		{
			// only care about states from simulatedStatesInB
			int s = accState;
			// v must not be empty word
			for (int a : A.getState(s).getEnabledLetters()) {
				for (int t : A.getSuccessors(s, a)) {
					if (!reachSet.get(t))
						continue;
					// add to worklist
					if (!inWorkList.get(t)) {
						workList.add(t);
						inWorkList.set(t);
					}
					// compute the simulation relations
					HashSet<CongruenceClassProfile> update = computePeriodNext(prefSim.get(s), a, false);
					if (debug)
						System.out.println(t + " AccTriple " + update);
					periodSim.put(t, update);
					if (debug) {
						System.out.println("t = " + t + " set = " + update);
					}
				}
			}
		}

		// 2. computation of simulated relations
		while (!workList.isEmpty()) {
			int s = workList.removeFirst();
			inWorkList.clear(s);
			HashSet<TreeSet<IntBoolTriple>> removedTreeSets = new HashSet<>();
			// update for successors
			for (int a : A.getState(s).getEnabledLetters()) {
				for (int t : A.getSuccessors(s, a)) {
					// Again, ignore states that cannot reach accState
					if (!reachSet.get(t))
						continue;
					// s - a -> t
					HashSet<CongruenceClassProfile> copy = new HashSet<>();
					for (CongruenceClassProfile set : periodSim.get(s)) {
						copy.add(set);
					}
					for (CongruenceClassProfile set : copy) {
						CongruenceClassProfile update = UtilCongruenceClassProfile.getNext(B, a, set, true);
						// we have extended for set
						if (canAddToProgressSet(periodSim.get(t), update)) {
							// only keep the set that are a subset of another
							HashSet<CongruenceClassProfile> curr = periodSim.get(t);
							update.representative = set.representative.append(a);
							HashSet<CongruenceClassProfile> subsetOfUpdate = new HashSet<>();
							HashSet<CongruenceClassProfile> result = addSetToPeriodAntichain(curr, update,
									subsetOfUpdate);
							periodSim.put(t, result);
							if (!inWorkList.get(t)) {
								workList.add(t);
								inWorkList.set(t);
							}
						}
						if (debug) {
							System.out.println("t = " + t + " set = " + update);
						}
						// not possible
						if (update.isEmpty()) {
							aQState = t;
							cePrefix = set.representative;
							aLetter = a;
							options.log.println("Early termination in computing representation of periods.");
							return;
						}
					}
				}
			}
		}
		timer.stop();
		this.timeForPeriodSim += timer.getTimeElapsed();
	}
	
	private HashSet<CongruenceClassProfile> addSetToPeriodAntichain(HashSet<CongruenceClassProfile> curr,
			CongruenceClassProfile update, HashSet<CongruenceClassProfile> subsetOfUpdate) {
		HashSet<CongruenceClassProfile> result = new HashSet<CongruenceClassProfile>();
		// PRECONDITION: update cannot simulate a set in orig
		for (CongruenceClassProfile elem : curr) {
			if (isPeriodSimulated(update, elem)) {
				// ignore triples that simulates update
				if (computeCounterexample) {
					if(debug) System.out.println("Need to remove " + elem);
					subsetOfUpdate.add(elem);
				}
				continue;
			} else {
				result.add(elem);
			}
		}
		if(debug) System.out.println("Need to add " + update);
		result.add(update);
		return result;
	}

	private boolean isBwSimulated(ISet leftSet, ISet rightSet) {
		return isSetSimulated(leftSet, rightSet, bwSimB);
	}
	
	private boolean isFwSimulated(ISet leftSet, ISet rightSet) {
		return isSetSimulated(leftSet, rightSet, fwSimB);
	}
	
	private boolean isSetSimulated(ISet leftSet, ISet rightSet, boolean[][] sim) {
		for (int p : leftSet) {
			boolean simulated = false;
			for (int q : rightSet) {
				if (sim[p][q]) {
					simulated = true;
					break;
				}
			}
			if (!simulated) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isPeriodSimulated(CongruenceClassProfile left, CongruenceClassProfile right) {
		for(int i = 0; i < left.getSetSize(); i ++) {
			boolean isSimulated = false;
			ISet pSet = left.getSet(i);
			Pair<ISet, Boolean> pair = left.getPrevSet(i);
			ISet qSet = pair.getLeft();
			boolean b = pair.getRight();
			for(int j = 0; j < right.getSetSize(); j ++) {
				ISet rpSet = right.getSet(j);
				Pair<ISet, Boolean> rpair = right.getPrevSet(j);
				ISet rqSet = rpair.getLeft();
				boolean rb = pair.getRight();
				if((!b || rb) && isBwSimulated(pSet, rpSet) && isFwSimulated(qSet, rqSet)) {
					isSimulated = true;
					break;
				}
			}
			if(! isSimulated) {
				return false;
			}
		}
		return true;
	}

	private boolean canAddToProgressSet(HashSet<CongruenceClassProfile> hashSet, CongruenceClassProfile update) {
		for(CongruenceClassProfile set : hashSet) {
			// some set in orig can be simulated by update
			if(isPeriodSimulated(set, update)) {
				if(debug) System.out.println("Ignore " + update);
				return false;
			}
		}
		return true;
	}

	// needs to check equivalence relation
	private void addToMinimizedPrefixSet(ISet update, int q) {
		ISet result = UtilISet.newISet();
		for (int p : update) {
			// q is simulated by p
			if (fwSimB[q][p]) {
				// assume that states in update cannot be simulated by another
				return;
			} else if (fwSimB[p][q]) {
				// p is simulated by q
				// changed = true;
				continue;
			} else {
				result.set(p);
			}
		}
		// q is not simulated by any states in update
		update.and(result);
		update.set(q);
	}

	private ISet getPredSet(int state, StateContainer[] states, NBA nba) {
		LinkedList<Integer> queue = new LinkedList<>();
		ISet visited = UtilISet.newISet();
		queue.add(state);
		visited.set(state);
		boolean accLoop = false;
		while (!queue.isEmpty()) {
			int lState = queue.remove();
			// ignore unused states
			for (int c = 0; c < nba.getAlphabetSize(); c++) {
				for (StateNFA lPred : states[lState].getPredecessors(c)) {
					if (!visited.get(lPred.getId())) {
						queue.add(lPred.getId());
						visited.set(lPred.getId());
					} else if (lPred.getId() == state) {
						// the state can reach itself
//                    	if(debug) System.out.println("Found loop from state " + state);
						accLoop = true;
					}
				}
			}
		}
		if (!accLoop) {
			visited.clear(state);
		}
		return visited;
	}



	public static void main(String[] args) {

//		TreeSet<IntBoolTriple> set1 = new TreeSet<>();
//		set1.add(new IntBoolTriple(0, 0, true));
//		set1.add(new IntBoolTriple(24, 0, true));
//		
//		TreeSet<IntBoolTriple> set2 = new TreeSet<>();
//		set2.add(new IntBoolTriple(0, 0, true));
//		
//		System.out.println(set1.containsAll(set1));
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

//		System.out.println(args.length + " " + args[0]);
		ISet pref = UtilISet.newISet();
		pref.set(0);
		pref.set(1);
		pref.set(2);
		TreeSet<IntBoolTriple> set1 = new TreeSet<>();
		set1.add(new IntBoolTriple(0, 0, false));
		set1.add(new IntBoolTriple(0, 108, false));
		set1.add(new IntBoolTriple(0, 109, true));
		set1.add(new IntBoolTriple(0, 110, true));
		set1.add(new IntBoolTriple(0, 112, false));
		set1.add(new IntBoolTriple(0, 120, false));
		set1.add(new IntBoolTriple(1, 1, false));

		set1.add(new IntBoolTriple(1, 2, true));
		set1.add(new IntBoolTriple(1, 112, true));
		set1.add(new IntBoolTriple(2, 112, true));

		set1.add(new IntBoolTriple(109, 109, true));
		set1.add(new IntBoolTriple(109, 110, true));
		set1.add(new IntBoolTriple(110, 109, true));
		set1.add(new IntBoolTriple(110, 110, true));
		set1.add(new IntBoolTriple(112, 112, false));

//		System.out.print(decideAcceptance(pref, set1));

//		System.exit(0);

		Options options = new Options();
		options.verbose = 1;
		options.minimization = false;
		options.simulation = true;
		PairParserBA pairParser = new PairParserBA(options, args[0], args[1]);
		NBA A = pairParser.getA();
		NBA B = pairParser.getB();
		System.out.println("#A = " + A.getStateSize() + ", #B = " + B.getStateSize());
		System.out.println("#AF = " + A.getFinalSize() + ", #BF = " + B.getFinalSize());
		AcceptNBA acc = (AcceptNBA) A.getAcc();
		acc.minimizeFinalSet();
		System.out.println("#AF = " + A.getFinalSize() + ", #BF = " + B.getFinalSize());
		Timer timer = new Timer();
		timer.start();

		CongruenceSimulationOpt sim = new CongruenceSimulationOpt(A, B, options);
		sim.computeCounterexample = true;
		sim.debug = false;
		sim.useSimulation = true;
		sim.minimizePrefix = true;
		sim.minimizePeriod = false;
		sim.useSimulationAB = true;

		boolean included = sim.isIncluded();
		System.out.println(included ? "Included" : "Not included");
		if (!included && sim.computeCounterexample) {
			NBALasso lasso = new NBALasso(sim.prefix, sim.period);
			pairParser.print(lasso.getNBA(), options.log.getOutputStream());
			boolean inA = NBAOperations.accepts(A, sim.prefix, sim.period);
			boolean inB = NBAOperations.accepts(B, sim.prefix, sim.period);
			if (!(inA && !inB)) {
				System.out.println("Error counterexample: " + inA + ", " + inB);
			}
			inA = NBAOperations.tarjanAccepts(A, sim.prefix, sim.period);
			inB = NBAOperations.tarjanAccepts(B, sim.prefix, sim.period);
			if (!(inA && !inB)) {
				System.out.println("Error counterexample: " + inA + ", " + inB);
			} else {
				System.out.println("True counterexample: " + inA + ", " + inB);
			}
			// System.out.println("SPOT: " +
			// NBAInclusionCheckTool.isIncludedSpot(lasso.getNBA(), A));
			// System.out.println("SPOT" +
			// NBAInclusionCheckTool.isIncludedSpot(lasso.getNBA(), B));
			// System.out.println("RABIT: " +
			// NBAInclusionCheckTool.isIncludedGoal("/home/liyong/tools/GOAL-20151018/goal",
			// lasso.getNBA(), A));
			// System.out.println("RABIT" +
			// NBAInclusionCheckTool.isIncludedGoal("/home/liyong/tools/GOAL-20151018/goal",
			// lasso.getNBA(), B));
		}
		timer.stop();
		System.out.println("Time elapsed " + timer.getTimeElapsed());

	}


}