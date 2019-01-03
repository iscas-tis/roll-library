package roll.oracle.nba.rabit;

import automata.FiniteAutomaton;
import roll.automata.NBA;
import roll.main.Options;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Word;

/**
 * use the idea in complementation to check equivalence of two Buchi automata
 * */

public class TeacherNBARABITFast extends TeacherNBARABIT {

	public TeacherNBARABITFast(Options options, NBA target) {
		super(options, target);
	}
	
	/**
	 * check whether two Buchi automata are equivalent
	 * */
	@Override
    protected Query<HashableValue> checkEquivalence(NBA hypothesis) {
        FiniteAutomaton rabitHypo = UtilRABIT.toRABITNBA(hypothesis);
        FiniteAutomaton A, B;
//        if(rabitHypo.states.size() > rabitTgt.states.size()) {
//            A = rabitTgt;
//            B = rabitHypo;
//        }else {
//            A = rabitHypo;
//            B = rabitTgt;
//        }
//        Pair<Word, Word> result = UtilRABIT.isIncluded(target.getAlphabet(), A, B);
        Query<HashableValue> ceQuery = null;
//        if(result != null) {
//            ceQuery = new QuerySimple<>(result.getLeft(), result.getRight());
//            ceQuery.answerQuery(new HashableValueBoolean(false));
//            return ceQuery;
//        }
//        result = UtilRABIT.isIncluded(target.getAlphabet(), B, A);
//        if(result != null) {
//            ceQuery = new QuerySimple<>(result.getLeft(), result.getRight());
//            ceQuery.answerQuery(new HashableValueBoolean(false));
//            return ceQuery;
//        }
        Word wordEmpty = target.getAlphabet().getEmptyWord();
        ceQuery = new QuerySimple<>(wordEmpty, wordEmpty);
        ceQuery.answerQuery(new HashableValueBoolean(true));
        return ceQuery;
    }

}
