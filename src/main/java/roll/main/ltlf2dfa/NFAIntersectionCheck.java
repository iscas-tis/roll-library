package roll.main.ltlf2dfa;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.DFA;
import roll.automata.NFA;
import roll.automata.operations.ProductState;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

// BFS search

public class NFAIntersectionCheck {
	
	DFA A;
	NFA B;
	
	int numOfStates = 0;
	ArrayList<ProductState> listOfProdStates;
	TIntObjectMap<ProductState> mapOfProdstates;
	
	public NFAIntersectionCheck(DFA A, NFA B) {
		this.A = A;
		this.B = B;
		this.listOfProdStates = new ArrayList<>();
		this.mapOfProdstates = new TIntObjectHashMap<ProductState>();
	}
	// do priority on length
	
	public ProductState addState(int fst, int snd) {
		ProductState prod = new ProductState(fst, snd, B.getStateSize());
		int key = prod.hashCode();
		if(this.mapOfProdstates.containsKey(key)) {
			return this.mapOfProdstates.get(key);
		}
		int resState = listOfProdStates.size();
		prod.resState = resState;
		listOfProdStates.add(prod);
		this.mapOfProdstates.put(key, prod);
		return prod;
	}
	
	public Word explore() {
		
		TIntIntMap predStates = new TIntIntHashMap();
        TIntIntMap predLabels = new TIntIntHashMap();
        Alphabet alphabet = A.getAlphabet();
        ISet visited = UtilISet.newISet();
        
        // initial state 
        Queue<ProductState> queue = new LinkedList<>();
        ProductState initState = addState(A.getInitialState(), B.getInitialState());
        queue.add(initState);
        visited.set(initState.resState);
        int target = -1;
        
        while(! queue.isEmpty()) {
            ProductState cur = queue.poll();
            for(int c = 0; c < alphabet.getLetterSize() && target < 0; c ++) {
            	// A successor
            	int succA = A.getSuccessor(cur.getFirst(), c);
                ISet succs = B.getSuccessors(cur.getSecond(), c);
                if(succs.isEmpty()) continue;
                for (final int succB : succs) {
                	// product state
                	ProductState succ = addState(succA, succB);
                    if (! visited.get(succ.resState)) {
                    	// in states allowed and not visited
                        queue.add(succ); // add in queue
                        predStates.put(succ.resState, cur.resState); // record predecessors
                        predLabels.put(succ.resState, c); // record previous letter
                        visited.set(succ.resState);
                    }
                    if(A.isFinal(succA) && B.isFinal(succB)) {
                    	target = succ.resState;
                    	break;
                    }
                }
            }
            if(target > 0) {
            	break;
            }
        }
        if(target < 0) {
        	return null;
        }
        // must have a path from s to t
        LinkedList<Integer> word = new LinkedList<>();
        int cur = target;
        while(cur != initState.resState) {
            word.addFirst(predLabels.get(cur));
            cur = predStates.get(cur);
        }
        Word w = A.getAlphabet().getEmptyWord();
        for(Integer letter : word) {
            w = w.append(letter);
        }
        
		return w;
	}
	
	

}
