package roll.oracle.nba.rabit;

import automata.FiniteAutomaton;
import mainfiles.RABIT;
import roll.main.Options;
import roll.main.complement.IsIncluded;
import roll.main.complement.UtilComplement;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

public class RabitThread extends Thread implements IsIncluded {
	
	public Boolean result = null;
	FiniteAutomaton rA;
	FiniteAutomaton rB;
	
	Alphabet alphabet;
	Options options;
	
	Pair<Word, Word> counterexample = null;
	
	public RabitThread(Alphabet alphabet, FiniteAutomaton a, FiniteAutomaton b, Options options) {
		this.alphabet = alphabet;
		this.rA = a;
		this.rB = b;
		this.options = options;
	}
	
	@Override
	public synchronized void run() {
		// note that RABIT may change this two automata
		result = RABIT.isIncluded(rA, rB);
		if(! result) {
			String prefixStr = RABIT.getPrefix();
			Word prefix = alphabet.getWordFromString(prefixStr);
	        String suffixStr = RABIT.getSuffix();
			Word suffix = alphabet.getWordFromString(suffixStr);
			counterexample = new Pair<>(prefix, suffix);
			options.log.println("A counterexmple has been found by RABIT");
		}else if(result){
//			options.log.println("HHIHH");
			options.log.println("Inclusion has been proved by RABIT");
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void interrupt() {
//		System.out.println("RABIT interrupt");
		super.interrupt();
		this.stop();
	}
	@Override
	public Boolean isIncluded() {
		return result;
	}
	@Override
	public Pair<Word, Word> getCounterexample() {
		return counterexample;
	}

}
