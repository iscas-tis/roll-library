package roll.learner.nba.lomega;

import java.util.List;

import roll.automata.DFA;
import roll.automata.FDFA;
import roll.automata.NBA;
import roll.automata.operations.TarjanSCCsNonrecursive;
import roll.learner.LearnerType;
import roll.learner.nba.mp.LearnerWDBAMP;
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

public class LearnerWDBALOmega extends LearnerNBALOmega implements LearnerWDBA {
	
	private int[] marks;
    // record the pairs of positive and negative samples
    // that visit the state infinitely often
    private ExprValue[] positiveSamples;
    private ExprValue[] negativeSamples;
    
    private ISet[] maxSCCs; 
    
    private boolean refined;
        
	public LearnerWDBALOmega(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
//		if (options.algorithm != Options.Algorithm.LIMIT) {
//			throw new UnsupportedOperationException("Only support limit FDFA for TDBA learner");
//		}
	}
	
	@Override
	public LearnerType getLearnerType() {
		return LearnerType.WDBA_FDFA;
	}

	
	// find in the progress DFA whether there are two states x and y
	// such that u = M(uxv1) /\ u(xv1) \in L
	//           u = M(uyv2) /\ u(yv2) \notin L
	private boolean markAndDetect(DFA dfa) {

		// for tree-based algorithm, we do consistency check
		// make sure state representative word and its state match
		// that is, check whether u = M(u) holds
		if (fdfaLearner.checkLeadingConsistency()) {
			return true;
		}
		marks = new int[dfa.getStateSize()];
		positiveSamples = new ExprValue[dfa.getStateSize()];
		negativeSamples = new ExprValue[dfa.getStateSize()];
		maxSCCs = new ISet[dfa.getStateSize()];
		
		// we need to decide the marking of the SCCs
        ISet inits = UtilISet.newISet();
        inits.set(dfa.getInitialState());
        TarjanSCCsNonrecursive tarjan = new TarjanSCCsNonrecursive(dfa, inits);
        List<ISet> sccs = tarjan.getSCCs();

        for (ISet mscc : sccs) {
        	// whether there are two states with different acceptance
        	int posState = -1;
        	int negState = -1;
        	boolean treeCornerCase = false;
        	// we now check the progress DFAs
        	for (int s : mscc) {
        		maxSCCs[s] = mscc;
        		DFA proDFA = fdfaLearner.getHypothesis().getProgressFA(s);
        		
        		// tree-based has only the leaf node ϵ
        		if (options.structure == Options.Structure.TREE 
        				&& proDFA.getStateSize() <= 1) {
        			treeCornerCase = true;
        			continue;
        		}
    			// we try to find conflict in progress DFAs
    			Word u = fdfaLearner.getLeadingStateLabel(s);
    			
    			// For table-based or tree-based with more than one state
    			// we check whether there exists a state u that transitions to 
    			// the initial state over a letter c
				boolean foundLetter = false;
				int initProState = proDFA.getInitialState();
				for (int c = 0; c < proDFA.getAlphabetSize() && !foundLetter; c++) {
					Word letter = proDFA.getAlphabet().getLetterWord(c);
					for (int t = 0; t < proDFA.getStateSize(); t++) {
						if (initProState == proDFA.getSuccessor(t, c)) {
							// s.c is equivalent to ϵ with respect to ϵ column
							// we have (+, -) for s.c.ϵ then
							// both table-based and tree-based have ϵ as experiment word
							Word x = fdfaLearner.getProgressStateLabel(s, t);
							negativeSamples[s] = new ExprValueWordPair(u, x.concat(letter));
							negState = s;
							foundLetter = true;
							break;
						}
					}
				}
        		
    			// Try to find two states x and y conflicting each other 
    			// such that u = M(uxv1) /\ u(xv1) \in L
    			//           u = M(uyv2) /\ u(yv2) \notin L
    			List<Query<HashableValue>> wordMarks = fdfaLearner.getProgressMark(s);
    			if (wordMarks.size() > 1) {
    				// we found conflict
    				Query<HashableValue> fst = wordMarks.get(0);
    				Query<HashableValue> snd = wordMarks.get(1);
    				Word x, y;
    				if (fst.getQueryAnswer().isAccepting()) {
    					x = fst.getPrefix().concat(fst.getSuffix());
    					y = snd.getPrefix().concat(snd.getSuffix());
    				}else {
    					x = snd.getPrefix().concat(snd.getSuffix());
    					y = fst.getPrefix().concat(fst.getSuffix());
    				}
    				
    				resolveConflict(membershipOracle, options, dfa, u, x, y);
    				return true;
    			}else if(wordMarks.size() > 0) {
    				// record the marking
    				Query<HashableValue> fst = wordMarks.get(0);

					Word x = fst.getPrefix().concat(fst.getSuffix());
					ExprValue sample = new ExprValueWordPair(u, x);
    				if (fst.getQueryAnswer().isAccepting()) {
    					// we get the word now
    					positiveSamples[s] = sample;
    					posState = s;
    				}else {
    					negativeSamples[s] = sample;
    					negState = s;
    				}
    			}
    			
    			if (posState >= 0 && negState >= 0) {
            		// now we check 
    				resolveConflict(membershipOracle, options, dfa, posState, negState);
    				return true;
            	}
    		}
        	// we do not see the usual cases but only 
        	// epsilon state in the progress state
        	// this only works for tree-based approach
        	if (negState < 0 && posState < 0
        		&& treeCornerCase) {
        		negState = 1;
        	}
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

	@Override
	protected void constructHypothesis() {
		DFA leadingDFA;
		while (true) {
			// construct BA from FDFA
			FDFA fdfa = fdfaLearner.getHypothesis();
			// we need to mark all the states in the leading DFA according to
			// status of progress DFAs
			leadingDFA = fdfa.getLeadingFA();
			if (! markAndDetect(leadingDFA)) {
				// we need to refine
				break;
			}
			// if modified, build a new WDBA
			constructHypothesis();
		}
		// now we mark states
		NBA nba = new NBA(leadingDFA.getAlphabet());
    	for (int s = 0; s < leadingDFA.getStateSize(); s ++) {
    		nba.createState();
    	}
    	nba.setInitial(leadingDFA.getInitialState());
    	for (int s = 0; s < nba.getStateSize(); s ++) {
    		for (int c = 0; c < nba.getAlphabetSize(); c ++) {
    			int t = leadingDFA.getSuccessor(s, c);
    			nba.getState(s).addTransition(c, t);
    		}
    		if (marks[s] == LearnerWDBAMP.ACC) nba.setFinal(s);
    	}
        hypothesis = nba;
	}

	@Override
	public void refineHypothesis(Query<HashableValue> query) {

		options.log.verbose("Current WDBA-FDFA:\n" + fdfaLearner.getHypothesis().toString());
		options.log.println("Analyzing counterexample for WDBA learner...");

		Timer timer = new Timer();
		timer.start();
		// lazy equivalence check is implemented here
		HashableValue mqResult = new HashableValueBoolean(!hypothesis.getAcc().accept(query.getPrefix(), query.getSuffix()));
		query.answerQuery(mqResult);
		
		DFA leadingDFA = this.fdfaLearner.getHypothesis().getLeadingFA();
		ISet infSet = this.getInfSetAndRecordWords(leadingDFA, query.getPrefix()
				, query.getSuffix(), mqResult.isAccepting());
		
		if (refined) {
			// we already updated the leading DFA
			// means that u' = u for some u with different samples
			constructHypothesis();
			return ;
		}
		// now we know that all the states in the loop does not
		// have a conflict, so there must one in the SCC
		// infSet must not be empty
		assert (!infSet.isEmpty());
		int uprime = infSet.iterator().next();
		ISet scc = maxSCCs[uprime];
		int u = -1;
		for (int s : scc) {
			// we have within the same SCC, two states correspond to words of different memberships 
			if (mqResult.isAccepting() && negativeSamples[s] != null
			|| mqResult.isRejecting() && positiveSamples[s] != null) {
				u = s;
				break;
			}
		}
		if (mqResult.isAccepting()) {
			// this is a positive counterexample trapped in rejecting SCC
			// there must be a u such that u = M(u) and u(v) notin L
			if (u != -1)
				resolveConflict(membershipOracle
					, options, fdfaLearner.getHypothesis().getLeadingFA(), uprime, u);
			else {
				// this applies to case whether there is one epsilon state,
				// with empty word as experiment or no experiments (tree-structure)
				refineProgressDFA(positiveSamples, uprime, mqResult);
			}
		}else {
			if (u != -1) {
				// there is a u such that u = M(u) and u(v) in L
				// in contrast to u'(y') in L and u' = M(u')
				resolveConflict(membershipOracle
						, options, fdfaLearner.getHypothesis().getLeadingFA(), u, uprime);
			}else {
				// in the worst case, we just refine the progress DFA of uprime
				// right now, we have u' = M(x') and y', we input (x', y')
				refineProgressDFA(negativeSamples, uprime, mqResult);
			}
		}

		constructHypothesis();
		timer.stop();
	}
	
	protected void refineProgressDFA(ExprValue[] samples, int uprime, HashableValue mqResult) {
		Query<HashableValue> queryProgress = new QuerySimple<>(samples[uprime].getLeft(),
				samples[uprime].getRight());
			queryProgress.answerQuery(mqResult);
			fdfaLearner.refineProgressDFA(uprime, queryProgress);
	}

	@Override
	public Word getStateLabel(int state) {
		return fdfaLearner.getLeadingStateLabel(state);
	}

	@Override
	public ExprValue getPositiveSample(int state) {
		return positiveSamples[state];
	}

	@Override
	public ExprValue getNegativeSample(int state) {
		return negativeSamples[state];
	}

	@Override
	public void refineWithCounterexample(Word prefix, Word loop) {
		Query<HashableValue> ceQuery = new QuerySimple<>(prefix, loop);
		// we will construct new hypothesis FDFA afterwards		
		fdfaLearner.refineLeadingDFA(ceQuery);
	}
	
	// TODO should be removed later
	private void ignoreEmptyLoop(ExprValue[] samples, int state) {
		// usually, for a single epsilon state, loop can be empty
		if (samples[state] != null) {
			Word loop = samples[state].getRight();
			if (loop.isEmpty()) samples[state] = null;
		}
	}

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
			refineWithCounterexample(xprime, yprime);
			refined = true;
			return true;
		}
		
		ignoreEmptyLoop(negativeSamples, state);
		ignoreEmptyLoop(positiveSamples, state);
 		// otherwise, x' and u' have same membership with respect to y'
		if (acc && negativeSamples[state] != null || !acc && positiveSamples[state] != null) {
			// this means that 
			// u' (v) has different membership than x'(y'), i.e., u' (y')
			if (negativeSamples[state] != null) {
				resolveConflict(membershipOracle
						, options, fdfaLearner.getHypothesis().getLeadingFA(), uprime, yprime, negativeSamples[state].getRight());
			}else if (positiveSamples[state] != null) {
				resolveConflict(membershipOracle
						, options, fdfaLearner.getHypothesis().getLeadingFA(), uprime, positiveSamples[state].getRight(), yprime);
			}else
				throw new RuntimeException("Impossible path in addSamples in LOmega");
			refined = true;
			return true;
		}
		// ignore if there is already words in there
		if (acc && positiveSamples[state] == null) {
    		positiveSamples[state] = new ExprValueWordPair(xprime, yprime);
    	}else if (!acc && negativeSamples[state] == null){
    		negativeSamples[state] = new ExprValueWordPair(xprime, yprime); 
    	}
		return false;
	}

}
