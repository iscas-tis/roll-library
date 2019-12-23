package roll.main.ltlf2dfa;

import roll.automata.DFA;
import roll.automata.NBA;
import roll.main.Options;
import roll.oracle.TeacherAbstract;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

public class TeacherLTLf2DFA extends TeacherAbstract<DFA> {

    private final Alphabet alphabet;
    private final NBA posA;
    private final NBA negB;
    
	public TeacherLTLf2DFA(Options options, String ltlf) {
		super(options);
		// now we convert NBA
        Pair<NBA, NBA> pair = UtilLTLf2DFA.translateLtlf2NFA(options, ltlf);
        this.posA = pair.getLeft();
        this.negB = pair.getRight(); 
        this.alphabet = this.posA.getAlphabet();
	}
	
	public NBA getNFA() {
		return posA;
	}

	@Override
	protected HashableValue checkMembership(Query<HashableValue> query) {
		Word word = query.getQueriedWord();
        boolean answer = posA.getSuccessors(word).overlap(posA.getFinalStates());
        return new HashableValueBoolean(answer);
	}

	@Override
	protected Query<HashableValue> checkEquivalence(DFA hypothesis) {
		// first check whether L(posA) is a subset of L(H)
		// posA /\ neg H
		NFAIntersectionCheck checker = new NFAIntersectionCheck(hypothesis, posA, true);
		Word wordCE = checker.explore();
		Query<HashableValue> query;
		if(wordCE != null) {
			query = new QuerySimple<>(wordCE);
			query.answerQuery(new HashableValueBoolean(false));
		}else {
			// check whether L(H) is a subset of L(posA)
			// posB /\ H
			checker = new NFAIntersectionCheck(hypothesis, negB, false);
			wordCE = checker.explore();
			if(wordCE != null) {
				query = new QuerySimple<>(wordCE);
				query.answerQuery(new HashableValueBoolean(false));
			}else {
				query = new QuerySimple<>(alphabet.getEmptyWord());
				query.answerQuery(new HashableValueBoolean(true));
			}
		}
		return query;
	}

}
