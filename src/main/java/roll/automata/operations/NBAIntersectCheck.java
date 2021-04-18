/* Copyright (c) 2016, 2017                                               */
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBAIntersectCheck {
    
    private NBA fstOp;
    private NBA sndOp;
    private boolean empty = true;
    private int numStates;
    
    public NBAIntersectCheck(NBA fstOp, NBA sndOp) {
        assert fstOp != null && sndOp != null;
        this.fstOp = fstOp;
        this.sndOp = sndOp;
        this.numStates = 0;
        new TarjanExplore();
    }
    
    public boolean isEmpty() {
        return empty;
    }
    // product state (p, q)
    private class ProductState {
        int fstState;
        int sndState;
        int resState;
        ProductState(int fstState, int sndState) {
            this.fstState = fstState;
            this.sndState = sndState;
        }
        
        @Override
        public int hashCode() {
            return fstState * sndOp.getStateSize() + sndState;
        }
        
        @Override
        public boolean equals(Object obj) {
            if(obj == null) return false;
            if(obj == this) return true;
            if(obj instanceof ProductState) {
                ProductState other = (ProductState)obj;
                return fstState == other.fstState
                    && sndState == other.sndState;
            }
            return false;
        }
        
        @Override
        public String toString() {
            return resState + ":(" + fstState + "," + sndState + ")";
        }
    }
    
    public class TarjanExplore {
    	//	private final NBA automaton;    	
    	private int tjNode;
    	private final ISet tjInStack;
    	private final TIntIntMap tjDfsMap ; // index
    	private final TIntIntMap tjLowlinkMap; // lowlink
//        private final int[] tjCallNodeStack;
//        private int[] tjCallSuccStack;
        private int tjCallStackIndex = -1;
        private int tjSuccIter;
        private int tjMaxdfs;
        private Stack<Integer> tjCallSuccStack;
        private Stack<Integer> tjCallNodeStack;
    	private final Stack<Integer> tjStack;
        private TIntObjectMap<int[]> tjSuccMap;
        private ArrayList<ProductState> prodArr;
        
        private Map<ProductState, ProductState> map;
        
    	private ISet hasLoop;

    	    
    	public TarjanExplore() {
//    		this.automaton = aut;
            this.hasLoop = UtilISet.newISet();
            this.prodArr = new ArrayList<ProductState>();
            this.tjStack = new Stack<>();
            this.tjLowlinkMap = new TIntIntHashMap();
            this.tjDfsMap   = new TIntIntHashMap();
            this.tjCallNodeStack = new Stack<>();
            this.tjCallSuccStack = new Stack<>();
            this.tjSuccMap = new TIntObjectHashMap<>();
            this.tjInStack = UtilISet.newISet();
            this.tjMaxdfs = 0;
            this.map = new HashMap<>();
            explore();
    	}
    	
        ProductState getOrAddState(int fst, int snd) {
            ProductState prod = new ProductState(fst, snd);
            if(map.containsKey(prod)) {
                return map.get(prod);
            }
            prod.resState = numStates;
            while(prodArr.size() <= numStates) {
            	prodArr.add(null);
            }
            prodArr.add(numStates, prod);
            map.put(prod, prod);
            ++ numStates;
            return prod;
        }

    	private void explore() {
    		// enumerator
    		getOrAddState(fstOp.getInitialState(), sndOp.getInitialState());
    		tjNode = 0;
    		tarjan();
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
            	// needs to compute the sucessor of tjNode
            	ProductState currProd = this.prodArr.get(tjNode);
            	assert(tjNode == currProd.resState);
            	if(! this.tjSuccMap.containsKey(currProd.resState)) {
            		HashSet<ProductState> succs = new HashSet<>();
                    for (int letter = 0; letter < fstOp.getAlphabetSize(); letter ++) {
                        for(int sndSucc : sndOp.getSuccessors(currProd.sndState, letter)) {
                            for(int fstSucc : fstOp.getSuccessors(currProd.fstState, letter)) {
                            	ProductState succ = getOrAddState(fstSucc, sndSucc);
                            	succs.add(succ);
                            	if(currProd.resState == succ.resState) {
                            		this.hasLoop.set(currProd.resState);
                            	}
                            }
                        }
                    }
                    int[] arrSuccs = new int[succs.size()];
                    int i = 0 ;
                    for(ProductState succ : succs) {
                    	arrSuccs[i] = succ.resState;
                    	i ++;
                    }
                    this.tjSuccMap.put(currProd.resState, arrSuccs);
            	}
            	// now we traverse the states that can be reachable from tjNode
                int numSucc = this.tjSuccMap.get(tjNode).length;
                if (tjSuccIter < numSucc) {
                    int succNode = this.tjSuccMap.get(tjNode)[tjSuccIter];
                    tjSuccIter++; // move to next successor
    				// did not visit succNode before
    				if (!tjDfsMap.containsKey(succNode)) {
    					// first, put node and tjSuccIter for return
    					System.out.println("CallStackIndex = " + tjCallStackIndex);
    					System.out.println("arr length = " + tjCallNodeStack.size());
    					tjCallNodeStack.push(tjNode);
    					tjCallSuccStack.push(tjSuccIter);
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
                        boolean fstAcc = false;
                        boolean sndAcc = false;
                        do {
                            succNode = tjStack.pop();
                            tjInStack.clear(succNode);
                            scc.set(succNode);
                            ProductState prod = this.prodArr.get(succNode);
                            fstAcc = fstAcc || fstOp.isFinal(prod.fstState);
                            sndAcc = sndAcc || sndOp.isFinal(prod.sndState);
                        } while (tjNode != succNode);
            			if((scc.cardinality() > 1 || hasLoop.get(tjNode)) && fstAcc && sndAcc) {
            				empty = false;
            				System.out.println("fstAcc = " + fstAcc + " sndAcc = " + sndAcc);
            				return;
            			}
                    }
                    // return to upper level
                    tjCallStackIndex--;
                    // now return back to tjNode
                    if (tjCallStackIndex >= 0) {
                        int succNode = tjNode;
                        tjNode = tjCallNodeStack.pop();
                        // restore
                        tjSuccIter = tjCallSuccStack.pop();
                        tjLowlinkMap.put(tjNode, Math.min(tjLowlinkMap.get(tjNode), tjLowlinkMap.get(succNode)));
                    }
                }
            }
    	}


    }
}
