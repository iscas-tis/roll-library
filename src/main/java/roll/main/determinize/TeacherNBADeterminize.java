package roll.main.determinize;

import roll.automata.FDFA;
import roll.oracle.Teacher;
import roll.query.Query;
import roll.table.HashableValue;

public class TeacherNBADeterminize implements Teacher<FDFA, Query<HashableValue>, HashableValue> {

	@Override
	public HashableValue answerMembershipQuery(Query<HashableValue> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query<HashableValue> answerEquivalenceQuery(FDFA hypothesis) {
		// TODO Auto-generated method stub
		return null;
	}

}
