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
import roll.util.Pair;
import roll.util.Triplet;
import roll.words.Alphabet;
import roll.words.Word;

public class TranslatorFDFATDBA extends TranslatorFDFAUnder {
	protected final TDBA tdba;
	protected MembershipOracle<HashableValue> membershipOracle; 
	protected Word prefix;
	protected Word suffix;
	
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
		System.out.println("CeStr = " + ceStr);
		assert(ceStr != null && ceStr != "");
		Query<HashableValue> query = getQuery(ceStr, ceQuery.getQueryAnswer());
		// verify whether it is correct
		System.out.println("Word CeStr = " + query.toString());
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
		int proState = tpl.getRight();
		int proInit = fdfa.getProgressFA(sinkleadState).getInitialState();
		// now we need to find out the last position where we see the initial state
		// (leadState, proInit)
		int position = 0;
		int recordPos = 0;
		int prevState = tdba.getInitialState();
		System.out.println("Look for triple = " + sinkleadState + ", " + sinkleadState + ", " + proInit);
		while (position < prefix.length()) {
			tpl = tdba.getTriplet(prevState);
			System.out.println("Current triple = " + tpl);
			if (tpl.getLeft() == sinkleadState && tpl.getMiddle() == sinkleadState
					&& tpl.getRight() == proInit) {
				recordPos = position;
			}
			prevState = tdba.getSuccessor(prevState, prefix.getLetter(position));
			position ++;
		}
		System.out.println("The last position for (l, 0) = " + recordPos);
		Word wordU = prefix.getPrefix(recordPos); // length?
		Word wordV1 = prefix.getSuffix(recordPos); // index
		System.out.println("U: " + wordU.toStringWithAlphabet());
		System.out.println("V1: " + wordV1.toStringWithAlphabet());
		System.out.println("V2: " + suffix.toStringWithAlphabet());
		return new Triplet<>(wordU, wordV1, suffix);
	}
	
	protected Query<HashableValue> analyseRepeatProgressState(
			Word wordU, Word wordM, Word wordV1, Word wordV2, Word wordY) {
		int repeatLeadState = fdfa.getLeadingFA().getSuccessor(wordM);
		int k = 1;
		while(true) {
			// create v1. v^k_2
			Word v1PlusV2 = wordV1;
			for (int i = 0; i < k; i ++) {
				v1PlusV2 = v1PlusV2.concat(wordV2);
			}
			// check whether m.(v1. v^k_2 . y)^k . v1. (v2)
			Word repeatK = v1PlusV2.concat(wordY);
			Word wordInfix = alphabet.getEmptyWord();
			for (int i = 0; i < k; i ++) {
				wordInfix = wordInfix.concat(repeatK);
			}
			Word prefix = wordM.concat(wordInfix);
			// M(m.(v1.v2^k.y)^k) = m
			int mprime = fdfa.getLeadingFA().getSuccessor(prefix);
			if (repeatLeadState != mprime) {
				System.out.println("m = " + wordM.toStringWithAlphabet());
				System.out.println("m.(v1.v^k_2.y)^k= " + prefix.toStringWithAlphabet());
				System.out.println("repeatLeadState = " + repeatLeadState + " mprime = " + mprime);
				// usually this happens when progress state [v1] is sink non-final
				// then [v1] and (v1.v2^k.y)^k can be distinguished by empty word
				Query<HashableValue> query = new QuerySimple<>(wordU, wordInfix);
				query.answerQuery(new HashableValueBoolean(false));
				return query;
				// when [v1] is not sink, then repeatLeadState must be equal to
				// mprime always.
			}
			prefix = prefix.concat(wordV1);
			Query<HashableValue> query = new QuerySimple<>(prefix, wordV2);
			HashableValue mqResult = membershipOracle.answerMembershipQuery(query);
			System.out.println("MQ(m.(v1.v2^" + k + ". y)^k .v1.(v2) = " + query.toString());
			System.out.println("MQ(m.(v1.v2^" + k + ". y)^k .v1.(v2) = " + mqResult);

			if (!mqResult.isAccepting()) {
				// must refine the leading DFA
				query.answerQuery(new HashableValueBoolean(true));
				return query;
			}
			// now check another query
			query = new QuerySimple<>(wordM, repeatK);
			mqResult = membershipOracle.answerMembershipQuery(query);
			System.out.println("MQ(m.(v1.v2^" + k + ". y) = " + query);
			System.out.println("MQ(m.(v1.v2^" + k + ". y) = " + mqResult);

			if (mqResult.isAccepting()) {
				query = new QuerySimple<>(wordU, repeatK);
//				mqResult2 = membershipOracle.answerMembershipQuery(query2);
				query.answerQuery(new HashableValueBoolean(false));
				return query;
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
	    System.out.println("Analysing counterexamples for TDBA...");
		if (! isInTarget ) {
			// negative counterexample
			counterexample = translateLower();
		}else {
		    System.out.println("Positive counterexample = " + ceQuery.toString());
			// positive counterexample
			counterexample = getPositiveCounterExample(autUVOmega);
		    System.out.println("Counterexample = " + counterexample);
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
				System.out.println("M( u) = " + repeatLeadState);
				System.out.println("[u] = " + leadRepState.toStringWithAlphabet());

				System.out.println("M( u . v1) = " + fdfa.getLeadingFA().getSuccessor(repeatLeadState, wordV1));
				System.out.println("M( u. v1. v2) = " + fdfa.getLeadingFA().getSuccessor(repeatLeadState, wordV1.concat(wordV2)));
				System.out.println("A^u(v1) = " + proDFA.getSuccessor(wordV1));
				System.out.println("A^u(v1.v2) = " + proDFA.getSuccessor(wordV1.concat(wordV2)));
				System.out.println("[v1] = [v1.v2] = " + proRepState.toStringWithAlphabet());
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
					System.out.println("Sink non-final states");
					wordY = alphabet.getEmptyWord();
					return analyseRepeatProgressState(wordU, leadRepState, wordV1, wordV2, wordY);
				}
				System.out.println("Y = " + wordY.toStringWithAlphabet());
				System.out.println("M( m. v1. Y) = m: " + fdfa.getLeadingFA().getSuccessor(repeatLeadState, wordV1.concat(wordY)));
				// now check m . v1 . (v2)
				HashableValue mqResult = membershipOracle.answerMembershipQuery(new QuerySimple<>(leadRepState.concat(wordV1), wordV2));
				System.out.println("MQ(m.v1.(v2) = " + mqResult);
				if (! mqResult.isAccepting()) {
					Query<HashableValue> query = new QuerySimple<>(wordU.concat(wordV1), wordV2);
					query.answerQuery(new HashableValueBoolean(true));
					return query;
				}
				
				int leadStateVY = fdfa.getLeadingFA().getSuccessor(repeatLeadState, wordV1.concat(wordY));
				if (repeatLeadState != leadStateVY) {
					System.out.println("M(m.v1.(Y) /= m");
					Query<HashableValue> query = new QuerySimple<>(wordU, wordV1.concat(wordY));
					mqResult = membershipOracle.answerMembershipQuery(query);
					query.answerQuery(new HashableValueBoolean(false));
//					System.exit(-1);
					return query;
				}else {
					// check MQ(m. (v1.y))
					Query<HashableValue> query = new QuerySimple<>(leadRepState, wordV1.concat(wordY));
					Query<HashableValue> query2 = new QuerySimple<>(wordU, wordV1.concat(wordY));
					mqResult = membershipOracle.answerMembershipQuery(query);
					System.out.println("MQ(m.(v1.y) = " + query.toString());
					System.out.println("MQ(m.(v1.y) = " + mqResult);
					HashableValue mqResult2 = membershipOracle.answerMembershipQuery(query2);
					if (mqResult.isAccepting()) {
						query2.answerQuery(new HashableValueBoolean(false));
						System.out.println("MQ(u.(v1.y) = " + mqResult2);
//						System.exit(-1);
						return query2;
					}
					// now we have M(m.v1.y) = m , m. (v1.y) not in L
					
					int k = 1;
					while(true) {
						// create v1. v^k_2
						Word v1PlusV2 = wordV1;
						for (int i = 0; i < k; i ++) {
							v1PlusV2 = v1PlusV2.concat(wordV2);
						}
						// check whether m.(v1. v^k_2 . y)^k . v1. (v2)
						Word repeatK = v1PlusV2.concat(wordY);
						Word wordInfix = alphabet.getEmptyWord();
						for (int i = 0; i < k; i ++) {
							wordInfix = wordInfix.concat(repeatK);
						}
						Word prefix = leadRepState.concat(wordInfix);
						prefix = prefix.concat(wordV1);
						query = new QuerySimple<>(prefix, wordV2);
						mqResult = membershipOracle.answerMembershipQuery(query);
						System.out.println("MQ(m.(v1.v2^" + k + ". y)^k .v1.(v2) = " + query.toString());
						System.out.println("MQ(m.(v1.v2^" + k + ". y)^k .v1.(v2) = " + mqResult);

						if (!mqResult.isAccepting()) {
							query.answerQuery(new HashableValueBoolean(true));
							return query;
						}
						// now check another query
						query = new QuerySimple<>(leadRepState, repeatK);
						mqResult = membershipOracle.answerMembershipQuery(query);
						System.out.println("MQ(m.(v1.v2^" + k + ". y) = " + query);
						System.out.println("MQ(m.(v1.v2^" + k + ". y) = " + mqResult);

						if (mqResult.isAccepting()) {
							query2 = new QuerySimple<>(wordU, repeatK);
//							mqResult2 = membershipOracle.answerMembershipQuery(query2);
							query2.answerQuery(new HashableValueBoolean(false));
							return query2;
						}
						k ++;
					}
				}
			}
		}
		// refine either leading or progress DFA
		return getQuery(counterexample, new HashableValueBoolean(false));
	}
	
	
	
	

}
