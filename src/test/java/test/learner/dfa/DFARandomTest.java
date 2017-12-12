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

import roll.automata.DFA;
import roll.automata.operations.DFAGenerator;
import roll.learner.LearnerDFA;
import roll.learner.LearnerType;
import roll.learner.dfa.table.LearnerDFATableColumn;
import roll.learner.dfa.table.LearnerDFATableLStar;
import roll.learner.dfa.tree.LearnerDFATreeColumn;
import roll.learner.dfa.tree.LearnerDFATreeKV;
import roll.main.Options;
import roll.query.Query;
import roll.table.HashableValue;
import roll.words.Alphabet;

public class DFARandomTest {
	
	public static void main(String[] args) {
		
		if(args.length < 3) {
			System.out.println("Usage: <PROGRAM> <tab|kv|tr|lstar> <NUM_OF_CASES> <NUM_OF_STATES_FOR_CASE>");
			System.exit(0);
		}
		
		LearnerType algo = LearnerType.DFA_TABLE_COLUMN;
		if(args[0].equals("lstar")) algo = LearnerType.DFA_TABLE_LSTAR;
		if(args[0].equals("kv")) algo =  LearnerType.DFA_TREE_KV;
		if(args[0].equals("tr")) algo = LearnerType.DFA_TREE_COLUMN;
		
		Alphabet input = new Alphabet();
		input.addLetter('a');
		input.addLetter('b');
		input.addLetter('c');
		
		int numCases = Integer.parseInt(args[1]);
		int numStates = Integer.parseInt(args[2]);
		int numOK = 0;
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < numCases; i ++) {
			DFA dfa = DFAGenerator.getRandomDFA(input, numStates);
			System.out.println("Case " + i );
			if(testLearnerDFA(dfa, input, algo)) {
				numOK ++;
			}
		}
		long end = System.currentTimeMillis();
        System.out.println("Algorithm: " + algo);
		System.out.println("Tested " + numCases + " cases and " + numOK + " cases passed in "
						+ ((end-start) / 1000) + " secs !");
		
	}
	
	private static boolean testLearnerDFA(DFA machine, Alphabet alphabet, LearnerType algo) {
		DFATeacherDK teacher = new DFATeacherDK(machine, alphabet);
		LearnerDFA learner = null;
		Options options = new Options();
		if(algo == LearnerType.DFA_TABLE_COLUMN) learner = new LearnerDFATableColumn(options, alphabet, teacher);
		else if(algo == LearnerType.DFA_TREE_KV) {
		    learner = new LearnerDFATreeKV(options, alphabet, teacher); 
		}else if(algo == LearnerType.DFA_TREE_COLUMN){
		    learner = new LearnerDFATreeColumn(options, alphabet, teacher); 
		}else {
		    learner = new LearnerDFATableLStar(options, alphabet, teacher); 
		}
		System.out.println("starting learning");
		learner.startLearning();

		while(true) {
			System.out.println("Table is both closed and consistent\n" + learner.toString());
			DFA model = learner.getHypothesis();
			// along with ce
			Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(model);
			boolean isEq = ceQuery.getQueryAnswer().get();
			if(isEq) {
				System.out.println(model.toString());
				break;
			}
//			HashableValue val = teacher.answerMembershipQuery(ceQuery);
//			ceQuery.answerQuery(val);
			learner.refineHypothesis(ceQuery);
		}
		
		return true;
	}

}
