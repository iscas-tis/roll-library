/* Written by Yong Li, Depeng Liu                                       */
/* Copyright (c) 2016                  	                               */
/* This program is free software: you can redistribute it and/or modify */
/* it under the terms of the GNU General Public License as published by */
/* the Free Software Foundation, either version 3 of the License, or    */
/* (at your option) any later version.                                  */

/* This program is distributed in the hope that it will be useful,      */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of       */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        */
/* GNU General Public License for more details.                         */

/* You should have received a copy of the GNU General Public License    */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package roll.learner.fdfa;

import java.util.List;

import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.table.ExprValue;
import roll.table.ExprValueWordPair;
import roll.table.HashableValue;
import roll.words.Alphabet;
import roll.words.Word;

public abstract class LearnerLeading extends LearnerGeneral {
	
    public LearnerLeading(Options options, Alphabet alphabet
            , MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
    }

    protected ExprValue getExprValueWord(Word left, Word right) {
        return new ExprValueWordPair(left, right);
    }
    
    abstract List<Word> getNewStates(); 
    
	
}
