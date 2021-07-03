package roll.main.inclusion.congr;

import java.util.LinkedList;
import java.util.List;

import roll.automata.NBA;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class UtilCongruenceClassProfile {
	
	private UtilCongruenceClassProfile() {
		
	}
	
	
	public static CongruenceClassProfile getNext(NBA A, int letter, CongruenceClassProfile congrClass, boolean progress) {
		List<ISet> ordSets = congrClass.getOrderedSets();
		
        ISet leftSuccs = UtilISet.newISet();
        ISet finalStates = A.getFinalStates();
        
        CongruenceClassProfile nextCongrClass = new CongruenceClassProfile();
        
        for(int i = 0; i < ordSets.size(); i ++) {
            // compute successors
            ISet Si = ordSets.get(i);
            ISet finalSuccs = UtilISet.newISet();
            ISet nonFinalSuccs = UtilISet.newISet();
            for(final int p : Si) {
                for(final int q : A.getState(p).getSuccessors(letter)) {
                    // ignore successors already have been visited
                    if(leftSuccs.get(q)) continue;
                    if(finalStates.get(q)) {
                        finalSuccs.set(q);
                    }else {
                        nonFinalSuccs.set(q);
                    }
                    leftSuccs.set(q);
                }
            }
            // now we will record every thing
            if(!finalSuccs.isEmpty()) {
                nextCongrClass.addSet(finalSuccs);
                // now we record the parent
                if(progress) {
                	Pair<Integer, Boolean> pair = congrClass.getMaxPres(i); 
                	nextCongrClass.setMaxPres(pair.getLeft(), true);
                }
            }
            
            if(!nonFinalSuccs.isEmpty()) {
                nextCongrClass.addSet(nonFinalSuccs);
             // now we record the parent
                if(congrClass.isProgress()) {
                	Pair<Integer, Boolean> pair = congrClass.getMaxPres(i); 
                	nextCongrClass.setMaxPres(pair.getLeft(), finalStates.overlap(Si) || pair.getRight());
                }
            }
        }
        if(progress) {
        	nextCongrClass.setPrevOrderedSets(congrClass.prevOrderedSets);
        }
        return nextCongrClass;
	}

	
	public static ISet getReachSet(int state, NBA nba) {
		LinkedList<Integer> queue = new LinkedList<>();
		queue.add(state);
		ISet visited = UtilISet.newISet();
		visited.set(state);
		while (!queue.isEmpty()) {
			int lState = queue.remove();
			// ignore unused states
			for (int c = 0; c < nba.getAlphabetSize(); c++) {
				for (int lSucc : nba.getSuccessors(lState, c)) {
					if (!visited.get(lSucc)) {
						queue.add(lSucc);
						visited.set(lSucc);
					}
				}
			}
		}
		return visited;
	}
}
