package roll.main.complement.algos;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.automata.NBA;
import roll.automata.operations.TarjanSCCsNonrecursive;
import roll.main.inclusion.congr.IntBoolTriple;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;

public class UtilCongruence {
	
	private UtilCongruence() {
		
	}
	
	// decide whether there exists an accepting run in B from states in sim
	// all states on the left are from pref
	public static boolean decideAcceptance(ISet pref, TreeSet<IntBoolTriple> period, boolean[][]fwSim) {
//		System.out.println("pref: " + pref);
//		System.out.println("period: " + period);
//		System.out.println("Start deciding acceptance ...");
		boolean foundLoop = false;
		ISet reachStates = pref.clone();
//		for(int state: pref) {
		// iteratively check whether there exists a triple (q, q: true) reachable from
		// state
		ISet reachableStates = pref.clone();
		TreeSet<IntBoolTriple> reachSet = new TreeSet<>();
		while (true) {
			ISet newReach = UtilISet.newISet();
			for (IntBoolTriple triple : period) {
//				if (bwSim) {
					// check whether there exists a triple whose left state is simulated by some
					// state in reach
				for (int q : reachStates) {
					if (fwSim[triple.getLeft()][q]) {
						reachSet.add(new IntBoolTriple(q, triple.getRight(), triple.getBool()));
						newReach.set(triple.getRight());
					}
				}
//				}
//				if (!bwSim && reachStates.get(triple.getLeft())) {
//					reachSet.add(triple);
//					// add states that can be reached
//					newReach.set(triple.getRight());
//				}
			}
//			System.out.println("ReachSet = " + reachSet);
			// first add reachable triples
			// compute update
			int origSize = reachSet.size();
			TreeSet<IntBoolTriple> update = compose(reachSet, period);
			// reachable states from pref can also be first states
			reachSet.addAll(update);
//			System.out.println("ReachSet = " + reachSet);
			// a triple (q, q: true) means that we have found an accepting run
			for (IntBoolTriple triple : reachSet) {
				if (triple.getLeft() == triple.getRight() && triple.getBool()) {
					foundLoop = true;
					break;
				}
			}
			for (IntBoolTriple triple : update) {
				// more reachable states
				newReach.set(triple.getRight());
			}
			// new reachable states
//			System.out.println("New reach = " + newReach);
			int statesSize = reachableStates.cardinality();
			newReach.andNot(reachableStates);
			reachableStates.or(newReach);
			// reach a fixed point
			if (foundLoop || (origSize == reachSet.size() && statesSize == reachableStates.cardinality())) {
				break;
			}
			reachStates = newReach;
		}
//		}
//		System.out.println("Finished deciding acceptance ...");

		return foundLoop;
	}
	
	//
	public static TreeSet<IntBoolTriple> compose(TreeSet<IntBoolTriple> first, TreeSet<IntBoolTriple> second) { // , ISet
																											// sndStates
		TreeSet<IntBoolTriple> result = new TreeSet<>();
		for (IntBoolTriple fstTriple : first) {
			for (IntBoolTriple sndTriple : second) {
				// (p, q, ) \times (q, r) -> (p, r)
				if (fstTriple.getRight() == sndTriple.getLeft()) {
					result.add(new IntBoolTriple(fstTriple.getLeft(), sndTriple.getRight(),
							fstTriple.getBool() || sndTriple.getBool()));
				}
			}
		}
		return result;
	}
	
	
	// Always guarantte that if there is (q, r: true), then no (q, r: false) appears
	public static void addTriple(TreeSet<IntBoolTriple> set, IntBoolTriple triple) {
		IntBoolTriple revTriple = new IntBoolTriple(triple.getLeft(), triple.getRight(), !triple.getBool());
		boolean containedRev = set.contains(revTriple);
		if (containedRev) {
			if (triple.getBool()) {
				set.remove(revTriple);
				set.add(triple);
			} else {
				// do nothing, keep the original one
			}
		} else {
			set.add(triple);
		}
	}
	
	public static boolean decideAcceptanceSim(ISet pref, TreeSet<IntBoolTriple> period, boolean[][]fwSim, boolean[][] bwSim) {
		Alphabet alphabet = new Alphabet();
		alphabet.addLetter('0');
//		alphabet.addLetter('1');
		TObjectIntMap<IntBoolTriple> fromMap = new TObjectIntHashMap<>();
//		ArrayList<IntBoolTriple> toMap = new ArrayList<>();
		NBA nba = new NBA(alphabet);
		ISet inits = UtilISet.newISet();
		int num = 0;
		for(IntBoolTriple tpl : period) {
			fromMap.put(tpl, num);
//			toMap.add(tpl);
			if(pref.get(tpl.getLeft())) {
				inits.set(num);
			}
			nba.createState();
			num ++;
//			assert toMap.size() == num;
			assert nba.getStateSize() == num;
		}
		// now add transition
		for(IntBoolTriple from: period) {
			int s = fromMap.get(from);
			for(IntBoolTriple to : period) {
				int t = fromMap.get(to);
				// can be simulated
				if(bwSim[from.getRight()][to.getLeft()]) {
					nba.getState(s).addTransition(0 , t);
				}
			}
			if(from.getBool()) {
				nba.setFinal(s);
			}
		}
		// now we compute the SCCs
		TarjanSCCsNonrecursive tarjan = new TarjanSCCsNonrecursive(nba, inits);
		ISet finals = nba.getFinalStates();
		boolean accepting = false;
		for(ISet scc : tarjan.getSCCs()) {
			if(finals.overlap(scc)) {
				accepting = true;
				break;
			}
		}
//		System.out.println("pref: " + pref);
//		System.out.println("period: " + period);
//		System.out.println("acc = " + accepting);
		return accepting;
	}
	
	public static boolean decideAcceptanceOpt(ArrayList<ISet> pref, CongruenceClassOpt period, ISet finals) {
		Alphabet alphabet = new Alphabet();
		alphabet.addLetter('0');
		alphabet.addLetter('1');
		TObjectIntMap<ISet> fromMap = new TObjectIntHashMap<>();
//		ArrayList<IntBoolTriple> toMap = new ArrayList<>();
		NBA nba = new NBA(alphabet);
		ISet inits = UtilISet.newISet();
		int num = 0;
		for(int i = 0; i < pref.size(); i ++) {
			fromMap.put(pref.get(i), num);
//			toMap.add(tpl);
			inits.set(i);
			nba.createState();
			num ++;
//			assert toMap.size() == num;
			assert nba.getStateSize() == num;
		}
		// now add transition
		for(int i = 0; i < pref.size(); i ++) {
			ISet from = pref.get(i);
			int s = fromMap.get(from);
			for(int j = 0; j < pref.size(); j ++) {
				ISet to = period.getSet(j);
				int t = fromMap.get(to);
				// can be simulated
				if(period.getMaxPres(j).getLeft() == i) {
					if(period.getMaxPres(j).getRight()) {
						nba.getState(s).addTransition(0 , t);
					}else {
						nba.getState(s).addTransition(1 , t);
					}
				}
			}
		}
		// now we compute the SCCs
		ISet localFinals = nba.getFinalStates();
		TarjanSCCsNonrecursive tarjan = new TarjanSCCsNonrecursive(nba, inits);
		boolean accepting = false;
		for(ISet scc : tarjan.getSCCs()) {
			if(localFinals.overlap(scc)) {
				accepting = true;
				break;
			}else {
				// check the transitions
				for(int p : scc) {
					for(int q : nba.getState(p).getSuccessors(0)) {
						if (!scc.get(q)) continue;
						accepting = true;
						break;
					}
					if(accepting) break;
				}
			}
			if(accepting) {
				break;
			}
		}
		System.out.println("pref: " + pref);
		System.out.println("period: " + period);
		System.out.println("acc = " + accepting);
		return accepting;
	}

}
