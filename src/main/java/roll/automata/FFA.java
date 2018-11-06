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

import java.util.ArrayList;
import java.util.List;

import roll.jupyter.NativeTool;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Family of Finite Automata
 * */

public abstract class FFA<M extends NFA, A extends NFA> implements Acceptor {
    protected final M leadingFA;
    protected final List<A> progressFAs;
    protected Accept acceptance;
    protected final Alphabet alphabet;
    
    public FFA(M m, List<A> ps) {
        assert m != null && ps != null;
        alphabet = m.getAlphabet();
        leadingFA = m;
        progressFAs = ps;
    }
    
    // -------------------------------------------------------
    public M getLeadingFA() {
        return leadingFA;
    }
    
    public A getProgressFA(int state) {
        assert state >= 0 && state < progressFAs.size(); 
        return progressFAs.get(state);
    }

    @Override
    public Alphabet getAlphabet() {
        return alphabet;
    }

    @Override
    public Accept getAcc() {
        return acceptance;
    }
    
    // -------------------------------------------------------
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("//FFA-M: \n" + leadingFA.toString() + "\n");
        for(int i = 0; i < progressFAs.size(); i ++) {
            builder.append("//FFA-P" + i + ": \n" + progressFAs.get(i).toString());
        }
        return builder.toString();
    }
    
    @Override
    public String toString(List<String> apList) {
        StringBuilder builder = new StringBuilder();
        builder.append("//FFA-M: \n" + leadingFA.toString(apList) + "\n");
        for(int i = 0; i < progressFAs.size(); i ++) {
            builder.append("//FFA-P" + i + ": \n" + progressFAs.get(i).toString(apList));
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
        builder.append("<p> Leading FA M :  </p> <br> "    
                     + NativeTool.dot2SVG(leadingFA.toString(apList)));
        for(int i = 0; i < progressFAs.size(); i ++) {
            builder.append("<p> Progress FA for " + i + ": </p> <br>"
                    + NativeTool.dot2SVG(progressFAs.get(i).toString(apList)) + "<br>");
        }
        return builder.toString();
    }
    
    public boolean isNormalized(Word prefix, Word period) {
        for(int state : leadingFA.getSuccessors(prefix)) {
            for(int nextState : leadingFA.getSuccessors(state, period)) {
                if(state == nextState) {
                    return true;
                }
            }
        }
        return false;
    }
}
