package roll.main.inclusion.congr;

import java.util.concurrent.Callable;

import roll.automata.NBA;
import roll.main.Options;
import roll.main.complement.IsIncluded;
import roll.util.Pair;
import roll.words.Word;

public class CongrThread implements Callable<IsIncluded>, IsIncluded {

	Boolean result = null;
	NBA A;
	NBA B;
	
	Options options;
	
	Pair<Word, Word> counterexample = null;
	
	public CongrThread(NBA fst, NBA snd, Options options) {
		this.A = fst;
		this.B = snd;
		this.options = options;
	}
	
	@Override
	public Boolean isIncluded() {
		return this.result;
	}

	@Override
	public Pair<Word, Word> getCounterexample() {
		return this.counterexample;
	}

	@Override
	public IsIncluded call() throws Exception {
		
		IsIncluded congr = null;
		if(options.parallel) {
			congr = new ParallelCongruenceSimulation(A, B, options.numWorkers);
		}else {
			congr = new CongruenceSimulation(A, B);
		}
		result = congr.isIncluded();
		if(! result) {
			counterexample = congr.getCounterexample();
			options.log.println("A counterexmple has been found by CONGR");
		}else {
			options.log.println("Inclusion has been proved by CONGR");
		}
		return this;
	}

}
