package roll.main.ltl2dpa;

import java.util.List;

import automata.FiniteAutomaton;
import dk.brics.automaton.Automaton;
import oracle.IntersectionCheck;
import roll.automata.FDFA;
import roll.automata.NBA;
import roll.automata.operations.FDFAOperations;
import roll.automata.operations.NBAOperations;
import roll.main.Options;

import roll.main.complement.UtilComplement;
import roll.main.inclusion.UtilInclusion;
import roll.oracle.Teacher;

import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.table.HashableValueBooleanExactPair;
import roll.util.Pair;
import roll.util.Timer;
import roll.words.Alphabet;
import roll.words.Word;


/**
 * make sure you have SPOT installed 
 * 
 * A learning framework for translating LTL formulae to Limit Deterministic Buchi Automata
 * 
 * */

public class TeacherLTL2LDBA implements Teacher<FDFA, Query<HashableValue>, HashableValue>  {
	
	private final Options options;
    private final Alphabet alphabet;
    private final NBA posA;
    private final NBA negB;
    
    public TeacherLTL2LDBA(Options options, String ltl) {
        assert options != null ;
        this.options = options;
        // now we convert NBA
        Pair<NBA, NBA> pair = UtilLTL2LDBA.translateLtl2BA(options, ltl);
        this.posA = pair.getLeft();
        this.negB = pair.getRight(); 
        this.alphabet = this.posA.getAlphabet();
    }
    
    public Alphabet getAlphabet() {
    	return this.alphabet;
    }

	@Override
	public HashableValue answerMembershipQuery(Query<HashableValue> query) {
        Timer timer = new Timer();
        timer.start();
        boolean result = UtilComplement.answerMembershipQuery(posA, query);
        timer.stop();
        options.stats.timeOfMembershipQuery += timer.getTimeElapsed();
        ++ options.stats.numOfMembershipQuery; 
        return new HashableValueBoolean(result); 
	}
	
    public int numInterBandBF;
    public long timeInterBandBF;
    
    public int numInterBFCandA;
    public long timeInterBFCandA;
	
    
    private Pair<Word, Word> getCounterexample(List<String> prefix, List<String> suffix) {
        return UtilComplement.getCounterexample(alphabet, prefix, suffix);
    }
    
	/**
	 * learning the complement of negB:
	 * 
	 * 	equivalence query:
	 *    L(BF) /\ L(B) is empty
	 *    L(BFC) /\ L(A) is empty => L(A) is equal to L(BF) since L(B) is the complement of L(A)
	 * 
	 * */
	@Override
	public Query<HashableValue> answerEquivalenceQuery(FDFA hypothesis) {
		Timer timer = new Timer();
        timer.start();
        options.log.println("Translating FDFA to under Buchi automaton ...");
        Automaton dkBF = FDFAOperations.buildUnderNBA(hypothesis);
        NBA BF = NBAOperations.fromDkNBA(dkBF, alphabet);
        
        // record the constructed Buchi automaton
        options.stats.hypothesis = BF;
        ++ this.numInterBandBF;
        options.log.println("Checking the intersection of BF (" + BF.getStateSize() + ") and B ("+ negB.getStateSize() + ")...");
        long t = timer.getCurrentTime();
        FiniteAutomaton rBF = UtilInclusion.toRABITNBA(BF);
        FiniteAutomaton rB = UtilInclusion.toRABITNBA(negB);
        /**
         * now check whether the complement B(F) intersects with the input automaton B
         * */
        IntersectionCheck checker = new IntersectionCheck(rBF, rB);
        boolean isEmpty = checker.checkEmptiness();
        t = timer.getCurrentTime() - t;
        this.timeInterBandBF += t;
        if(options.verbose()) {
            options.log.println("Hypothesis for complementation B");
            options.log.println(BF.toString());
        }
        Word prefix = null;
        Word suffix = null;
        boolean isEq = false, isInTarget = false;
        if(! isEmpty) {
            // we have omega word in FDFA which should not be there
            checker.computePath();
            Pair<Word, Word> pair = getCounterexample(checker.getPrefix(), checker.getSuffix());
            prefix = pair.getLeft();
            suffix = pair.getRight();
            isEq = false;
            isInTarget = true;
        } else {
            Automaton dkBFC = FDFAOperations.buildNegNBA(hypothesis);
            NBA BFC = NBAOperations.fromDkNBA(dkBFC, alphabet);
            options.log.println("Checking the intersection for A (" + posA.getStateSize() + ") and B(F^c) ("
                    + BFC.getStateSize() + ")...");
            ++this.numInterBFCandA;
            FiniteAutomaton rBFC = UtilInclusion.toRABITNBA(BFC);
            FiniteAutomaton rA = UtilInclusion.toRABITNBA(posA);
            t = timer.getCurrentTime();
            checker = new IntersectionCheck(rA, rBFC);
            isEmpty = checker.checkEmptiness();               
            t = timer.getCurrentTime() - t;
            this.timeInterBFCandA += t;
            if (isEmpty) {
//            	UtilComplement.print(BFC, "A.ba");
//            	UtilComplement.print(B, "B.ba");
                isEq = true;
            }else {
            	checker.computePath();
                Pair<Word, Word> pair = getCounterexample(checker.getPrefix(), checker.getSuffix());
                prefix = pair.getLeft();
                suffix = pair.getRight();
                isEq = false;
                isInTarget = NBAOperations.accepts(negB, prefix, suffix);
            }
        }
        
        options.log.println("Done for checking equivalence...");
        Query<HashableValue> query = null;
        isInTarget = ! isInTarget;
        
        if(isEq) {
            query = new QuerySimple<>(alphabet.getEmptyWord(), alphabet.getEmptyWord());
            query.answerQuery(new HashableValueBooleanExactPair(true, true));
        }else {
            query = new QuerySimple<>(prefix, suffix);
            query.answerQuery(new HashableValueBooleanExactPair(false, isInTarget));
        }
        
        timer.stop();
        options.stats.timeOfEquivalenceQuery += timer.getTimeElapsed();
        ++ options.stats.numOfEquivalenceQuery;
        options.stats.timeOfLastEquivalenceQuery = timer.getTimeElapsed();
        
        if(options.verbose()) options.log.println("counter example = " + query);
        return query;
	}
	
    public void print() {
        final int indent = 30;
        options.log.println("#B(F)&B = " + numInterBandBF, indent, "    // #number of B(F) intersection with B", true);
        options.log.println("#B(F^c)&BF = " + numInterBFCandA, indent, "    // #number of B(F^c) intersection with B(F)", true);
        options.log.println("#TB(F^c)&BF = " + timeInterBFCandA, indent, "    // time for B(F^c) intersection with B(F)", true);
    }

}
