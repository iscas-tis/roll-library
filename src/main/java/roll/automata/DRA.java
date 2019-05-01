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

import roll.util.sets.ISet;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * deterministic Parity automaton
 * 
 * */

public class DRA extends DFA {

    public DRA(Alphabet alphabet) {
        super(alphabet);
        this.accept = new AcceptDRA(this);
    }
    
    @Override
    public AutType getAccType() {
        return AutType.RABIN;
    }

    public void setFinal(int state) {
        throw new UnsupportedOperationException("setFinal unsupported for DRA");
    }
    
    public ISet getFinalStates() {
        throw new UnsupportedOperationException("getFinalStates unsupported for DRA");
    }
    
    public boolean isFinal(int state) {
        throw new UnsupportedOperationException("isFinal unsupported for DRA");
    }
}
