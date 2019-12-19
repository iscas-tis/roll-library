package roll.main.ltlf2dfa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import roll.automata.NBA;
import roll.main.Options;
import roll.parser.hoa.PairParserHOA;
import roll.util.Pair;

public class UtilLTLf2DFA {

	private UtilLTLf2DFA() {

	}

	public static Pair<NBA, NBA> translateLtlf2NFA(Options options, String ltlf) {
		final Runtime rt = Runtime.getRuntime();
		Process process = null;
		// first translate LTLf to LTL
		try {
			// ltl to A
			process = rt.exec(new String[] { "ltlfilt", "--from-ltlf=" + TrimParserHOA.TAIL, "-f", ltlf });
			process.waitFor();
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		}
		// positive NBA
		InputStream streamA = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String posLtl = null;
		try {
			while ((posLtl = reader.readLine()) != null) {
				System.out.println(posLtl);
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			// ltl to B
			process = rt.exec(new String[] { "ltlfilt", "--from-ltlf=" + TrimParserHOA.TAIL, "-f", "!(" + ltlf + ")" });
			process.waitFor();
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		}

		// negative NBA
		InputStream streamB = process.getInputStream();
		reader = new BufferedReader(new InputStreamReader(streamB));
		String negLtl = null;
		try {
			while ((negLtl = reader.readLine()) != null) {
				System.out.println(negLtl);
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// translating LTL to NBA
		try {
        	// ltl to A
        	process = rt.exec(new String[]{"ltl2tgba", "-f", posLtl, "-B"});
        	process.waitFor();
        } catch (IOException | InterruptedException e1) {
            e1.printStackTrace();
        }
        // positive NBA
        streamA = process.getInputStream();
        // remove !_TAIL and states those are reached by !_TAIL
        
        try {
        	// ltl to A
        	process = rt.exec(new String[]{"ltl2tgba", "-f", negLtl, "-B"});
        	process.waitFor();
        } catch (IOException | InterruptedException e1) {
            e1.printStackTrace();
        }
        // positive NBA
        streamB = process.getInputStream();
        TrimPairParserHOA pairParser = new TrimPairParserHOA(options, streamA, streamB);
		NBA A = pairParser.getA();
		NBA B = pairParser.getB();
		options.parser = pairParser;
		System.out.println("#E = " + A.getAlphabetSize());
		pairParser.print(A, System.out);
		return new Pair<>(A, B);
	}
	
	public static void main(String[] args) {
		Options options = new Options();
		translateLtlf2NFA(options, "a U b");
	}

}
