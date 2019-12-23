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
			process = rt.exec(new String[] { "/bin/sh", "-c", "ltlfilt --from-ltlf=_TAIL -f "+ "\"" + ltlf + "\"" 
			+ " |   ltl2tgba |   autfilt --remove-ap=_TAIL -B"});
			process.waitFor();
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		}
		// positive NBA
		InputStream streamA = process.getInputStream();
		try {
			// ltl to B
			process = rt.exec(new String[] { "/bin/sh", "-c", "ltlfilt --from-ltlf=_TAIL -f "+ "\"!(" + ltlf + ")\"" 
					+ " |   ltl2tgba |   autfilt --remove-ap=_TAIL -B"});
			process.waitFor();
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		}
		// negative NBA
		InputStream streamB = process.getInputStream();
        PairParserHOA pairParser = new PairParserHOA(options, streamA, streamB);
		NBA A = pairParser.getA();
		NBA B = pairParser.getB();
		options.parser = pairParser;
		System.out.println("#E = " + A.getAlphabetSize());
		pairParser.print(A, System.out);
		pairParser.print(B, System.out);
		return new Pair<>(A, B);
	}
	
	public static void main(String[] args) {
		Options options = new Options();
		translateLtlf2NFA(options, "a U b");
		System.out.println("-------------------- HELLO ---------------");
		final Runtime rt = Runtime.getRuntime();
		Process process = null;
		// first translate LTLf to LTL
		try {
			// ltl to A
			process = rt.exec(new String[] { "/bin/sh", "-c", "ltlfilt --from-ltlf -f \"(a U b) & Fc\" |   ltl2tgba |   autfilt --remove-ap=alive -B"});
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
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		try {
			// ltl to A
			process = rt.exec(new String[] { "/bin/sh", "-c", "ltlfilt --from-ltlf -f \"!(!(G F p1 & G F p2 & G F p3 & G F p4 & G F p5 ) | G(! q | F r))\" |   ltl2tgba |   autfilt --remove-ap=alive -B"});
			process.waitFor();
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		}
		// positive NBA
		streamA = process.getInputStream();
		reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		try {
			while ((posLtl = reader.readLine()) != null) {
				System.out.println(posLtl);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
