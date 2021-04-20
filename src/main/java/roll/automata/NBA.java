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

package roll.automata;

import java.util.LinkedList;

import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class NBA extends NFA {

    public NBA(final Alphabet alphabet) {
        super(alphabet);
        this.accept = new AcceptNBA(this);
    }

    @Override
    public AutType getAccType() {
        return AutType.NBA;
    }
    
    public void explore() {
        LinkedList<StateNFA> walkList = new LinkedList<>();
        walkList.add(this.getState(this.getInitialState()));
        explore(walkList);
    }
    
    public void explore(LinkedList<StateNFA> walkList) {
    	ISet visited = UtilISet.newISet();

        while (!walkList.isEmpty()) {
            StateNFA s = walkList.remove();
            if (visited.get(s.getId()))
                continue;
            visited.set(s.getId());

            for (int letter = 0; letter < this.getAlphabetSize(); letter++) {
                for(int succ : s.getSuccessors(letter)) {
                    if (!visited.get(succ)) {
                        walkList.addFirst(this.getState(succ));
                    }
                }
            }
        }
    }

}
