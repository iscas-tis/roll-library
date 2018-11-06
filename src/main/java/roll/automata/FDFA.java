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

import roll.automata.operations.FDFAOperations;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Family of Deterministic Finite Automata (FDFA)
 * */
public class FDFA extends FFA<DFA, DFA> {
    
    public FDFA(DFA m, List<DFA> ps) {
        super(m, ps);
        acceptance = new AcceptFDFA(this);
    }
    
    // --------------------------------------------------
    @Override
    public AutType getAccType() {
        return AutType.FDFA;
    }

    // --------------------------------------------------

    private class AcceptFDFA implements Accept {
        final FDFA fdfa;
        
        AcceptFDFA(FDFA fdfa) {
            this.fdfa = fdfa;
        }

        @Override
        public boolean accept(ISet states) {
            throw new UnsupportedOperationException("FDFA doesnot support isAccepting(ISet states)");
        }

        @Override
        public boolean accept(Word prefix, Word period) {
            if(! isNormalized(prefix, period) ) {
                Pair<Word, Word> pair = FDFAOperations.normalize(fdfa, prefix, period);
                if(pair == null) return false;
                prefix = pair.getLeft();
                period = pair.getRight();
            }
            int state = leadingFA.getSuccessor(prefix);
            DFA proDFA = getProgressFA(state);
            int proState = proDFA.getSuccessor(period);
            return proDFA.isFinal(proState);
        }

        @Override
        public boolean accept(Word word) {
            return false;
        }
    }
    
    @Override
    public boolean isNormalized(Word prefix, Word period) {
        int state = leadingFA.getSuccessor(prefix);
        int nextState = leadingFA.getSuccessor(state, period);
        return state == nextState;
    }


}
