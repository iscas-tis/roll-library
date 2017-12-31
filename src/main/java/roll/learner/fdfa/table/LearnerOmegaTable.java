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

import roll.learner.dfa.table.LearnerDFATable;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

abstract class LearnerOmegaTable extends LearnerDFATable {
    
    public LearnerOmegaTable(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
    }

    protected Query<HashableValue> getQuerySimple(ObservationRow row, Word prefix, Word suffix, int column) {
        return new QuerySimple<>(row, prefix, suffix, column);
    }

    protected HashableValue processMembershipQuery(ObservationRow row, Word prefix, Word suffix, int column) {
        return membershipOracle.answerMembershipQuery(getQuerySimple(row, prefix, suffix, column));
    }
    
    @Override
    protected CeAnalyzer getCeAnalyzerInstance(ExprValue exprValue, HashableValue result) {
        return new CeAnalyzerTable(exprValue, result);
    }

}
