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

package roll.learner.fdfa.table;

import roll.learner.LearnerType;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.words.Alphabet;
import roll.words.Word;

public class LearnerProgressTablePeriodic extends LearnerProgressTable {

    public LearnerProgressTablePeriodic(Options options, Alphabet alphabet,
            MembershipOracle<HashableValue> membershipOracle, Word label) {
        super(options, alphabet, membershipOracle, label);
       
    }

    @Override
    public Word getLeadingLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Word getStateLabel(Word word) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LearnerType getLearnerType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected CeAnalyzer getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
        // TODO Auto-generated method stub
        return null;
    }

}
