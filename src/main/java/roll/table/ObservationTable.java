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

import java.util.List;

import roll.words.Word;
/**
 * An observation table consists of three components, namely: <BR/>
 * @upper upper part of observation table, each one represent a state 
 *        in the unknown automaton <BR/>
 * @lower lower part of observation table, each one can be mapped to 
 *        one state in upper table <BR/>
 * @column the set of experiments to distinguish the strings in upper table <BR/>
 * 
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public interface ObservationTable {
	
	// experiments, must be ordered
	List<ExprValue> getColumns();
	
	List<ObservationRow> getUpperTable();
	
	List<ObservationRow> getLowerTable();
	
	// expose row may be modified,should we allow that?
	ObservationRow getUnclosedLowerRow();
	
	// may not be used, should be removed later
	default boolean isClosed() {
		return getUnclosedLowerRow() == null;
	}
	
	// may not be used, should be removed later
	default boolean isConsistent() {
		return getInconsistentColumn() == null;
	}
	
	Word getInconsistentColumn();
	
	String toString();
	
	default ObservationRow getTableRow(Word word) {
		ObservationRow result = getUpperTableRow(word);
		if(result == null) result = getLowerTableRow(word);
		return result;
	}
	
	default ObservationRow getUpperTableRow(Word word) {
		for(ObservationRow row : getUpperTable()) {
			if(row.getWord().equals(word)) return row;
		}
		return null;
	}
	
	default ObservationRow getLowerTableRow(Word word) {
		for(ObservationRow row : getLowerTable()) {
			if(row.getWord().equals(word)) return row;
		}
		return null;
	}
	
	default int getColumnIndex(ExprValue column) {
		for(int index = 0; index < getColumns().size() ; index ++) {
			if(getColumns().get(index).equals(column)) return index;
		}
		return -1;
	}
	
	default boolean addUpperTableValue(Word state, ExprValue column, HashableValue value) {
		ObservationRow row = getUpperTableRow(state);
		int columnIndex = getColumnIndex(column);
		if(row == null || columnIndex == -1) return false;
		row.set(columnIndex, value);
		return true;
	}
	
	default boolean addLowerTableValue(Word state, ExprValue column, HashableValue value) {
		ObservationRow row = getLowerTableRow(state);
		int columnIndex = getColumnIndex(column);
		if(row == null || columnIndex == -1) return false;
		row.set(columnIndex, value);
		return true;
	}
	
	default boolean addTableValue(Word state, ExprValue column, HashableValue value) {
		return addUpperTableValue(state, column, value) 
			|| addLowerTableValue(state, column, value);
	}
	
	void moveRowFromLowerToUpper(ObservationRow row);
	
	ObservationRow addLowerRow(Word word);
	ObservationRow addUpperRow(Word word);
	
	int addColumn(ExprValue word);
	
	void clear();
	

}
