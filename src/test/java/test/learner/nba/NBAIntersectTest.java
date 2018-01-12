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

package test.learner.nba;

import roll.automata.NBA;
import roll.automata.operations.NBAIntersectionCheck;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBAIntersectTest {
    
    public static void main(String[] args) {
        NBA A = NBAStore.getNBA1();
        NBA B = NBAStore.getNBA2();
        System.out.println(A.toString());
        System.out.println(B.toString());
        NBAIntersectionCheck checker = new NBAIntersectionCheck(A, B, true);
        if(! checker.isEmpty()) {
            checker.computePath();
            System.out.println(checker.getCounterexample());
        }
    }

}
