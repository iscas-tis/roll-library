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

import automata.FiniteAutomaton;
import roll.automata.NBA;
import roll.automata.operations.NBAGenerator;
import roll.automata.operations.NBAOperations;
import roll.oracle.nba.rabit.UtilRABIT;
import roll.util.Timer;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBARemoveStates {
    
    @Test
    public void testRemoval() {
        NBA A = NBAStore.getNBA7();
        System.out.println("A:\n" + A);
        NBA B = NBAOperations.removeDeadStates(A);
        System.out.println("B:\n" + B);
    }
    
    @Test
    public void testRandom() {
        final int test = 5;
        final int state = 3;
        for(int i = 0; i < test; i ++) {
            NBA A = NBAGenerator.getRandomNBA(state, 4);
//            System.out.println("A:\n" + A);
            NBA B = NBAOperations.removeDeadStates(A);
//            System.out.println("B:\n" + B);
            Timer timer = new Timer();
            FiniteAutomaton rA = UtilRABIT.toRABITNBA(A);
            FiniteAutomaton rB = UtilRABIT.toRABITNBA(B);
            timer.start();
            boolean isUniv1 = UtilRABIT.isIncluded(A.getAlphabet(), rA, rB) == null;
            if(!isUniv1) {
                System.out.println(A);
            }
            timer.stop();
            System.out.println("RABIT checking: " + timer.getTimeElapsed());
            timer.start();
            boolean isUniv2 = UtilRABIT.isIncluded(A.getAlphabet(), rB, rA) == null;
            timer.stop();
            assert isUniv1 && isUniv2: "Wrong answer";
        }

    }
    
    @Test
    public void testRandomLDBA() {
        final int test = 1;
        final int state = 30;
        final int det = state / 4;
        final int acc = (int) (state * 0.2);
        final double density = 1.2;
        for (int i = 0; i < test; i++) {
            NBA A = NBAGenerator.getRandomLDBA(state, det, 3, acc, density);
            NBA B = NBAOperations.removeDeadStates(A);
            // System.out.println("B:\n" + B);
            boolean isSemiDet1 = NBAOperations.isSemideterministic(A);
            boolean isSemiDet2 = NBAOperations.isSemideterministic(B);
            System.out.println(B.toString());
            assert isSemiDet1 && isSemiDet2 : "Wrong answer";
        }

    }

}
