package roll.main.ltlf2dfa;

import roll.automata.DFA;
import roll.main.Options;
import roll.oracle.TeacherAbstract;
import roll.query.Query;
import roll.table.HashableValue;

public class TeacherLTLf2DFA extends TeacherAbstract<DFA> {

	public TeacherLTLf2DFA(Options options, String ltlf) {
		super(options);
	}

	@Override
	protected HashableValue checkMembership(Query<HashableValue> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Query<HashableValue> checkEquivalence(DFA hypothesis) {
		// TODO Auto-generated method stub
		return null;
	}

}
