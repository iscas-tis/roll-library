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

package roll.parser.hoa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * represents a set of atomic propositions
 * */
public class APSet implements Iterable<String> {
	
	private List<String> aps;
	
	public APSet() {
		aps = new ArrayList<>();
	}
	
	public APSet(List<String> aps) {
		this.aps = new ArrayList<>();
		for(String ap : aps) {
			addAP(ap);
		}
	}
	
	public int addAP(String ap) {
		int i = aps.indexOf(ap);
		if(i == -1) {
			aps.add(ap);
			i = aps.size() - 1;
		}
		return i;
	}
	
	public String getAP(int i) {
		return aps.get(i);
	}
	
	public int indexOf(String ap) {
		return aps.indexOf(ap);
	}
	
	public int size() {
		return aps.size();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for(int i = 0; i < aps.size(); i ++) {
			builder.append(aps.get(i));
			if(i != aps.size() - 1) {
				builder.append(", ");
			}
		}
		builder.append("}");
		return builder.toString();
	}
	
	@Override
	public Iterator<String> iterator() {
		return aps.iterator();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof APSet) {
			APSet set = (APSet)obj;
			return this.aps.equals(set.aps);
		}
		return false;
	}

}
