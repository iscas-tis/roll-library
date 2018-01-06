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
        int indent = 30;
        if(numOfStatesInProgress.size() > indent) {
            indent = 4 * numOfStatesInProgress.size();
        }
        log.println("");
        log.println("");
        log.println("#LT = " + numOfLetters, indent , "    // #number of letters");
        log.println("#T.S = " + numOfStatesInTraget , indent , "    // #states of target");
        log.println("#T.T = " + numOfTransInTraget , indent , "    // #transitions of target");
        
        log.println("#H.S = " + numOfStatesInHypothesis , indent , "    // #states of learned automaton");
        log.println("#H.T = " + numOfTransInHypothesis , indent , "    // #transitions of learned automaton");
        
        int numTotal = numOfStatesInLeading;
        log.println("#L.S = " + numOfStatesInLeading , indent , "    // #states of leading automaton or L$");
        StringBuilder builder = new StringBuilder();
        builder.append("#P.S = [" );
        for(Integer numStates : numOfStatesInProgress) {
            builder.append(numStates + ", ");
            numTotal += numStates;
        }
        builder.append("]");
        log.println(builder.toString(),  indent ,"    // #states of each progress automaton");
        // total number of the states in final FDFA
        log.println("#F.S = " + numTotal , indent , "    // #L.S + #P.S");
        
        log.println("#MQ = " + numOfMembershipQuery , indent , "    // #membership query");
        log.println("#EQ = " + numOfEquivalenceQuery , indent , "    // #equivalence query");
        
        log.println("#TMQ = " + timeOfMembershipQuery + " (ms)" , indent , "    // time for membership queries");
        log.println("#TEQ = " + timeOfEquivalenceQuery + " (ms)" , indent , "    // time for equivalence queries");
        log.println("#TLEQ = " + timeOfLastEquivalenceQuery + " (ms)" , indent , "    // time for the last equivalence query");
        log.println("#TTR = " + timeOfTranslator + " (ms)" , indent , "    // time for the translator");
        
        log.println("#TLR = " + timeOfLearner + " (ms)" , indent , "    // time for the learner");
        log.println("#TLRL = " + timeOfLearnerLeading + " (ms)"  , indent , "    // time for the learning leading automaton" );
        log.println("#TLRP = " + timeOfLearnerProgress + " (ms)" , indent , "    // time for the learning progress automata" );
        
        if(options.runningMode == Options.RunningMode.SAMPLING) {
            log.println("#SW = " + numOfSamplingOmegaWords, indent, "    // #number of sample omega words");
            log.println("#SMQ = " + numOfMembershipSampling, indent, "    // #number of sample membership queries");
            log.println("#ST = " + numOfSamplingTried, indent, "    // #number of sample have been tried");
            log.println("#SSD = " + numOfSamplingSucceeded, indent, "    // #number of sample succeeded");
            log.println("#TSA = " + timeOfSampling, indent, "    // time of sampling");
        }
                
        log.println("#TTO = " + timeInTotal + " (ms)" , indent , "    // total time for learning Buechi automata");
                
    }

}
