package roll.learner.dpa.lomega;

import roll.automata.DPA;
import roll.automata.FDFA;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.learner.fdfa.LearnerFDFA;
import roll.learner.nba.lomega.UtilLOmega;
import roll.learner.nba.lomega.translator.TranslatorFDFA;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.table.HashableValue;
import roll.util.Timer;
import roll.words.Alphabet;

public class LearnerDPALOmega extends LearnerBase<DPA> {

	private final LearnerFDFA fdfaLearner;

	public LearnerDPALOmega(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
		fdfaLearner = UtilLOmega.getLearnerFDFA(options, alphabet, membershipOracle);
	}

	@Override
	public LearnerType getLearnerType() {
		return LearnerType.DPA_FDFA;
	}

	protected void initialize() {
		fdfaLearner.startLearning();
		constructHypothesis();
	}

	@Override
	protected void constructHypothesis() {
		// construct BA from FDFA
		FDFA fdfa = fdfaLearner.getHypothesis();
//	        hypothesis = UtilLOmega.constructNBA(options, fdfa);
	}

	@Override
	public void refineHypothesis(Query<HashableValue> query) {

		options.log.verbose("Current FDFA:\n" + fdfaLearner.getHypothesis().toString());
		options.log.println("Analyzing counterexample for FDFA learner...");
		Timer timer = new Timer();
		timer.start();
		TranslatorFDFA translator = UtilLOmega.getTranslator(options, fdfaLearner, membershipOracle);
		// lazy equivalence check is implemented here
		HashableValue mqResult = query.getQueryAnswer();
		if (mqResult == null) {
			mqResult = membershipOracle.answerMembershipQuery(query);
		}
		query.answerQuery(mqResult);
		translator.setQuery(query);
		timer.stop();
		options.stats.timeOfTranslator += timer.getTimeElapsed();
		while (translator.canRefine()) {
			timer.start();
			Query<HashableValue> ceQuery = translator.translate();
			timer.stop();
			options.stats.timeOfTranslator += timer.getTimeElapsed();
			fdfaLearner.refineHypothesis(ceQuery);
			// usually lazyeq is not very useful
			if (options.optimization != Options.Optimization.LAZY_EQ)
				break;
		}

		constructHypothesis();
	}

	public LearnerFDFA getLearnerFDFA() {
		return fdfaLearner;
	}

	@Override
	public String toString() {
		return fdfaLearner.toString();
	}

	@Override
	public String toHTML() {
		return fdfaLearner.toHTML();
	}

}
