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

package roll.oracle.fdfa.dk;

import dk.brics.automaton.Automaton;
import roll.automata.DFA;
import roll.automata.FDFA;
import roll.automata.operations.FDFAOperations;
import roll.oracle.EquivalenceOracle;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class TeacherFDFADK implements MembershipOracle<HashableValue>, EquivalenceOracle<FDFA, Query<HashableValue>> {

    private final FDFA fdfa;
    private final Alphabet alphabet;
    private final Automaton d1; 
    private int numMembership = 0;
    private int numEquiv = 0;
    
    public TeacherFDFADK(FDFA fdfa, Alphabet alphabet) {
        this.fdfa = fdfa;
        this.alphabet = alphabet;
        this.d1 = FDFAOperations.buildDOne(fdfa);
    }
    
    @Override
    public Query<HashableValue> answerEquivalenceQuery(FDFA hypothesis) {
        Automaton hypo = FDFAOperations.buildDOne(hypothesis);
        Automaton temp = hypo.clone().minus(d1.clone());
        String ce = temp.getShortestExample(true);

        Query<HashableValue> ceQuery = null;
        if(ce != null) {
            Pair<Word, Word> pair = alphabet.getWordPairFromString(ce);
            ceQuery = new QuerySimple<>(pair.getLeft(), pair.getRight());
            ceQuery.answerQuery(new HashableValueBoolean(false));
            return ceQuery;
        }
        temp = d1.clone().minus(hypo.clone());
        ce = temp.getShortestExample(true);
        if(ce != null) {
            Pair<Word, Word> pair = alphabet.getWordPairFromString(ce);
            ceQuery = new QuerySimple<>(pair.getLeft(), pair.getRight());
            ceQuery.answerQuery(new HashableValueBoolean(false));
            return ceQuery;
        }
        Word wordEmpty = alphabet.getEmptyWord();
        ceQuery = new QuerySimple<>(wordEmpty, wordEmpty);
        ceQuery.answerQuery(new HashableValueBoolean(true));
        return ceQuery;
    }

    @Override
    public HashableValue answerMembershipQuery(Query<HashableValue> query) {
        Word prefix = query.getPrefix();
        Word suffix = query.getSuffix();
        if(suffix.isEmpty()) {
            return new HashableValueBoolean(false);
        }
        System.out.println(prefix + " : " + suffix);
        Pair<Word, Word> pair = FDFAOperations.normalize(fdfa, prefix, suffix);
        if(pair == null) {
            return new HashableValueBoolean(false);
        }
        DFA leadDFA = fdfa.getLeadingDFA();
        int state = leadDFA.getSuccessor(pair.getLeft());
        DFA proDFA = fdfa.getProgressDFA(state);
        boolean result = proDFA.isFinal(proDFA.getSuccessor(pair.getRight()));  
        return new HashableValueBoolean(result);
    }

}
