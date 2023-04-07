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

package roll.learner.fdfa;

import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.table.HashableValueBooleanPair;
import roll.table.HashableValueImplBoolPair;
import roll.table.HashableValueIntEnum;
import roll.words.Word;

public interface LearnerProgress extends LearnerGeneral {
	
    Word getLeadingLabel();
    
    LearnerLeading getLearnerLeading();
    
    // only for syntactic FDFA
    default HashableValue getHashableValueIntBoolPair(int state, boolean recur, boolean mq) {
        return new HashableValueIntEnum(state, recur, mq);
    }
    
    // only for recurrent FDFA
    default HashableValue getHashableValueBoolPair(boolean recur, boolean mq) {
        return new HashableValueBooleanPair(recur, mq);
    }
    
    // only for limit FDFA
    default HashableValue getHashableValueImplBoolPair(boolean recur, boolean mq) {
        return new HashableValueImplBoolPair(recur, mq);
    }
    
//    default HashableValue getHashableValueBoolExactPair(boolean recur, boolean mq) {
//        return new HashableValueBooleanExactPair(recur, mq);
//    }
    // for periodic FDFA
    default HashableValue getHashableValueBool(boolean result) {
        return new HashableValueBoolean(result);
    }
    // prepare entry value in the observation table
    HashableValue prepareRowHashableValue(boolean mqResult, Word x, Word e);
    
    // get entry value for counterexample analysis
    default HashableValue getCeAnalyzerHashableValue(boolean mqResult, Word x, Word e) {
        return prepareRowHashableValue(mqResult, x, e);
    }
    
    int getLeadingState();
}
