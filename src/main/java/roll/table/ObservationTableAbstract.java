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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public abstract class ObservationTableAbstract implements ObservationTable {

	protected final List<ObservationRow> upperTable; // S
	protected final List<ObservationRow> lowerTable; // SA
	protected final List<ExprValue> columns;    // E
	
	protected ObservationTableAbstract() {
		this.upperTable = new ArrayList<>();
		this.lowerTable = new ArrayList<>();
		this.columns = new ArrayList<>();
	}
	
	@Override
	public List<ExprValue> getColumns() {
		return Collections.unmodifiableList(columns);
	}
	

	@Override
	public List<ObservationRow> getUpperTable() {
		return Collections.unmodifiableList(upperTable);
	}

	@Override
	public List<ObservationRow> getLowerTable() {
		return Collections.unmodifiableList(lowerTable);
	}
	
	// may not be used, should be removed later
	public boolean isClosed() {
		return getUnclosedLowerRow() == null;
	}
	// may not be used, should be removed later
	public boolean isConsistent() {
		return getInconsistentColumn() == null;
	}
	
	
	public ObservationRow getTableRow(Word word) {
		ObservationRow result = getUpperTableRow(word);
		if(result == null) result = getLowerTableRow(word);
		return result;
	}
	
	private ObservationRow getTableRow(List<ObservationRow> rows, Word word) {
		for(ObservationRow row : rows) {
			if(row.getWord().equals(word)) return row;
		}
		return null;
	}
	
	public ObservationRow getUpperTableRow(Word word) {
		return getTableRow(getUpperTable(), word);
	}
	
	public ObservationRow getLowerTableRow(Word word) {
		return getTableRow(getLowerTable(), word);
	}
	
	public int getColumnIndex(ExprValue column) {
		for(int index = 0; index < getColumns().size() ; index ++) {
			if(getColumns().get(index).valueEqual(column)) return index;
		}
		return -1;
	}
	
	public boolean addUpperTableValue(Word state, ExprValue column, HashableValue value) {
		return addTableValue(getUpperTable(), state, column, value);
	}
	
	public boolean addLowerTableValue(Word state, ExprValue column, HashableValue value) {
		return addTableValue(getLowerTable(), state, column, value);
	}
	
	private boolean addTableValue(List<ObservationRow> rows, Word state
			, ExprValue column, HashableValue value) {
		ObservationRow row = getTableRow(rows, state);
		if(row == null) return false;
		int columnIndex = getColumnIndex(column);
		if(columnIndex == -1) return false;
		row.set(columnIndex, value);
		return true;
	}
	
	public boolean addTableValue(Word state, ExprValue column, HashableValue value) {
		return addUpperTableValue(state, column, value) 
			|| addLowerTableValue(state, column, value);
	}

	@Override
	public void moveRowFromLowerToUpper(ObservationRow row) {
		lowerTable.remove(row);
		upperTable.add(row);
	}
	
	@Override
	public int addColumn(ExprValue column) {
		int index = columns.size();
		columns.add(column);
		assert columns.get(index).equals(column);
		return index;
	}
	
	public void clear() {
		for(ObservationRow row : this.upperTable) {
			row.clear();
		}
		this.upperTable.clear();
		for(ObservationRow row : this.lowerTable) {
			row.clear();
		}
		this.lowerTable.clear();
		this.columns.clear();
	}
	
}
