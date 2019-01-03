package roll.main.complement;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import roll.automata.NBA;

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

}
