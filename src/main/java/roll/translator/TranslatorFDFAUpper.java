package roll.translator;

import dk.brics.automaton.State;
import roll.learner.fdfa.LearnerFDFA;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.util.Timer;
import roll.words.Word;

public class TranslatorFDFAUpper extends TranslatorFDFA {

	private MembershipOracle<Boolean> membershipOracle;
	private boolean result ;
	
	public TranslatorFDFAUpper(LearnerFDFA learner
			, MembershipOracle<Boolean> membershipOracle) {
		super(learner);
		assert membershipOracle != null;
 		this.membershipOracle = membershipOracle;
	}
	

	@Override
	public Query<Boolean> translate() {
		Timer timer = new Timer();
		timer.start();
		String counterexample = translateUpper();
		timer.stop();
		options.stats.timeOfTranslator += timer.getTimeElapsed();
		return getQuery(counterexample, result);
	}
	
	private String translateUpper() {
		// for general case 
//		if(Options.verbose) AutomatonPrinter.print(autUVOmega, System.out);
//		if(Options.verbose) learnerFDFA.getHypothesis();
		if(ceQuery.getQueryAnswer()) {
			// (u, v) is in L, but it is not in FDFA
			result = true;
			return getPositiveCounterExample(learnerFDFA, autUVOmega);
		}else {
			// (u, v) is not in L, but it is in constructed Buechi
			// possibly it is not in FDFA, already normalized.
			result = false;
			return getCorrectNormalizedCounterExample(learnerFDFA, membershipOracle);
		}
	}
	
	
	// decompose x, y, z
	private int getLastIndexAtFinal(
			dk.brics.automaton.Automaton dkAut, int startNr, String word) {
		int lastNr = -1, currNr = startNr;
		State stateCurr = dkAut.getInitialState();
		// record last accepting index
		if(stateCurr.isAccept()) lastNr = startNr;
		while(currNr < word.length()) {
			State stateNext = stateCurr.step(word.charAt(currNr));
			if(stateNext == null) break;
			if(stateNext.isAccept()) {
				lastNr = currNr;
			}
			stateCurr = stateNext;
			++ currNr;
		}
		
		return lastNr;
	}
	
	private String getCorrectNormalizedCounterExample(LearnerFDFA learnerFDFA
			, MembershipOracle<Boolean> membershipOracle) {
		
//		Word pre = null, suf = null;
//		// 1. first build dollar deterministic automaton for (u, v) 
//		if(Options.verbose) AutomatonPrinter.print(autUVOmega, System.out);
//		
//		// 2. for every final state, we get normalized counterexample
//		Automaton autL = learnerFDFA.getLeadingAutomaton();
//		TIntObjectMap<State> map = new TIntObjectHashMap<>(); 
//		dk.brics.automaton.Automaton dkAutL = UtilAutomaton.convertToDkAutomaton(map, autL);
//		
//		for(int stateNr = 0; stateNr < autL.getNumStates(); stateNr ++) {
//		    Automaton autP = learnerFDFA.getProgressAutomaton(stateNr);
//		    BitSet accStates = autP.getAcceptingStates();
//		    int stateInitP = autP.getInitialStates().nextSetBit(0);
//		    boolean found = false;
//		    // for every final state we get the language intersection
//		    for(int accNr = accStates.nextSetBit(0); accNr >= 0; accNr = accStates.nextSetBit(accNr + 1)) {
//		    	// language for A^u_f
//		    	dk.brics.automaton.Automaton dkAutP = UtilAutomaton.convertToDkAutomaton(autP, stateInitP, accNr);
//		    	dkAutP.minimize();
//		    	// language for M^u_u
//		    	dk.brics.automaton.Automaton dkAutLOther = UtilAutomaton.convertToDkAutomaton(autL, stateNr, stateNr);
//		    	dkAutLOther.minimize();
//		    	// build product for A^u_f and M^u_u
//		    	dk.brics.automaton.Automaton product = dkAutP.intersection(dkAutLOther);
//		    	product.minimize();
//		    	
//		    	if(! product.getAcceptStates().isEmpty()) {
//		    		assert product.getAcceptStates().size() == 1;
//		    		// add epsilon transition to allow x, concatenate y
//		    		product = UtilAutomaton.addEpsilon(product);
////					if(Options.verbose) System.out.println(
////							"\n product: \n " + product.toDot()
////							+ "\nrun a$cbcbcbab, " + BasicOperations.run(product, "cccccbcccbccbcaaaccc"));
//		    		// get u state in leading automaton
//		    		State u = map.get(stateNr); 
//		    		// add Dollar transition
//		    		Transition transDollar = new Transition(ContextWord.getStringDollar().charAt(0), product.getInitialState());
//		    		u.addTransition(transDollar);
////					if(Options.verbose) System.out.println(
////							"\n dkAutL: \n " + dkAutL.toDot()
////							+ "\nrun a$cbcbcbab, " + BasicOperations.run(dkAutL, "ccaccc$cccccbcccbccbcaaaccc"));
//		            // find u $ v string
//		    		dk.brics.automaton.Automaton dkNFA = autUVOmega.intersection(dkAutL);
////		    		dkNFA.minimize();
////		    		if(Options.verbose) System.out.println(
////							"\n dkNFA " + dkNFA.toDot()
////							+ "\nrun a$cbcbcbab, " + BasicOperations.run(dkNFA, "a$cbcbcbab"));
//					
//		    		String counterexample = dkNFA.getShortestExample(true);
//					if(Options.verbose && counterexample != null) System.out.println(" found counterexample " + counterexample);
//		    		// remove duplicate dollar transition
//		    		if(counterexample == null) {
//		    			u.getTransitions().remove(transDollar);
//		    			continue;
//		    		}
//		    		
//		    		found = true;
//		    		// get decomposition (u, xyz...) which is not in L
//					if(Options.verbose) System.out.println("normalized counterexample " + counterexample 
//							+ ", " + BasicOperations.run(dkNFA, counterexample));
//					
//					int dollarNr = counterexample.indexOf(ContextWord.getStringDollar().charAt(0)); //
//					pre = contextWord.getWordFromString(counterexample.substring(0, dollarNr));
//					// must be some x,y,z concatenation
//					String period = counterexample.substring(dollarNr + 1);
//					if(Options.verbose) System.out.println("dkAutLPOther\n " + dkAutLOther.toDot());
//					if(Options.verbose) System.out.println("dkAutP\n " + dkAutP.toDot());
//					if(Options.verbose) System.out.println("dkNFA\n " + dkAutP.toDot());
//					// build deterministic automaton 
//					dk.brics.automaton.Automaton dkAutCE = dkAutP.intersection(dkAutLOther);  
//					Word p = learnerFDFA.getProgressStateLabel(stateNr, accNr);
//					suf = findCorrectPeriod(dkAutCE, pre, p, period, membershipOracle);
//		    	}
//		    	if(found) break;
//		    }
//		    if(found) break;
//		}
//		
//		assert pre != null && suf != null;
//		String ce = pre.toStringExact() + ContextWord.getStringDollar() + suf.toStringExact();
//		return ce;
	    return null;
	}
	
	// complex analysis for counter example
	private Word findCorrectPeriod(dk.brics.automaton.Automaton dkAutCE
			, Word pre, Word p, String period, MembershipOracle<Boolean> membershipOracle) {
		
		int startNr = 0;
		Word suf = null;
		//INVARIANT:  Assume that (u, xyz...) is not in L
		while(startNr < period.length()) {
			
			int lastNr = getLastIndexAtFinal(dkAutCE, startNr, period);
			// only one x left, and (u, x) not in L
			Word x = alphabet.getWordFromString(period.substring(startNr, lastNr + 1));
			if(lastNr == period.length() - 1) {
				boolean r = membershipOracle.answerMembershipQuery(new QuerySimple<>(pre, x));
				if(! r) {
					suf = x;
				}
				
				break;
			}
			// now it must be at least two period, x, y
			boolean r = membershipOracle.answerMembershipQuery(new QuerySimple<>(pre, x));
			// 1. if (u, x) not in L
			if(! r) { 
				suf = x;
				break;
			}
            // (u, yz...) is not in L
            startNr = lastNr + 1;
            suf = null;
		}
		
		if(suf == null) {
			System.err.println("Not able to find counterexample...");
			System.exit(-1);
		}
		return suf;
	}
	
	// complex analysis for counter example
	private Word findCorrectPeriod2(dk.brics.automaton.Automaton dkAutCE
			, Word pre, Word p, String period, MembershipOracle<Boolean> membershipOracle) {
		
		int startNr = 0;
		Word suf = null;
		//INVARIANT:  Assume that (u, xyz...) is not in L
		while(startNr < period.length()) {
			
			int lastNr = getLastIndexAtFinal(dkAutCE, startNr, period);
			// only one x left, and (u, x) not in L
			Word x = alphabet.getWordFromString(period.substring(startNr, lastNr + 1));
			if(lastNr == period.length() - 1) {
				suf = x;
				break;
			}
			// now it must be at least two period, x, y
			boolean r = membershipOracle.answerMembershipQuery(new QuerySimple<>(pre, x));
			// 1. if (u, x) not in L
			if(! r) { 
				suf = x;
				break;
			}
			// 2. Assume (u, x) in L
			// try (u, pyz..)
			Word yzetc = alphabet.getWordFromString(period.substring(lastNr + 1));
			Word pyzetc = p.concat(yzetc);
			r = membershipOracle.answerMembershipQuery(new QuerySimple<>(pre, pyzetc));
			
			// 2.1 (u, pyz...) is in L, then yz.. can distinguish p and x
			if(r) {
				suf = x.concat(yzetc); // still false
				break;
			}
			// 2.2 (u, pyz...) is not in L, we check (u, yz...)
			r = membershipOracle.answerMembershipQuery(new QuerySimple<>(pre, yzetc));
            
            // (u, yz...) is in L, then check the length of yz...
            if(r) {
            	int lastYNr = getLastIndexAtFinal(dkAutCE, lastNr + 1, period);
            	Word yz = alphabet.getWordFromString(period.substring(lastNr + 1));
            	if(lastYNr == period.length() - 1) {
            		result = true;
            		suf = yz.concat(yz); // y distinguish p and y since (u, py) is not in L
            	}else {
            		// there exists z... and yz... can not lead to that accepting state?
            		System.err.println( "Unfortunately, we can not find a valid counterexample");
            	    if(options.verbose) {
            	    	System.out.println("period left: " + yz.toStringExact());
            	    	System.out.println("progress automaton left: \n" + dkAutCE.toDot());
            	    }
            	    System.exit(-1);
            		suf = yz;
            	}
            	break;
            }
            // (u, yz...) is not in L
            startNr = lastNr + 1;
		}
		
		return suf;
	}



}
