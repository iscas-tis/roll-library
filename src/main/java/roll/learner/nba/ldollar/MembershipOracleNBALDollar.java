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

package roll.learner.nba.ldollar;

import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

class MembershipOracleNBALDollar implements MembershipOracle<HashableValue> {
    private final MembershipOracle<HashableValue> membershipOracle;
    private final int dollarLetter ;
    public MembershipOracleNBALDollar(
            MembershipOracle<HashableValue> membershipOracle,
            final int dollarLetter) {
        this.membershipOracle = membershipOracle;
        this.dollarLetter = dollarLetter;
    }

    @Override
    public HashableValue answerMembershipQuery(Query<HashableValue> query) {
        // input word is finite word
        Word word = query.getQueriedWord();
        // last word is '$'
        if (word.getLastLetter() == dollarLetter) {
            return new HashableValueBoolean(false);
        }
        // Counts the number of $.
        int counter = 0;
        // Records u,v of Word u$v.
        Word prefix = null, suffix = null;
        for (int letterNr = 0; letterNr < word.length(); letterNr++) {
            if (word.getLetter(letterNr) == dollarLetter) {
                counter++;
                prefix = word.getPrefix(letterNr);
                suffix = word.getSuffix(letterNr + 1);
            }
            if (counter > 1) {
                return new HashableValueBoolean(false);
            }
        }
        if (counter == 0) {
            return new HashableValueBoolean(false);
        }
       
        // '$' only occurs once new QuerySimple
        HashableValue answer = membershipOracle.answerMembershipQuery(new QuerySimple<>(prefix, suffix));
        return answer;
    }
    
    

}
