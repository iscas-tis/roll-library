package roll.learner.nba.lomega.translator;


import dk.brics.automaton.Automaton;
import roll.automata.operations.FDFAOperations;
import roll.learner.fdfa.LearnerFDFA;
import roll.query.Query;
import roll.table.HashableValue;
import roll.util.Timer;

public class TranslatorFDFAUnder extends TranslatorFDFA {
	
	public TranslatorFDFAUnder(LearnerFDFA learner) {
		super(learner);
	}

	@Override
	public Query<HashableValue> translate() {
	    // every time we initialize fdfa, in case it is modified
	    fdfa = fdfaLearner.getHypothesis();
		Timer timer = new Timer();
		timer.start();
		String counterexample = translateLower();
		timer.stop();
		options.stats.timeOfTranslator += timer.getTimeElapsed();
		return getQuery(counterexample, ceQuery.getQueryAnswer());
	}
	
	// -------- this is for lower BA construction ----------------
	private String translateLower() {
		if(options.verbose) System.out.println(autUVOmega.toString());
		boolean isCeInTarget = ceQuery.getQueryAnswer().get();
		
		String ceStr = null;
		if(isCeInTarget) {
			// positive Counterexample, (u, v) is in target, not in constructed
			// BA, but possibly it is in FDFA, , already normalized
			ceStr = getPositiveCounterExample(autUVOmega);
		}else {
			// negative Counterexample, (u, v) is not in target, but in FDFA
			// get intersection, already normalized.
			Automaton dollarFDFA = FDFAOperations.buildDOne(fdfa);
			Automaton autInter = autUVOmega.intersection(dollarFDFA);
			assert autInter != null;
			ceStr = autInter.getShortestExample(true);
			if(options.verbose) System.out.println("Not in target: " + ceStr);
		}
		
		return ceStr;
	}

}
