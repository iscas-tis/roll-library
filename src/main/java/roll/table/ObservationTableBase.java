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

package roll.table;

import java.io.ByteArrayOutputStream;

import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public abstract class ObservationTableBase extends ObservationTableAbstract {
	// empty table
	public ObservationTableBase() {
		super();
	}
	
	
	@Override
	public ObservationRow getUnclosedLowerRow() {
		for(ObservationRow lowerRow : lowerTable) {
			boolean found = false;
			// found equal upper row
			for(ObservationRow upperRow : upperTable) {
				if(lowerRow.valuesEqual(upperRow)) { 
					found = true;
					break;
				}
			}
			if(!found) {
				return lowerRow;
			}
		}
		return null;
	}

	// row(s1) = row(s2) then it should be row(s1.a) = row(s2.a) for every a
	@Override
	public ExprValue getInconsistentColumn() {
		return null;
	}
 
	@Override
	public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
        	ObservationTablePrinterBoolean.print(this, out);
            return out.toString();
        } catch (Exception e) {
            return "ERROR";
        }
	}
	
    // simple add methods, do not contain any check 
	@Override
	public ObservationRowBase addLowerRow(Word word) {
		ObservationRowBase row = getRowInstance(word);
		lowerTable.add(row);
		return row;
	}

	@Override
	public ObservationRowBase addUpperRow(Word word) {
		ObservationRowBase row = getRowInstance(word);
		upperTable.add(row);
		return row;
	}
	
	public ObservationRowBase getRowInstance(Word word) {
		return new ObservationRowBase(word);
	}

}
