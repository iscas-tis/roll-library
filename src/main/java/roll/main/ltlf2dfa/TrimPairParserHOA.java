package roll.main.ltlf2dfa;

import java.io.InputStream;
import java.util.List;

import dk.brics.automaton.Automaton;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import jhoafparser.ast.AtomAcceptance;
import jhoafparser.ast.BooleanExpression;
import jhoafparser.consumer.HOAConsumerException;
import jhoafparser.parser.HOAFParser;
import jhoafparser.parser.generated.ParseException;
import net.sf.javabdd.BDD;
import roll.automata.NBA;
import roll.automata.operations.NBAOperations;
import roll.bdd.BDDManager;
import roll.main.Options;
import roll.parser.PairParser;
import roll.parser.hoa.APSet;

public class TrimPairParserHOA extends TrimParserHOA implements PairParser {

	protected NBA A;
	protected NBA B;

	public TrimPairParserHOA(Options options, InputStream fileA, InputStream fileB) {
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
			if (!leftLabels.isZero()) {
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
	TObjectIntMap<String> propMap = new  TObjectIntHashMap<String>();
	
	@Override
	public void setAPs(List<String> aps) throws HOAConsumerException {
		if (apset == null) {
			apset = new APSet(aps);
			bdd = new BDDManager();
			bdd.setNumVar(apset.size());
			atomRemaining = bdd.getZero();
		} else {
			assert apset != null;
			if (apset.size() < aps.size()) {
				throw new UnsupportedOperationException("Alphabets not the same between A and B");
			}
			for (int i = 0; i < aps.size(); i++) {
				if (!apset.getAP(i).equals(aps.get(i))) {
					throw new UnsupportedOperationException("Alphabets not the same between A and B");
				}
			}
		}

		options.log.verbose("alphabet: " + apset + " size: 2^" + apset.size());
	}

	@Override
	public void notifyBodyStart() throws HOAConsumerException {
		options.log.verbose("Start parsing body...");
	}
//
//	// add support for alias of transitions
//	@Override
//	public void addEdgeWithLabel(int stateId, BooleanExpression<AtomLabel> labelExpr, List<Integer> conjSuccessors,
//			List<Integer> accSignature) throws HOAConsumerException {
//		if (conjSuccessors.size() != 1) {
//			throw new UnsupportedOperationException("successor conjunction does not allowed");
//		}
//
//		assert labelExpr != null;
//
//		int targetId = conjSuccessors.get(0);
////	      System.out.println(labelExpr);
//		BDD expr = null;
//
//		if (labelExpr.getAtom() != null && labelExpr.getAtom().isAlias()) {
//			expr = aliasBddMap.get(labelExpr.getAtom().getAliasName()).id();
//		} else {
//			expr = bdd.fromBoolExpr(labelExpr);
//		}
//		Set<Valuation> vals = null;
//		if (apset.size() <= VAR_NUM_BOUND_TO_USE_BDD) {
//			vals = bdd.toValuationSet(expr, apset.size());
//		} else {
//			vals = bdd.toValuationSet(expr);
//		}
//		// record every transition label
//		atomRemaining = atomRemaining.orWith(expr);
////	      System.out.println(vals);       
//		addTransition(stateId, vals, targetId);
//
//	}

	@Override
	public void notifyEnd() throws HOAConsumerException {
		nba = NBAOperations.fromDkNBA(automaton, alphabet);
//	        System.out.println(automaton.toDot());
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
