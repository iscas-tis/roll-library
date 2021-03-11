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

package roll.automata.operations;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class TarjanSCCs {
	
	private final NBA automaton;
	private final LinkedList<ISet> maxSCCs;
	private int index;
	private final Stack<Integer> stack;
	private final TIntIntMap indexMap ;
	private final TIntIntMap lowlinkMap;
	private ISet initials;
	    
	public TarjanSCCs(NBA aut, ISet initials) {
		this.automaton = aut;
		this.initials = initials;
		this.maxSCCs = new LinkedList<>();
        this.stack = new Stack<>();
        this.lowlinkMap = new TIntIntHashMap();
        this.indexMap   = new TIntIntHashMap();
        explore();
	}
	
	public List<ISet> getSCCs() {
		return maxSCCs;
	}

	private void explore() {
		index = 0;
		for (final int n : initials) {
			if (! indexMap.containsKey(n)) {
				strongConnect(n);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void strongConnect(int v) {
		stack.push(v);
		indexMap.put(v, index);
		lowlinkMap.put(v, index);
		++index;

		StateNFA state = automaton.getState(v);
		boolean hasSelfLoop = false;
		for (int letter : state.getEnabledLetters()) {
			for (int succ : state.getSuccessors(letter)) {
				if(! hasSelfLoop && v == succ) {
					hasSelfLoop = true;
				}
				if (!indexMap.containsKey(succ)) {
					strongConnect(succ);
					lowlinkMap.put(v, Math.min(lowlinkMap.get(v), lowlinkMap.get(succ)));
				} else if (stack.contains(succ)) {
					lowlinkMap.put(v, Math.min(lowlinkMap.get(v), indexMap.get(succ)));
				}
			}
		}

		if (lowlinkMap.get(v) == indexMap.get(v)) {
			// record scc states
			ISet scc = UtilISet.newISet();
			while (! stack.empty()) {
				int t = stack.pop();
				scc.set(t);
				if (t == v)
					break;
			}
			if(scc.cardinality() > 1 || hasSelfLoop) {
				maxSCCs.add(scc);
			}
		}
	}
	
//	public boolean isBSCC(ISet scc) {
//		for(int c = 0; c < automaton.getAlphabetSize(); c ++) {
//			ISet succs = automaton.getSuccessors(scc, c);
//			if(! succs.subsetOf(scc)) {
//				return false;
//			}
//		}
//		return true;
//	}
	
	public boolean hasPath(TreeSet<Integer> states, int p, int q) {
		ISet visited = UtilISet.newISet();
		return hasPath(states, visited, p, q);
	}
	
	private boolean hasPath(TreeSet<Integer> states, ISet visited, int p, int q) {
		assert states.contains(p) && states.contains(q);
	    if(visited.get(p)) {
	    	return false;
	    }
	    visited.set(p);
	    for (int letter : automaton.getState(p).getEnabledLetters()) {
			for (int succ : automaton.getState(p).getSuccessors(letter)) {
				if(! states.contains(succ)) continue;
				if(q == succ) return true;
				if(hasPath(states, visited, succ, q)) {
					return true;
				}
			}
		}
		return false;
	}


}

