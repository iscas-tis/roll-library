package roll.main.inclusion.congr;

import java.util.ArrayList;

import roll.automata.NBA;
import roll.automata.operations.NBALasso;
import roll.main.Options;
import roll.parser.ba.PairParserBA;
import roll.util.Pair;
import roll.util.Timer;
import roll.util.sets.ISet;
import roll.words.Word;

public class ParallelCongruenceSimulation {
	
	NBA A;
	NBA B;
	
	Pair<Word, Word> ce;
	Boolean result;
	protected boolean terminate = false;
	
	public ParallelCongruenceSimulation(NBA A, NBA B) {
		this.A = A;
		this.B = B;
	}
	
	public Pair<Word, Word> getCounterexample() {
		return ce;
	} 
	
	public boolean isIncluded() {
		
		ISet aFinals = A.getFinalStates();
		NBA[] nbas = new NBA[aFinals.cardinality()];
		int i = 0;
		for(int finalState : aFinals) {
			nbas[i] = new NBA(A.getAlphabet());
			// copy NBA
			for(int s = 0; s < A.getStateSize(); s ++) {
				nbas[i].createState();
			}
			for(int s = 0; s < A.getStateSize(); s ++) {
				for(int a : A.getState(s).getEnabledLetters()) {
					for(int t : A.getState(s).getSuccessors(a)) {
						nbas[i].getState(s).addTransition(a, t);
					}
				}
			}
			nbas[i].setInitial(A.getInitialState());
			nbas[i].setFinal(finalState);
			i ++;
		}
		// run parallelly for checking inclusion
		ArrayList<InclusionCheck> threads = new ArrayList<>();
		for(int j = 0; j < nbas.length; j ++) {
			NBA nba = nbas[j];
			InclusionCheck checker = new InclusionCheck(nba, B);
			threads.add(checker);
			checker.start();
		}
		boolean isIncluded = true;
		while (! terminate) {
			// create a thread to look up
			boolean oneAlive = false;
			for(int j = 0; j < threads.size(); j ++) {
				if(threads.get(j).isAlive()) {
					oneAlive = true;
				}
			}
			if(! oneAlive) {
				break;
			}
		}
		
		System.out.println("Hello");
		for(int j = 0; j < threads.size(); j ++) {
			if(threads.get(j).isAlive()) {
				threads.get(j).interrupt();
			}
		}
		if(this.result != null) {
			return this.result;
		}
		return isIncluded;
	}
	
	class InclusionCheck extends Thread {
		
		NBA first;
		NBA second;
		
//		boolean stopping;
		
		InclusionCheck(NBA fst, NBA snd) {
			this.first = fst;
			this.second = snd;
		}
		
		@Override
		public void run() {
			try {
				CongruenceSimulation sim = new CongruenceSimulation(first, second);
				sim.antichain = true;
				sim.computeCounterexample = true;
				boolean included = sim.isIncluded();
				System.out.println(included ? "Included" : "Not included");
				if(!included) {
					write(sim.getCounterexample());
					System.out.println(" result = " + result);
				}
			} catch (Exception e) {
				System.out.println("Interrupted");
			}
		}
		
		public synchronized void write(Pair<Word, Word> counterexample) {
			ce = counterexample;
			result = false;
			terminate = true;
			System.out.println("Visited" + " " + terminate);
		}
		
//		public synchronized void shutdown() {
//		    stopping = true;
//		    this.notifyAll();
//		}

//		public synchronized boolean isStopping() {
//		    return stopping;
//		}
		
	}
	
	
	
	public static void main(String[] args) {
		Options options = new Options();
		PairParserBA pairParser = new PairParserBA(options, args[0], args[1]);
		NBA A = pairParser.getA();
		NBA B = pairParser.getB();
		System.out.println("#A = " + A.getStateSize() + ", #B = " + B.getStateSize());
		Timer timer = new Timer();
		timer.start();
		
		ParallelCongruenceSimulation sim = new ParallelCongruenceSimulation(A, B);
		boolean included = sim.isIncluded();
		System.out.println(included ? "Included" : "Not included");
		if(!included) {
			Pair<Word, Word> counterexample = sim.getCounterexample();
			NBALasso lasso = new NBALasso(counterexample.getLeft(), counterexample.getRight());
			pairParser.print(lasso.getNBA(), options.log.getOutputStream());
		}
		timer.stop();
		System.out.println("Time elapsed " + timer.getTimeElapsed());
	}

}
