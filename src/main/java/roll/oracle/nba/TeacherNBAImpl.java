package roll.oracle.nba;

import automata.FiniteAutomaton;
import roll.automata.NBA;
import roll.main.Options;
import roll.main.complement.IsIncluded;
import roll.main.complement.UtilComplement;
import roll.oracle.nba.rabit.UtilRABIT;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Word;

public class TeacherNBAImpl extends TeacherNBA {

	FiniteAutomaton rTarget = null;
	
	public TeacherNBAImpl(Options options, NBA target) {
		super(options, target);
		if(options.parallel || !options.spot) {
			rTarget = UtilRABIT.toRABITNBA(target);
		}
	}

	@Override
	protected Query<HashableValue> checkEquivalence(NBA hypothesis) {
		FiniteAutomaton rA = null, rB;
		NBA A, B;
		if(options.parallel || !options.spot) {
			rA = UtilRABIT.toRABITNBA(hypothesis);
		}
		rB = rTarget;
		if(target.getStateSize() > hypothesis.getStateSize()) {
            A = target;
            B = hypothesis;
            FiniteAutomaton tmp = rA;
            rA = rTarget;
            rB = tmp;
        }else {
            A = hypothesis;
            B = target;
        }
		
		IsIncluded included = UtilComplement.checkInclusion(options, target.getAlphabet(), A, B, rA, rB);
		Pair<Word, Word> result = included.getCounterexample();
        Query<HashableValue> ceQuery = null;
        if(result != null) {
            ceQuery = new QuerySimple<>(result.getLeft(), result.getRight());
            ceQuery.answerQuery(new HashableValueBoolean(false));
            return ceQuery;
        }
        included = UtilComplement.checkInclusion(options, target.getAlphabet(), B, A, rB, rA);
        result = included.getCounterexample();
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
