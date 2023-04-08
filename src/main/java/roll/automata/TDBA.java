package roll.automata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import roll.automata.operations.NBAOperations;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.words.Alphabet;

public class TDBA extends DFA {
	
	HashSet<Pair<Integer, Integer>> trans;
    
	public TDBA(final Alphabet alphabet) {
        super(alphabet);
        this.accept = new AcceptTDBA(this);
        this.trans = new HashSet<>();
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
    
    @Override
    public boolean isFinal(int state) {
    	throw new UnsupportedOperationException("TDBA does not support isFinal(int)");

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
            builder.append("  " + node + " [label=\"" + node + "\"");
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
