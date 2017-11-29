package roll.parser;

import java.io.OutputStream;

import roll.automata.NBA;

public interface Parser {

	NBA parse();
	
	void print(NBA fa, OutputStream out);
	
	void close();
}