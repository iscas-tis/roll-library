package roll.main.complement.algos;

import java.util.ArrayList;
import java.util.LinkedList;

import dk.brics.automaton.Automaton;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import roll.automata.AcceptNBA;
import roll.automata.DFA;
import roll.automata.FDFA;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.DFAOperations;
import roll.automata.operations.FDFAOperations;
import roll.automata.operations.NBAOperations;
import roll.automata.operations.TarjanSCCsNonrecursive;
import roll.learner.nba.ldollar.UtilNBALDollar;
import roll.main.Options;
import roll.main.complement.Complement;
import roll.parser.ba.ParserBA;
import roll.util.Timer;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class ComplementCongruenceOpt extends Complement {
	
	protected TObjectIntMap<StateCongruenceOpt> stateIndices;
	
	protected NBA result;
	boolean debug = true;
	
	public ComplementCongruenceOpt(Options options, NBA operand) {
		super(options, operand);
	}

	@Override
	protected void computeInitialState() {
		stateIndices = new TObjectIntHashMap<>();
		ISet init = UtilISet.newISet();
		init.set(operand.getInitialState());
		CongruenceClassOpt congrCls = new CongruenceClassOpt(false);
		congrCls.addSet(init);
		StateCongruenceOpt state = this.getOrAddState(congrCls);
		this.setInitial(state.getId());
	}
	
	@Override
	public NBA getResult() {
		if(result == null) {
			this.explore();
		}
		return this.result;
	}

	protected StateCongruenceOpt getOrAddState(CongruenceClassOpt congrCls) {
		
		StateCongruenceOpt state = new StateCongruenceOpt(this, 0, congrCls);

		if (stateIndices.containsKey(state)) {
			return getStateCongr(stateIndices.get(state));
		} else {
			int index = getStateSize();
			StateCongruenceOpt newState = new StateCongruenceOpt(this, index, congrCls);
			int id = this.addState(newState);
			if (id != index) {
				throw new RuntimeException("ComplementCongruenceOpt state index error");
			}
			stateIndices.put(newState, id);
			return newState;
		}
	}

	public StateCongruenceOpt getStateCongr(int id) {
		return (StateCongruenceOpt) getState(id);
	}
	
	@Override
	public void explore() {
//		System.out.println("Acc = " + operand.getFinalStates());
		if(debug) System.out.println("input \n" + operand.toBA());
		options.log.println("Perform subset construction for prefix classes...");
		super.explore();
		ISet inits = UtilISet.newISet();
		inits.set(this.getInitialState());
		StateCongruenceOpt initState = this.getStateCongr(this.getInitialState());
		DFACongruenceOpt leadingDFA = new DFACongruenceOpt(operand, initState.congrClass);
		// construct leading DFA from current inits
		ArrayList<DFACongruenceOpt> proDFAs = new ArrayList<>();
		ArrayList<StateCongruenceOpt> walkListArr = new ArrayList<>();
		final int numLeadingStates = this.getStateSize();
		for(int i = 0; i < numLeadingStates; i ++) {
			StateCongruenceOpt iStateCongr = this.getStateCongr(i);
			StateDFAOpt iState = leadingDFA.getOrAddState(iStateCongr.congrClass);
			if(iState.getId() != i) {
				throw new RuntimeException("Leading DFA state index error");
			}
			CongruenceClassOpt proCls = new CongruenceClassOpt(true);
			for(int j = 0; j < iStateCongr.congrClass.getSetSize(); j ++) {
				proCls.addSet(iStateCongr.congrClass.getSet(j));
				proCls.setMaxPres(j, false);
			}
			proDFAs.add(new DFACongruenceOpt(operand, proCls));
			// add this state to complement 
			StateCongruenceOpt levelState = this.getOrAddState(proCls);
			walkListArr.add(levelState);
		}
		//copy leading DFA
//		System.out.println("leading DFA size: " + leadingDFA.getStateSize());
		for(int i = 0; i < numLeadingStates; i ++) {
			StateCongruenceOpt iState = this.getStateCongr(i);
			for(int letter = 0; letter < this.getAlphabetSize(); letter ++) {
				for(int j : iState.getSuccessors(letter)) {
					// for now, ignore the level states
					if(j >= numLeadingStates) continue;
					leadingDFA.getState(i).addTransition(letter, j);
				}
			}
		}
		leadingDFA.setInitial(getInitialState());
//		System.out.println(leadingDFA.toBA());
		// we check the subset construction
		TarjanSCCsNonrecursive tarjan = new TarjanSCCsNonrecursive(this, inits);
		ISet sccStates = UtilISet.newISet();
		for(ISet scc : tarjan.getSCCs()) {
			if(debug) System.out.println(" scc " + scc );
			sccStates.or(scc);
		}
		// create DFAs for each state in leading DFA
		// now traverse the level states
		LinkedList<StateNFA> walkList = new LinkedList<>();
		for(int i = 0; i < numLeadingStates; i ++) {
			if(! sccStates.get(i)) continue;
			walkList.add(walkListArr.get(i));
		}
		options.log.println("There are " + numLeadingStates + " states in the leading DFA...");
		options.stats.numOfStatesInLeading = numLeadingStates;
		options.log.println("Exploring state space for period classes...");
		super.explore(walkList);
		options.log.println("Explored " + this.getStateSize() + " states for constructing FDFAs...");
		if(debug) {
			System.out.println("Leading DFA: ");
			System.out.println(leadingDFA.toBA());
		}
		ArrayList<DFA> castDFAs = new ArrayList<>();
		for (int i = 0; i < leadingDFA.getStateSize(); i++) {
			DFACongruenceOpt proDFA = proDFAs.get(i);
			proDFA.computeInitialState();
			if (sccStates.get(i)) {
				// first the transitions for initial state
				TIntIntMap map = new TIntIntHashMap();
				LinkedList<StateCongruenceOpt> list = new LinkedList<>();
				ISet visited = UtilISet.newISet();
				list.add(walkListArr.get(i));
				map.put(i, proDFA.getInitialState());
				while (!list.isEmpty()) {
					StateCongruenceOpt s = list.remove();
					if (visited.get(s.getId()))
						continue;
					visited.set(s.getId());
					int dfaId = map.get(s.getId());
					for (int letter = 0; letter < this.getAlphabetSize(); letter++) {
						for (int succ : s.getSuccessors(letter)) {
							StateCongruenceOpt succState = this.getStateCongr(succ);
							// now add to proDFA
							StateDFAOpt succStateDFA = proDFA.getOrAddState(succState.congrClass);
							proDFA.getState(dfaId).addTransition(letter, succStateDFA.getId());
							map.put(succState.getId(), succStateDFA.getId());
							if (!visited.get(succ)) {
								list.addFirst(succState);
							}
						}
					}
				}
			}
			if(!sccStates.get(i)) {
				DFACongruenceOpt dfa = constructEmptyDFA(new DFACongruenceOpt(operand, walkListArr.get(i).congrClass));
				proDFAs.set(i, dfa);
			}else if(proDFAs.get(i).getFinalSize() <= 0) {
				// check whether there is empty states
				DFACongruenceOpt dfa = constructEmptyDFA(new DFACongruenceOpt(operand, walkListArr.get(i).congrClass));
				if(walkListArr.get(i).congrClass.getSetSize() <= 0) {
					// this state is empty
					dfa.setFinal(0);
//					System.out.println("CongrClass " + walkListArr.get(i).congrClass.level);
				}
				proDFAs.set(i, dfa);
			}
			// we can minimize the automaton
			DFA castDFA = null;
			if(options.minimization) {
				Automaton dkDFA = DFAOperations.toDkDFA(proDFAs.get(i));
				options.log.println("Minimizing the corresponding progress DFAs...");
				dkDFA.minimize();
				castDFA = DFAOperations.fromDkDFA(getAlphabet(), dkDFA);
			}else {
				castDFA = proDFAs.get(i);
			}
			castDFAs.add(i, castDFA);
			if(false) {
				System.out.println("proDFA " + i);
				System.out.println(castDFA.toBA());
			}
			options.stats.numOfStatesInProgress.add(castDFA.getStateSize());
			options.log.println("There are " + castDFA.getStateSize() + " states in the progress DFA " + i + " ...");
			//proDFAs.get(i).setInitial(0);
//			System.out.println("proDRFA: " + i + "\n" + proDFAs.get(i).toBA());
		}
//		System.out.println("leading DFA size: " + leadingDFA.getStateSize() + " progress Size " + castDFAs.size() );
		FDFA fdfa = new FDFA(leadingDFA, castDFAs);
		if(false) {
			ArrayList<String> apList = new ArrayList<>();
			for(int c = 0; c < this.getAlphabetSize(); c ++) {
				apList.add("a" + c);
			}
			System.out.println(fdfa.toString(apList));
		}

//		Automaton dkNBA = FDFAOperations.buildUnderNBA(fdfa);
//		this.result = NBAOperations.fromDkNBA(dkNBA, getAlphabet());
//		System.out.println(result.toBA());
//		Options ops = new Options();
//		ops.lazyB = true;
//		ops.lazyS = false;
//		ComplementNcsbOtf comp = new ComplementNcsbOtf(options, operand);
//		comp.explore();
//		System.out.println(comp.toBA());
		Automaton dkNBA = FDFAOperations.buildDOne(fdfa);
		options.log.println("Minimizing the corresponding family of DFAs...");
		dkNBA.minimize();
		if(debug) System.out.println("acc state size " + dkNBA.getAcceptStates().size());
		dkNBA = UtilNBALDollar.dkDFAToBuchi(dkNBA);
		this.result = NBAOperations.fromDkNBA(dkNBA, getAlphabet());
		if(debug) System.out.println(result.toBA());
	}

	private DFACongruenceOpt constructEmptyDFA(DFACongruenceOpt dfaCongruenceOpt) {
		// TODO Auto-generated method stub
		dfaCongruenceOpt.computeInitialState();
		for(int letter = 0; letter < this.getAlphabetSize(); letter ++) {
			dfaCongruenceOpt.getState(0).addTransition(letter, 0);
		}
		dfaCongruenceOpt.clearFinal(0);
		return dfaCongruenceOpt;
	}

	public static void main(String[] args) {
		Options options = new Options();
		ParserBA pairParser = new ParserBA(options, args[0]);
		
		NBA A = pairParser.parse();
		System.out.println("#A = " + A.getStateSize());
		System.out.println("#AF = " + A.getFinalSize());
		AcceptNBA acc = (AcceptNBA) A.getAcc();
		acc.minimizeFinalSet();
		System.out.println("#AF = " + A.getFinalSize());
		Timer timer = new Timer();
		timer.start();
		options.simulation = false;
		options.verbose = 1;
		ComplementCongruenceOpt complement = new ComplementCongruenceOpt(options, A);
		complement.debug = true;
		complement.explore();
		timer.stop();
		System.out.println("Time elapsed: " + timer.getTimeElapsed());
	}

}
