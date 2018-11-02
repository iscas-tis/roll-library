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

package roll.learner.nfa.nlstar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.table.ObservationRow;
import roll.table.ObservationRowAbstract;
import roll.table.ObservationRowBase;
import roll.table.ObservationTableBase;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class ObservationTableNLStar extends ObservationTableBase {
    
    private final Alphabet alphabet;
    private final Set<Word> columnSet;
    private final List<ObservationRow> upperPrimes;
    
    public ObservationTableNLStar(Alphabet alphabet) {
        super();
        this.alphabet = alphabet;
        this.columnSet = new TreeSet<>();
        this.upperPrimes = new ArrayList<>();
    }
    
    protected boolean isInColumn(Word word) {
        return columnSet.contains(word); 
    }
    
    @Override
    public int addColumn(ExprValue column) {
        Word word = column.get();
        if(isInColumn(word)) {
            return columns.indexOf(column);
        }
        columnSet.add(word);
        int index = columns.size();
        columns.add(column);
        assert columns.get(index).equals(column);
        assert columns.indexOf(column) == index : "new column to be added: " + column;
        return index;
    }
    
    protected List<ObservationRow> getUpperPrimes() {
        return Collections.unmodifiableList(upperPrimes);
    }
    
    protected boolean isPrimeRow(ObservationRow row) {
        return isPrimeRow(upperTable, row);
    }
    
    protected boolean isPrimeRow(List<ObservationRow> upperPrimes, ObservationRow row) {
        ObservationRowAbstract rowOrs = new ObservationRowBase(alphabet.getEmptyWord());
        boolean merged = false;
        // upperTable has been replaced by upperPrimes 
        for(ObservationRow upperRow : upperPrimes) {
            // get all upper rows union
            if(row != upperRow && !row.getWord().equals(upperRow) && !row.valuesEqual(upperRow) && row.covers(upperRow)) {
                rowOrOp(rowOrs, upperRow);
                merged = true;
            }
        }
        
        // merged and rowOrs are equivalent
        if(merged && row.valuesEqual(rowOrs)) {
            return false;
        }
        
        // try lower table
        for(ObservationRow lowerRow : lowerTable) {
            // get all upper rows union
            if(row != lowerRow && !row.getWord().equals(lowerRow) && !row.valuesEqual(lowerRow) && row.covers(lowerRow)) {
                rowOrOp(rowOrs, lowerRow);
                merged = true;
            }
        }
        
        return !(merged && row.valuesEqual(rowOrs));
    }
    
    
    public ObservationRow getUnclosedLowerRow() {
        
        // first we have to find out the prime rows in the upper row
        upperPrimes.clear();
        for(ObservationRow upperRow : upperTable) {
            if(isPrimeRow(upperRow)) {
                upperPrimes.add(upperRow);
            }
        }
        
        for(ObservationRow lowerRow : lowerTable) {
            // first make sure this lower row is in prime
            boolean inPrime = false;
            for(int i = 0; i < upperPrimes.size(); i ++) {
                if(lowerRow.valuesEqual(upperPrimes.get(i))) {
                    inPrime = true;
                    break;
                }
            }
            // second make sure this lower row is prime
            if(inPrime || !isPrimeRow(upperPrimes, lowerRow)) {
                continue;
            }
            // it is prime
            return lowerRow;
        }
        return null;
    }
    
    
    protected void rowOrOp(ObservationRowAbstract row1, ObservationRow row2) {
        List<HashableValue> row2Values = row2.getValues();
        List<HashableValue> row1Values = row1.getValues();
        for(int i = 0; i < row2Values.size(); i ++) {
            if(row1Values.size() <= i) row1.add(new HashableValueBoolean(false));
            row1.set(i, new HashableValueBoolean(row1Values.get(i).isAccepting() || row2Values.get(i).isAccepting()));
        }
    }

    // row(s1) covers row(s2) and for some a, v, row(s1.a.v) = - and row(s2.a.v) = +
    @Override
    public Word getInconsistentColumn() {
        for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
            for(int rowNr1 = 0; rowNr1 < upperTable.size(); rowNr1 ++) {
                for(int rowNr2 = rowNr1 + 1; rowNr2 < upperTable.size(); rowNr2 ++) {
                    ObservationRow upperRow1 = upperTable.get(rowNr1);
                    ObservationRow upperRow2 = upperTable.get(rowNr2);
                    if(upperRow1.covers(upperRow2)) {
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
    
    // if row(s1.a.v) = - and  row(s.a.v) = + return v
    private Word checkConsistency(
            ObservationRow row1
          , ObservationRow row2) {
        int index = 0;
        Word columnExperiment = null;
        while(index < columns.size()) {
            if(!row1.getValues().get(index).isAccepting() && row2.getValues().get(index).isAccepting()) {
                columnExperiment = columns.get(index).get();
                break;
            }
            ++ index; 
        }
        return columnExperiment;
    }

}
