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

package test.learner.dfa;

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.operations.DFAOperations;
import roll.query.EquivalenceOracle;
import roll.query.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.words.Alphabet;
import roll.words.Word;

public class DFATeacherDK implements MembershipOracle<HashableValue>, EquivalenceOracle<DFA, Query<HashableValue>> {

	private final Automaton automaton;
	private final DFA dfa;
	private final Alphabet alphabet;
	
	private int numMembership = 0;
	private int numEquiv = 0;
	
	public DFATeacherDK(DFA dfa, Alphabet alphabet) {
		this.automaton = DFAOperations.toDkDFA(dfa);
		this.dfa = dfa;
		this.alphabet = alphabet;
	}
	
	private Word parseString(String counterexample) {
		return alphabet.getWordFromString(counterexample);
	}
	
	@Override
	public Query<HashableValue> answerEquivalenceQuery(DFA machine) {
		numEquiv ++;
		Automaton conjecture = DFAOperations.toDkDFA(machine);
		Automaton result = automaton.clone().minus(conjecture.clone());
		String counterexample = result.getShortestExample(true);
		Word wordCE = alphabet.getEmptyWord();
		boolean isEq = true;
		
		if(counterexample == null) {
			result = conjecture.clone().minus(automaton.clone());
			counterexample = result.getShortestExample(true);
		}
		
		if(counterexample != null) {
			wordCE = parseString(counterexample);
			isEq = false;
		}
		
		
		Query<HashableValue> ceQuery = new QuerySimple<>(wordCE);
		ceQuery.answerQuery(new HashableValueBoolean(isEq));
		return ceQuery;
	}

	@Override
	public HashableValue answerMembershipQuery(Query<HashableValue> query) {
		numMembership ++;
		Word word = query.getQueriedWord();
		boolean result = dfa.isFinal(dfa.getSuccessor(word));
		return new HashableValueBoolean(result);
	}
	
	
	public int getNumMembership() {
		return numMembership;
	}
	
	public int getNumEquivalence() {
		return numEquiv;
	}
	

}
