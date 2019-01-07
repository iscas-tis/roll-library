package roll.main.complement;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import automata.FiniteAutomaton;
import roll.automata.NBA;
import roll.main.Options;
import roll.oracle.nba.rabit.RabitThread;
import roll.oracle.nba.rabit.RabitThread3;
import roll.oracle.nba.spot.SpotThread2;
import roll.oracle.nba.spot.SpotThread3;
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
			// TODO Auto-generated catch block
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
			rabitThread.interrupt();
			if(bigEnough) {
				spotThread.interrupt();
			}
//			included = getAnyOne(options, alphabet, A, B, rA, rB);
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
