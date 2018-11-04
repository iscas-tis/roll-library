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

import roll.jupyter.NativeTool;
import roll.util.sets.ISet;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Family of Residual Finite State Automata
 * */
public class FRFSA implements Acceptor {
    
    private final NFA leadingNFA;
    private final List<NFA> progressNFAs;
    private final Accept acceptance;
    private final Alphabet alphabet;
    
    public FRFSA(NFA m, List<NFA> ps) {
        assert m != null && ps != null;
        alphabet = m.getAlphabet();
        leadingNFA = m;
        progressNFAs = ps;
        acceptance = new AcceptFNFA(this);
    }
    
    // -------------------------------------------------------
    public NFA getLeadingDFA() {
        return leadingNFA;
    }
    
    public NFA getProgressNFA(int state) {
        assert state >= 0 && state < progressNFAs.size(); 
        return progressNFAs.get(state);
    }

    @Override
    public Alphabet getAlphabet() {
        return alphabet;
    }

    @Override
    public AutType getAccType() {
        return AutType.FRFSA;
    }

    @Override
    public Accept getAcc() {
        return acceptance;
    }
    
    // -------------------------------------------------------
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("//FRFSA-M: \n" + leadingNFA.toString() + "\n");
        for(int i = 0; i < progressNFAs.size(); i ++) {
            builder.append("//FRFSA-P" + i + ": \n" + progressNFAs.get(i).toString());
        }
        return builder.toString();
    }
    
    @Override
    public String toString(List<String> apList) {
        StringBuilder builder = new StringBuilder();
        builder.append("//FRFSA-M: \n" + leadingNFA.toString(apList) + "\n");
        for(int i = 0; i < progressNFAs.size(); i ++) {
            builder.append("//FRFSA-P" + i + ": \n" + progressNFAs.get(i).toString(apList));
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
        builder.append("<p> Leading NFA M :  </p> <br> "    
                     + NativeTool.dot2SVG(leadingNFA.toString(apList)));
        for(int i = 0; i < progressNFAs.size(); i ++) {
            builder.append("<p> Progress NFA for " + i + ": </p> <br>"
                    + NativeTool.dot2SVG(progressNFAs.get(i).toString(apList)) + "<br>");
        }
        return builder.toString();
    }

    // -------------------------------------------------------
    private class AcceptFNFA implements Accept {
        final FRFSA fnfa;
        
        AcceptFNFA(FRFSA fdfa) {
            this.fnfa = fdfa;
        }

        @Override
        public boolean accept(ISet states) {
            throw new UnsupportedOperationException("FRFSA doesnot support isAccepting(ISet states)");
        }

        @Override
        public boolean accept(Word prefix, Word suffix) {
            NFA nfa = fnfa.getLeadingDFA();
            for(int state : nfa.getSuccessors(prefix)) {
                NFA proNFA = getProgressNFA(state);
                boolean found = false;
                for(int proState : proNFA.getSuccessors(suffix)) {
                    found = proNFA.isFinal(proState);
                    if(found) return true;
                }
            }
            return false;
        }
        
    }


}
