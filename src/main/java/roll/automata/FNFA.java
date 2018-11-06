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

import roll.automata.operations.FNFAOperations;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Family of Nodeterministic Finite Automata
 * */
public class FNFA extends FFA<NFA, DFA> {
    
    public FNFA(NFA m, List<DFA> ps) {
        super(m, ps);
        acceptance = new AcceptFNFA(this);
    }
    
    // -------------------------------------------------------
    @Override
    public AutType getAccType() {
        return AutType.FNFA;
    }
    
    private class AcceptFNFA implements Accept {
        final FNFA fnfa;
        
        AcceptFNFA(FNFA fdfa) {
            this.fnfa = fdfa;
        }

        @Override
        public boolean accept(ISet states) {
            throw new UnsupportedOperationException("FNFA doesnot support isAccepting(ISet states)");
        }

        @Override
        public boolean accept(Word prefix, Word period) {
            if(!isNormalized(prefix, period)) {
                Pair<Word, Word> pair = FNFAOperations.normalize(fnfa, prefix, period);
                prefix = pair.getLeft();
                period = pair.getRight();
            }
            NFA nfa = fnfa.getLeadingFA();
            for(int state : nfa.getSuccessors(period)) {
                NFA proNFA = getProgressFA(state);
                boolean found = proNFA.getAcc().accept(period);
                if(found) return true;
            }
            return false;
        }
        
    }


}
