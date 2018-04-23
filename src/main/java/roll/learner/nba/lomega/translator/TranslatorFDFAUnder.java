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
import roll.automata.operations.FDFAOperations;
import roll.learner.fdfa.LearnerFDFA;
import roll.query.Query;
import roll.table.HashableValue;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 *
 */
public class TranslatorFDFAUnder extends TranslatorFDFA {
	
	public TranslatorFDFAUnder(LearnerFDFA learner) {
		super(learner);
	}

	@Override
	public Query<HashableValue> translate() {
	    // every time we initialize fdfa, in case it is modified
	    fdfa = fdfaLearner.getHypothesis();
		String counterexample = translateLower();
		return getQuery(counterexample, ceQuery.getQueryAnswer());
	}
	
	// -------- this is for lower BA construction ----------------
	private String translateLower() {
		options.log.verbose(autUVOmega.toDot());
		boolean isCeInTarget = ceQuery.getQueryAnswer().get();
		
		String ceStr = null;
		if(isCeInTarget) {
			// positive Counterexample, (u, v) is in target, not in constructed
			// BA, but possibly it is in FDFA, , already normalized
			ceStr = getPositiveCounterExample(autUVOmega);
		}else {
			// negative Counterexample, (u, v) is not in target, but in FDFA
			// get intersection, already normalized.
			Automaton dollarFDFA = FDFAOperations.buildDOne(fdfa);
			Automaton autInter = autUVOmega.intersection(dollarFDFA);
			assert autInter != null;
			ceStr = autInter.getShortestExample(true);
			options.log.verbose("Not in target: " + ceStr);
		}
		
		return ceStr;
	}

}
