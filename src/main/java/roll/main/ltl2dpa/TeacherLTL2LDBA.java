package roll.main.ltl2dpa;

import roll.automata.FDFA;
import roll.automata.NBA;
import roll.main.Options;
import roll.main.complement.TeacherNBAComplement;
import roll.oracle.Teacher;
import roll.query.Query;
import roll.table.HashableValue;

public class TeacherLTL2LDBA extends TeacherNBAComplement  {

	public TeacherLTL2LDBA(Options options, NBA nba) {
		super(options, nba);
	}

}
