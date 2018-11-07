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

package roll.learner.ffa;

import java.util.List;

import roll.automata.NFA;
import roll.learner.LearnerType;
import roll.learner.nfa.nlstar.LearnerNFATable;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.table.ExprValue;
import roll.table.ExprValueWordPair;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Leading Learner for a residual finite state automaton
 * */

public class LearnerLeadingNFA extends LearnerNFATable implements LearnerLeading<NFA> {

    public LearnerLeadingNFA(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.FDFA_LEADING_TABLE;
    }

    @Override
    protected boolean isAccepting(List<ObservationRow> upperPrimes, int state) {
        return false;
    }

    @Override
    protected int addNewColumnsToTable(Query<HashableValue> query) {
        Word prefix = query.getPrefix();
        Word period = query.getSuffix();
        int num = 0;
        for(int offset = 0; offset < prefix.length(); offset ++) {
            Word suffix = prefix.getSuffix(offset);
            ExprValue exprValue = new ExprValueWordPair(suffix, period);
            if(observationTable.isInColumn(exprValue)) {
                continue;
            }
            observationTable.addColumn(exprValue); // add new experiment
            num ++;
        }        
        return num;
    }

    @Override
    protected ExprValue makeInconsistencyColumn(ExprValue exprValue, int preletter) {
        Pair<Word, Word> pair = UtilFFA.getOmegaWord(alphabet.getLetterWord(preletter), exprValue);
        return new ExprValueWordPair(pair.getLeft(), pair.getRight());
    }

    @Override
    protected Query<HashableValue> makeMembershipQuery(Word prefix, ExprValue exprValue) {
        return UtilFFA.makeMembershipQuery(prefix, exprValue);
    }
    
    @Override
    protected Query<HashableValue> makeMembershipQuery(ObservationRow row, int offset, ExprValue exprValue) {
        return UtilFFA.makeMembershipQuery(row, offset, exprValue);
    }

    @Override
    protected ExprValue getInitialColumnExprValue() {
        return UtilFFA.getInitialColumnExprValue(alphabet);
    }

    @Override
    public int getStateSize() {
        return super.getPrimeNum();
    }

    @Override
    public Word getStateLabel(int state) {
        return super.getStateLabel(state);
    }

}
