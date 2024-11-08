package roll.learner.nba.mp;

import java.util.LinkedList;
import java.util.Queue;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.NFA;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

public class UtilPath {
	
	//TODO: should replace the findPath function in EmptinessCheck 
	public static Word findPath(NFA result, int s, int t) {
        // store the predecessors (value) of the specific states (key)
        TIntIntMap predStates = new TIntIntHashMap();
        TIntIntMap predLabels = new TIntIntHashMap();
        Alphabet alphabet = result.getAlphabet();
        ISet visited = UtilISet.newISet();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(s);
        visited.set(s);
        while(! queue.isEmpty()) {
            if(visited.get(t)) break; // already found it
            int cur = queue.poll();
            for(int c = 0; c < alphabet.getLetterSize(); c ++) {
                ISet succs = result.getSuccessors(cur, c);
                if(succs.isEmpty()) continue;
                for (final int succ : succs) {
                    if (!visited.get(succ)) {// in states allowed and not visited
                        queue.add(succ); // add in queue
                        predStates.put(succ, cur); // record predecessors
                        predLabels.put(succ, c); // record previous letter
                        visited.set(succ);
                    }
                }
            }
        }
        // must have a path from s to t
//        LinkedList<Integer> run = new LinkedList<>();
        Word word = result.getAlphabet().getEmptyWord();
        int cur = t;
        while(cur != s) {
            word = word.preappend(predLabels.get(cur));
            cur = predStates.get(cur);
        }
        return word;
	}

}
