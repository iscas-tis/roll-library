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

import algorithms.InclusionOptBVLayered;
import automata.FiniteAutomaton;
import roll.automata.NBA;
import roll.automata.operations.NBAGenerator;
import roll.automata.operations.nba.universality.NBAInclusionCheckRank;
import roll.automata.operations.nba.universality.NBAUniversalityCheck;
import roll.oracle.nba.rabit.UtilRABIT;
import roll.util.Timer;

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
    public void isIncluded() {
        NBA B = NBAStore.getNBA5();
        NBA A = NBAStore.getNBA8();

        NBAInclusionCheckRank checker = new NBAInclusionCheckRank(A, B);
        assert checker.isIncluded(): "Wrong, should be universal";
    }
    
    @Test
    public void isNotIncluded() {
        NBA B = NBAStore.getNBA6();
        NBA A = NBAStore.getNBA8();

        NBAInclusionCheckRank checker = new NBAInclusionCheckRank(A, B);
        assert !checker.isIncluded(): "Wrong, should be universal";
    }
    
    @Test
    public void testRandomIncluded() {
        final int test = 20;
        final int state = 15;
        for(int i = 0; i < test; i ++) {
            NBA nba = NBAGenerator.getRandomNBA(state, 2);
            NBA univ = NBAStore.getNBA5();
            System.out.println("Model: \n" + nba.toString());
            Timer timer = new Timer();
            FiniteAutomaton rA = UtilRABIT.toRABITNBA(univ);
            FiniteAutomaton rB = UtilRABIT.toRABITNBA(nba);
            timer.start();
            algorithms.Options.debug = false;
            InclusionOptBVLayered inclusion = new InclusionOptBVLayered(rA, rB, 0);
            inclusion.run();
            boolean isUniv2 = inclusion.isIncluded();
            timer.stop();
            System.out.println("RABIT checking: " + timer.getTimeElapsed());
            timer.start();
            NBAInclusionCheckRank checker = new NBAInclusionCheckRank(univ, nba);
            boolean isUniv1 = checker.isIncluded();
            timer.stop();
            System.out.println("Rank-based checking: " + timer.getTimeElapsed());
            System.out.println("Result: " + isUniv1 + ", " + isUniv2);
            if(isUniv1) {
                System.out.println(nba.toBA());
            }
            assert isUniv1 == isUniv2: "Wrong answer";
        }

    }
    
    @Test
    public void isNonUniversal() {
        NBA nba = NBAStore.getNBA6();
        System.out.println("Model: \n" + nba.toString());
        NBAUniversalityCheck checker = new NBAUniversalityCheck(nba);
        assert !checker.isUniversal(): "Wrong, should be universal";
    }
    
    @Test
    public void testRandom() {
        final int test = 20;
        final int state = 25;
        for(int i = 0; i < test; i ++) {
            NBA nba = NBAGenerator.getRandomNBA(state, 2);
            NBA univ = NBAStore.getNBA5();
            System.out.println("Model: \n" + nba.toString());
            Timer timer = new Timer();
            FiniteAutomaton rA = UtilRABIT.toRABITNBA(univ);
            FiniteAutomaton rB = UtilRABIT.toRABITNBA(nba);
            timer.start();
            boolean isUniv2 = UtilRABIT.isIncluded(univ.getAlphabet(), rA, rB) == null;
            timer.stop();
            System.out.println("RABIT checking: " + timer.getTimeElapsed());
            timer.start();
            NBAUniversalityCheck checker = new NBAUniversalityCheck(nba);
            boolean isUniv1 = checker.isUniversal();
            timer.stop();
            System.out.println("Rank-based checking: " + timer.getTimeElapsed());
            System.out.println("Result: " + isUniv1 + ", " + isUniv2);
            if(isUniv1) {
                System.out.println(nba.toBA());
            }
            assert isUniv1 == isUniv2: "Wrong answer";
        }

    }

}
