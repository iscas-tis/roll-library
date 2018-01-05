package roll.learner.nba.lomega.translator;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.DFA;
import roll.automata.operations.DFAOperations;
import roll.learner.fdfa.LearnerFDFA;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Timer;
import roll.util.sets.ISet;
import roll.words.Alphabet;
import roll.words.Word;

public class TranslatorFDFAOver extends TranslatorFDFA {

	private MembershipOracle<HashableValue> membershipOracle;
	private boolean result ;
	
	public TranslatorFDFAOver(LearnerFDFA learner
			, MembershipOracle<HashableValue> membershipOracle) {
		super(learner);
		assert membershipOracle != null;
 		this.membershipOracle = membershipOracle;
	}
	

	@Override
	public Query<HashableValue> translate() {
	    fdfa = fdfaLearner.getHypothesis();
		Timer timer = new Timer();
		timer.start();
		String counterexample = translateUpper();
		timer.stop();
		options.stats.timeOfTranslator += timer.getTimeElapsed();
		return getQuery(counterexample, new HashableValueBoolean(result));
	}
	
	private String translateUpper() {
		// for general case 
//		if(Options.verbose) AutomatonPrinter.print(autUVOmega, System.out);
//		if(Options.verbose) learnerFDFA.getHypothesis();
	    boolean isCeInTarget = ceQuery.getQueryAnswer().get();
		if(isCeInTarget) {
			// (u, v) is in L, but it is not in FDFA
			result = true;
			return getPositiveCounterExample(autUVOmega);
		}else {
			// (u, v) is not in L, but it is in constructed Buechi
			// possibly it is not in FDFA, already normalized.
			result = false;
			return getCorrectNormalizedCounterExample(fdfaLearner, membershipOracle);
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
			, MembershipOracle<HashableValue> membershipOracle) {
		
		Word pre = null, suf = null;
		// 1. first build dollar deterministic automaton for (u, v) 
		if(options.verbose) options.log.println(autUVOmega.toDot());
		
		// 2. for every final state, we get normalized counterexample
		DFA autL = fdfa.getLeadingDFA();
		TIntObjectMap<State> map = new TIntObjectHashMap<>(); 
		Automaton dkAutL = DFAOperations.toDkDFA(map, autL);
		
		for(int stateNr = 0; stateNr < autL.getStateSize(); stateNr ++) {
		    DFA autP = fdfa.getProgressDFA(stateNr);
		    ISet finalStates = autP.getFinalStates();
		    int stateInitP = autP.getInitialState();
		    boolean found = false;
		    // for every final state we get the language intersection
		    for(final int accNr : finalStates) {
		    	// language for A^u_f
		    	Automaton dkAutP = DFAOperations.toDkDFA(autP, stateInitP, accNr);
		    	dkAutP.minimize();
		    	// language for M^u_u
		    	Automaton dkAutLOther = DFAOperations.toDkDFA(autL, stateNr, stateNr);
		    	dkAutLOther.minimize();
		    	// build product for A^u_f and M^u_u
		    	Automaton product = dkAutP.intersection(dkAutLOther);
		    	product.minimize();
		    	
		    	if(! product.getAcceptStates().isEmpty()) {
		    		assert product.getAcceptStates().size() == 1;
		    		// add epsilon transition to allow x, concatenate y
		    		product = DFAOperations.addEpsilon(product);
//					if(Options.verbose) System.out.println(
//							"\n product: \n " + product.toDot()
//							+ "\nrun a$cbcbcbab, " + BasicOperations.run(product, "cccccbcccbccbcaaaccc"));
		    		// get u state in leading automaton
		    		State u = map.get(stateNr); 
		    		// add Dollar transition
		    		Transition transDollar = new Transition(Alphabet.DOLLAR, product.getInitialState());
		    		u.addTransition(transDollar);
//					if(Options.verbose) System.out.println(
//							"\n dkAutL: \n " + dkAutL.toDot()
//							+ "\nrun a$cbcbcbab, " + BasicOperations.run(dkAutL, "ccaccc$cccccbcccbccbcaaaccc"));
		            // find u $ v string
		    		Automaton dkNFA = autUVOmega.intersection(dkAutL);
//		    		dkNFA.minimize();
//		    		if(Options.verbose) System.out.println(
//							"\n dkNFA " + dkNFA.toDot()
//							+ "\nrun a$cbcbcbab, " + BasicOperations.run(dkNFA, "a$cbcbcbab"));
					
		    		String counterexample = dkNFA.getShortestExample(true);
					if(options.verbose && counterexample != null) System.out.println(" found counterexample " + counterexample);
		    		// remove duplicate dollar transition
		    		if(counterexample == null) {
		    			u.getTransitions().remove(transDollar);
		    			continue;
		    		}
		    		
		    		found = true;
		    		// get decomposition (u, xyz...) which is not in L
					if(options.verbose) System.out.println("normalized counterexample " + counterexample 
							+ ", " + BasicOperations.run(dkNFA, counterexample));
					
					int dollarNr = counterexample.indexOf(Alphabet.DOLLAR); //
					pre = alphabet.getWordFromString(counterexample.substring(0, dollarNr));
					// must be some x,y,z concatenation
					String period = counterexample.substring(dollarNr + 1);
					if(options.verbose) System.out.println("dkAutLPOther\n " + dkAutLOther.toDot());
					if(options.verbose) System.out.println("dkAutP\n " + dkAutP.toDot());
					if(options.verbose) System.out.println("dkNFA\n " + dkAutP.toDot());
					// build deterministic automaton 
					Automaton dkAutCE = dkAutP.intersection(dkAutLOther);  
//					Word p = learnerFDFA.getProgressStateLabel(stateNr, accNr);
					suf = findCorrectPeriod(dkAutCE, pre, period, membershipOracle);
		    	}
		    	if(found) break;
		    }
		    if(found) break;
		}
		
		assert pre != null && suf != null;
		String ce = pre.toStringExact() + Alphabet.DOLLAR + suf.toStringExact();
		return ce;
	}
	
	// complex analysis for counter example
	private Word findCorrectPeriod(Automaton dkAutCE
			, Word pre, String period, MembershipOracle<HashableValue> membershipOracle) {
		
		int startNr = 0;
		Word suf = null;
		//INVARIANT:  Assume that (u, xyz...) is not in L
		while(startNr < period.length()) {
			
			int lastNr = getLastIndexAtFinal(dkAutCE, startNr, period);
			// only one x left, and (u, x) not in L
			Word x = alphabet.getWordFromString(period.substring(startNr, lastNr + 1));
			if(lastNr == period.length() - 1) {
				HashableValue r = membershipOracle.answerMembershipQuery(new QuerySimple<>(pre, x));
				if(! r.isAccepting()) {
					suf = x;
				}
				
				break;
			}
			// now it must be at least two period, x, y
			HashableValue r = membershipOracle.answerMembershipQuery(new QuerySimple<>(pre, x));
			// 1. if (u, x) not in L
			if(! r.isAccepting()) { 
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
	private Word findCorrectPeriod2(Automaton dkAutCE
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
