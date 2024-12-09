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


import java.util.ArrayList;
import java.util.List;

import roll.learner.LearnerType;
import roll.learner.fdfa.LearnerLeading;
import roll.learner.fdfa.LearnerProgressLimit;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.table.ObservationRow;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LearnerProgressTableLimit extends LearnerProgressTable implements LearnerProgressLimit {

    public LearnerProgressTableLimit(Options options, Alphabet alphabet,
            MembershipOracle<HashableValue> membershipOracle, LearnerLeading learnerLeading, int state) {
        super(options, alphabet, membershipOracle, learnerLeading, state);
    }

    @Override
    public LearnerType getLearnerType() {
        return LearnerType.FDFA_LIMIT_TABLE;
    }
    
    @Override
    public void startLearning() {
        initialize();
    }

    // we have to be careful about the initial table with only
    // one row epsilon in 
	@Override
	public List<Query<HashableValue>> computeMark() {
		List<ObservationRow> upperTable = observationTable.getUpperTable();
        List<ExprValue> columns = observationTable.getColumns();
        
        int conflict = 0;
        Query<HashableValue> pos = null;
        Query<HashableValue> neg = null;

		for (int j = 0; j < columns.size() && conflict < 3; j++) {
			Word wordE = columns.get(j).get();
			for (int i = 0; i < upperTable.size() && conflict < 3; i++) {
				Word wordX = upperTable.get(i).getWord();
				Word loop = wordX.concat(wordE);
				// ignore empty loop word
				if (loop.isEmpty())
					continue;
				HashableValue mq = upperTable.get(i).getValues().get(j);
				// we need to check the value
				if ((Boolean) mq.getLeft()) {
					// now we check whether it is accepting or not
					if ((Boolean) mq.getRight()) {
						pos = new QuerySimple<>(alphabet.getEmptyWord(), loop);
						pos.answerQuery(new HashableValueBoolean(true));
						conflict |= 2;
					} else {
						neg = new QuerySimple<>(alphabet.getEmptyWord(), loop);
						neg.answerQuery(new HashableValueBoolean(false));
						conflict |= 1;
					}
				}
			}
		}

		List<Query<HashableValue>> res = new ArrayList<>();
		if (pos != null) {
			res.add(pos);
		}
		if (neg != null) {
			res.add(neg);
		}

		return res;
	}

}
