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

class SuccessorInfo implements Comparator<SuccessorInfo>{
    final int state;
    int predState;
    int letter;
    int distance;
    
    SuccessorInfo(int state) {
        this.state = state;
        distance = Integer.MAX_VALUE;
    }
    
    boolean unreachable() {
        return distance == Integer.MAX_VALUE;
    }

    @Override
    public int compare(SuccessorInfo arg0, SuccessorInfo arg1) {
        return arg0.distance - arg1.distance;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(! (obj instanceof SuccessorInfo)) {
            return false;
        }
        SuccessorInfo other = (SuccessorInfo)obj;
        return other.state == state;
    }
    
    @Override
    public String toString() {
        return "<" + state + "," + predState + "," + letter + "," + distance + ">";
    }
    
}