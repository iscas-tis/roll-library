/* Copyright (c) since 2016                                               */
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

// The code is borrowed from omega library

package roll.main.inclusion.run;

import java.util.Comparator;

import roll.words.Word;

public class SuccessorInfo<T> implements Comparator<SuccessorInfo<T>>, Comparable<SuccessorInfo<T>>{
    public final T state;
    public Word word;
    
    public SuccessorInfo(T state) {
        this.state = state;
    }

    @Override
    public int compare(SuccessorInfo<T> arg0, SuccessorInfo<T> arg1) {
        return arg0.word.length() - arg1.word.length();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(! (obj instanceof SuccessorInfo)) {
            return false;
        }
        @SuppressWarnings("unchecked")
		SuccessorInfo<T> other = (SuccessorInfo<T>)obj;
        return other.state == state;
    }
    
    @Override
    public String toString() {
        return "<" + state + "," + word + "," + word.length() + ">";
    }

	@Override
	public int compareTo(SuccessorInfo<T> other) {
		return word.length() - other.word.length();
	}
    
}