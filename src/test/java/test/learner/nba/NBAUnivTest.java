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

import org.junit.Test;

import roll.automata.NBA;
import roll.automata.operations.nba.universality.NBAUniversalityCheck;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBAUnivTest {
    
    @Test
    public void isUniversal() {
        NBA nba = NBAStore.getNBA5();
        System.out.println("Model: \n" + nba.toString());
        NBAUniversalityCheck checker = new NBAUniversalityCheck(nba);
        assert checker.isUniversal(): "Wrong, should be universal";
    }
    
    @Test
    public void isNonUniversal() {
        NBA nba = NBAStore.getNBA6();
        System.out.println("Model: \n" + nba.toString());
        NBAUniversalityCheck checker = new NBAUniversalityCheck(nba);
        assert !checker.isUniversal(): "Wrong, should be universal";
    }

}
