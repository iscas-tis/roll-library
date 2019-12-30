package roll.main.complement;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import automata.FAState;
import automata.FiniteAutomaton;
import mainfiles.RABIT;
import roll.automata.NBA;
import roll.automata.operations.NBAOperations;
import roll.main.Options;
import roll.oracle.nba.rabit.RabitThread;
import roll.oracle.nba.rabit.RabitThread3;
import roll.oracle.nba.spot.SpotThread3;
import roll.query.Query;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

public class UtilComplement {
	
	/**
	 * help to print NBA file in BA format for debugging
	 * */
	public static void print(NBA nba, String file) {
		try {
			FileOutputStream stream = new FileOutputStream(file);
			PrintStream out = new PrintStream(stream);
			out.print(nba.toBA());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static Pair<Word, Word> getCounterexample(Alphabet alphabet, List<String> prefix, List<String> suffix) {
        int[] pre = new int[prefix.size()];
        for(int i = 0; i < pre.length; i ++) {
            pre[i] = alphabet.indexOf(prefix.get(i).charAt(0));
        }
        int[] suf = new int[suffix.size()];
        for(int i = 0; i < suf.length; i ++) {
            suf[i] = alphabet.indexOf(suffix.get(i).charAt(0));
        }
        return new Pair<>(alphabet.getArrayWord(pre), alphabet.getArrayWord(suf));
    }
	
	public static boolean answerMembershipQuery(NBA B, Query<HashableValue> query) {
        boolean result;
        Word prefix = query.getPrefix();
        Word suffix = query.getSuffix();
        
        if(suffix.isEmpty()) {
            return false;
        }else {
            result = NBAOperations.accepts(B, prefix, suffix);
        }
        return result;
	}
	
	public static void print(Alphabet alphabet, FiniteAutomaton nba, String file) {
		try {
			FileOutputStream stream = new FileOutputStream(file);
			PrintStream out = new PrintStream(stream);
			Set<FAState> states = nba.states;
			out.print("[" + nba.getInitialState().id + "]");
	        for (FAState state : states) {
	            for (String label : nba.getAllTransitionSymbols()) {
	            	Set<FAState> succs = state.getNext(label);
	            	if(succs == null) continue;
	            	for(FAState succ : succs) {
	            		out.print("\na" + (alphabet.indexOf(label.charAt(0))) + ",[" + state.id + "]->[" + succ.id + "]");
	            	}
	            }
	        }	
	        for(FAState state : nba.F) {
				out.print("\n[" + state.id + "]");
	        }
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static IsIncluded checkInclusion(Options options
			, Alphabet alphabet, NBA A, NBA B, FiniteAutomaton rA, FiniteAutomaton rB) {
		IsIncluded included = null;
		if(options.parallel) {
			final int size = 45;
			boolean bigEnough = A.getStateSize() + B.getStateSize() > size;
			SpotThread1 spotThread = null;
			if(bigEnough) {
				spotThread = new SpotThread1(A, B, options);
			}
			RabitThread rabitThread = new RabitThread(alphabet, rA, rB, options);
			if(bigEnough) {
				spotThread.start();
			}			
			rabitThread.start();
			while(true) {
				if(bigEnough && ! spotThread.isAlive()) {
					included = spotThread;
					break;
				}
				if(! rabitThread.isAlive()) {
					included = rabitThread;
					break;
				}
			}
//			System.out.println("Hi " + (bigEnough ? spotThread.isAlive() : "") + ", " + rabitThread.isAlive());
			assert spotThread.result != null || rabitThread.result != null;
			if(bigEnough && !spotThread.isAlive() && !rabitThread.isAlive()) {
				assert spotThread.result == rabitThread.result;
			}
			rabitThread.interrupt();
			if(bigEnough) {
				spotThread.interrupt();
			}
		}else {
			Callable<IsIncluded> caller = null;
			if(options.spot) {
				SpotThread3 spotThread = new SpotThread3(A, B, options);
				caller = spotThread;
			}else {
				RabitThread3 rabitThread = new RabitThread3(alphabet, rA, rB, options);
				caller = rabitThread;
			}
			try {
				included = caller.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
//			if( ! options.spot && options.verbose >= 2) {
//				caller = new SpotThread3(A, B, options);
//				try {
//					IsIncluded spot = caller.call();
//					if(spot.isIncluded() != included.isIncluded()) {
//						print(A, "A.ba");
//						print(B, "B.ba");
//						print(rA, "A1.ba");
//						print(rB, "B1.ba");
//						options.log.err("Wrong inclusion proof for RABIT");
//						options.log.err(included.isIncluded() + "");
////						options.log.err("result = " + RABIT.isIncluded(rA, rB));
//						String[] args = new String[] {"A1.ba", "B1.ba", "-fastc"};
//						RABIT.main(args);
//						System.exit(-1);
//					}
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
		}
		
		return included;
	}
	
	private static IsIncluded getAnyOne(Options options
			, Alphabet alphabet, NBA A, NBA B, FiniteAutomaton rA, FiniteAutomaton rB) {
		final int size = 45;
		boolean bigEnough = A.getStateSize() + B.getStateSize() > size;
		SpotThread3 spotThread = null;
		if(bigEnough) {
			spotThread = new SpotThread3(A, B, options);
		}
		RabitThread3 rabitThread = new RabitThread3(alphabet, rA, rB, options);
		ExecutorService executor = Executors.newWorkStealingPool();
		List<Callable<IsIncluded>> callables = new ArrayList<>(2);
		callables.add(rabitThread);
		if(bigEnough) {
			callables.add(spotThread);
		}
//		Runtime rt = Runtime.getRuntime();
//		Process process = rt.exec(cmdarray);
		
		IsIncluded result = null;
		try {
			result = executor.invokeAny(callables);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return result;
	}

}
