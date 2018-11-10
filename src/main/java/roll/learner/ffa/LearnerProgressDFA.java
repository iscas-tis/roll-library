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

package roll.learner.ffa;

import roll.automata.DFA;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.learner.dfa.table.LearnerDFATableColumn;
import roll.learner.dfa.tree.LearnerDFATreeColumn;
import roll.learner.fdfa.LearnerLeading;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class LearnerProgressDFA extends LearnerBase<DFA> implements LearnerProgress<DFA> {

    protected final LearnerBase<DFA> dfaLearner;
    
    public LearnerProgressDFA(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
        super(options, alphabet, membershipOracle);
        MembershipOracle<HashableValue> mqOracle = getMembershipOracleInstance();
        if(options.structure.isTable()) {
            dfaLearner = new LearnerDFATableColumn(options, alphabet, mqOracle);
        }else {
            dfaLearner = new LearnerDFATreeColumn(options, alphabet, mqOracle);
        }
    }
    
    protected MembershipOracle<HashableValue> getMembershipOracleInstance() {
        return null;
    }

    @Override
    public LearnerType getLearnerType() {
        return null;
    }

    @Override
    public void refineHypothesis(Query<HashableValue> query) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String toHTML() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Word getLeadingLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LearnerLeading getLearnerLeading() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashableValue prepareRowHashableValue(boolean mqResult, Word x, Word e) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLeadingState() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected Query<HashableValue> makeMembershipQuery(ObservationRow row, int offset, ExprValue exprValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ExprValue getInitialColumnExprValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void initialize() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void constructHypothesis() {
        // TODO Auto-generated method stub
        
    }
}
