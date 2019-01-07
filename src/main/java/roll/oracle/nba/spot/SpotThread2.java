package roll.oracle.nba.spot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import spotj.SpotJ;

/**
 * use spot to check inclusion of two Buchi automata
 * */
public class SpotThread2 extends Thread {
	
	// not included
	private Boolean result = null;
	
	private NBA spotA;
	private NBA spotB;
		
	private Options options;
	
	private SpotJ spot;
	
	private Pair<Word, Word> counterexample = null;
	
	public SpotThread2(NBA A, NBA B, Options options) {
		this.spotA = A;
		this.spotB = B;
		this.options = options;
		this.spot = new SpotJ();
	}
	
	public Boolean getResult() {
		return result;
	}
	
	public Pair<Word, Word> getCounterexample() {
		return counterexample;
	}
	
	@Override
	public void run() {
		
		File fileA = new File("A.hoa");
        File fileB = new File("B.hoa");
    	int numBits = UtilHelper.getNumBits(spotA.getAlphabetSize());

        try {
            Function<Integer, String> apList = x -> "a" + x;
        	NBAInclusionCheckTool.outputHOAStream(spotA, new PrintStream(new FileOutputStream(fileA)), numBits, apList);
        	NBAInclusionCheckTool.outputHOAStream(spotB, new PrintStream(new FileOutputStream(fileB)), numBits, apList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        String ceStr = spot.is_included(fileA.getAbsolutePath(), fileB.getAbsolutePath());
        Function<String, Integer> revApList = str -> Integer.parseInt(str.substring(1));
        counterexample = parse(spot, spotA.getAlphabet(), ceStr, numBits, revApList);
        if(counterexample == null) {
        	result = true;
            options.log.println("Inclusion has been proved by SPOT");
        }else {
        	result = false;
            options.log.println("A counterexample has been found by SPOT");
        	assert spotA.getAcc().accept(counterexample.getLeft(), counterexample.getRight())
        	&& ! spotB.getAcc().accept(counterexample.getLeft(), counterexample.getRight());
        }
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void interrupt() {
		super.interrupt();
		this.stop();
	}
	
	protected Pair<Word, Word> parse(
			SpotJ spot, Alphabet alphabet, String counterexample, int numAp, Function<String, Integer> apList) {
		if (counterexample.startsWith(spot.getIncluded())) {
			return null;
		}else {
			// get all set propositions
			// sc is ""
			final String sc = ":";
			int pos = counterexample.indexOf(sc);
			pos += 1;
			// cycle
			int cpos = counterexample.indexOf(spot.cycle(), pos);
			String prefixStr = counterexample.substring(pos, cpos);
			// parse prefix
			Word prefix = parse(alphabet, prefixStr, numAp, apList);
			pos = counterexample.indexOf('{');
			pos += 1;
			cpos = counterexample.indexOf('}', pos);
			String suffixStr = counterexample.substring(pos, cpos);
			// parse suffix
			Word suffix = parse(alphabet, suffixStr, numAp, apList);

			return new Pair<>(prefix, suffix);
		}
	}
	
    /**
     * parse a finite word returned from SPOT
     * */
	protected Word parse(Alphabet alphabet, String str, int numAp, Function<String, Integer> apList) {
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


}

