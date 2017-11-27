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

package roll.automata;

import java.util.List;

import roll.util.sets.ISet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class FDFA implements Acceptor {
    
    private final DFA leadingDFA;
    private final List<DFA> progressDFAs;
    private final Acc acceptance;
    
    public FDFA(DFA m, List<DFA> ps) {
        leadingDFA = m;
        progressDFAs = ps;
        acceptance = new AccFDFA();
    }
    
    public DFA getLeadingDFA() {
        return leadingDFA;
    }
    
    public DFA getProgressDFA(int state) {
        assert state >= 0 && state < progressDFAs.size(); 
        return progressDFAs.get(state);
    }

    @Override
    public AccType getAccType() {
        return AccType.FDFA;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // todo
        return sb.toString();
    }

    @Override
    public Acc getAcc() {
        return acceptance;
    }
    
    private class AccFDFA implements Acc {

        @Override
        public boolean isAccepting(ISet states) {
            throw new UnsupportedOperationException("FDFA doesnot support isAccepting(ISet states)");
        }

        @Override
        public boolean isAccepting(Word prefix, Word suffix) {
            assert isNormalized(prefix, suffix);
            int state = leadingDFA.getSuccessor(prefix);
            DFA proDFA = getProgressDFA(state);
            int proState = proDFA.getSuccessor(suffix);
            return proDFA.isFinal(proState);
        }
        
    }
    
    public boolean isNormalized(Word stem, Word loop) {
        int state = leadingDFA.getSuccessor(stem);
        int nextState = leadingDFA.getSuccessor(state, loop);
        return state == nextState;
    }

}
