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
import roll.automata.operations.FDFAOperations;
import roll.automata.operations.NBAOperations;
import roll.automata.operations.TarjanSCCsNonrecursive;
import roll.main.Options;
import roll.main.complement.Complement;
import roll.main.inclusion.congr.IntBoolTriple;
import roll.parser.ba.ParserBA;
import roll.util.Timer;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class ComplementCongruence extends Complement {
	
	protected TObjectIntMap<StateCongruence> stateIndices;
	
	protected NBA result;

	public ComplementCongruence(NBA operand) {
		super(operand);
	}

	@Override
	protected void computeInitialState() {
		stateIndices = new TObjectIntHashMap<>();
		ISet init = UtilISet.newISet();
		init.set(operand.getInitialState());
		CongruenceClass congrCls = new CongruenceClass(init);
		StateCongruence state = this.getOrAddState(congrCls);
		this.setInitial(state.getId());
	}
	
	public NBA getResult() {
		if(result == null) {
			this.explore();
		}
		return this.result;
	}

	protected StateCongruence getOrAddState(CongruenceClass congrCls) {

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
			CongruenceClass congrCls = new CongruenceClass(level);
			proDFAs.add(new DFACongruence(operand, congrCls));
			// add this state to complement 
			StateCongruence levelState = this.getOrAddState(congrCls);
			walkListArr.add(levelState);
		}
		//copy leading DFA
		System.out.println("leading DFA size: " + leadingDFA.getStateSize());
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
		System.out.println(leadingDFA.toBA());
		// we check the subset construction
		TarjanSCCsNonrecursive tarjan = new TarjanSCCsNonrecursive(this, inits);
		ISet sccStates = UtilISet.newISet();
		for(ISet scc : tarjan.getSCCs()) {
			System.out.println(" scc " + scc );
			sccStates.or(scc);
		}
		// create DFAs for each state in leading DFA
		// now traverse the level states
		LinkedList<StateNFA> walkList = new LinkedList<>();
		for(int i = 0; i < numLeadingStates; i ++) {
			if(! sccStates.get(i)) continue;
			walkList.add(walkListArr.get(i));
		}
		super.explore(walkList);
		for (int i = 0; i < leadingDFA.getStateSize(); i++) {
			DFACongruence proDFA = proDFAs.get(i);
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
				// check accepting states
				if(proDFA.getFinalSize() <= 0) {
					proDFAs.set(i, new DFACongruence(operand, walkListArr.get(i).congrClass));
				}
			}
			proDFAs.get(i).setInitial(0);
			System.out.println(proDFA.toBA());
		}
		// now we have an FDFA
		ArrayList<DFA> castDFAs = new ArrayList<>();
		for(int i = 0; i < leadingDFA.getStateSize(); i ++) {
			castDFAs.add(proDFAs.get(i));
		}
		System.out.println("leading DFA size: " + leadingDFA.getStateSize());
		FDFA fdfa = new FDFA(leadingDFA, castDFAs);
		ArrayList<String> apList = new ArrayList<>();
		apList.add("a");
		apList.add("b");
		System.out.println(fdfa.toString(apList));
		Automaton dkNBA = FDFAOperations.buildUnderNBA(fdfa);
		this.result = NBAOperations.fromDkNBA(dkNBA, getAlphabet());
		System.out.println(result.toBA());
		ComplementNcsbOtf comp = new ComplementNcsbOtf(new Options(), operand);
		comp.explore();
		System.out.println(comp.toBA());
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
		ComplementCongruence complement = new ComplementCongruence(A);
		complement.explore();
		timer.stop();
		System.out.println("Time elapsed: " + timer.getTimeElapsed());
	}

}
