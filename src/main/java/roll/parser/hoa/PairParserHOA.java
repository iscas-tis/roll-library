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
import java.util.List;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import jhoafparser.ast.AtomAcceptance;
import jhoafparser.ast.AtomLabel;
import jhoafparser.ast.BooleanExpression;
import jhoafparser.consumer.HOAConsumerException;
import jhoafparser.parser.HOAFParser;
import jhoafparser.parser.generated.ParseException;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDPairing;
import roll.automata.NBA;
import roll.automata.operations.NBAOperations;
import roll.bdd.BDDManager;
import roll.main.Options;
import roll.parser.PairParser;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * parsing a pair of automata
 * 
 * */
public class PairParserHOA extends ParserHOA implements PairParser {
    protected NBA A;
    protected NBA B;
    BDDPairing apB2A = null;
    
    public PairParserHOA(Options options, String fileA, String fileB) {
        super(options);
        try {
            InputStream fileInputStream = new FileInputStream(fileA);
            this.automaton = new Automaton();
            HOAFParser.parseHOA(fileInputStream, this);
            this.A = nba;
            fileInputStream = new FileInputStream(fileB);
            this.indexStateMap.clear();
            this.aliasBddMap.clear();
            this.automaton = new Automaton();
            this.apB2A = this.bdd.makeBDDPair();
            this.initialAdded = false;
            HOAFParser.parseHOA(fileInputStream, this);
            this.B = nba;
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
    
    public PairParserHOA(Options options, InputStream fileA, InputStream fileB) {
        super(options);
        try {
            InputStream fileInputStream = fileA;
            this.automaton = new Automaton();
            HOAFParser.parseHOA(fileInputStream, this);
            this.A = nba;
            fileInputStream = fileB;
            this.indexStateMap.clear();
            this.aliasBddMap.clear();
            this.automaton = new Automaton();
            this.initialAdded = false;
            HOAFParser.parseHOA(fileInputStream, this);
            this.B = nba;
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
        }
    }
//  
    @Override
    public NBA parse() {
        return nba;
    }
    
    @Override
    public NBA getA() {
        return A;
    }
    
    @Override
    public NBA getB() {
        return B;
    }
    
    // --------------------------- following are parts for HANOI parser
    // initialize bdd manager from atomic proposition set
    @Override
    public void setAPs(List<String> aps) throws HOAConsumerException {
        if(apset == null) {
            apset = new APSet(aps);
            bdd = new BDDManager();
            bdd.setNumVar(apset.size());
            atomRemaining = bdd.getZero();
        }else {
            assert apset != null ;
            if(apset.size() != aps.size()) {
                throw new UnsupportedOperationException("Alphabets not the same between A and B");
            }
            for(int i = 0; i < aps.size(); i ++) {
            	int apIndex = apset.indexOf(aps.get(i));
                if(apIndex < 0) {
                    throw new UnsupportedOperationException("Alphabets not the same between A and B");
                }else {
                	// index of B mapped to index of A
                	apB2A.set(i, apIndex);
                }
            }
        }
        
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
//      System.out.println(labelExpr);
        BDD expr = null;
        
        if(labelExpr.getAtom() != null && labelExpr.getAtom().isAlias()) {
            expr = aliasBddMap.get(labelExpr.getAtom().getAliasName()).id();
        }else {
            expr = bdd.fromBoolExpr(labelExpr);
        }
        // replace bdd values of B with those of A
        if(apB2A != null) {
        	expr = expr.replaceWith(apB2A);
        }
        Set<Valuation> vals = null;
        if(apset.size() <= VAR_NUM_BOUND_TO_USE_BDD) {
            vals = bdd.toValuationSet(expr, apset.size());
        }else {
            vals = bdd.toValuationSet(expr);
        }
        // record every transition label
        atomRemaining = atomRemaining.orWith(expr);
//      System.out.println(vals);       
        addTransition(stateId, vals, targetId);
        
    }
    

    @Override
    public void notifyEnd() throws HOAConsumerException {
        nba = NBAOperations.fromDkNBA(automaton, alphabet);
//        System.out.println(automaton.toDot());
        automaton = null;
    }
    
    protected void addTransition(int sourceId, Set<Valuation> vals, int targetId) {
        State source = indexStateMap.get(sourceId); 
        State target = indexStateMap.get(targetId);
        for(Valuation val : vals) {
            source.addTransition(new Transition(getValFromAtom(val), target));
        }
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
