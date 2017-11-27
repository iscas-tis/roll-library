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

package roll.main;

import java.util.List;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class Statistics {
    
    public int numTransHypothesis  ; // number of transitions in hypothesis
    
    public int numMembershipQuery ; // number of membership query
    public int numEquivalenceQuery ; // number of equivalence query
    
    public int numStatesLeading; // number of states in leading automaton
    public List<Integer> numStatesProgress; // number of states in progress automata
    
    
    public long timeMembershipQuery ; // milliseconds used in membership query 
    public long timeEquivalenceQuery ;// milliseconds used in equivalence query
    public long timeLastEquivalenceQuery; // time for last eq check
    public long timeTotal; // milliseconds used in learning
    public long timeTranslator; // milliseconds used in CE translation
    public long timeLearner; // milliseconds used in FDFA learner
    
    public long timeLearnerLeading; // milliseconds used in FDFA learner for leading automaton
    public long timeLearnerProgress; // milliseconds used in FDFA learner for progress automata
    
    public long timeMinimizationBuechi; // milliseconds used in Buechi minimization before eq check
    public long timeSampling; //milliseconds used in sampling
    public int numMembershipSampling; // number of membership queries in sampling
    public int numSamplingSucceeded; // number of sampling succeeded
    public int numSamplingTried; // number of sampling we tried
    
    // sampling as the teacher
    public long numSamplingOmegaWord;
    
    public Statistics() {
        
    }

}
