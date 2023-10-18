package roll.automata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.operations.NBAOperations;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.util.Triplet;
import roll.words.Alphabet;

public class TDBA extends DFA {
	
	HashSet<Pair<Integer, Integer>> trans;
	TIntObjectMap<Triplet<Integer, Integer, Integer>> stateMap;
    
	public TDBA(final Alphabet alphabet) {
        super(alphabet);
        this.accept = new AcceptTDBA(this);
        this.trans = new HashSet<>();
        this.stateMap = new TIntObjectHashMap<>();
    }

    @Override
    public AutType getAccType() {
        return AutType.TDBA;
    }
    
    @Override
    public ISet getFinalStates() {
    	throw new UnsupportedOperationException("TDBA does not support getFinalStates()");
    }
    @Override
    public int getFinalSize() {
    	return trans.size();
    }
    
    public void setFinal(int state, int letter) {
    	trans.add(new Pair<>(state, letter));
    }
    
    public boolean isFinal(int state, int letter) {
    	return trans.contains(new Pair<>(state, letter));
    }
    
    // these two methods are designed for CE analysis
    public void setTriplet(int state, Triplet<Integer, Integer, Integer> tpl) {
    	stateMap.put(state
    			, new Triplet<>(tpl.getLeft(), tpl.getMiddle(), tpl.getRight()));
    }
    
    public Triplet<Integer, Integer, Integer> getTriplet(int state) {
    	return stateMap.get(state);
    }
    
    @Override
    public boolean isFinal(int state) {
    	return false;
    }
    
    @Override
    public void setFinal(int state) {
    	throw new UnsupportedOperationException("TDBA does not support setFinal(int)");
    }
    
    @Override
    public void clearFinal(int state) {
    	throw new UnsupportedOperationException("TDBA does not support clearFinal(int)");
    }
    
    @Override
    public String toString(List<String> apList) {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph {\n");
        builder.append("  rankdir=LR;\n");
        int startNode = this.getStateSize();
        for (int node = 0; node < this.getStateSize(); node++) {
        	if (stateMap.size() <= 0) {
        		builder.append("  " + node + " [label=\"" + node + "\"");
        	}else {
            	Triplet<Integer, Integer, Integer> tpl = this.getTriplet(node);
                builder.append("  " + node + " [label=\"" + node + ", " + tpl.toString() + "\"");
        	}
            builder.append(", shape = circle");
            builder.append("];\n");
            for (int letter = 0; letter < this.getAlphabetSize(); letter ++) {
            	ISet succs = this.getState(node).getSuccessors(letter);
            	if (!succs.isEmpty())
            	{
            		int succ = succs.iterator().next();
                    builder.append("  " + node + " -> " + succ
                            + " [label=\"" + apList.get(letter) + "\"");
                    if (this.isFinal(node, letter)) {
                    	builder.append(", style=dashed");
                    }
                    builder.append("];\n");
                }
            }
        }   
        builder.append("  " + startNode + " [label=\"\", shape = plaintext];\n");
        builder.append("  " + startNode + " -> " + this.getInitialState() + " [label=\"\"];\n");
        builder.append("}\n");
        return builder.toString();
    }
    
    @Override
    public String toDot() {
        List<String> apList = new ArrayList<>();
        for(int i = 0; i < alphabet.getLetterSize(); i ++) {
            apList.add("" + alphabet.getLetter(i));
        }
        return toString(apList);
    }
    
    @Override
    public String toBA() {
    	NBA res = NBAOperations.fromTDBA(this);
    	return res.toBA();
    }

}
