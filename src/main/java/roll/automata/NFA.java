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

package roll.automata;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class NFA extends FASimple {

    public NFA(int numOfLetters) {
        super(numOfLetters);
    }

    @Override
    public AccType getAccType() {
        return AccType.NFA;
    }

    @Override
    public Acc getAcc() {
        return null;
    }

    @Override
    public State makeState(int index) {
        return new NFAState(this, index);
    }

    @Override
    public State createState() {
        State state = makeState(states.size());
        return state;
    }

}
