/* Copyright (c) 2016, 2017                                               */
/*       Institute of Software, Chinese Academy of Sciences               */
/* This file is part of ROLL, a Regular Omega Language Learning library.  */
/* ROLL is free software: you can redistribute it and/or modify           */
/* it under the terms of the GNU General Public License as published by   */
/* the Free Software Foundation, either version 3 of the License, or      */
/* (at your option) any later version.                                    */

/* This program is distributed in the hope that it will be useful,        */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          */
/* GNU General Public License for more details.                           */

/* You should have received a copy of the GNU General Public License      */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package roll.parser.hoa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectCharMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectCharHashMap;
import jhoafparser.ast.AtomAcceptance;
import jhoafparser.ast.AtomLabel;
import jhoafparser.ast.BooleanExpression;
import jhoafparser.consumer.HOAConsumer;
import jhoafparser.consumer.HOAConsumerException;
import jhoafparser.parser.HOAFParser;
import jhoafparser.parser.generated.ParseException;
import net.sf.javabdd.BDD;
import roll.automata.NBA;
import roll.automata.operations.NBAOperations;
import roll.bdd.BDDManager;
import roll.main.Options;
import roll.parser.Parser;
import roll.parser.UtilParser;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class HOAParser implements Parser, HOAConsumer{

	// this class only allow the characters
	private Automaton automaton;
	
	// left labels
	private BDD atomRemaining = null;
	private Valuation valsRemaining = null;
	
	// new members to handle HANOI format
	private BDDManager bdd;
	// string -> index
	private APSet apset;
	// char -> valuation
	private TCharObjectMap<Valuation> charValMap = new TCharObjectHashMap<>();
	// valuation -> char
	private TObjectCharMap<Valuation> valCharMap = new TObjectCharHashMap<>();
	// int -> state
	private TIntObjectMap<State> indexStateMap = new TIntObjectHashMap<>();
	
	// we allow alias in the given HANOI file
	private Map<String, BDD> aliasBddMap = new HashMap<>();
	
	// if there are more than VAR_NUM_BOUND_TO_USE_BDD atomic propositions, then use bdd
	private final int VAR_NUM_BOUND_TO_USE_BDD = 7;
	private NBA nba;
	private final Options options;
	private final Alphabet alphabet;
	
	public HOAParser(Options options, String file) {
		this.options = options;
	    this.automaton = new Automaton();
		this.alphabet = new Alphabet();
		try {
			InputStream fileInputStream = new FileInputStream(file);
			HOAFParser.parseHOA(fileInputStream, this);
			// now check if every possible combination of AP are there
			BDD leftLabels = atomRemaining.not();
			// compute the left labels
			if(! leftLabels.isZero()) {
				BDD oneSat = leftLabels.fullSatOne();
				valsRemaining = bdd.toOneFullValuation(oneSat);
				oneSat.free();
				// add those letters which did not appear before
				getValFromAtom(valsRemaining);
			}
			atomRemaining.free();
			atomRemaining = leftLabels;
		} catch (ParseException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
//	
	@Override
	public NBA parse() {
		return nba;
	}
//
	// we do not merge transitions which share the same source and target states
	@Override
	public void print(NBA nba, OutputStream stream) {
		PrintStream out = new PrintStream(stream);
		
		if(options.dot) {
		    TIntObjectMap<String> map = new TIntObjectHashMap<>();
		    for(int letter = 0; letter < nba.getAlphabetSize(); letter ++) {
		        BDD labelDD = getBDDFromLabel(nba.getAlphabet().getLetter(letter));
		        map.put(letter, bdd.toString(labelDD));
		        labelDD.free();
		    }
		    Function<Integer, String> fun = index -> map.get(index);
	        UtilParser.print(nba, stream, fun);
		}else {
	        out.println("HOA: v1");
	        out.println("tool: \"ROLL\"");
	        out.println("properties: explicit-labels state-acc trans-labels ");
	        
	        out.println("States: " + nba.getStateSize());
	        out.println("Start: " + nba.getInitialState());
	        out.println("acc-name: Buchi");
	        out.println("Acceptance: 1 Inf(0)");
	        out.print("AP: " + apset.size());
	        for(int index = 0; index < apset.size(); index ++) {
	        	out.print(" \"" + apset.getAP(index) + "\"");
	        }
	        out.println();
	        out.println("--BODY--");
	        
	        for (int stateNr = 0; stateNr < nba.getStateSize(); stateNr ++) {
	        	out.print("State: " + stateNr);
	            if(nba.isFinal(stateNr)) out.print(" {0}");
	            out.println();
	            for (int letter = 0; letter < nba.getAlphabetSize(); letter ++) {
	                BDD labelDD = getBDDFromLabel(nba.getAlphabet().getLetter(letter));
	            	for(int succNr : nba.getSuccessors(stateNr, letter)) {
	                    out.println("[" + bdd.toString(labelDD) + "]  " + succNr);
	            	}
	            	labelDD.free();
	            }
	        }	
	        out.println("--END--");
		}

	}

	@Override
	public void close() {
	    atomRemaining.free();
		for(BDD dd : aliasBddMap.values()) {
			dd.free();
		}
		bdd.close();
	}
	
	// --------------------------- following are parts for HANOI parser
	// we reserve '$' sign for L dollar automaton
	private char getValFromAtom(Valuation label) {		
		char ch = 0;
		if(valCharMap.containsKey(label)) {
			return valCharMap.get(label);
		}
		
		ch = (char) valCharMap.size();
		if(ch >= '$' )   ch ++; // reserve '$' sign
		valCharMap.put(label, ch);
		charValMap.put(ch, label);
		alphabet.addLetter(ch);
		return ch;
	}
	
	// get the original evaluation w.r.t. the label on transition
	private BDD getBDDFromLabel(char ch) {
		Valuation valuation = charValMap.get(ch);
		if(valuation == valsRemaining) {
			return atomRemaining.id();
		}else {
			return bdd.fromValuation(valuation);
		}
	}


	@Override
	public void setNumberOfStates(int numberOfStates) throws HOAConsumerException {
		for(int stateNr = 0; stateNr < numberOfStates; stateNr ++) {
			indexStateMap.put(stateNr, new State());
		}
	}

	// adding multiple states in alternating automaton is allowed, 
    // here we do not allow this
	@Override
	public void addStartStates(List<Integer> stateConjunction) throws HOAConsumerException {
	    if(stateConjunction.size() != 1) {
	        throw new UnsupportedOperationException( "only allow one initial state one time");
	    }
		int initNr = stateConjunction.get(0);
		automaton.setInitialState(indexStateMap.get(initNr));
	}

	// allow alias for transition label
	@Override
	public void addAlias(String name, BooleanExpression<AtomLabel> labelExpr) throws HOAConsumerException {
		aliasBddMap.put(name, bdd.fromBoolExpr(labelExpr));
	}

	// initialize bdd manager from atomic proposition set
	@Override
	public void setAPs(List<String> aps) throws HOAConsumerException {
		apset = new APSet(aps);
		bdd = new BDDManager();
		bdd.setNumVar(apset.size());
		atomRemaining = bdd.getZero();
		options.log.verbose("alphabet: " + apset + " size: 2^" + apset.size());
	}


	@Override
	public void notifyBodyStart() throws HOAConsumerException {
		options.log.verbose("Start parsing body...");
	}

	@Override
	public void addState(int id, String info, BooleanExpression<AtomLabel> labelExpr, List<Integer> accSignature)
			throws HOAConsumerException {
		// only need to consider the labels of this state
		if(accSignature != null && accSignature.size() > 0) {
		    indexStateMap.get(id).setAccept(true);
		}		
	}


	// add support for alias of transitions
	@Override
	public void addEdgeWithLabel(int stateId, BooleanExpression<AtomLabel> labelExpr, List<Integer> conjSuccessors,
			List<Integer> accSignature) throws HOAConsumerException {
	    if(conjSuccessors.size() != 1) {
	        throw new UnsupportedOperationException("successor conjunction does not allowed");
	    }
	    
		assert labelExpr != null;
		
		int targetId = conjSuccessors.get(0);
//		System.out.println(labelExpr);
		BDD expr = null;
		
		if(labelExpr.getAtom() != null && labelExpr.getAtom().isAlias()) {
			expr = aliasBddMap.get(labelExpr.getAtom().getAliasName()).id();
		}else {
			expr = bdd.fromBoolExpr(labelExpr);
		}
		Set<Valuation> vals = null;
		if(apset.size() <= VAR_NUM_BOUND_TO_USE_BDD) {
			vals = bdd.toValuationSet(expr, apset.size());
		}else {
			vals = bdd.toValuationSet(expr);
		}
		// record every transition label
		atomRemaining = atomRemaining.orWith(expr);
//		System.out.println(vals);		
		addTransition(stateId, vals, targetId);
		
	}
	

    @Override
    public void notifyEnd() throws HOAConsumerException {
        nba = NBAOperations.fromDkNBA(automaton, alphabet);
//        System.out.println(automaton.toDot());
        automaton = null;
    }
	
	private void addTransition(int sourceId, Set<Valuation> vals, int targetId) {
		State source = indexStateMap.get(sourceId); 
		State target = indexStateMap.get(targetId);
        for(Valuation val : vals) {
            source.addTransition(new Transition(getValFromAtom(val), target));
        }
	}

//	
	public static void main(String []args) {
		//String file = "/home/liyong/Desktop/learning/hoa/exp1.hoa";
		String file = "/home/liyong/projects/logs-concur/buechi-complement/fairness/ltl1.hoa";
		HOAParser parser = new HOAParser(new Options(System.out), file);
		NBA nba = parser.parse();
		parser.print(nba, System.out);
	}
	
	// ------------ donot care

    @Override
    public void notifyEndOfState(int stateId) throws HOAConsumerException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean parserResolvesAliases() {
        return false;
    }

    @Override
    public void notifyHeaderStart(String version) throws HOAConsumerException {     
    }

    @Override
    public void notifyAbort() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void notifyWarning(String warning) throws HOAConsumerException {
        
    }

    @Override
    public void provideAcceptanceName(String name, List<Object> extraInfo) throws HOAConsumerException {
        // do not care
    }

    @Override
    public void setName(String name) throws HOAConsumerException {
        // do not care
    }

    @Override
    public void setTool(String name, String version) throws HOAConsumerException {
        // do not care
    }

    @Override
    public void addProperties(List<String> properties) throws HOAConsumerException {
        // do not care
    }

    @Override
    public void addMiscHeader(String name, List<Object> content) throws HOAConsumerException {
        // do not care
    }
    @Override
    public void setAcceptanceCondition(int numberOfSets, BooleanExpression<AtomAcceptance> accExpr)
            throws HOAConsumerException {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void addEdgeImplicit(int stateId, List<Integer> conjSuccessors, List<Integer> accSignature)
            throws HOAConsumerException {
        
    }

}
