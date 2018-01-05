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

import java.util.ArrayList;
import java.util.List;

import roll.automata.NBA;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class Statistics {
    
    public int numOfLetters;
    
    public int numOfStatesInTraget;
    public int numOfTransInTraget;
    
    public int numOfStatesInHypothesis ;
    public int numOfTransInHypothesis  ; // number of transitions in hypothesis
    
    public int numOfMembershipQuery ; // number of membership query
    public int numOfEquivalenceQuery ; // number of equivalence query
    
    public int numOfStatesInLeading; // number of states in leading automaton
    public List<Integer> numOfStatesInProgress; // number of states in progress automata
    
    
    public long timeOfMembershipQuery ; // milliseconds used in membership query 
    public long timeOfEquivalenceQuery ;// milliseconds used in equivalence query
    public long timeOfLastEquivalenceQuery; // time for last eq check
    public long timeInTotal; // milliseconds used in learning
    public long timeOfTranslator; // milliseconds used in CE translation
    public long timeOfLearner; // milliseconds used in FDFA learner
    
    public long timeOfLearnerLeading; // milliseconds used in FDFA learner for leading automaton
    public long timeOfLearnerProgress; // milliseconds used in FDFA learner for progress automata
    
    public long timeOfSampling; //milliseconds used in sampling
    public int numOfMembershipSampling; // number of membership queries in sampling
    public int numOfSamplingSucceeded; // number of sampling succeeded
    public int numOfSamplingTried; // number of sampling we tried
    
    // sampling as the teacher
    public long numOfSamplingOmegaWords;
    
    private final Log log;
    private final Options options;
    
    public NBA hypothesis;
    
    public Statistics(Options options) {
        this.options = options;
        this.log = options.log;
        this.numOfStatesInProgress = new ArrayList<>();
    }
    
    public void print() {

        log.println("");
        log.println("");
        log.println("#LT = " + numOfLetters + "    //#number of letters");
        log.println("#T.S = " + numOfStatesInTraget + "    //#states of target");
        log.println("#T.T = " + numOfTransInTraget + "    //#transitions of target");
        
        log.println("#H.S = " + numOfStatesInHypothesis + "    //#states of learned automaton");
        log.println("#H.T = " + numOfTransInHypothesis + "    //#transitions of learned automaton");
        
        int numTotal = numOfStatesInLeading;
        log.println("#L.S = " + numOfStatesInLeading + "    // #states of leading automaton or L$");
        log.print("#P.S = [" );
        for(Integer numStates : numOfStatesInProgress) {
            log.print(numStates + ", ");
            numTotal += numStates;
        }
        log.println(" ]    // #states of each progress automaton");
        // total number of the states in final FDFA
        log.println("#F.S = " + numTotal + "    // #L.S + #P.S");
        
        log.println("#MQ = " + numOfMembershipQuery + "    // #membership query");
        log.println("#EQ = " + numOfEquivalenceQuery + "    // #equivalence query");
        
        log.println("#TMQ = " + timeOfMembershipQuery + " (ms)" + "    // time for membership queries");
        log.println("#TEQ = " + timeOfEquivalenceQuery + " (ms)" + "    // time for equivalence queries");
        log.println("#TLEQ = " + timeOfLastEquivalenceQuery + " (ms)" + "    // time for the last equivalence query");
        log.println("#TTR = " + timeOfTranslator + " (ms)" + "    // time for the translator");
        
        log.println("#TLR = " + timeOfLearner + " (ms)" + "    // time for the learner");
        log.println("#TLRL = " + timeOfLearnerLeading + " (ms)"  + "    // time for the learning leading automaton" );
        log.println("#TLRP = " + timeOfLearnerProgress + " (ms)" + "    // time for the learning progress automata" );
        
        if(options.runningMode == Options.RunningMode.SAMPLING) {
            log.println("#SW = " + numOfSamplingOmegaWords);
            log.println("#SMQ = " + numOfMembershipSampling);
            log.println("#ST = " + numOfSamplingTried);
            log.println("#SSD = " + numOfSamplingSucceeded);
            log.println("#TSA = " + timeOfSampling);
        }
                
        log.println("#TTO = " + timeInTotal + " (ms)" + "    //total time for learning Buechi automata");
                
    }

}
