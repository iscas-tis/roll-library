package roll.translator;


import roll.learner.fdfa.LearnerFDFA;
import roll.query.Query;
import roll.util.Timer;

public class TranslatorFDFALower extends TranslatorFDFA {
	
	public TranslatorFDFALower(LearnerFDFA learner) {
		super(learner);
	}

	@Override
	public Query<Boolean> translate() {
		Timer timer = new Timer();
		timer.start();
		String counterexample = translateLower();
		timer.stop();
		options.stats.timeOfTranslator += timer.getTimeElapsed();
		return getQuery(counterexample, ceQuery.getQueryAnswer());
	}
	
	// -------- this is for lower Buechi automaton ----------------
	private String translateLower() {
//		if(options.verbose) AutomatonPrinter.print(autUVOmega, System.out);
//		EqResult eqResult = ceQuery.getQueryAnswer();
//		
//		String ceStr = null;
//		if(eqResult.isCeInTarget) {
//			// positive Counterexample, (u, v) is in target, not in constructed
//			// Buechi, but possibly it is in FDFA, , already normalized
//			ceStr = getPositiveCounterExample(learnerFDFA, autUVOmega);
//		}else {
//			// negative Counterexample, (u, v) is not in target, but in FDFA
//			// get intersection, already normalized.
//			dk.brics.automaton.Automaton dollarFDFA = DollarAutomatonBuilder
//					.buildDollarAutomaton(learnerFDFA);
//			dk.brics.automaton.Automaton autInter = autUVOmega.intersection(dollarFDFA);
//			assert autInter != null;
//			ceStr = autInter.getShortestExample(true);
//			if(Options.verbose) System.out.println("Not in target: " + ceStr);
//		}
		
		return null;
	}


}
