/* Copyright (c) 2016, 2017                                               */
/*       Institute of Software, Chinese Academy of Sciences               */
/* This file is part of ROLL, a Regular Omega Language Learning library.  */
/* ROLL is free software: you can redistribute it and/or modify           */
/* it under the terms of the GNU General Public License as published by   */
/* the Free Software Foundation, either version 3 of the License, or      */
/* (at your option) any later version.                                    */

/* This program is distributed in the hope that it will be useful,        */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          */
/* GNU General Public License for more details.                           */

/* You should have received a copy of the GNU General Public License      */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package roll.learner;

import roll.automata.DFA;
import roll.main.Options;
import roll.query.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWord;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public abstract class LearnerDFA extends LearnerBase<DFA> {
	
	protected final Alphabet alphabet;
	protected final MembershipOracle<HashableValue> membershipOracle;
	private boolean alreadyStarted = false;
	protected DFA dfa;
	protected Options options;
	
	public LearnerDFA(Options options, Alphabet alphabet
	        , MembershipOracle<HashableValue> membershipOracle) {
		assert options != null && alphabet != null && membershipOracle != null;
		this.options = options;
		this.alphabet = alphabet;
		this.membershipOracle = membershipOracle;
	}
	
	@Override
	public void startLearning() {
		if(alreadyStarted)
			try {
				throw new Exception("Learner should not be started twice");
			} catch (Exception e) {
				e.printStackTrace();
			}
		alreadyStarted = true;
		initialize();
	}
	
	protected abstract void initialize();
	
	protected ExprValue getCounterExampleWord(Query<HashableValue> query) {
		assert query != null;
		Word word = query.getQueriedWord();
		assert word != null;
		return new ExprValueWord(word);
	}
	
	protected ExprValue getExprValueWord(Word word) {
		return new ExprValueWord(word);
	}
	
	protected HashableValue getHashableValueBoolean(boolean result) {
		return new HashableValueBoolean(result);
	}

	@Override
	public DFA getHypothesis() {
		return dfa;
	}
	
	@Override
	public Options getOptions() {
	    return options;
	}
	
	public abstract Word getStateLabel(int state);
	
	protected abstract CeAnalyzer getCeAnalyzerInstance(ExprValue exprValue, HashableValue result);
	
	protected HashableValue processMembershipQuery(Word prefix, Word suffix) {
		Query<HashableValue> query = new QuerySimple<>(null, prefix, suffix, -1);
		return membershipOracle.answerMembershipQuery(query);
	}
	
	protected HashableValue processMembershipQuery(Query<HashableValue> query) {
		return membershipOracle.answerMembershipQuery(query);
	}
	
	public abstract class CeAnalyzer {

	    protected ExprValue wordExpr;
	    protected final ExprValue exprValue; 
	    protected final HashableValue result;
	    
	    public CeAnalyzer(ExprValue exprValue, HashableValue result) {
	        this.exprValue = exprValue;
	        this.result = result;
	    }
	    
	    // only for table-based algorithms
	    public ExprValue getNewExpriment() {
	        return wordExpr;
	    }
	    
	    protected abstract void update(CeAnalysisResult result);
	    	    
	    public void analyze() {
	        CeAnalysisResult result = findBreakIndex();
	        update(result);
	    }
	    
	    protected CeAnalysisResult findBreakIndex() {
	        Word wordCE = this.exprValue.get();
            // get the initial state from automaton
           int letterNr = 0, currState = -1, prevState = dfa.getInitialState();
            if(! options.binarySearch) {
                for (letterNr = 0; letterNr < wordCE.length(); letterNr++) {
                    currState = dfa.getSuccessor(prevState, wordCE.getLetter(letterNr));
                    Word prefix = getStateLabel(currState);
                    Word suffix = wordCE.getSuffix(letterNr + 1);
                    HashableValue memMq = processMembershipQuery(prefix, suffix);
                    if (!result.valueEqual(memMq)) {
                        break;
                    }
                    prevState = currState;
                }
            }else {
                // binary search
                int low = 0, high = wordCE.length() - 1;
                while(low <= high) {
                    int mid = (low + high) / 2;
                    assert mid < wordCE.length();
                    int fst = dfa.getSuccessor(wordCE.getPrefix(mid));
                    int snd = dfa.getSuccessor(fst, wordCE.getLetter(mid));
                    Word fstLabel = getStateLabel(fst);
                    Word sndLabel = getStateLabel(snd);
                                        
                    HashableValue fstMq = processMembershipQuery(fstLabel, wordCE.getSuffix(mid));
                    HashableValue sndMq = processMembershipQuery(sndLabel, wordCE.getSuffix(mid + 1));
                    
                    if (! fstMq.valueEqual(sndMq)) {
                        prevState = fst;
                        letterNr = mid;
                        currState = snd;
                        break;
                    }

                    if (fstMq.valueEqual(result)) {
                        low = mid + 1;
                    } else {
                        high = mid;
                    }
                }
            }
            CeAnalysisResult result = new CeAnalysisResult();
            result.breakIndex = letterNr;
            result.prevState = prevState;
            result.currState = currState;
            return result;
	    }

	}
	// only valid for column based algorithms
	protected static class CeAnalysisResult {
	    public int prevState;
	    public int currState;
	    public int breakIndex;
	}
}
