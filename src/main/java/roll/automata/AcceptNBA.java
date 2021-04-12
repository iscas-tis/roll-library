/* Copyright (c) 2018 -                                                   */
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

package roll.automata;

import java.util.List;

import roll.automata.operations.NBAOperations;
import roll.automata.operations.TarjanSCCsNonrecursive;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class AcceptNBA extends AcceptNFA {

    public AcceptNBA(NFA nfa) {
        super(nfa);
    }
    
    @Override
    public boolean accept(Word prefix, Word suffix) {
        return NBAOperations.accepts((NBA)nfa, prefix, suffix);
    }
    
    @Override
    public boolean accept(Word word) {
        throw new UnsupportedOperationException("The input word is a finite word");
    }
    
    public void minimizeFinalSet() {
    	ISet inits = UtilISet.newISet();
    	inits.set(nfa.getInitialState());
    	TarjanSCCsNonrecursive tarjan = new TarjanSCCsNonrecursive((NBA)nfa, inits);
    	List<ISet> msccs = tarjan.getSCCs(); // scc that has state with self-loop or with multiple states
    	ISet mayStates = UtilISet.newISet();
    	System.out.println("The number of maximal SCC in automaton: " + msccs.size());
    	for(ISet scc : msccs) {
    		mayStates.or(scc);
    	}
    	ISet rmStates = nfa.getFinalStates();
    	for(int q : rmStates) {
    		if(!mayStates.get(q)) {
    			// remove final states that are not in an SCC
    			nfa.clearFinal(q);
    		}
    	}
    	System.out.println("The number of accepting states in automaton: " + nfa.getFinalSize());
    	// now remove those states that must reach itself via another final state
    }

}
