package roll.main.complement.algos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;

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
import roll.automata.operations.StateContainer;
import roll.automata.operations.TarjanSCCsNonrecursive;
import roll.learner.nba.ldollar.UtilNBALDollar;
import roll.main.Options;
import roll.main.complement.Complement;
import roll.main.inclusion.congr.IntBoolTriple;
import roll.main.inclusion.congr.Simulation;
import roll.parser.ba.ParserBA;
import roll.util.Timer;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class ComplementCongruence extends Complement {
	
	protected TObjectIntMap<StateCongruence> stateIndices;
	
	protected NBA result;
	protected boolean[][] fsim;
	protected boolean[][] bsim;
	boolean debug = true;
	
	public ComplementCongruence(Options options, NBA operand) {
		super(options, operand);
	}

	@Override
	protected void computeInitialState() {
		if(options.simulation) {
			StateContainer[] states = new StateContainer[operand.getStateSize()];
//			// compute the predecessors and successors
			for(int i = 0; i < operand.getStateSize(); i ++) {
				states[i] = new StateContainer(i, operand);
			}
			// initialize the information for B
			for (int i = 0; i < operand.getStateSize(); i++) {
				StateNFA st = states[i].getState();
				for (int letter = 0; letter < operand.getAlphabetSize(); letter++) {
					for (int succ : st.getSuccessors(letter)) {
						//aStates[i].addSuccessors(letter, succ);
						states[succ].addPredecessors(letter, i);
					}
				}
			}
			fsim = Simulation.computeForwardSimilation(operand, states);
			bsim = Simulation.computeBackwardSimilation(operand, states);
		}else {
			if(debug) System.out.println("State size = " + operand.getStateSize());
			fsim = new boolean[operand.getStateSize()][operand.getStateSize()];
			bsim = new boolean[operand.getStateSize()][operand.getStateSize()];
			for(int p = 0; p < operand.getStateSize(); p ++) {
				for(int q = 0; q < operand.getStateSize(); q ++) {
					if(p == q) {
						fsim[p][q] = true;
						bsim[p][q] = true;
					}else {
						fsim[p][q] = false;
						bsim[p][q] = false;
					}
				}
			}
		}
//		for(int p = 0; p < operand.getStateSize(); p ++) {
//			for(int q = 0; q < operand.getStateSize(); q ++) {
//				if(p == q) {
////					fsim[p][q] = true;
//					bsim[p][q] = true;
//				}else {
////					fsim[p][q] = false;
//					bsim[p][q] = false;
//				}
//			}
//		}
	
		stateIndices = new TObjectIntHashMap<>();
		ISet init = UtilISet.newISet();
		init.set(operand.getInitialState());
		CongruenceClass congrCls = new CongruenceClass(init, fsim, bsim);
		StateCongruence state = this.getOrAddState(congrCls);
		this.setInitial(state.getId());
	}
	
	@Override
	public NBA getResult() {
		if(result == null) {
			this.explore();
		}
		return this.result;
	}

	protected StateCongruence getOrAddState(CongruenceClass congrCls) {
		
		if(options.simulation) {
			if(debug) System.out.println("Before " + congrCls);
			congrCls.minimize();
			if(debug) System.out.println("After " + congrCls);
		}
		StateCongruence state = new StateCongruence(this, 0, congrCls);

		if (stateIndices.containsKey(state)) {
			return getStateCongr(stateIndices.get(state));
		} else {
			int index = getStateSize();
			StateCongruence newState = new StateCongruence(this, index, congrCls);
			int id = this.addState(newState);
			if (id != index) {
				throw new RuntimeException("ComplementCongruence state index error");
			}
			stateIndices.put(newState, id);
//			if (congrCls.isAccepted()) {
//				setFinal(index);
//				System.out.println("Final: " + index + " " + newState);
//			}
			return newState;
		}
	}

	public StateCongruence getStateCongr(int id) {
		return (StateCongruence) getState(id);
	}
	
	@Override
	public void explore() {
		System.out.println("Acc = " + operand.getFinalStates());
		if(debug) System.out.println("input \n" + operand.toBA());
		if(true) {
			for(int p = 0; p < operand.getStateSize(); p ++) {
				for(int q = 0; q < operand.getStateSize(); q ++) {
					if(p == q) continue;
					if(fsim[p][q]) {
						System.out.println(p + " fw-simulated by " + q);
					}
					if(bsim[p][q]) {
						System.out.println(p + " bw-simulated by " + q);
					}
					if(bsim[p][q] && fsim[p][q]) {
						System.out.println(p + " fbw-simulated by " + q);
					}
				}
			}
		}
		options.log.println("Perform subset construction for prefix classes...");
		super.explore();
		ISet inits = UtilISet.newISet();
		inits.set(this.getInitialState());
		StateCongruence initState = this.getStateCongr(this.getInitialState());
		DFACongruence leadingDFA = new DFACongruence(operand, initState.congrClass);
		// construct leading DFA from current inits
		ArrayList<DFACongruence> proDFAs = new ArrayList<>();
		ArrayList<StateCongruence> walkListArr = new ArrayList<>();
		final int numLeadingStates = this.getStateSize();
		for(int i = 0; i < numLeadingStates; i ++) {
			StateCongruence iStateCongr = this.getStateCongr(i);
			StateDFA iState = leadingDFA.getOrAddState(iStateCongr.congrClass);
			if(iState.getId() != i) {
				throw new RuntimeException("Leading DFA state index error");
			}
			TreeSet<IntBoolTriple> level = new TreeSet<>();
			for(int s : iStateCongr.congrClass.guess) {
				UtilCongruence.addTriple(level, new IntBoolTriple(s, s, false));
			}
			CongruenceClass congrCls = new CongruenceClass(level, fsim, bsim);
			proDFAs.add(new DFACongruence(operand, congrCls));
			// add this state to complement 
			StateCongruence levelState = this.getOrAddState(congrCls);
			walkListArr.add(levelState);
		}
		//copy leading DFA
//		System.out.println("leading DFA size: " + leadingDFA.getStateSize());
		for(int i = 0; i < numLeadingStates; i ++) {
			StateCongruence iState = this.getStateCongr(i);
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
			DFACongruence proDFA = proDFAs.get(i);
			proDFA.computeInitialState();
			if (sccStates.get(i)) {
				// first the transitions for initial state
				TIntIntMap map = new TIntIntHashMap();
				LinkedList<StateCongruence> list = new LinkedList<>();
				ISet visited = UtilISet.newISet();
				list.add(walkListArr.get(i));
				map.put(i, proDFA.getInitialState());
				while (!list.isEmpty()) {
					StateCongruence s = list.remove();
					if (visited.get(s.getId()))
						continue;
					visited.set(s.getId());
					int dfaId = map.get(s.getId());
					for (int letter = 0; letter < this.getAlphabetSize(); letter++) {
						for (int succ : s.getSuccessors(letter)) {
							StateCongruence succState = this.getStateCongr(succ);
							// now add to proDFA
							StateDFA succStateDFA = proDFA.getOrAddState(succState.congrClass);
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
				DFACongruence dfa = constructEmptyDFA(new DFACongruence(operand, walkListArr.get(i).congrClass));
				proDFAs.set(i, dfa);
			}else if(proDFAs.get(i).getFinalSize() <= 0) {
				// check whether there is empty states
				DFACongruence dfa = constructEmptyDFA(new DFACongruence(operand, walkListArr.get(i).congrClass));
				if(walkListArr.get(i).congrClass.level.isEmpty()) {
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
			if(debug) {
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
		if(debug) {
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
//		System.out.println(result.getStateSize() + ", " + comp.getStateSize() + ", " + minimizedNBA.getStateSize());
	}

	private DFACongruence constructEmptyDFA(DFACongruence dfa) {
		// TODO Auto-generated method stub
		dfa.computeInitialState();
		for(int letter = 0; letter < this.getAlphabetSize(); letter ++) {
			dfa.getState(0).addTransition(letter, 0);
		}
		dfa.clearFinal(0);
		return dfa;
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
		ComplementCongruence complement = new ComplementCongruence(options, A);
		complement.debug = false;
		complement.explore();
		timer.stop();
		System.out.println("Time elapsed: " + timer.getTimeElapsed());
	}

}
