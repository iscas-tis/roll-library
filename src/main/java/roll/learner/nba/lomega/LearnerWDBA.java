package roll.learner.nba.lomega;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.DFA;
import roll.learner.nba.mp.UtilPath;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

public interface LearnerWDBA {
	
	Word getStateLabel(int state);
	
	ExprValue getPositiveSample(int state);
	ExprValue getNegativeSample(int state);
	
	default void resolveConflict(MembershipOracle<HashableValue> membershipOracle,
			Options options, DFA dfa, int posState, int negState) {
		
		// so, s has a loop over zw and t has a loop over wz 
		ExprValue positiveSample = getPositiveSample(posState);
		ExprValue negativeSample = getNegativeSample(negState);
		
		Word s = getStateLabel(posState);
		Word t = getStateLabel(negState);
		
		if (posState == negState) {
			// this can happen 
			resolveConflict(membershipOracle, options, dfa
					, s, positiveSample.getRight(), negativeSample.getRight());
			return ;
		}
				
		// we need to find conflict here
		Word z = UtilPath.findPath(dfa, posState, negState);
		Word w = UtilPath.findPath(dfa, negState, posState);
		
		// word leads from s to t
		Word zw = z.concat(w);
		// word leads from t to s
		Word wz = w.concat(z);
		
		// s.(zw) and t.(wz)
		HashableValue mq;
		mq = membershipOracle.answerMembershipQuery(new QuerySimple<>(s, zw));
		if (mq.isRejecting()) {
			resolveConflict(membershipOracle, options, dfa, s, positiveSample.getRight(), zw);
			return ;
		}
		
		// now check whether sz is equivalent to t
		mq = membershipOracle.answerMembershipQuery(new QuerySimple<>(t, wz));
		if (mq.isAccepting()) {
			resolveConflict(membershipOracle, options, dfa, t, wz, negativeSample.getRight());
			return ;
		}
		// if both of them not feasible
		// now we have a problem
		// this means sz /= t, as wz can distinguish them
		// sz.(wz) is accepting, while t.(wz) is rejecting
		refineWithCounterexample(s.concat(z), wz);
	}
	
	void refineWithCounterexample(Word prefix, Word loop);

	default void resolveConflict(MembershipOracle<HashableValue> membershipOracle
			, Options options, DFA dfa, Word u, Word x, Word y) {

		Alphabet alphabet = dfa.getAlphabet();
		int k = 1;
		Word xpower = alphabet.getEmptyWord().concat(x);
		Word ypower = alphabet.getEmptyWord().concat(y);
		while (true) {
			int h = 1;
			// this is y^k.x^k
			// initialise the power (y^k.x^k)^{h-1}
			Word m = alphabet.getEmptyWord().concat(u);
//			Word yxpower = ypower.concat(xpower);
			while (h <= k) {
				// ask membership query
				HashableValue mq;
				m = m.concat(ypower);
				mq = membershipOracle.answerMembershipQuery(new QuerySimple<>(m, x));
				if (mq.isRejecting()) {
					// (x) can be used to to distinguish m and u
					// u has a loop over x and m
					refineWithCounterexample(m, x);
					return ;
				}
				m = m.concat(xpower);
				mq = membershipOracle.answerMembershipQuery(new QuerySimple<>(m, y));
				if (mq.isAccepting()) {
					// (y) can be used to to distinguish m and u
					// u has a loop over y
					refineWithCounterexample(m, y);
					return ;
				}
				h ++;
			}
			k ++;
			xpower = xpower.concat(x);
			ypower = ypower.concat(y);
		}
		// the algorithm will for sure terminate
	}
		
//	default void analyzeFinitePrefix(MembershipOracle<HashableValue> membershipOracle
//			, DFA dfa, Word prefix, Word loop, boolean mqResult) {
//    	// we only analyze the finite prefix because we are certain it is wrong
//        int last = dfa.getInitialState();
//        int index = 0;
//    	while(index < prefix.length()) {
//    		int letter = prefix.getLetter(index);
//    		last = dfa.getSuccessor(last, letter);
//    		Word repr = getStateLabel(last);
//    		repr = repr.concat(prefix.getSuffix(index + 1));
//    		HashableValue result;
//    		result = membershipOracle.answerMembershipQuery(new QuerySimple<>(repr, loop));
//    		if (result.isAccepting() != mqResult) {
//    			// found the word to be put in the column
//    			refineWithCounterexample(prefix.getSuffix(index + 1), loop);
//    			break;
//    		}
//    		index ++;
//    	}
//    }

	boolean addSamples(int state, boolean acc, Word prefix, Word loop);
	
	
    // get the states infinitely occurs on the run of prefix . period .. 
    // i + j < size + 1 and 0 <= i < j 
    default ISet getInfSetAndRecordWords(DFA dfa, Word prefix, Word period, boolean acc) {
    	Alphabet alphabet = dfa.getAlphabet();
        ISet inf = UtilISet.newISet();
        int state = dfa.getSuccessor(prefix);
        Word stem = alphabet.getEmptyWord();
        stem = stem.concat(prefix);
        Word loop = alphabet.getEmptyWord();
        TIntIntMap map = new TIntIntHashMap();
        // first is prefix
        int count = 0;
        int loopState = -1;
        while (true) {
        	if (map.containsKey(state)) {
        		// already seen this state before
        		loopState = state;
        		break;
        	}
        	map.put(state, count); // the power of the period is count
        	state = dfa.getSuccessor(state, period);
        	count ++;
        }
        // now we know the loopState and how many periods to get there
        for (int i = 0; i < map.get(loopState); i ++) {
        	stem = stem.concat(period);
        }
        for (int i = 0; i < (count - map.get(loopState)); i ++) {
        	loop = loop.concat(period);
        }
        // collect all states in the loop
        inf.set(loopState);
        int last = loopState;
        Word head = alphabet.getEmptyWord();
        Word tail = alphabet.getEmptyWord();
        //TODO we can speed up when there is only one state in inf
        // but then the loop has length more then 1
        for (int i = 0; i < loop.length(); i ++) {
        	int letter = loop.getLetter(i);
        	head = head.append(letter);
        	tail = loop.getSuffix(i + 1);
        	stem = stem.append(letter);
        	Word rotation = tail.concat(head);
        	last = dfa.getSuccessor(last, letter);
        	inf.set(last);
        	if(addSamples(last, acc, stem, rotation)) {
        		return null;
        	}
        }
        return inf;
    }

}
