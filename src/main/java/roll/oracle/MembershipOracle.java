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

package roll.oracle;

import java.util.ArrayList;
import java.util.List;

import roll.query.Query;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public interface MembershipOracle<O> {
	
	O answerMembershipQuery(Query<O> query);
	
	default List<O> answerMembershipQueries(@SuppressWarnings("unchecked") Query<O>... queries) {
		List<O> answers = new ArrayList<>();
		for(Query<O> query : queries) {
			answers.add(answerMembershipQuery(query));
		}
		return answers;
	}
	
	default List<O> answerMembershipQueries(List<Query<O>> queries) {
		List<O> answers = new ArrayList<>();
		for(Query<O> query : queries) {
			answers.add(answerMembershipQuery(query));
		}
		return answers;
	}
}
