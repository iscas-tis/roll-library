package roll.automata;

import roll.automata.operations.NBAOperations;
import roll.util.sets.ISet;
import roll.words.Word;

public class AcceptTDBA implements Accept {
	
	
	protected final TDBA tdba;
    
    public AcceptTDBA(TDBA ba) {
        this.tdba = ba;
    }
    
    @Override
    public boolean accept(ISet states) {
        throw new UnsupportedOperationException("TDBA does not have state-based acceptance condition");
    }
    @Override
    public boolean accept(Word prefix, Word period) {
    	// need to check whether there is accepting transition
    	NBA ba = NBAOperations.fromTDBA(tdba);
        return NBAOperations.accepts(ba, prefix, period);
    }
}
