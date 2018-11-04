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

import java.util.ArrayList;
import java.util.List;

import roll.automata.operations.FDFAOperations;
import roll.jupyter.NativeTool;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Family of Deterministic Finite Automata (FDFA)
 * */
public class FDFA implements Acceptor {
    
    private final DFA leadingDFA;
    private final List<DFA> progressDFAs;
    private final Accept acceptance;
    private final Alphabet alphabet;
    
    public FDFA(DFA m, List<DFA> ps) {
        assert m != null && ps != null;
        alphabet = m.getAlphabet();
        leadingDFA = m;
        progressDFAs = ps;
        acceptance = new AcceptFDFA(this);
    }
    
    // --------------------------------------------------
    public DFA getLeadingDFA() {
        return leadingDFA;
    }
    
    public DFA getProgressDFA(int state) {
        assert state >= 0 && state < progressDFAs.size(); 
        return progressDFAs.get(state);
    }

    @Override
    public Alphabet getAlphabet() {
        return alphabet;
    }

    @Override
    public AutType getAccType() {
        return AutType.FDFA;
    }
    
    @Override
    public Accept getAcc() {
        return acceptance;
    }
    
 
    // --------------------------------------------------
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("//FDFA-M: \n" + leadingDFA.toString() + "\n");
        for(int i = 0; i < progressDFAs.size(); i ++) {
            builder.append("//FDFA-P" + i + ": \n" + progressDFAs.get(i).toString());
        }
        return builder.toString();
    }
    
    @Override
    public String toString(List<String> apList) {
        StringBuilder builder = new StringBuilder();
        builder.append("//FDFA-M: \n" + leadingDFA.toString(apList) + "\n");
        for(int i = 0; i < progressDFAs.size(); i ++) {
            builder.append("//FDFA-P" + i + ": \n" + progressDFAs.get(i).toString(apList));
        }
        return builder.toString();
    }
    
    @Override
    public String toHTML() {
        StringBuilder builder = new StringBuilder();
        List<String> apList = new ArrayList<>();
        for(int i = 0; i < alphabet.getLetterSize(); i ++) {
            apList.add("" + alphabet.getLetter(i));
        }
        builder.append("<p> Leading DFA M :  </p> <br> "    
                     + NativeTool.dot2SVG(leadingDFA.toString(apList)));
        for(int i = 0; i < progressDFAs.size(); i ++) {
            builder.append("<p> Progress DFA for " + i + ": </p> <br>"
                    + NativeTool.dot2SVG(progressDFAs.get(i).toString(apList)) + "<br>");
        }
        return builder.toString();
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
            int state = leadingDFA.getSuccessor(prefix);
            DFA proDFA = getProgressDFA(state);
            int proState = proDFA.getSuccessor(period);
            return proDFA.isFinal(proState);
        }

        @Override
        public boolean accept(Word word) {
            return false;
        }
        
    }
    
    public boolean isNormalized(Word stem, Word loop) {
        int state = leadingDFA.getSuccessor(stem);
        int nextState = leadingDFA.getSuccessor(state, loop);
        return state == nextState;
    }


}
