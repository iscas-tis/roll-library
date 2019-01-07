package roll.oracle.nba.rabit;

import java.util.concurrent.Callable;

import automata.FiniteAutomaton;
import mainfiles.RABIT;
import roll.main.Options;
import roll.main.complement.IsIncluded;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

public class RabitThread3 implements Callable<IsIncluded>, IsIncluded {

	Boolean result = null;
	FiniteAutomaton rA;
	FiniteAutomaton rB;
	
	Alphabet alphabet;
	Options options;
	
	Pair<Word, Word> counterexample = null;
	
	public RabitThread3(Alphabet alphabet, FiniteAutomaton a, FiniteAutomaton b, Options options) {
		this.alphabet = alphabet;
		this.rA = a;
		this.rB = b;
		this.options = options;
	}
	
	@Override
	public Boolean isIncluded() {
		return result;
	}
	
	@Override
	public Pair<Word, Word> getCounterexample() {
		return counterexample;
	}
	
	@Override
	public IsIncluded call() throws Exception {
		result = RABIT.isIncluded(rA, rB);
		if(! result) {
			String prefixStr = RABIT.getPrefix();
			Word prefix = alphabet.getWordFromString(prefixStr);
	        String suffixStr = RABIT.getSuffix();
			Word suffix = alphabet.getWordFromString(suffixStr);
			counterexample = new Pair<>(prefix, suffix);
			options.log.println("A counterexmple has been found by RABIT");
		}else {
			options.log.println("Inclusion has been proved by RABIT");
		}
		return this;
	}

	

}
