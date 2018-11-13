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

package roll.learner.nba.lomega.translator;

import dk.brics.automaton.Automaton;
import roll.automata.FDFA;
import roll.automata.NBA;
import roll.automata.operations.FDFAOperations;
import roll.automata.operations.NBAOperations;
import roll.learner.fdfa.LearnerFDFA;
import roll.learner.nba.lomega.UtilLOmega;
import roll.main.Options;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 *
 */
public abstract class TranslatorFDFA implements Translator {
	
	protected LearnerFDFA fdfaLearner;
	protected FDFA fdfa;
	protected dk.brics.automaton.Automaton autUVOmega;
	protected Query<HashableValue> ceQuery;
	protected boolean called ;
	protected final Options options;
	protected final Alphabet alphabet;
	
	public TranslatorFDFA(LearnerFDFA learner) {
		assert learner != null ;
		this.fdfaLearner = learner;
		this.fdfa = fdfaLearner.getHypothesis();
		this.options = learner.getOptions();
		this.alphabet = fdfa.getAlphabet();
	}
	
    @Override
    public Alphabet getAlphabet() {
        return alphabet;
    }

    @Override
    public Options getOptions() {
        return options;
    }
    
	// initialize the translator for query
	@Override
	public void setQuery(Query<HashableValue> query) {
		this.ceQuery = query;
		this.called = false;
		// get deterministic automaton for (u,v)
		Word prefix = query.getPrefix();
		Word suffix = query.getSuffix();
		assert prefix != null && suffix != null;
		autUVOmega = FDFAOperations.buildDDollar(prefix, suffix);
		autUVOmega.setDeterministic(true);
	}
	
	
	protected String getPositiveCounterExample(Automaton autDollar) {
		// get it from complement of FDFA
		Automaton dollarFDFAComplement = FDFAOperations.buildDTwo(fdfa);
		Automaton autMinus = autDollar.intersection(dollarFDFAComplement);
		assert autMinus != null;
		String ceStr = autMinus.getShortestExample(true);
		options.log.verbose("Counterexample in target: " + ceStr);
		return ceStr;
	}
	
	protected Query<HashableValue> getQuery(String counterexample, HashableValue result) {
		options.log.verbose("final counterexample for the FDFA learner: " + counterexample);
		int dollarNr = counterexample.indexOf(Alphabet.DOLLAR); //
		Word prefix = alphabet.getWordFromString(counterexample.substring(0, dollarNr));
		Word period = alphabet.getWordFromString(counterexample.substring(dollarNr + 1));
		Query<HashableValue> query = new QuerySimple<>(prefix, period);
		query.answerQuery(result);
		return query;
	}
	
	@Override
	public boolean canRefine() {
		if(! called) {
			called = true;
			return true;
		}
	    // else it must be using optimization treating eq test as the last resort
        // check whether we can still use current counter example 
        assert ceQuery != null && autUVOmega != null;
        // construct lower/upper Buechi automaton
        fdfa = fdfaLearner.getHypothesis();
        NBA nba = UtilLOmega.constructNBA(options, fdfa);
        // (u, v) is in target, not accepted then needs refine again
        boolean isCeInTarget = ceQuery.getQueryAnswer().get();
        boolean accepted = NBAOperations.accepts(nba, ceQuery.getPrefix(), ceQuery.getSuffix());
        boolean result ;
        if (isCeInTarget){
            result = ! accepted;
        }//else
        else {
            result = accepted;
        }
        
        return result;
		
	}
	/* another alternative approach for canRefine is to construct the corresponding
	 * Buechi automaton and then check whether (u, v) is accepted accordingly.
	 * This method may need less memory */


}
