package roll.parser.hoa;

import jhoafparser.consumer.HOAConsumer;
import roll.parser.Parser;

public abstract class HOAParser implements Parser, HOAConsumer{

//	// this class only allow the characters
//	private FiniteAutomaton automaton;
//	
//	// left labels
//	private BDD leftAtoms = null;
//	private Valuation leftVals = null;
//	
//	// new members to handle HANOI format
//	private BDDManager bdd;
//	private APSet apset;
//	private Map<String, Valuation> atomValMap = new HashMap<>();
//	private TObjectCharMap<String> atomStrMap = new TObjectCharHashMap<>();
//	
//	private TIntObjectMap<FAState> idStateMap = new TIntObjectHashMap<>();
//	
//	// we allow alias in the given HANOI file
//	private Map<String, BDD> aliasBddMap = new HashMap<>();
//	
//	// if there are more than VAR_NUM_BOUND_TO_USE_BDD atomic propositions, then use bdd
//	private int VAR_NUM_BOUND_TO_USE_BDD = 7;
//	
//	public HOAParser(String file) {
//		automaton = new FiniteAutomaton();
//		try {
//			InputStream fileInputStream = new FileInputStream(file);
//			HOAFParser.parseHOA(fileInputStream, this);
//			
//			// now check if every possible combination of AP are there
//			BDD leftLabels = leftAtoms.not();
//			// compute the left labels
//			if(! leftLabels.isZero()) {
//				BDD oneSat = leftLabels.fullSatOne();
//				leftVals = bdd.toOneFullValuation(oneSat);
//				oneSat.free();
//				automaton.alphabet.add("" + getValFromAtom(leftVals));
//			}
//			leftAtoms.free();
//			leftAtoms = leftLabels;
//		} catch (ParseException e) {
//			e.printStackTrace();
//		} catch(FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	@Override
//	public FiniteAutomaton parse() {
//		return automaton;
//	}
//
//	// we do not merge transitions which share the same source and target states
//	@Override
//	public void print(FiniteAutomaton fa, OutputStream stream) {
//		PrintStream out = new PrintStream(stream);
//		
//		if(Options.dot) {
//	        out.println("//Buechi ");
//	        out.println("digraph {");
//	        
//	        Set<FAState> states = fa.states;
//	        for (FAState state : states) {
//	            out.print("  " + state.id + " [label=\"" +  state.id + "\"");
//	            if(fa.F.contains(state)) out.print(", shape = doublecircle");
//	            else out.print(", shape = circle");
//	            out.println("];");
//	            for (String label : fa.getAllTransitionSymbols()) {
//	            	Set<FAState> succs = state.getNext(label);
//	            	BDD labelDD = getBDDFromLabel(label);
//	            	if(succs == null) continue;
//	            	for(FAState succ : succs) {
//	            		out.println("  " + state.id + " -> " + succ.id + " [label=\"" + bdd.toString(labelDD) + "\"];");
//	            	}
//	            	labelDD.free();
//	            }
//	        }	
//	        out.println("  " + states.size() + " [label=\"\", shape = plaintext];");
//	        FAState initState = fa.getInitialState();
//	        out.println("  " + states.size() + " -> " + initState.id + " [label=\"\"];");
//	        out.println();
//	        out.println("}");
//		}else {
//	        out.println("HOA: v1");
//	        out.println("tool: \"BuechiC\"");
//	        out.println("properties: explicit-labels state-acc trans-labels ");
//	        
//	        Set<FAState> states = fa.states;
//	        out.println("States: " + states.size());
//	        out.println("Start: " + fa.getInitialState().id);
//	        out.println("acc-name: Buchi");
//	        out.println("Acceptance: 1 Inf(0)");
//	        out.print("AP: " + apset.size());
//	        for(int index = 0; index < apset.size(); index ++) {
//	        	out.print(" \"" + apset.getAP(index) + "\"");
//	        }
//	        out.println();
//	        out.println("--BODY--");
//	        
//	        for (FAState state : states) {
//	        	out.print("State: " + state.id);
//	            if(fa.F.contains(state)) out.print(" {0}");
//	            out.println();
//	            for (String label : fa.getAllTransitionSymbols()) {
//	            	Set<FAState> succs = state.getNext(label);
//	            	BDD labelDD = getBDDFromLabel(label);
//	            	if(succs == null) continue;
//	            	for(FAState succ : succs) {
//	            		out.println("[" + bdd.toString(labelDD) + "]  " + succ.id);
//	            	}
//	            	labelDD.free();
//	            }
//	        }	
//	        out.println("--END--");
//		}
//
//	}
//
//	@Override
//	public void close() {
//		// TODO Auto-generated method stub
//		leftAtoms.free();
//		for(BDD dd : aliasBddMap.values()) {
//			dd.free();
//		}
//		bdd.close();
//	}
//	
//	// --------------------------- following are parts for HANOI parser
//	// we reserve '$' sign for L dollar automaton
//	private char getValFromAtom(Valuation label) {
//		
//		String atom = label.toString();
//		char ch = 0;
//		if(atomStrMap.containsKey(atom)) {
//			return atomStrMap.get(atom);
//		}
//		
//		ch = (char) atomStrMap.size();
//		if(ch >= '$' )   ch ++; // reserve '$' sign
//		atomStrMap.put(atom, ch);
//		atomValMap.put(ch + "", label);
//		return ch;
//	}
//	
//	// get the original evaluation w.r.t. the label on transition
//	private BDD getBDDFromLabel(String label) {
//		Valuation valuation = atomValMap.get(label);
//		if(valuation == leftVals) {
//			return leftAtoms.id();
//		}else {
//			return bdd.fromValuation(valuation);
//		}
//	}
//
//	@Override
//	public boolean parserResolvesAliases() {
//		return false;
//	}
//
//	@Override
//	public void notifyHeaderStart(String version) throws HOAConsumerException {		
//	}
//
//	@Override
//	public void setNumberOfStates(int numberOfStates) throws HOAConsumerException {
//		for(int stateNr = 0; stateNr < numberOfStates; stateNr ++) {
//			idStateMap.put(stateNr, automaton.createState());
//		}
//	}
//
//	// adding multiple states in alternating automaton is allowed, which is not in this case
//	@Override
//	public void addStartStates(List<Integer> stateConjunction) throws HOAConsumerException {
//		assert stateConjunction.size() == 1 : "only allow one initial state one time";
//		int initNr = stateConjunction.get(0);
//		automaton.setInitialState(idStateMap.get(initNr));
//	}
//
//	// allow alias for transition label
//	@Override
//	public void addAlias(String name, BooleanExpression<AtomLabel> labelExpr) throws HOAConsumerException {
//		aliasBddMap.put(name, bdd.fromBoolExpr(labelExpr));
//	}
//
//	// initialize bdd manager from atomic proposition set
//	@Override
//	public void setAPs(List<String> aps) throws HOAConsumerException {
//		apset = new APSet(aps);
//		bdd = new BDDManager();
//		bdd.setNumVar(apset.size());
//		leftAtoms = bdd.getZero();
//		Options.log.verbose("alphabet: " + apset + " size: " + apset.size());
//	}
//
//	@Override
//	public void setAcceptanceCondition(int numberOfSets, BooleanExpression<AtomAcceptance> accExpr)
//			throws HOAConsumerException {
//		 // by default this is Buchi acceptance conditions
//	}
//
//	@Override
//	public void provideAcceptanceName(String name, List<Object> extraInfo) throws HOAConsumerException {
//		// do not care
//	}
//
//	@Override
//	public void setName(String name) throws HOAConsumerException {
//		// do not care
//	}
//
//	@Override
//	public void setTool(String name, String version) throws HOAConsumerException {
//		// do not care
//	}
//
//	@Override
//	public void addProperties(List<String> properties) throws HOAConsumerException {
//		// do not care
//	}
//
//	@Override
//	public void addMiscHeader(String name, List<Object> content) throws HOAConsumerException {
//		// do not care
//	}
//
//	@Override
//	public void notifyBodyStart() throws HOAConsumerException {
//		Options.log.verbose("Start parsing body...");
//	}
//
//	@Override
//	public void addState(int id, String info, BooleanExpression<AtomLabel> labelExpr, List<Integer> accSignature)
//			throws HOAConsumerException {
//		// only need to consider the labels of this state
//		if(accSignature != null && accSignature.size() > 0) {
//			automaton.F.add(idStateMap.get(id));
//		}		
//	}
//
//	@Override
//	public void addEdgeImplicit(int stateId, List<Integer> conjSuccessors, List<Integer> accSignature)
//			throws HOAConsumerException {
//		
//	}
//
//	// add support for alias of transitions
//	@Override
//	public void addEdgeWithLabel(int stateId, BooleanExpression<AtomLabel> labelExpr, List<Integer> conjSuccessors,
//			List<Integer> accSignature) throws HOAConsumerException {
//		assert conjSuccessors.size() == 1: "only allow one successors";
//		assert labelExpr != null;
//		
//		if(conjSuccessors.size() != 1) {
//			Options.log.err("successor conjunction does not allowed");
//			System.exit(-1);
//		}
//		
//		int targetId = conjSuccessors.get(0);
////		System.out.println(labelExpr);
//		BDD expr = null;
//		
//		if(labelExpr.getAtom() != null && labelExpr.getAtom().isAlias()) {
//			expr = aliasBddMap.get(labelExpr.getAtom().getAliasName()).id();
//		}else {
//			expr = bdd.fromBoolExpr(labelExpr);
//		}
//		Set<Valuation> vals = null;
//		if(apset.size() <= VAR_NUM_BOUND_TO_USE_BDD) {
//			vals = bdd.toValuationSet(expr, apset.size());
//		}else {
//			vals = bdd.toValuationSet(expr);
//		}
//		// record every transition label
//		leftAtoms = leftAtoms.orWith(expr);
////		System.out.println(vals);		
//		addTransition(stateId, vals, targetId);
//		
//	}
//	
//	private void addTransition(int sourceId, Set<Valuation> vals, int targetId) {
//		FAState source = idStateMap.get(sourceId); 
//		FAState target = idStateMap.get(targetId);
//        for(Valuation val : vals) {
//        	automaton.addTransition(source, target, "" + getValFromAtom(val));
//        }
//	}
//
//	@Override
//	public void notifyEndOfState(int stateId) throws HOAConsumerException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void notifyEnd() throws HOAConsumerException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void notifyAbort() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void notifyWarning(String warning) throws HOAConsumerException {
//		
//	}
//	
//	public static void main(String []args) {
//		//String file = "/home/liyong/Desktop/learning/hoa/exp1.hoa";
//		String file = "/home/liyong/Desktop/sdbw/hoa/exp1.hoa";
//		HanoiParser parser = new HanoiParser(file);
//		parser.print(parser.getAutomaton(), System.out);
//	}

}
