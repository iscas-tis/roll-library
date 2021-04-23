package roll.main.complement.algos;

import java.util.TreeSet;

import roll.main.inclusion.congr.IntBoolTriple;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class UtilCongruence {
	
	private UtilCongruence() {
		
	}
	
	// decide whether there exists an accepting run in B from states in sim
	// all states on the left are from pref
	public static boolean decideAcceptance(ISet pref, TreeSet<IntBoolTriple> period) {
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
				if ( reachStates.get(triple.getLeft())) {
					reachSet.add(triple);
					// add states that can be reached
					newReach.set(triple.getRight());
				}
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

}
