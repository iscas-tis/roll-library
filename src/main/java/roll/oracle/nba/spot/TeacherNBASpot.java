package roll.oracle.nba.spot;

import roll.automata.NBA;
import roll.main.Options;
import roll.oracle.nba.TeacherNBA;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Word;

@Deprecated
public class TeacherNBASpot extends TeacherNBA {

	public TeacherNBASpot(Options options, NBA target) {
		super(options, target);
	}

	@Override
	protected Query<HashableValue> checkEquivalence(NBA hypothesis) {
		NBA A, B;
        if(target.getStateSize() > hypothesis.getStateSize()) {
            A = target;
            B = hypothesis;
        }else {
            A = hypothesis;
            B = target;
        }
        SpotThread2 spot = new SpotThread2(A, B, options);
        spot.start();
        while(spot.isAlive()) {
        	// do nothing
        }
        Pair<Word, Word> result = spot.getCounterexample();        
        Query<HashableValue> ceQuery = null;
        if(result != null) {
            ceQuery = new QuerySimple<>(result.getLeft(), result.getRight());
            ceQuery.answerQuery(new HashableValueBoolean(false));
            return ceQuery;
        }
        spot = new SpotThread2(B, A, options);
        spot.start();
        while(spot.isAlive()) {
        	// do nothing
        }
        result = spot.getCounterexample();
        if(result != null) {
            ceQuery = new QuerySimple<>(result.getLeft(), result.getRight());
            ceQuery.answerQuery(new HashableValueBoolean(false));
            return ceQuery;
        }
        Word wordEmpty = target.getAlphabet().getEmptyWord();
        ceQuery = new QuerySimple<>(wordEmpty, wordEmpty);
        ceQuery.answerQuery(new HashableValueBoolean(true));
        return ceQuery;
	}
	
	
	

}
