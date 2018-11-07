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
import roll.learner.dfa.tree.LearnerDFATreeKV;
import roll.main.Options;
import roll.oracle.dfa.dk.TeacherDFADK;
import roll.query.Query;
import roll.table.HashableValue;
import roll.words.Alphabet;

public class DFARandomTest {
	
	public static void main(String[] args) {
		
		if(args.length < 3) {
			System.out.println("Usage: <PROGRAM> <tab|kv|tr|lstar> <NUM_OF_CASES> <NUM_OF_STATES_FOR_CASE>");
			System.exit(0);
		}
		
		LearnerType algo = LearnerType.DFA_COLUMN_TABLE;
		if(args[0].equals("lstar")) algo = LearnerType.DFA_LSTAR;
		if(args[0].equals("kv")) algo =  LearnerType.DFA_KV;
		if(args[0].equals("tr")) algo = LearnerType.DFA_COLUMN_TREE;
		
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
			if(testLearnerDFA2(dfa, input, algo)) {
				numOK ++;
			}
		}
		long end = System.currentTimeMillis();
        System.out.println("Algorithm: " + algo);
		System.out.println("Tested " + numCases + " cases and " + numOK + " cases passed in "
						+ ((end-start) / 1000) + " secs !");
		
	}
	
//	private static boolean testLearnerDFA(DFA machine, Alphabet alphabet, LearnerType algo) {
//	    Options options = new Options();
//		TeacherDFADK teacher = new TeacherDFADK(options, machine);
//		LearnerDFA learner = null;
//		
//		if(algo == LearnerType.DFA_COLUMN_TABLE) learner = new LearnerDFATableColumn(options, alphabet, teacher);
//		else if(algo == LearnerType.DFA_KV) {
//		    learner = new LearnerDFATreeKV(options, alphabet, teacher); 
//		}else if(algo == LearnerType.DFA_COLUMN_TREE){
//		    learner = new LearnerDFATreeColumn(options, alphabet, teacher); 
//		}else {
//		    learner = new LearnerDFATableLStar(options, alphabet, teacher); 
//		}
//		System.out.println("starting learning");
//		learner.startLearning();
//
//		while(true) {
//			System.out.println("Table is both closed and consistent\n" + learner.toString());
//			DFA model = learner.getHypothesis();
//			// along with ce
//			Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(model);
//			boolean isEq = ceQuery.getQueryAnswer().get();
//			if(isEq) {
//				System.out.println(model.toString());
//				break;
//			}
//			ceQuery.answerQuery(null);
////			HashableValue val = teacher.answerMembershipQuery(ceQuery);
////			ceQuery.answerQuery(val);
//			learner.refineHypothesis(ceQuery);
//		}
//		
//		return true;
//	}
	
	   private static boolean testLearnerDFA2(DFA machine, Alphabet alphabet, LearnerType algo) {
	        Options options = new Options();
	        TeacherDFADK teacher = new TeacherDFADK(options, machine);
	        LearnerDFA learner = null;
	        System.out.println("target:\n" + machine.toDot());
	        learner = new LearnerDFATreeKV(options, alphabet, teacher);
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
	            ceQuery.answerQuery(null);
//	          HashableValue val = teacher.answerMembershipQuery(ceQuery);
//	          ceQuery.answerQuery(val);
	            learner.refineHypothesis(ceQuery);
	        }
	        
	        return true;
	    }


}
