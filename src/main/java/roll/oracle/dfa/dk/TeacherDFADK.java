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

package roll.oracle.dfa.dk;

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.operations.DFAOperations;
import roll.main.Options;
import roll.oracle.dfa.TeacherDFA;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Use dk.brics.automaton package as DFA teacher
 * 
 * */

public class TeacherDFADK extends TeacherDFA {

	private final Automaton automaton;
	
	public TeacherDFADK(Options options, DFA dfa) {
	    super(options, dfa);
		this.automaton = DFAOperations.toDkDFA(dfa);
	}
	
	private Word parseString(String counterexample) {
		return alphabet.getWordFromString(counterexample);
	}

    @Override
    protected Query<HashableValue> checkEquivalence(DFA hypothesis) {
        Automaton conjecture = DFAOperations.toDkDFA(hypothesis);
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

}
