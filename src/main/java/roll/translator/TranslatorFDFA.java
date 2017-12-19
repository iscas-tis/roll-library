package roll.translator;

import roll.automata.FDFA;
import roll.automata.operations.FDFAOperations;
import roll.learner.fdfa.LearnerFDFA;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.util.Timer;
import roll.words.Word;

public abstract class TranslatorFDFA extends TranslatorSimple<FDFA> {
	
	protected LearnerFDFA learnerFDFA;
	protected FDFA fdfa;
	protected dk.brics.automaton.Automaton autUVOmega;
	
	public TranslatorFDFA(LearnerFDFA learner) {
		super(learner);
		assert learner != null ;
		this.learnerFDFA = learner;
		this.fdfa = learnerFDFA.getHypothesis();
	}
	
	// initialize the translator for query
	@Override
	public void setQuery(Query<Boolean> query) {
		this.ceQuery = query;
		this.called = false;
		// get deterministic automaton for (u,v)
		Timer timer = new Timer();
		timer.start();
		Word prefix = query.getPrefix();
		Word suffix = query.getSuffix();
		assert prefix != null && suffix != null;
		autUVOmega = FDFAOperations.buildDDollar(prefix, suffix);
		autUVOmega.setDeterministic(true);
		timer.stop();
		options.stats.timeOfTranslator += timer.getTimeElapsed();
	}
	
	
	protected String getPositiveCounterExample(LearnerFDFA learnerFDFA
			, dk.brics.automaton.Automaton autDollar) {
		
		dk.brics.automaton.Automaton dollarFDFAComplement = FDFAOperations.buildDTwo(fdfa);
		dk.brics.automaton.Automaton autMinus = autDollar.intersection(dollarFDFAComplement);
		assert autMinus != null;
		String ceStr = autMinus.getShortestExample(true);
		if(options.verbose) System.out.println("in target: " + ceStr);
		return ceStr;
	}
	
	protected Query<Boolean> getQuery(String counterexample, boolean result) {
		Timer timer = new Timer();
		timer.start();
		if(options.verbose) System.out.println("final counterexample " + counterexample);
		int dollarNr = counterexample.indexOf(alphabet.DOLLAR); //
		Word prefix = alphabet.getWordFromString(counterexample.substring(0, dollarNr));
		Word period = alphabet.getWordFromString(counterexample.substring(dollarNr + 1));
		Query<Boolean> query = new QuerySimple<>(prefix, period);
		query.answerQuery(result);
		
		timer.stop();
		options.stats.timeOfTranslator += timer.getTimeElapsed();
		return query;
	}
	
	@Override
	public boolean canRefine() {
		if(! called) {
			called = true;
			return true;
		}
		// else it must be using optimization treating eq test as the last resort
		Timer timer = new Timer();
		timer.start();
		
		// check whether we can still use current counter example 
		assert ceQuery != null && autUVOmega != null;
		// construct lower/upper Buechi automaton

//		NBA buechi = FDFAOperations.
//		List<String> prefix = new ArrayList<String>();
//		List<String> suffix = new ArrayList<String>();
//		
//		for(int letterNr = 0; letterNr < ceQuery.getPrefix().length(); letterNr ++) {
//			prefix.add(alphabet.letterToString(ceQuery.getPrefix().getLetter(letterNr)));
//		}
//		
//		for(int letterNr = 0; letterNr < ceQuery.getSuffix().length(); letterNr ++) {
//			suffix.add(contextWord.letterToString(ceQuery.getSuffix().getLetter(letterNr)));
//		}
		
//		boolean accepted = BuechiRunner.isAccepting(buechi, prefix, suffix);

		// (u, v) is in target, not accepted then needs refine again
		boolean result = false ;
//		if (ceQuery.getQueryAnswer().isCeInTarget){
//			result = ! accepted;
//		}//else
//		else {
//			result = accepted;
//		}
		
		
		timer.stop();
		options.stats.timeOfTranslator += timer.getTimeElapsed();
		
		return result;
		
	}
	/* another alternative approach for canRefine is to construct the corresponding
	 * Buechi automaton and then check whether (u, v) is accepted accordingly.
	 * This method may need less memory */


}
