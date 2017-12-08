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

package roll.learner.dfa.table;

import roll.table.ObservationRow;
import roll.table.ObservationTableBase;
import roll.words.Alphabet;
import roll.words.Word;


/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class ObservationTableDFALStar extends ObservationTableBase {
    private final Alphabet alphabet;
    
    ObservationTableDFALStar(Alphabet alphabet) {
        super();
        this.alphabet = alphabet;
    }

    // row(s1) = row(s2) then it should be row(s1.a) = row(s2.a) for every a
    @Override
    public Word getInconsistentColumn() {
        for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
            for(int rowNr1 = 0; rowNr1 < upperTable.size(); rowNr1 ++) {
                for(int rowNr2 = rowNr1 + 1; rowNr2 < upperTable.size(); rowNr2 ++) {
                    ObservationRow upperRow1 = upperTable.get(rowNr1);
                    ObservationRow upperRow2 = upperTable.get(rowNr2);
                    if(upperRow1.getValues().equals(upperRow2.getValues())) {
                        ObservationRow rowState1 = getTableRow(upperRow1.getWord().append(letter));
                        ObservationRow rowState2 = getTableRow(upperRow2.getWord().append(letter));
                        Word columnExperiment = checkConsistency(rowState1, rowState2);
                        if(columnExperiment != null) return columnExperiment.preappend(letter);
                    }
                }
            }
        }
        return null;
    }
    
    // if row(s1.a) = row(s.a) return null
    private Word checkConsistency(
            ObservationRow row1
          , ObservationRow row2) {
        int index = 0;
        Word columnExperiment = null;
        while(index < columns.size()) {
            if(!row1.getValues().get(index).valueEqual(row2.getValues().get(index))) {
                columnExperiment = columns.get(index).get();
                break;
            }
            ++ index; 
        }
        return columnExperiment;
    }

}
