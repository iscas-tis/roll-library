package roll.learner.sdfa;

import roll.automata.SDFA;
import roll.learner.LearnerType;
import roll.learner.dfa.table.LearnerDFATable;

import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.words.Alphabet;

public class LearnerSDFATable extends LearnerDFATable {
	
	public LearnerSDFATable(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
	}

	@Override
	public LearnerType getLearnerType() {
		return LearnerType.SDFA_TABLE;
	}

	@Override
	protected void createConjecture() {
    	hypothesis = new SDFA(alphabet);
    }

	@Override
	protected CeAnalyzer getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
        return new CeAnalyzerTable(exprValue, result);
	}

}
