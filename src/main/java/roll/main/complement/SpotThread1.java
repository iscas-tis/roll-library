package roll.main.complement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.function.Function;

import roll.automata.NBA;
import roll.automata.operations.nba.inclusion.NBAInclusionCheckTool;
import roll.main.Options;
import roll.parser.hoa.Valuation;
import roll.util.Pair;
import roll.util.UtilHelper;
import roll.words.Alphabet;
import roll.words.Word;

public class SpotThread1 extends Thread implements IsIncluded {
	Boolean result = null;
	
	NBA spotA;
	NBA spotB;
	
	Process process;
	
	boolean flag;
	
	Options options;
	
	Pair<Word, Word> counterexample;
	boolean write = false;
	boolean entered = false;
	public SpotThread1(NBA BFC, NBA B, Options options) {
		this.spotA = BFC;
		this.spotB = B;
		this.options = options;
		this.flag = true;
	}
	
	@Override
	public Boolean isIncluded() {
		return result;
	}
	
	@Override
	public void run() {
		String command = "spotj ";
		File fileA = new File("/tmp/A.hoa");
        File fileB = new File("/tmp/B.hoa");
    	int numAp = UtilHelper.getNumBits(spotB.getAlphabetSize());
    	synchronized(this) {
        	// must execute part once entered ----- start------------
        	try {
    			Function<Integer, String> apList = x -> "a" + x;
    			NBAInclusionCheckTool.outputHOAStream(spotA, new PrintStream(new FileOutputStream(fileA)), numAp, apList);
    			NBAInclusionCheckTool.outputHOAStream(spotB, new PrintStream(new FileOutputStream(fileB)), numAp, apList);
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		}
        	// must execute part once entered -----------------
    		// entered a point where this thread can be terminated
            update();
    	}
    	
        final Runtime rt = Runtime.getRuntime();
        // check whether it is included in A.hoa
        command = command + fileA.getAbsolutePath() + " " + fileB.getAbsolutePath();
        options.log.println(command);
        process = null;
        try {
        	if(flag) {
//        		System.out.println("exe...");
        		process = rt.exec(command);
        	}
//    		System.out.println("Wait...");
        	while(flag && process != null && process.isAlive()) {
        		// do nothing here
        	}
//        	System.out.println("Finished...");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        write = true;
        if(flag) {
        	final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            try {
                while (flag && (line = reader.readLine()) != null ) {
                    if (line.contains("Included.")) {
                        result = true;
                        options.log.println("Inclusion has been proved by SPOT");
                    }else if(line.contains("Not included")) {
                    	result = false;
                    	// parse result
                        options.log.println("A counterexample has been found by SPOT");
                    	counterexample = parse(spotB.getAlphabet(), line, numAp, x -> Integer.parseInt(x.substring(1)));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
//	@SuppressWarnings("deprecation")
	@Override
	public void interrupt() {
		flag = false;
//		System.out.println("interrupt" + write + ", " + entered);
		while(! write && entered) {
			// if it entered run and has not finished output yet, then wait
		}
		if(process != null) {
			process.destroyForcibly();
		}
//		System.out.println("interrupt");
		super.interrupt();
//		this.stop();
//		System.out.println("stop");
	}
	
	protected Pair<Word, Word> parse(Alphabet alphabet, String counterexample, int numAp, Function<String, Integer> apList) {
		// get all set propositions
		// sc is ""
		final String sc = ":";
		int pos = counterexample.indexOf(sc);
		pos += 1;
		// cycle
		int cpos = counterexample.indexOf("cycle", pos);
		String prefixStr = counterexample.substring(pos, cpos);
		// parse prefix
		Word prefix = parseFinite(alphabet, prefixStr, numAp, apList);
		pos = counterexample.indexOf('{');
		pos += 1;
		cpos = counterexample.indexOf('}', pos);
		String suffixStr = counterexample.substring(pos, cpos);
		// parse suffix
		Word suffix = parseFinite(alphabet, suffixStr, numAp, apList);

		return new Pair<>(prefix, suffix);
	}
	
    /**
     * parse a finite word returned from SPOT
     * */
	protected Word parseFinite(Alphabet alphabet, String str, int numAp, Function<String, Integer> apList) {
		final String sep = ";";
		final String neg = "!";
		final String and = "&";
		Word word = alphabet.getEmptyWord();
		for(String ap : str.split(sep)) {
			// a list of ands
			ap = ap.trim();
			if(ap.isEmpty()) continue;
			Valuation val = new Valuation(numAp);
			for(String lit : ap.split(and)) {
				lit = lit.trim();
				if(lit.isEmpty()) continue;
				if(! lit.contains(neg)) {
					int index = apList.apply(lit);
					val.set(index);
				}
			}
			int letter = val.toInt();
			assert letter < alphabet.getLetterSize();
			word = word.append(val.toInt());
		}
		return word;
	}

	@Override
	public Pair<Word, Word> getCounterexample() {
		return counterexample;
	}
	
	/**
	 * when updating this two variables, no other threads should update them
	 * */
	private synchronized void update() {
		this.entered = true;
		this.write = true;
	}
}
