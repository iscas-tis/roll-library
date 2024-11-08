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

package roll.learner.nba.mp;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.DFA;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.TarjanSCCsNonrecursive;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWordPair;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.table.ObservationTableAbstract;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * On the Learnability of Infinitary Regular Sets
 * by Oded Maler and Amir Pnueli
 * 
 * In its original algorithm, all suffixes of counterexamples will be added
 * here, we only add one suffix and may find conflict before SCC decomposition  
 * */

public class LearnerWDBAMP extends LearnerBase<NBA> {

    private boolean alreadyStarted = false;

    private ObservationTableAbstract observationTable;
    private int[] marks;
    // record the pairs of positive and negative samples
    // that visit the state infinitely often
    private ExprValue[] positiveSamples;
    private ExprValue[] negativeSamples;
    
    // the transition graph
    private DFA transGraph;

    static protected final int ACC = 2;
    static protected final int REJ = 1;
    
    public LearnerWDBAMP(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        observationTable = new ObservationTableWDBAMP();
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.NBA_MP;
    }

    @Override
    public void startLearning() {
        if(alreadyStarted) {
            throw new UnsupportedOperationException("Learner can not start twice");
        }
        alreadyStarted = true;
        initialize();
    }

    protected void initialize() {
        observationTable.clear();
        Word wordEmpty = alphabet.getEmptyWord();
        observationTable.addUpperRow(wordEmpty);
        
        // add every letter^omega
        for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
            Word letterWord = alphabet.getLetterWord(letterNr);
            observationTable.addLowerRow(alphabet.getLetterWord(letterNr));
            observationTable.addColumn(new ExprValueWordPair(wordEmpty, letterWord));
        }
        
        // ask initial queries for upper table
        processMembershipQueries(observationTable.getUpperTable()
                , 0, observationTable.getColumns().size());
        // ask initial queries for lower table
        processMembershipQueries(observationTable.getLowerTable()
                , 0, observationTable.getColumns().size());
        makeTableClosed();
    }
    
    protected void makeTableClosed() {
        ObservationRow lowerRow = observationTable.getUnclosedLowerRow();
        while(lowerRow != null) {
            // 1. move to upper table
            observationTable.moveRowFromLowerToUpper(lowerRow);
            // 2. add one letter to lower table
            List<ObservationRow> newLowerRows = new ArrayList<>();
            for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
                Word newWord = lowerRow.getWord().append(letterNr);
                ObservationRow row = observationTable.getTableRow(newWord); // already existing
                if(row != null) continue;
                ObservationRow newRow = observationTable.addLowerRow(newWord);
                newLowerRows.add(newRow);
            }
            // 3. process membership queries
            processMembershipQueries(newLowerRows, 0, observationTable.getColumns().size());
            lowerRow = observationTable.getUnclosedLowerRow();
        }
    	System.out.println(observationTable.toString());
    	// construct the transition graph and resolve conflict
        constructHypothesis();
    }
    
    @Override
    protected void constructHypothesis() {
        transGraph = constructTransitionGraph();
        markOrDetect(transGraph);
    }
    
    private boolean findConflict(DFA dfa, ISet inf, int mark) {
		List<ObservationRow> upperTable = observationTable.getUpperTable();
		// acceleration, check conflict before SCC decomposition
		int conflictState = -1;
		for (int state : inf) {
			marks[state] = marks[state] | mark;
			// check if there is a state with conflict signs
			if (marks[state] == 3) {
				conflictState = state;
				break;
			}
		}
		if (conflictState >= 0) {
			// found one conflict
			// first, check positive samples
			Word repr = upperTable.get(conflictState).getWord();
			ExprValue pos = positiveSamples[conflictState];
			ExprValue neg = negativeSamples[conflictState];
			HashableValue res = membershipOracle.answerMembershipQuery(new QuerySimple<>(repr, pos.getRight()));
			if (!res.isAccepting()) {
				// loop can distinguish s and prefix
				if (options.minimization) {
					analyzeFinitePrefix(dfa, pos.getLeft(), pos.getRight(), true);
				}else 
					refineWithCounterexample(pos.getLeft(), pos.getRight());
				return true;
			} else {
				res = membershipOracle.answerMembershipQuery(new QuerySimple<>(repr, neg.getRight()));
				if (!res.isRejecting()) {
					// loop can distinguish s and prefix
					if (options.minimization) 
						analyzeFinitePrefix(dfa, neg.getLeft(), neg.getRight(), false);
					else 
						refineWithCounterexample(neg.getLeft(), neg.getRight());
					return true;
				}
			}
			// neither of them holds
			// conflict pair (s, pos, neg)
			resolveConflict(conflictState, repr, pos.getRight(), neg.getRight());
			return true;
		}
		return false;
    }

    protected void markOrDetect(DFA dfa) {
    	// mark every state whether they are rejecting or accepting
        marks = new int[dfa.getStateSize()];
        // associate each state with a positive or negative sample
        positiveSamples = new ExprValue[dfa.getStateSize()];
        negativeSamples = new ExprValue[dfa.getStateSize()];
        
        List<ObservationRow> upperTable = observationTable.getUpperTable();
        List<ExprValue> columns = observationTable.getColumns();
        
        for(int i = 0; i < upperTable.size(); i ++) {
            Word s = upperTable.get(i).getWord();
            for(int j = 0; j < columns.size(); j ++) {
                // compute Inf(s . expr)
                ExprValue expr = columns.get(j);
                Word suffix = expr.getLeft();
                Word period = expr.getRight();
                Word prefix = s.concat(suffix);
                HashableValue mq = upperTable.get(i).getValues().get(j);
                ISet inf = getInfSetAndRecordWords(dfa, prefix, period, mq.isAccepting());
                // now try to find conflict
                boolean foundConflict = findConflict(dfa, inf, mq.isAccepting()? ACC : REJ);
                if (foundConflict) return;
            }
        }
        
        // we need to decide the marking of the SCCs
        ISet inits = UtilISet.newISet();
        inits.set(dfa.getInitialState());
        TarjanSCCsNonrecursive tarjan = new TarjanSCCsNonrecursive(dfa, inits);
        for (ISet mscc : tarjan.getSCCs()) {
        	System.out.println("SCC: " + mscc);
        	// whether there are two states with different acceptance
        	int posState = -1;
        	int negState = -1;
        	for (int s : mscc) {
        		if (marks[s] == ACC) {
        			posState = s;
        		}else if(marks[s] == REJ) {
        			negState = s;
        		}
        	}
        	int mark = 0;
        	if (posState >= 0 && negState >= 0) {
        		// we need to find conflict here
        		Word z = UtilPath.findPath(dfa, posState, negState);
        		Word w = UtilPath.findPath(dfa, negState, posState);
        		Word s = upperTable.get(posState).getWord();
        		Word t = upperTable.get(negState).getWord();
        		// word leads from s to t
        		Word zw = z.concat(w);
        		// word leads from t to s
        		Word wz = w.concat(z);

        		// s.(zw) and t.(wz)
        		HashableValue mq;
        		mq = membershipOracle.answerMembershipQuery(new QuerySimple<>(s, zw));
        		if (!mq.isAccepting()) {
        			resolveConflict(posState, s, positiveSamples[posState].getRight(), zw);
        		}
        		// now check whether sz is equivalent to t
        		mq = membershipOracle.answerMembershipQuery(new QuerySimple<>(t, wz));
        		if (!mq.isRejecting()) {
        			resolveConflict(negState, t, negativeSamples[negState].getRight(), wz);
        		}
        		// if both of them not feasible
        		// now we have a problem
        		System.err.println("Serious problem");
        		// this means sz /= t, as wz can distinguish them
        		// sz.(wz) is accepting, while t.(wz) is rejecting
        		if (options.minimization) {
            		analyzeFinitePrefix(dfa, s.concat(z), wz, true);
        		}else {
        			refineWithCounterexample(s.concat(z), wz);
        		}
        		// now we check 
        	}else if (posState >= 0){
        		mark = ACC;
        	}else if (negState >= 0) {
        		mark = REJ;
        	}
        	for (int s : mscc) {
        		marks[s] = mark;
        	}
        }
        
    }
    
    private void addColumn(Word prefix, Word loop) {
    	int colSize = observationTable.getColumns().size();
    	System.out.println("colsize: " + colSize);
    	ExprValue col = new ExprValueWordPair(prefix, loop);
    	// we cannot add repeated column to table
    	if (observationTable.getColumns().indexOf(col) >= 0) {
    		return ;
    	}
    	System.out.println("add column: " + col);
        observationTable.addColumn(col);
//    	System.out.println("colsize: " + observationTable.getColumns().size());

        // add one column and fill those entries
        processMembershipQueries(observationTable.getUpperTable()
                , colSize, 1);
        processMembershipQueries(observationTable.getLowerTable()
                , colSize, 1);
    }
    
    private void analyzeFinitePrefix(DFA dfa, Word prefix, Word loop, boolean mqResult) {
    	// we only analyze the finite prefix because we are certain it is wrong
        List<ObservationRow> upperTable = observationTable.getUpperTable();
        int last = dfa.getInitialState();
        int index = 0;
    	while(index < prefix.length()) {
    		int letter = prefix.getLetter(index);
    		last = dfa.getSuccessor(last, letter);
    		Word repr = upperTable.get(last).getWord();
    		repr = repr.concat(prefix.getSuffix(index + 1));
    		HashableValue result;
    		result = membershipOracle.answerMembershipQuery(new QuerySimple<>(repr, loop));
    		if (result.isAccepting() != mqResult) {
    			// found the word to be put in the column
    			addColumn(prefix.getSuffix(index + 1), loop);
    	        makeTableClosed();
    			break;
    		}
    		index ++;
    	}
    }
//    
    // we use our algorithm to generate the suffix
    // original algorithm does not provide a concrete procedure 
    // for this function
    private void resolveConflict(int state, Word u, Word x, Word y) {
		// TODO Auto-generated method stub
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
					if (options.minimization) {
						analyzeFinitePrefix(transGraph, m, x, false);
					}else
						refineWithCounterexample(m, x);
					return ;
				}
				m = m.concat(xpower);
				mq = membershipOracle.answerMembershipQuery(new QuerySimple<>(m, y));
				if (mq.isAccepting()) {
					if (options.minimization) 
						analyzeFinitePrefix(transGraph, m, y, true);
					else
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

	protected DFA constructTransitionGraph() {
        DFA dfa = new DFA(alphabet);
        List<ObservationRow> upperTable = observationTable.getUpperTable();
        for(int i = 0; i < upperTable.size(); i ++) {
            dfa.createState();
        }
        // build transition system
        for(int currNr = 0; currNr < upperTable.size(); currNr ++) {
            StateNFA state = dfa.getState(currNr);
            Word currWord = upperTable.get(currNr).getWord();
            for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
                Word succWord = currWord.append(letter);
                int succNr = observationTable.getUpperTableRowIndex(succWord);
                state.addTransition(letter, succNr);
            }
            // now decide whether it is initial state
            if(currWord.isEmpty()) {
                dfa.setInitial(currNr);
            }
        }
        return dfa;
    }

    protected Query<HashableValue> processMembershipQuery(ObservationRow row, int offset, ExprValue valueExpr) {
        assert valueExpr instanceof ExprValueWordPair;
        Word prefix = row.getWord();        //u
        Word left = valueExpr.getLeft();    //x
        prefix = prefix.concat(left);       //ux
        Word suffix = valueExpr.getRight();  // ux(y)^w
        HashableValue result = processMembershipQuery(row, prefix, suffix, offset);
        Query<HashableValue> query = getQuerySimple(row, prefix, suffix, offset);
        query.answerQuery(result);
        return query;
    }
    
    protected Query<HashableValue> getQuerySimple(ObservationRow row, Word prefix, Word suffix, int column) {
        return new QuerySimple<>(row, prefix, suffix, column);
    }

    protected HashableValue processMembershipQuery(ObservationRow row, Word prefix, Word suffix, int column) {
        return membershipOracle.answerMembershipQuery(getQuerySimple(row, prefix, suffix, column));
    }
    
    protected void processMembershipQueries(List<ObservationRow> rows
            , int colOffset, int length) {
        List<Query<HashableValue>> results = new ArrayList<>();
        List<ExprValue> columns = observationTable.getColumns();
        int endNr = length + colOffset;
        for(ObservationRow row : rows) {
            for(int colNr = colOffset; colNr < endNr; colNr ++) {
                results.add(processMembershipQuery(row, colNr, columns.get(colNr)));
            }
        }
        putQueryAnswers(results);
    }
        
    protected void putQueryAnswers(List<Query<HashableValue>> queries) {
        for(Query<HashableValue> query : queries) {
            putQueryAnswers(query);
        }
    }
    
    protected void putQueryAnswers(Query<HashableValue> query) {
        ObservationRow row = query.getPrefixRow();
        HashableValue result = query.getQueryAnswer();
        assert result != null;
        row.set(query.getSuffixColumn(), result);
    }

    @Override
    public NBA getHypothesis() {
    	constructHypothesis();
    	NBA nba = new NBA(transGraph.getAlphabet());
    	for (int s = 0; s < transGraph.getStateSize(); s ++) {
    		nba.createState();
    	}
    	nba.setInitial(transGraph.getInitialState());
    	for (int s = 0; s < nba.getStateSize(); s ++) {
    		for (int c = 0; c < nba.getAlphabetSize(); c ++) {
    			int t = transGraph.getSuccessor(s, c);
    			nba.getState(s).addTransition(c, t);
    		}
    	}
    	for (int s = 0; s < nba.getStateSize(); s ++) {
    		if (marks[s] == ACC) nba.setFinal(s );
    	}
        return nba;
    }
    
    @Override
    public String toString() {
    	return observationTable.toString();
    }
    
    protected void refineWithCounterexample(Word prefix, Word loop) {
    	addColumns(prefix, loop);
    	makeTableClosed();
    }

    @Override
    public void refineHypothesis(Query<HashableValue> query) {
    	// now we get a counterexample
//    	if (options.minimization) {
//        	HashableValue mq = membershipOracle.answerMembershipQuery(query);
//            ISet inf = getInfSetAndRecordWords(transGraph, query.getPrefix(), query.getSuffix(), mq.isAccepting());
//            findConflict(transGraph, inf, mq.isAccepting()? ACC : REJ);    		
//    	}else 
    		refineWithCounterexample(query.getPrefix(), query.getSuffix());
    }

    @Override
    public String toHTML() {
        return "<pre> " + toString() + "</pre>";
    }
    
    protected void addColumns(Word prefix, Word loop) {
    	// now we need to add every suffix to the column
    	// add including the word (emptyword, loop)
    	for (int i = 0; i < prefix.length(); i ++) {
        	addColumn(prefix.getSuffix(i), loop);
        	System.out.println("suffix: " + prefix.getSuffix(i));
    	}
    	// we need to add all rotation of the word
    	Word head = alphabet.getEmptyWord();
        Word tail = alphabet.getEmptyWord();
        Word wordEmpty = alphabet.getEmptyWord();
        // only its rotations
        for (int i = 0; i < loop.length(); i ++) {
        	int letter = loop.getLetter(i);
        	head = head.append(letter);
        	tail = loop.getSuffix(i + 1);
        	Word rotation = tail.concat(head);
        	addColumn(wordEmpty, rotation);
        	System.out.println("rotation: " + rotation);
        }
    }
    
    // get the states infinitely occurs on the run of prefix . period .. 
    // i + j < size + 1 and 0 <= i < j 
    private ISet getInfSetAndRecordWords(DFA dfa, Word prefix, Word period, boolean acc) {
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
        for (int i = 0; i < loop.length(); i ++) {
        	int letter = loop.getLetter(i);
        	head = head.append(letter);
        	tail = loop.getSuffix(i + 1);
        	stem = stem.append(letter);
        	Word rotation = tail.concat(head);
        	last = dfa.getSuccessor(last, letter);
        	inf.set(last);
        	if (acc) {
        		positiveSamples[last] = new ExprValueWordPair(stem, rotation);
        	}else {
        		negativeSamples[last] = new ExprValueWordPair(stem, rotation); 
        	}
        }
        return inf;
    }
    

}
