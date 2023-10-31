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

package roll.learner.nba.lomega.translator;

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.TDBA;
import roll.automata.operations.FDFAOperations;
import roll.learner.fdfa.LearnerFDFA;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Triplet;
import roll.words.Word;

public class TranslatorFDFATDBA extends TranslatorFDFAUnder {
	protected final TDBA tdba;
	protected MembershipOracle<HashableValue> membershipOracle; 
	
	public TranslatorFDFATDBA(LearnerFDFA learner, TDBA tdba
			, MembershipOracle<HashableValue> membershipOracle) {
		super(learner);
		this.tdba = tdba;
		this.membershipOracle = membershipOracle;
	}
	
	protected Triplet<Word, Word, Word> decomposeCounterexample() {
		// now we construct DFA for each state
		Automaton dollarTDBA = FDFAOperations.buildDFAFromTDBA(tdba);
		Automaton autInter = autUVOmega.intersection(dollarTDBA);
		assert autInter != null;
		String ceStr = autInter.getShortestExample(true);
		options.log.verbose("CeStr = " + ceStr);
		assert(ceStr != null && ceStr != "");
		Query<HashableValue> query = getQuery(ceStr, ceQuery.getQueryAnswer());
		// verify whether it is correct
		options.log.verbose("Word CeStr = " + query.toString());
		Word prefix = query.getPrefix();
		Word suffix = query.getSuffix();
		int repeatDBAState = tdba.getSuccessor(prefix);
//		Word uv = prefix.concat(suffix);
		int nextDBAState = tdba.getSuccessor(repeatDBAState, suffix);
		if (repeatDBAState != nextDBAState) {
			throw new RuntimeException("B(uv) not equal to B(u)");
		}
		// now we can decompose it to three parts
		// the leading state of s should be fixed
		Triplet<Integer, Integer, Integer> tpl = tdba.getTriplet(repeatDBAState);
		int sinkleadState = tpl.getMiddle();
		int proInit = fdfa.getProgressFA(sinkleadState).getInitialState();
		// now we need to find out the last position where we see the initial state
		// (leadState, proInit)
		int position = 0;
		int recordPos = 0;
		int prevState = tdba.getInitialState();
		options.log.verbose("Look for triple = " + sinkleadState + ", " + sinkleadState + ", " + proInit);
		while (position < prefix.length()) {
			tpl = tdba.getTriplet(prevState);
			options.log.verbose("Current triple = " + tpl);
			if (tpl.getLeft() == sinkleadState && tpl.getMiddle() == sinkleadState
					&& tpl.getRight() == proInit) {
				recordPos = position;
			}
			prevState = tdba.getSuccessor(prevState, prefix.getLetter(position));
			position ++;
		}
		options.log.verbose("The last position for (l, 0) = " + recordPos);
		Word wordU = prefix.getPrefix(recordPos); // length?
		Word wordV1 = prefix.getSuffix(recordPos); // index
		options.log.verbose("U: " + wordU.toStringWithAlphabet());
		options.log.verbose("V1: " + wordV1.toStringWithAlphabet());
		options.log.verbose("V2: " + suffix.toStringWithAlphabet());
		return new Triplet<>(wordU, wordV1, suffix);
	}
	
	protected Query<HashableValue> analyseRepeatProgressState(
			Word wordU, Word wordM, Word wordV1, Word wordV2, Word wordY, boolean sink) {
//		int repeatLeadState = fdfa.getLeadingFA().getSuccessor(wordU);
		// previously, above line is used, but for tree-based algorithm
		// M(wordU) is not necessarily equal to M(wordM) and hence state m
		// hence, we still need to get to the state over U, i.e., M(wordU) 
		// since wordM is its label
		int repeatLeadState = fdfa.getLeadingFA().getSuccessor(wordU);
		// INVARIANT: wordM is the label string of repeatLeadState
		int k = 1;
		while(true) {
			// create v1. v^k_2
			Word v1PlusV2 = wordV1;
			for (int i = 0; i < k; i ++) {
				v1PlusV2 = v1PlusV2.concat(wordV2);
			}
			// for a fixed k, we check all 1 <= i < k
			// check whether m.(v1. v^k_2 . y)^i . v1. (v2)
			Word repeatK = v1PlusV2.concat(wordY);
			//3. check whether MQ(m, (v1.v2^k.y)^i) = +
			// if yes, then [v1] and v1.v2^k can be distinguished by y
			// we can first check this
			Query<HashableValue> query = new QuerySimple<>(wordM, repeatK);
			HashableValue mqResult = membershipOracle.answerMembershipQuery(query);
			options.log.verbose("MQ(m.(v1.v2^" + k + ". y) = " + query);
			options.log.verbose("MQ(m.(v1.v2^" + k + ". y) = " + mqResult);

			if (mqResult.isAccepting()) {
				query = new QuerySimple<>(wordU, repeatK);
//				mqResult2 = membershipOracle.answerMembershipQuery(query2);
				query.answerQuery(new HashableValueBoolean(false));
				return query;
			}
			Word wordInfix = alphabet.getEmptyWord();
			for (int i = 0; i < k; i ++) {
				wordInfix = wordInfix.concat(repeatK);
				// (v1.v^k_2.y)^(i+1)
				//1. check whether m = M(m, (v1.v2^k.y)^i)
				// M(m, (v1.v2^k.y)^k) = m, must based on current leading DFA
				int mprime = fdfa.getLeadingFA().getSuccessor(repeatLeadState, wordInfix);
				if (sink && repeatLeadState != mprime) {
					options.log.verbose("m = " + wordM.toStringWithAlphabet());
					options.log.verbose("(v1.v^k_2.y)^i= " + wordInfix.toStringWithAlphabet());
					options.log.verbose("repeatLeadState = " + repeatLeadState + " mprime = " + mprime);
					// usually this happens when progress state [v1] is sink non-final
					// then [v1] and (v1.v2^k.y)^k can be distinguished by empty word
					query = new QuerySimple<>(wordU, wordInfix);
					query.answerQuery(new HashableValueBoolean(false));
					return query;
					// when [v1] is not sink, then repeatLeadState must be equal to
					// mprime always.
				}
				assert (sink || (repeatLeadState == mprime)) : "nonsink has no loop for v1.v2^k.y";
				//2. check whether MQ(m.(v1.v2^k.y)^i.v1.(v2)) = +?
				// if not, then m and m.(v1.v2^k.y)^i can be distinguished by v1.(v2) 
				Word prefix = wordM.concat(wordInfix);
				// when M(m.v1) = m' but not m 
				options.log.verbose("(v1.v2^k.y)^i = " + wordInfix.toStringWithAlphabet());
				options.log.verbose("m.(v1.v2^k.y)^i = " + prefix.toStringWithAlphabet());
				options.log.verbose("M(m.(v1.v2^k.y)^i) = " + mprime);
				prefix = prefix.concat(wordV1);
				options.log.verbose("m.(v1.v2^k.y)^i.v1 = " + prefix.toStringWithAlphabet());
				options.log.verbose("m = " + repeatLeadState);
				query = new QuerySimple<>(prefix, wordV2);
				mqResult = membershipOracle.answerMembershipQuery(query);
				options.log.verbose("MQ(m.(v1.v2^" + k + ". y)^i .v1.(v2) = " + query.toString());
				options.log.verbose("MQ(m.(v1.v2^" + k + ". y)^i .v1.(v2) = " + mqResult);

				if (!mqResult.isAccepting()) {
					// must refine the leading DFA
					Word p = wordU.concat(wordInfix);
					p = p.concat(wordV1);
					Query<HashableValue> query2 = new QuerySimple<>(p, wordV2);
					query2.answerQuery(new HashableValueBoolean(true));
					return query2;
				}
			}
			k ++;
		}
		
	}
	
	@Override
	public Query<HashableValue> translate() {
	    // every time we initialize fdfa, in case it is modified
	    fdfa = fdfaLearner.getHypothesis();
	    String counterexample = "";
	    boolean isInTarget = ceQuery.getQueryAnswer().get();
	    options.log.println("Analysing counterexamples for TDBA...");
		if (! isInTarget ) {
			// negative counterexample
			counterexample = translateLower();
		}else {
		    options.log.verbose("Positive counterexample = " + ceQuery.toString());
			// positive counterexample
			counterexample = getPositiveCounterExample(autUVOmega);
		    options.log.verbose("Counterexample = " + counterexample);
			if (counterexample != null && counterexample != "") {
				counterexample = translateLower();
			}else {
				// all normalised decompositions are accepted by FDFA
				Triplet<Word, Word, Word> partitions = decomposeCounterexample();
				Word wordU = partitions.getLeft();
				Word wordV1 = partitions.getMiddle();
				Word wordV2 = partitions.getRight();
				int repeatLeadState = fdfa.getLeadingFA().getSuccessor(wordU);
				DFA proDFA = fdfa.getProgressFA(repeatLeadState);
				int repeatProState = proDFA.getSuccessor(wordV1);
				Word leadRepState = fdfaLearner.getLeadingStateLabel(repeatLeadState);
				Word proRepState = fdfaLearner.getProgressStateLabel(repeatLeadState, repeatProState);
				
				// INVARIANT:
				// 1. M(wordU . wordV1) = M(wordU . wordV1 . wordV2)
				// 2. A^{repeatLeadState}(wordV1) = A^{repeatLeadState}(wordV1 . wordV2)
				options.log.verbose("M( u) = " + repeatLeadState);
				options.log.verbose("[u] = " + leadRepState.toStringWithAlphabet());

				options.log.verbose("M( u . v1) = " + fdfa.getLeadingFA().getSuccessor(repeatLeadState, wordV1));
				options.log.verbose("M( u. v1. v2) = " + fdfa.getLeadingFA().getSuccessor(repeatLeadState, wordV1.concat(wordV2)));
				options.log.verbose("A^u(v1) = " + proDFA.getSuccessor(wordV1));
				options.log.verbose("A^u(v1.v2) = " + proDFA.getSuccessor(wordV1.concat(wordV2)));
				options.log.verbose("[v1] = [v1.v2] = " + proRepState.toStringWithAlphabet());
				
				//1. check MQ(m.v1.(v2)) = +?
				// if not, u and m can be distinguished with v1.(v2)
				HashableValue mqResult = membershipOracle.answerMembershipQuery(new QuerySimple<>(leadRepState.concat(wordV1), wordV2));
				options.log.verbose("MQ(m.v1.(v2) = " + mqResult);
				if (! mqResult.isAccepting()) {
					Query<HashableValue> query = new QuerySimple<>(wordU.concat(wordV1), wordV2);
					query.answerQuery(new HashableValueBoolean(true));
					return query;
				}
				
				//2. obtain some experiment y such that M(m.[v1].y) = m but m.([v1].y) not in L
				// [v1] might be a sink non-final state, then y will be empty word
				// wordY == null if [v1] is sink and non-final state
				Word wordY = fdfaLearner.getProgressNegativeExperiment(repeatLeadState, repeatProState);
				if (wordY == null) {
					// repeatProState must be sink but non-final state
					// by definition, this means that M(m.[v1]) = m and m.([v1]) not in L
					// We know that [v1] = A^m(v1) = A^m(v1.v2)
					// 1. first check whether M(m.v1.v2^k) = m 
					// if not equal, then return (u, v1.v2^k) as emptyword can distinguish
					// v1.v2^k and [v1] with emptyword
					
					// 2. if M(m.(v1.v2^k)^k) = m, then check
					// 2.1 m.(v1.v2^k).v1.(v2) in L? 
					//     if not, then m.(v1.v2^k)^k and m can be distinguished with v1.(v2)
					// 2.2 m.(v1.v2^k) in L?
					//     if in L, then emptyword can distinguish [v1] and v1.v2^k					
					// 3. there must be some k 
					options.log.verbose("Sink non-final states");
					wordY = alphabet.getEmptyWord();
//					return analyseRepeatProgressState(wordU, leadRepState, wordV1, wordV2, wordY, true);
				}
				options.log.verbose("Y = " + wordY.toStringWithAlphabet());
				options.log.verbose("M( m. v1. Y) = m: " + fdfa.getLeadingFA().getSuccessor(repeatLeadState, wordV1.concat(wordY)));
				
				int leadStateVY = fdfa.getLeadingFA().getSuccessor(repeatLeadState, wordV1.concat(wordY));
				if (repeatLeadState != leadStateVY) {
					options.log.verbose("M(m.v1.(Y) /= m");
					Query<HashableValue> query = new QuerySimple<>(wordU, wordV1.concat(wordY));
					mqResult = membershipOracle.answerMembershipQuery(query);
					query.answerQuery(new HashableValueBoolean(false));
					return query;
				}
				
				// check MQ(m. (v1.y))
				Query<HashableValue> query = new QuerySimple<>(leadRepState, wordV1.concat(wordY));
				Query<HashableValue> query2 = new QuerySimple<>(wordU, wordV1.concat(wordY));
				mqResult = membershipOracle.answerMembershipQuery(query);
				options.log.verbose("MQ(m.(v1.y) = " + query.toString());
				options.log.verbose("MQ(m.(v1.y) = " + mqResult);
				HashableValue mqResult2 = membershipOracle.answerMembershipQuery(query2);
				if (mqResult.isAccepting()) {
					query2.answerQuery(new HashableValueBoolean(false));
					options.log.verbose("MQ(u.(v1.y) = " + mqResult2);
					return query2;
				}
				// now we have M(m.v1.y) = m , m. (v1.y) not in L
				return analyseRepeatProgressState(wordU, leadRepState, wordV1, wordV2, wordY, true);

			}
		}
		// refine either leading or progress DFA
		return getQuery(counterexample, new HashableValueBoolean(false));
	}

}
