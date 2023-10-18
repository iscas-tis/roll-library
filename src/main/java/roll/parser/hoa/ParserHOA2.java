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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import gnu.trove.iterator.TIntObjectIterator;
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
import roll.util.Pair;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * only for single file parsing
 * 
 * */
public class ParserHOA2 implements Parser, HOAConsumer{

    // this class only allow the characters
    protected Automaton automaton;
    
    // left labels
    protected BDD atomRemaining = null;
    
    // new members to handle HANOI format
    protected BDDManager bdd;
    // string -> index
    protected APSet apset;
    // char -> valuation
    protected TCharObjectMap<BDD> charValMap = new TCharObjectHashMap<>();
    // valuation -> char
    protected TObjectCharMap<BDD> valCharMap = new TObjectCharHashMap<>();
    // int -> state
    protected TIntObjectMap<State> indexStateMap = new TIntObjectHashMap<>();
    
    // we allow alias in the given HANOI file
    protected Map<String, BDD> aliasBddMap = new HashMap<>();
    
    // if there are more than VAR_NUM_BOUND_TO_USE_BDD atomic propositions, then use bdd
//    protected final int VAR_NUM_BOUND_TO_USE_BDD = 7;
    protected NBA nba;
    protected final Options options;
    protected final Alphabet alphabet;
    protected final GraphBDD graphBDD;
    protected LinkedList<BDD> letters;
    
    protected boolean initialAdded = false;
    
    protected boolean pairParsing = false;
    
    public ParserHOA2(Options options, String file) {
        this.options = options;
        this.automaton = new Automaton();
        this.alphabet = new Alphabet();
        this.graphBDD = new GraphBDD();
        this.letters = new LinkedList<BDD>();
        
        try {
            InputStream fileInputStream = new FileInputStream(file);
            HOAFParser.parseHOA(fileInputStream, this);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public int getNumLetters()
    {
    	return letters.size();
    }
    
    protected ParserHOA2(Options options) {
        this.options = options;
        this.alphabet = new Alphabet();
        this.graphBDD = new GraphBDD();
        this.letters = new LinkedList<BDD>();
    }

    @Override
    public NBA parse() {
        return nba;
    }

    // we do not merge transitions which share the same source and target states
    @Override
    public void print(NBA nba, OutputStream stream) {
        PrintStream out = new PrintStream(stream);
        
        if(options.dot) {
            TIntObjectMap<String> map = new TIntObjectHashMap<>();
            for(int letter = 0; letter < nba.getAlphabetSize(); letter ++) {
                if(nba.getAlphabet().indexOf(Alphabet.DOLLAR) == letter) continue;
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
                    if(nba.getAlphabet().indexOf(Alphabet.DOLLAR) == letter) continue;
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
//        atomRemaining.free();
        for(BDD dd : aliasBddMap.values()) {
            dd.free();
        }
        // maps using the same copy of BDDs as letters
        for (BDD l : letters)
        {
        	l.free();
        }
        graphBDD.free();
        bdd.close();
    }
    
    // --------------------------- following are parts for HANOI parser
    // we reserve '$' sign for L dollar automaton
    protected char getValFromAtom(BDD label) {      
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
    protected BDD getBDDFromLabel(char ch) {
        return charValMap.get(ch).id();
    }


    @Override
    public void setNumberOfStates(int numberOfStates) throws HOAConsumerException {
    	// sometimes, this function will not be called
        for(int stateNr = 0; stateNr < numberOfStates; stateNr ++) {
        	getOrAddState(stateNr);
        }
    }
    
    private State getOrAddState(int stateNr)
    {
    	if (! indexStateMap.containsKey(stateNr))
    	{
    		indexStateMap.put(stateNr, new State());
    	}
    	return indexStateMap.get(stateNr);
    }

    // adding multiple states in alternating automaton is allowed, 
    // here we do not allow this
    @Override
    public void addStartStates(List<Integer> stateConjunction) throws HOAConsumerException {
    	if(initialAdded) {
    		throw new UnsupportedOperationException( "only allow one initial state and addStartStates has already been called");
    	}
        if(stateConjunction.size() != 1) {
            throw new UnsupportedOperationException( "only allow one initial state one time");
        }
        initialAdded = true;
        int initNr = stateConjunction.get(0);
        graphBDD.setInitial(initNr);
        automaton.setInitialState(getOrAddState(initNr));
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
        options.log.verbose("Number of APs is " + apset.size());
        if(apset.size() <= 0){
        	options.log.println("[Warning]: added \"dummy\" proposition as original alphabet is empty");
        	apset.addAP("dummy");
        }
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
        	graphBDD.setFinal(id);
        	getOrAddState(id).setAccept(true);
        }       
    }


    // add support for alias of transitions
    @Override
    public void addEdgeWithLabel(int stateId, BooleanExpression<AtomLabel> labelExpr, List<Integer> conjSuccessors,
            List<Integer> accSignature) throws HOAConsumerException {
        if(conjSuccessors.size() != 1) {
            throw new UnsupportedOperationException("Successor conjunction is not allowed");
        }
        if (accSignature != null && accSignature.size() > 0) {
        	throw new UnsupportedOperationException("Transition-based acceptance is not allowed");
        }
        
        assert labelExpr != null;
        
        int targetId = conjSuccessors.get(0);
//      System.out.println(labelExpr);
        BDD expr = null;
        
        if(labelExpr.getAtom() != null && labelExpr.getAtom().isAlias()) {
            expr = aliasBddMap.get(labelExpr.getAtom().getAliasName()).id();
        }else {
            expr = bdd.fromBoolExpr(labelExpr);
        }
        // add conditions
        letters.addLast(expr);
//        Set<Valuation> vals = null;
//        if(apset.size() <= VAR_NUM_BOUND_TO_USE_BDD) {
//            vals = bdd.toValuationSet(expr, apset.size());
//        }else {
//            vals = bdd.toValuationSet(expr);
//        }
        // need to store this transition
        // record every transition label
        atomRemaining = atomRemaining.orWith(expr.id());
//      System.out.println(vals);       
        //addTransition(stateId, vals, targetId);
        graphBDD.addTransition(stateId, expr.id(), targetId);   
    }
    
    protected void freeBDDs(LinkedList<BDD> l)
    {
    	for (BDD b : l)
    	{
    		b.free();
    	}
    }
    
    protected void partitionLetters()
    {
    	// now we compute the BDDs
    	LinkedList<BDD> partitions = new LinkedList<>();
    	for (BDD b : letters)
    	{
    		BDD curr = b.id();
    		if (partitions.isEmpty())
    		{	
    			partitions.addLast(curr);
    		}else
    		{
    			// traverse partitions to add curr
    			LinkedList<BDD> tmp = new LinkedList<>();
    			for (BDD c : partitions)
    			{
    				BDD t = c.and(curr);
    				if (! t.isZero())
    				{
    					tmp.addLast(t);
    					if (! c.equals(t))
    					{
    						tmp.addLast(c.id().andWith(t.not()));
    					}
    					curr = curr.andWith(c.not());
    				}else if (! c.isZero())
    				{
    					tmp.addLast(c.id());
    				}
    			}
    			if (! curr.isZero())
    			{
    				tmp.addLast(curr);
    			}
    			// copy tmp
    			freeBDDs(partitions);
    			partitions = tmp;
    		}	
    	}
    	freeBDDs(letters);
    	letters = partitions;
    }
    
    protected void computeDkNBA()
    {
    	// initial and final states should be ready
    	TIntObjectIterator<ArrayList<Pair<BDD, Integer>>> iter = graphBDD.transitions.iterator();
    	while(iter.hasNext())
    	{
    		iter.advance();
    		int src = iter.key();
    		State source = getOrAddState(src);
    		for (Pair<BDD, Integer> edge : iter.value())
    		{
    	        State target = getOrAddState(edge.getRight());
    	        for (BDD l : letters)
    	        {
//    	    		System.out.println("Src= " + src + " Dst = " + edge.getRight() + " cond: " + bdd.toString(edge.getLeft())
//    	    		+ " letter=" + bdd.toString(l) + "\n");

    	        	BDD implies = l.id().andWith(edge.getLeft().not());
    	        	if (implies.isZero())
    	        	{
    	        		source.addTransition(new Transition(getValFromAtom(l), target));
    	        	}
    	        	implies.free();
    	        }
    		}
    	}
    }
    @Override
    public void notifyEnd() throws HOAConsumerException {
  
    	// now we have all the letters
    	partitionLetters();
    	computeDkNBA();
    	// now check if every possible combination of AP are there
        BDD leftLabels = atomRemaining.not();
        // compute the left labels
        if(! leftLabels.isZero()) {
            // add those letters which did not appear before
            getValFromAtom(leftLabels);
            letters.addLast(leftLabels);
        }
        nba = NBAOperations.fromDkNBA(automaton, alphabet);
//        System.out.println(automaton.toDot());
        automaton = null;
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
    	if(! name.contains("Buchi")) {
    		throw new UnsupportedOperationException("Current acc-name is not Buchi: " + name);
    	}
    		
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
    
    public static void main(String[] args)
    {
    	String fileName = "/home/liyong/vscode/CAV/COLA/split-automata-le/A.hoa";
    	Options options = new Options();
    	ParserHOA2 parser = new ParserHOA2(options, fileName);
    	NBA input = parser.parse();
    	try {
			parser.print(input, new FileOutputStream("/home/liyong/vscode/CAV/COLA/split-automata-le/A-copy.hoa"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("Size: " + parser.getNumLetters());
    	
    }

}
