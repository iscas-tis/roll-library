package roll.learner.nba.lomega;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.DFA;
import roll.automata.FDFA;
import roll.automata.TDBA;
import roll.automata.operations.DFAOperations;
import roll.automata.operations.FDFAOperations;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.learner.fdfa.LearnerFDFA;
import roll.learner.fdfa.LearnerProgress;
import roll.learner.nba.lomega.translator.TranslatorFDFA;
import roll.learner.nba.lomega.translator.TranslatorFDFATDBA;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.table.HashableValue;
import roll.util.Timer;
import roll.words.Alphabet;

public class LearnerTDBALOmega extends LearnerBase<TDBA> {
	
	private final LearnerFDFA fdfaLearner;
	
	public LearnerTDBALOmega(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
		// TODO Auto-generated constructor stub
		if (options.algorithm != Options.Algorithm.LIMIT) {
			throw new UnsupportedOperationException("Only support limit FDFA for TDBA learner");
		}
		// do not allow binary search
		options.binarySearch = false;
        fdfaLearner = UtilLOmega.getLearnerFDFA(options, alphabet, membershipOracle);
	}
	
	@Override
    public LearnerType getLearnerType() {
        return LearnerType.TDBA_FDFA;
    }
    
    protected void initialize() {
        fdfaLearner.startLearning();
        constructHypothesis();
    }

    @Override
    protected void constructHypothesis() {
        // construct BA from FDFA
        FDFA fdfa = fdfaLearner.getHypothesis();
        hypothesis = FDFAOperations.buildTDBA(fdfa);
    }
    
    @Override
    public void refineHypothesis(Query<HashableValue> query) {
        
        options.log.verbose("Current FDFA:\n" + fdfaLearner.getHypothesis().toString());
        options.log.println("Analyzing counterexample for FDFA learner...");
        Timer timer = new Timer();
        timer.start();
        TranslatorFDFA translator = new TranslatorFDFATDBA(fdfaLearner, hypothesis, membershipOracle);
        // lazy equivalence check is implemented here
        HashableValue mqResult = membershipOracle.answerMembershipQuery(query);
//        System.out.println("Membership for CE: " + mqResult);
        query.answerQuery(mqResult);
        translator.setQuery(query);
        timer.stop();
        options.stats.timeOfTranslator += timer.getTimeElapsed();
        Query<HashableValue> ceQuery = null;
        timer.start();
        
        ceQuery = translator.translate();
        options.log.verbose("Counterexample for FDFA: " + ceQuery);

        // now we anaylse the counterexample to see which DFA to be refined
        timer.stop();
        options.stats.timeOfTranslator += timer.getTimeElapsed();
        if (ceQuery.getQueryAnswer().isAccepting()) {
        	// this means the counterexample should be fed to leading DFA
            ceQuery.answerQuery(null);
            fdfaLearner.refineLeadingDFA(ceQuery);
        }else {
        	// else we can let fdfa to decide which DFA to be refined
        	ceQuery.answerQuery(null);
        	fdfaLearner.refineHypothesis(ceQuery);
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
