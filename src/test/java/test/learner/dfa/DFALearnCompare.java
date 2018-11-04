package test.learner.dfa;

import roll.automata.DFA;
import roll.automata.operations.DFAGenerator;
import roll.learner.Learner;
import roll.learner.dfa.table.LearnerDFATableColumn;
import roll.learner.dfa.tree.LearnerDFATreeColumn;
import roll.main.Options;
import roll.oracle.dfa.dk.TeacherDFADK;
import roll.query.Query;
import roll.table.HashableValue;
import roll.words.Alphabet;
import roll.words.Word;

public class DFALearnCompare {
	
	private static int numMemTable = 0;
	private static int numMemTree = 0;
	private static int numEqTable = 0;
	private static int numEqTree = 0;
	private static long timeEqTable = 0;
	private static long timeEqTree = 0;
	
	private static long testLearnerDFA(DFA dfa, Options options, Alphabet alphabet, boolean table) {
	    TeacherDFADK teacher = new TeacherDFADK(options, dfa);
		Learner<DFA, HashableValue> learner = null;
		if(table) learner = new LearnerDFATableColumn(options, alphabet, teacher);
		else learner = new LearnerDFATreeColumn(options, alphabet, teacher);
		
		long time = System.currentTimeMillis();
		System.out.println("starting learning");
		learner.startLearning();
        
		while(true) {
//			System.out.println("Table is both closed and consistent\n" + learner.toString());
			DFA model = learner.getHypothesis();
			// along with ce
			long eq = System.currentTimeMillis();
			Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(model);
			eq = System.currentTimeMillis() - eq;
			if(table) {
			    timeEqTable += eq;
	        }else {
	            timeEqTree += eq;
	        }
			boolean isEq = ceQuery.getQueryAnswer().get();
			final boolean lazyeq = true;
			if(isEq) {
//				System.out.println(model.toString());
				break;
			}
			HashableValue val = teacher.answerMembershipQuery(ceQuery);
			int i = 0;
			do {
			     i ++;
		         Word word = ceQuery.getQueriedWord();
		         ceQuery.answerQuery(val);		         
		         learner.refineHypothesis(ceQuery);
		         model = learner.getHypothesis();
		         boolean accepted = model.getAcc().accept(word, word.getEmptyWord());
		         if(val.isAccepting() == accepted) {
                     System.out.println("Get out after " + i + " times");
                     break;
                 }
			}while(lazyeq);

		}
		
		time = System.currentTimeMillis() - time;
		if(table) {
			numMemTable += options.stats.numOfMembershipQuery;
			numEqTable += options.stats.numOfEquivalenceQuery;
		}else {
			numMemTree += options.stats.numOfMembershipQuery;
			numEqTree += options.stats.numOfEquivalenceQuery;
		}
		System.out.println("finished learning");
		return time;
	}
	
	public static void main(String[] args) {
		
		int numCases = Integer.parseInt(args[0]);
		int numStates = Integer.parseInt(args[1]);
		
		final int apSize = 40;
		long timeTable = 0;
		long timeTree = 0;
		
		int n = 0;
		
		for(int k = apSize; k <= apSize; k ++) {
			for(int i = 0; i < numCases; i ++) {
				n ++;
				Alphabet alphabet = new Alphabet();
				char[] as = {'a', 'b', 'c', 'd', 'e'};
				for(int c = 0; c < apSize; c ++) {
					alphabet.addLetter((char)c);
				}
				Options options = new Options();
				DFA dfa = DFAGenerator.getRandomDFA(alphabet, numStates);
				timeTable += testLearnerDFA(dfa, options, alphabet, true);
				timeTree += testLearnerDFA(dfa, options, alphabet, false);
				System.out.println("Done for case " + n);
			}
			
		}
		System.out.println("numCases = " + n);
		System.out.println("table=" + (timeTable / 1000) + " tree=" + (timeTree / 1000));
		System.out.println("table tLR=" + (timeTable - timeEqTable) + " tree tLR=" + (timeTree - timeEqTree));
		System.out.println("table tEQ=" + timeEqTable + " tree tEQ=" + timeEqTree);
		System.out.println("table MQ=" + numMemTable + " tree MQ=" + numMemTree);
		System.out.println("table EQ=" + numEqTable + " tree EQ=" + numEqTree);
		
	}
	
	

}
