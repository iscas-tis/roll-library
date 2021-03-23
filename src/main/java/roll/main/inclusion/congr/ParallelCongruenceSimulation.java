package roll.main.inclusion.congr;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import automata.FiniteAutomaton;
import roll.automata.NBA;
import roll.automata.operations.NBALasso;
import roll.main.Options;
import roll.main.complement.IsIncluded;
import roll.main.inclusion.UtilInclusion;
import roll.parser.ba.PairParserBA;
import roll.util.Pair;
import roll.util.Timer;
import roll.util.sets.ISet;
import roll.words.Word;

public class ParallelCongruenceSimulation implements IsIncluded {
	
	NBA A;
	NBA B;
	
	Pair<Word, Word> counterexample;
	Boolean result;
	boolean terminate = false;
	
	int numWorkers;
	
	
	public ParallelCongruenceSimulation(NBA A, NBA B, int numWorks) {
		this.A = A;
		this.B = B;
		this.numWorkers = numWorks;
	}
	
	@Override
	public Pair<Word, Word> getCounterexample() {
		return counterexample;
	} 
	
	@Override
	public Boolean isIncluded() {
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
		System.out.println("There are " + nbas.length + " small NBAs");
		for(int j = 0; j < nbas.length; j ++) {
			NBA nba = nbas[j];
			InclusionCheck checker = new InclusionCheck(nba, B);
			threads.add(checker);
		}
		ExecutorService executor = Executors.newFixedThreadPool(numWorkers);//Executors.newWorkStealingPool(numCores);
		for(int j = 0; j < threads.size(); j ++) {
			executor.execute(threads.get(j));
			System.out.println("Create thread " + j);
		}
		executor.shutdown();
		System.out.println("Entering while loop ......");
		while (true) {
			// create a thread to look up
			if(executor.isTerminated()) {
				System.out.println("executor terminate ?");
				break;
			}
			if(terminate) {
				executor.shutdownNow();
				System.out.println("Terminate ?");
				break;
			}
		}
		
		if(this.result != null) {
			return this.result;
		}
		return true;
	}
	
	class InclusionCheck extends Thread {
		
		NBA first;
		NBA second;
				
		InclusionCheck(NBA fst, NBA snd) {
			this.first = fst;
			this.second = snd;
		}
		
		@Override
		public void run() {
			try {
				synchronized(this) {
					if(result != null && !result) {
						return;
					}
				}
				CongruenceSimulation sim = new CongruenceSimulation(first, second);
				// light processing 
//				FiniteAutomaton faA = UtilInclusion.toRABITNBA(first);
//				FiniteAutomaton faB = UtilInclusion.toRABITNBA(second);
//		        System.out.println("Before minimization: #A.St=" + faA.states.size() + ", #A.Tr=" + faA.trans);
//		        System.out.println("Before minimization: #B.St=" + faB.states.size() + ", #B.Tr=" + faB.trans);
//				System.out.println("Light proceesing for NBA ");
//				// now do light processing
//		        Pair<Boolean, Pair<FiniteAutomaton, FiniteAutomaton>> pair = UtilInclusion.lightPrepocess(faA, faB);
//		        if(pair.getLeft()) {
//			        System.out.println("Light processing finished the task.");
//		        	return;
//		        }
//		        // else do a bit more processing
//		        faA = pair.getRight().getLeft();
//		        faB = pair.getRight().getRight();
//				System.out.println("Proceesing for NBA ");
//		        pair = UtilInclusion.prepocess(faA, faB);
//		        if(pair.getLeft()) {
//			        System.out.println("Processing finished the task.");
//		        	return;
//		        }
//		        System.out.println("Processing did not finish the task.");
//		        faA = pair.getRight().getLeft();
//		        faB = pair.getRight().getRight();
//		        NBA fst = UtilInclusion.toNBA(faA, A.getAlphabet());
//		        NBA snd = UtilInclusion.toNBA(faB, B.getAlphabet());
//		        System.out.println("After minimization: #A.St=" + faA.states.size() + ", #A.Tr=" + faA.trans);
//		        System.out.println("After minimization: #B.St=" + faB.states.size() + ", #B.Tr=" + faB.trans);
//
//				CongruenceSimulation sim = new CongruenceSimulation(fst, snd);
				sim.antichain = true;
				sim.computeCounterexample = true;
				sim.useSimulation = true;
				boolean included = sim.isIncluded();
//				System.out.println(included ? "Included" : "Not included");
				if(!included) {
					writeCounterexample(sim.getCounterexample());
//					System.out.println(" result = " + result);
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		public synchronized void writeCounterexample(Pair<Word, Word> ce) {
			if(result != null && !result) {
				return ;
			}
			counterexample = ce;
			result = false;
			terminate = true;
			System.out.println("Visited" + " " + terminate);
		}
		
	}
	
	
	
	public static void main(String[] args) {
		Options options = new Options();
		PairParserBA pairParser = new PairParserBA(options, args[0], args[1]);
		NBA A = pairParser.getA();
		NBA B = pairParser.getB();
		System.out.println("#A = " + A.getStateSize() + ", #B = " + B.getStateSize());
		Timer timer = new Timer();
		timer.start();
		int numCores = Runtime.getRuntime().availableProcessors();
		System.out.println("NumCores = " + numCores);
		ParallelCongruenceSimulation sim = new ParallelCongruenceSimulation(A, B, 4);
		boolean included = sim.isIncluded();
		System.out.println(included ? "Included" : "Not included");
		if(!included) {
				Pair<Word, Word> counterexample = sim.getCounterexample();
				NBALasso lasso = new NBALasso(counterexample.getLeft(), counterexample.getRight());
				pairParser.print(lasso.getNBA(), options.log.getOutputStream());
		}
		timer.stop();
		System.out.println("Total time elapsed " + timer.getTimeElapsed());
	}

}
