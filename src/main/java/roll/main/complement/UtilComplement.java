package roll.main.complement;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import automata.FiniteAutomaton;
import roll.automata.NBA;
import roll.main.Options;
import roll.oracle.nba.rabit.RabitThread;
import roll.oracle.nba.spot.SpotThread2;
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
		final int size = 45;
		IsIncluded included = null;
		if(options.parallel) {
			boolean bigEnough = B.getStateSize() + A.getStateSize() > size;
			SpotThread2 spotThread = new SpotThread2(A, B, options);
			RabitThread rabitThread = new RabitThread(alphabet, rA, rB, options);
			if(bigEnough) {
				spotThread.start();
			}
			rabitThread.start();
			while(true) {
				if(bigEnough && !spotThread.isAlive()) {
					included = spotThread;
					break;
				}
				if(! rabitThread.isAlive()) {
					included = rabitThread;
					break;
				}
			}
			if(bigEnough) {
				spotThread.interrupt();
			}
			rabitThread.interrupt();
		}else {
			Thread thread = null;
			if(options.spot) {
				SpotThread2 spotThread = new SpotThread2(A, B, options);
				included = spotThread;
				thread = spotThread;
			}else {
				RabitThread rabitThread = new RabitThread(alphabet, rA, rB, options);
				included = rabitThread;
				thread = rabitThread;
			}
			thread.start();
			while(thread.isAlive()) {
				// do nothing but wait
			}
			thread.interrupt();
		}
		
		return included;
	}

}
