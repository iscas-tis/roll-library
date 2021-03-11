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

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class TarjanSCCsNonrecursive {
	
//	private final NBA automaton;
	private ISet initials;
	
	private int tjNode;
	private final ISet tjInStack;
	private final TIntIntMap tjDfsMap ; // index
	private final TIntIntMap tjLowlinkMap; // lowlink
    private final int[] tjCallNodeStack;
    private int[] tjCallSuccStack;
    private int tjCallStackIndex = -1;
    private int tjSuccIter;
    private int tjMaxdfs;
	private final Stack<Integer> tjStack;
    private TIntObjectMap<int[]> tjSuccMap;
    
    
	private boolean[] hasLoop;
	private final LinkedList<ISet> maxSCCs;

	    
	public TarjanSCCsNonrecursive(NBA aut, ISet initials) {
//		this.automaton = aut;
		this.initials = initials;
		this.maxSCCs = new LinkedList<>();
        this.hasLoop = new boolean[aut.getStateSize()];

        this.tjStack = new Stack<>();
        this.tjLowlinkMap = new TIntIntHashMap();
        this.tjDfsMap   = new TIntIntHashMap();
        this.tjCallNodeStack = new int[aut.getStateSize()];
        this.tjCallSuccStack = new int[aut.getStateSize()];
        this.tjSuccMap = new TIntObjectHashMap<>();
        this.tjInStack = UtilISet.newISet();
        this.tjMaxdfs = 0;
        for(int s = 0; s < aut.getStateSize(); s ++) {
        	ISet succs = UtilISet.newISet();
        	for(int a : aut.getState(s).getEnabledLetters()) {
        		for (int succ : aut.getState(s).getSuccessors(a)) {
        				succs.set(succ);
        				if(succ == s) {
        					hasLoop[s] = true;
        				}
        		}
        	}
        	int[] arrSuccs = new int[succs.cardinality()];
        	int i = 0;
        	for(int succ : succs) {
        		arrSuccs[i] = succ;
        		i ++;
        	}
        	this.tjSuccMap.put(s, arrSuccs);
        }
        explore();
	}
	
	public List<ISet> getSCCs() {
		return maxSCCs;
	}

	private void explore() {
		// enumerator
		for(int init : this.initials ) {
			if(! tjDfsMap.containsKey(init)) {
				tjNode = init;
				tarjan();
			}
		}
	}
	
	private void tarjan() {
		
		// initialize
        tjCallStackIndex = 0;
        tjSuccIter = 0;
        
        // put tjNode into stack
        tjDfsMap.put(tjNode, tjMaxdfs);
        tjLowlinkMap.put(tjNode, tjMaxdfs);
        tjMaxdfs++;
        tjInStack.set(tjNode);
        tjStack.push(tjNode);

        while (tjCallStackIndex >= 0) {
        	// now we traverse the states that can be reachable from tjNode
            int numSucc = this.tjSuccMap.get(tjNode).length;
            if (tjSuccIter < numSucc) {
                int succNode = this.tjSuccMap.get(tjNode)[tjSuccIter];
                tjSuccIter++; // move to next successor
				// did not visit succNode before
				if (!tjDfsMap.containsKey(succNode)) {
					// first, put node and tjSuccIter for return
					tjCallNodeStack[tjCallStackIndex] = tjNode;
					tjCallSuccStack[tjCallStackIndex] = tjSuccIter;
					tjCallStackIndex++; // stack size
					// jump to succNode
					tjNode = succNode;
					tjSuccIter = 0; // prepare for traversing successors of succNode
					tjDfsMap.put(tjNode, tjMaxdfs);
					tjLowlinkMap.put(tjNode, tjMaxdfs);
					tjMaxdfs++;
					tjStack.push(tjNode);
					tjInStack.set(tjNode);
				} else if (tjInStack.get(succNode)) {
					tjLowlinkMap.put(tjNode, Math.min(tjLowlinkMap.get(tjNode), tjDfsMap.get(succNode)));
				}
            } else {
            	// pop out sccs
                if (tjLowlinkMap.get(tjNode) == tjDfsMap.get(tjNode)) {
                    int succNode;
                    ISet scc = UtilISet.newISet();

                    do {
                        succNode = tjStack.pop();
                        tjInStack.clear(succNode);
                        scc.set(succNode);
                    } while (tjNode != succNode);
        			if(scc.cardinality() > 1 || hasLoop[tjNode]) {
        				maxSCCs.add(scc);
        			}
                }
                // return to upper level
                tjCallStackIndex--;
                // now return back to tjNode
                if (tjCallStackIndex >= 0) {
                    int succNode = tjNode;
                    tjNode = tjCallNodeStack[tjCallStackIndex];
                    // restore
                    tjSuccIter = tjCallSuccStack[tjCallStackIndex];
                    tjLowlinkMap.put(tjNode, Math.min(tjLowlinkMap.get(tjNode), tjLowlinkMap.get(succNode)));
                }
            }
        }
	}


}

