package roll.learner.nba.lomega.translator;


import dk.brics.automaton.Automaton;
import roll.automata.operations.FDFAOperations;
import roll.learner.fdfa.LearnerFDFA;
import roll.query.Query;
import roll.table.HashableValue;

public class TranslatorFDFAUnder extends TranslatorFDFA {
	
	public TranslatorFDFAUnder(LearnerFDFA learner) {
		super(learner);
	}

	@Override
	public Query<HashableValue> translate() {
	    // every time we initialize fdfa, in case it is modified
	    fdfa = fdfaLearner.getHypothesis();
		String counterexample = translateLower();
		return getQuery(counterexample, ceQuery.getQueryAnswer());
	}
	
	// -------- this is for lower BA construction ----------------
	private String translateLower() {
		if(options.verbose) options.log.println(autUVOmega.toDot());
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
			if(options.verbose) options.log.println("Not in target: " + ceStr);
		}
		
		return ceStr;
	}

}
