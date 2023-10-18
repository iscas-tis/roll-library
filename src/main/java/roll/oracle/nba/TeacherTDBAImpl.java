package roll.oracle.nba;

import roll.automata.NBA;
import roll.automata.operations.NBAIntersectionCheck;
import roll.automata.operations.NBAOperations;
import roll.automata.operations.complement.DBAComplement;
import roll.main.Options;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Word;

public class TeacherTDBAImpl extends TeacherNBA {
	
	DBAComplement complement;

	public TeacherTDBAImpl(Options options, NBA target) {
		super(options, target);
		if (! target.isDeterministic()) {
			throw new RuntimeException("Input target is not deterministic BA");
		}
		complement = new DBAComplement(target);
		complement.explore();
	}
	
	// the input should be deterministic
	@Override
	protected Query<HashableValue> checkEquivalence(NBA hypothesis) {
		if (! hypothesis.isDeterministic()) {
			throw new RuntimeException("Input hypothesis is not deterministic BA");
		}
		// first check whether hypothesis contain less language
		DBAComplement compHypo = new DBAComplement(hypothesis);
		compHypo.explore();
		NBAIntersectionCheck checker = new NBAIntersectionCheck(target, compHypo, true);
		Query<HashableValue> ceQuery = null;
		if (! checker.isEmpty()) {
			checker.computePath();
			Pair<Word, Word> counterexample = checker.getCounterexample();
			ceQuery = new QuerySimple<>(counterexample.getLeft(), counterexample.getRight());
            ceQuery.answerQuery(new HashableValueBoolean(false));
            assert (NBAOperations.accepts(target, counterexample.getLeft(), counterexample.getRight()));
            assert (!NBAOperations.accepts(hypothesis, counterexample.getLeft(), counterexample.getRight()));
            return ceQuery;
		}
		checker = new NBAIntersectionCheck(hypothesis, complement, true);
		if (! checker.isEmpty()) {
			checker.computePath();
			Pair<Word, Word> counterexample = checker.getCounterexample();
			ceQuery = new QuerySimple<>(counterexample.getLeft(), counterexample.getRight());
            ceQuery.answerQuery(new HashableValueBoolean(false));
            assert (!NBAOperations.accepts(target, counterexample.getLeft(), counterexample.getRight()));
            assert (NBAOperations.accepts(hypothesis, counterexample.getLeft(), counterexample.getRight()));
            return ceQuery;
		}
		Word wordEmpty = target.getAlphabet().getEmptyWord();
        ceQuery = new QuerySimple<>(wordEmpty, wordEmpty);
        ceQuery.answerQuery(new HashableValueBoolean(true));
        return ceQuery;
	}

}
