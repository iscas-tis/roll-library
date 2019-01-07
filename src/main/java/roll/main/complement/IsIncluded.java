package roll.main.complement;

import roll.util.Pair;
import roll.words.Word;

public interface IsIncluded {
	
	Boolean isIncluded();
	
	Pair<Word, Word> getCounterexample();

}
