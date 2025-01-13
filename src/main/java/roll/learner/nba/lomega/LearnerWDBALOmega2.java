package roll.learner.nba.lomega;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import roll.automata.DFA;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.TarjanSCCsNonrecursive;
import roll.jupyter.NativeTool;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.learner.fdfa.LearnerLeading;
import roll.learner.fdfa.table.LearnerLeadingTable;
import roll.learner.fdfa.tree.LearnerLeadingTree;
import roll.learner.nba.mp.LearnerWDBAMP;
import roll.learner.nba.mp.UtilPath;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWordPair;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Timer;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

public class LearnerWDBALOmega2 extends LearnerBase<NBA> implements LearnerWDBA {
	
	private int[] marks;
    // record the pairs of positive and negative samples
    // that visit the state infinitely often
    private ArrayList<ExprValue> positiveSamples;
    private ArrayList<ExprValue> negativeSamples;
    
    private ISet[] maxSCCs; 
    
    private boolean refined;
    protected LearnerLeading learner;
        
	public LearnerWDBALOmega2(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
		kb = new KnowledgeBase(options.kbSize);
		if (options.structure.isTable()) {
			learner = new LearnerLeadingTable(options, alphabet, membershipOracle);
		}else {
			learner = new LearnerLeadingTree(options, alphabet, membershipOracle);
		}
		positiveSamples = new ArrayList<>();
		negativeSamples = new ArrayList<>();
	}
	
	@Override
	public LearnerType getLearnerType() {
		return LearnerType.WDBA_DFA;
	}
	
    public int getSefLoopLetter(DFA dfa, int stateId) {
    	StateNFA state = dfa.getState(stateId);
    	for (int letter : state.getEnabledLetters()) {
    		if (state.getSuccessor(letter) == stateId) {
    			return letter;
    		}
    	}
    	return -1;
    }

	
	/** find in the progress DFA whether there are two states x and y
	 such that u = M(uxv1) /\ u(xv1) \in L
	           u = M(uyv2) /\ u(yv2) \notin L
    @Return true if the automaton has been changed
    */
	private boolean markAndDetect(DFA dfa) {

		// for tree-based algorithm, we do consistency check
		// make sure state representative word and its state match
		// that is, check whether u = M(u) holds
		if (UtilLOmega.makeTreeConsistency(options, learner)) {
			//tree has been changed
			return true;
		}
		
		// now we try to mark all states
		marks = new int[dfa.getStateSize()];
		maxSCCs = new ISet[dfa.getStateSize()];

		
		// we need to decide the marking of the SCCs
        ISet inits = UtilISet.newISet();
        inits.set(dfa.getInitialState());
        TarjanSCCsNonrecursive tarjan = new TarjanSCCsNonrecursive(dfa, inits);
        List<ISet> sccs = tarjan.getSCCs();
        
        //traverse all SCCs
        for (ISet mscc : sccs) {
        	int posState = -1;
        	int negState = -1;
        	// whether there are two states with different acceptance
        	Iterator<Integer> iter = mscc.iterator();
        	int reprState = iter.next();
        	int loopLetter = -1;
        	if (mscc.cardinality() == 1) {
        		maxSCCs[reprState] = mscc;
        		loopLetter = getSefLoopLetter(dfa, reprState);
        		if (loopLetter < 0) {
        			marks[reprState] = LearnerWDBAMP.REJ;
        			negState = reprState;
        			continue;
        		}
        	}
        
        	// now deal with nontrivial SCCs
        	for (int s : mscc) {
        		maxSCCs[s] = mscc;
        		
    			// we try to find conflict in progress DFAs
    			Word u = learner.getStateLabel(s);
    			
    			// Try to find two states x and y conflicting each other 
    			// such that u = M(uxv1) /\ u(xv1) \in L
    			//           u = M(uyv2) /\ u(yv2) \notin L
    			ExprValue posSample = positiveSamples.get(s);
    			ExprValue negSample = negativeSamples.get(s);
    			Word x = posSample != null ? posSample.getRight() : null;
    			Word y = negSample != null ? negSample.getRight() : null;
    			// we will check consistency when refining
    			if (x != null && y == null) {
    				// we found conflict
    				marks[reprState] = LearnerWDBAMP.ACC;
    				posState = s;
    			}else if (x == null && y != null) {
    				marks[reprState] = LearnerWDBAMP.REJ;
    				negState = s;
    			}else if (x == null && y == null){
    				// no samples yet
    				Word loop;
    				if (mscc.cardinality() == 1) {
    					loop = alphabet.getLetterWord(loopLetter);
    				}else {
    					// we get another state other than s
    					int p = -1;
    					if (reprState != s) {
    						p = reprState;
    					}else {
    						p = iter.next();
    					}
    					// now we get the word z
    					Word z = UtilPath.findPath(dfa, s, p);
    					Word w = UtilPath.findPath(dfa, p, s);
    					loop = z.concat(w);
    				}
    				HashableValue mq = membershipOracle.answerMembershipQuery(
    						new QuerySimple<>(u, loop));
    				if (mq.isAccepting()) {
    					marks[s] = LearnerWDBAMP.ACC;
    					positiveSamples.set(s, new ExprValueWordPair(u, loop));
    					posState = s;
    				}else {
    					marks[s] = LearnerWDBAMP.REJ;
    					negativeSamples.set(s, new ExprValueWordPair(u, loop));
    					negState = s;
    				}	
    			}
    			// we check again for conflict 
    			posSample = positiveSamples.get(s);
    			negSample = negativeSamples.get(s);
    			x = posSample != null ? posSample.getRight() : null;
    			y = negSample != null ? negSample.getRight() : null;
    			// after all these have done, we check again
    			if (x != null && y != null) {
    				resolveConflict(membershipOracle, options, dfa, u, x, y);
    				return true;
    			}
    			
    			// now we need to check 
    			if (posState >= 0 && negState >= 0) {
            		// now we check 
    				resolveConflict(membershipOracle, options, dfa, posState, negState);
    				return true;
            	}
    		}
        	// we do not see the usual cases but only 
        	// epsilon state in the progress state
        	// this only works for tree-based approach
        	int mark;
        	if (negState >= 0) {
        		mark = LearnerWDBAMP.REJ;
        	}else {
        		// by default, we set all states to accepting
        		mark = LearnerWDBAMP.ACC;
        	}
        	for (int s : mscc) {
        		marks[s] = mark;
        	}
        }
        return false;
	}
	
	private void checkSamples(DFA dfa, int s, Word loop, boolean pos) {
		int succ = dfa.getSuccessor(s, loop);
		if (succ != s && pos) {
			positiveSamples.set(s, null);
		}else if (succ != s && !pos) {
			negativeSamples.set(s, null);
		}
	}
	
	private void checkSamples(DFA dfa) {
		for (int s = 0; s < positiveSamples.size(); s ++) {
			ExprValue x = positiveSamples.get(s);
			ExprValue y = negativeSamples.get(s);
			if (x != null) {
				checkSamples(dfa, s, x.getRight(), true);
			}
			
			if (y != null) {
				checkSamples(dfa, s, y.getRight(), false);
			}
		}
		// make sure we have sufficient size
		while (positiveSamples.size() < dfa.getStateSize()) {
			positiveSamples.add(null);
			negativeSamples.add(null);
		}
	}

	@Override
	protected void constructHypothesis() {
		DFA dfa;
		while (true) {
			// construct BA from FDFA
			dfa = learner.getHypothesis();
			//1. make sure that the current samples are normalised
			checkSamples(dfa);
			//2. mark all SCC states
			if (! markAndDetect(dfa)) {
				// we need to refine
				break;
			}
			// if modified, build a new WDBA
			constructHypothesis();
		}
		// now we mark states
		NBA nba = new NBA(dfa.getAlphabet());
    	for (int s = 0; s < dfa.getStateSize(); s ++) {
    		nba.createState();
    	}
    	nba.setInitial(dfa.getInitialState());
    	for (int s = 0; s < nba.getStateSize(); s ++) {
    		for (int c = 0; c < nba.getAlphabetSize(); c ++) {
    			int t = dfa.getSuccessor(s, c);
    			nba.getState(s).addTransition(c, t);
    		}
    		if (marks[s] == LearnerWDBAMP.ACC) nba.setFinal(s);
    	}
        hypothesis = nba;
	}
	
	protected void refineHypothesisOnce(Query<HashableValue> query, HashableValue mqResult) {
		// lazy equivalence check is implemented here
		query.answerQuery(mqResult);

		DFA leadingDFA = this.learner.getHypothesis();
		ISet infSet = this.getInfSetAndRecordWords(leadingDFA, query.getPrefix(), query.getSuffix(),
				mqResult.isAccepting());

		if (refined) {
			// we already updated the leading DFA
			// means that u' = u for some u with different samples
			constructHypothesis();
			return;
		}
		//TODO the following code may not be reached
		// now we know that all the states in the loop does not
		// have a conflict with itself, so there must be two states in the SCC
		// infSet that have conflicts
		assert (!infSet.isEmpty());
		// pick one state and traverse others
		int uprime = infSet.iterator().next();
		ISet scc = maxSCCs[uprime];
		int u = -1;
		for (int s : scc) {
			// we have within the same SCC, two states correspond to words of different
			// memberships
			if (mqResult.isAccepting() && negativeSamples.get(s) != null
					|| mqResult.isRejecting() && positiveSamples.get(s) != null) {
				u = s;
				break;
			}
		}
		assert (u != -1): "The state within the same SCC as CEX not found";
		if (mqResult.isAccepting()) {
			// this is a positive counterexample trapped in rejecting SCC
			// there must be a u such that u = M(u) and u(v) notin L
			resolveConflict(membershipOracle, options, learner.getHypothesis(), uprime, u);
		} else {
			// there is a u such that u = M(u) and u(v) in L
			// in contrast to u'(y') in L and u' = M(u')
			resolveConflict(membershipOracle, options, learner.getHypothesis(), u, uprime);
		}

		constructHypothesis();

	}
	
	KnowledgeBase kb;

	@Override
	public void refineHypothesis(Query<HashableValue> query) {

		options.log.verbose("Current WDBA-DFA:\n" + learner.getHypothesis().toString());
		options.log.println("Analyzing counterexample for WDBA learner...");
		Timer timer = new Timer();
		timer.start();
		HashableValue mqResult = new HashableValueBoolean(
				!hypothesis.getAcc().accept(query.getPrefix(), query.getSuffix()));
		kb.addQuery(query, mqResult.isAccepting());
		refineHypothesisOnce(query, mqResult);
		while (options.kbSize > 0) {
			Query<HashableValue> ceQuery = kb.testCorrectness(hypothesis);
			if (ceQuery == null) break;
			refineHypothesisOnce(ceQuery, ceQuery.getQueryAnswer());
		}
		timer.stop();
	}
	
	@Override
	public Word getStateLabel(int state) {
		return learner.getStateLabel(state);
	}

	@Override
	public ExprValue getPositiveSample(int state) {
		return positiveSamples.get(state);
	}

	@Override
	public ExprValue getNegativeSample(int state) {
		return negativeSamples.get(state);
	}

	@Override
	public void refineWithCounterexample(Word prefix, Word loop, boolean acc) {
		Query<HashableValue> ceQuery = new QuerySimple<>(prefix, loop);
		// we will construct new hypothesis FDFA afterwards	
		ceQuery.answerQuery(new HashableValueBoolean(acc));
		kb.addQuery(ceQuery, acc);
		options.stats.numOfLeadingCex ++;
		learner.refineHypothesis(ceQuery);
	}
	
	/**
	 * Try to add a sample (x', y') for state, where acc indicates the membership
	 * of (x', y')
	 * 1. first test whether u'(y') in L where u' = M(x')
	 * 2. whether this example has conflict to existing samples of state
	 * 3. add (x', y') to the samples
	 * @Return true if wdba has been refined
	 * */
	@Override
	public boolean addSamples(int state, boolean acc, Word xprime, Word yprime) {
		// do we allow to override the samples
		// we can check whether there is a conflict here
		refined = false;
		// first test u'(y') \in L? where u' = M(x')
		Word uprime = getStateLabel(state);
		HashableValue mq = membershipOracle.answerMembershipQuery(new QuerySimple<>(uprime, yprime));
		if (mq.isAccepting() && !acc || mq.isRejecting() && acc) {
			// this means that prefix x' and u' can be distinguished by (y')
			refineWithCounterexample(xprime, yprime, acc);
			refined = true;
			return true;
		}
		
 		// otherwise, x' and u' have same membership with respect to y'
		if (acc && negativeSamples.get(state) != null || !acc && positiveSamples.get(state) != null) {
			// this means that 
			// u' (v) has different membership than x'(y'), i.e., u' (y')
			if (negativeSamples.get(state) != null) {
				resolveConflict(membershipOracle
						, options, learner.getHypothesis(), uprime, yprime, negativeSamples.get(state).getRight());
			}else if (positiveSamples.get(state) != null) {
				resolveConflict(membershipOracle
						, options, learner.getHypothesis(), uprime, positiveSamples.get(state).getRight(), yprime);
			}else
				throw new RuntimeException("Impossible path in addSamples in LOmega");
			refined = true;
			return true;
		}
		// ignore if there is already words in there
		if (acc && positiveSamples.get(state) == null) {
    		positiveSamples.set(state, new ExprValueWordPair(xprime, yprime));
    	}else if (!acc && negativeSamples.get(state) == null){
    		negativeSamples.set(state, new ExprValueWordPair(xprime, yprime));
    	}
		return false;
	}

	@Override
	public String toHTML() {
		if (options.structure == Options.Structure.TREE) {
            StringBuilder builder = new StringBuilder();
            builder.append("<p> WDBA Learner :  </p> <br> "    
                         + NativeTool.dot2SVG(learner.toString()));
            return builder.toString();
     }else {
         return "<pre> " + toString() + "</pre>";
     }
	}
	
	@Override
	public String toString() {
		return learner.toString();
	}

	@Override
	protected void initialize() {
		learner.startLearning();
        constructHypothesis();
	}

}
