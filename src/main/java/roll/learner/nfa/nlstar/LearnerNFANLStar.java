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

package roll.learner.nfa.nlstar;

import java.util.List;

import roll.learner.LearnerType;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * Benedikt Bollig, Peter Habermehl, Carsten Kern, and Martin Leucker
 * "Angluin-Style Learning of NFA" in IJCAI 2009.
 * 
 * */

public class LearnerNFANLStar extends LearnerNFATable {
    
    public LearnerNFANLStar(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.NFA_NLSTAR;
    }
    // -----------------------------------------------------------------------------------
    // a state is accepting iff it accepts empty language
    @Override
    protected boolean isAccepting(List<ObservationRow> upperPrimes, int state) {
        ObservationRow stateRow = upperPrimes.get(state);
        int emptyNr = observationTable.getColumnIndex(getExprValueWord(alphabet.getEmptyWord()));
        assert emptyNr != -1 : "index -> " + emptyNr;
        return stateRow.getValues().get(emptyNr).isAccepting();
    }

    @Override
    protected int addNewColumnsToTable(Query<HashableValue> query) {
        Word wordCE = query.getQueriedWord();
        int number = 0;
        for(int offset = 0; offset < wordCE.length(); offset ++) {
            Word suffix = wordCE.getSuffix(offset);
            ExprValue exprValue = getExprValueWord(suffix);
            if(observationTable.isInColumn(exprValue)) {
                continue;
            }
            observationTable.addColumn(exprValue); // add new experiment
            number ++;
        }        
        return number;
    }

    @Override
    protected ExprValue makeInconsistencyColumn(ExprValue exprValue, int preletter) {
        Word word = exprValue.get();
        word = word.preappend(preletter);
        return getExprValueWord(word);
    }

}
