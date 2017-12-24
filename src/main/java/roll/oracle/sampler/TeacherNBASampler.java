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

package roll.oracle.sampler;

import roll.automata.NBA;
import roll.automata.operations.NBAOperations;
import roll.main.Options;
import roll.oracle.Teacher;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class TeacherNBASampler implements Teacher<NBA, Query<HashableValue>, HashableValue> {
    
    private final Options options;
    private final NBA target;
    private final long numOfSamples;
    
    public TeacherNBASampler(Options options, NBA target) {
        this.options = options;
        this.target = target;
        this.numOfSamples = MonteCarloSampler.getSampleSize(options.epsilon, options.delta);
    }

    @Override
    public HashableValue answerMembershipQuery(Query<HashableValue> query) {
        Word prefix = query.getPrefix();
        Word suffix = query.getSuffix();
        boolean answer = NBAOperations.accepts(target, prefix, suffix);
        return new HashableValueBoolean(answer);
    }

    @Override
    public Query<HashableValue> answerEquivalenceQuery(NBA hypothesis) {
        // sample words from hypothesis
        for (int i = 0; i < numOfSamples; i++) {
            Pair<Pair<Word, Word>, Boolean> result = MonteCarloSampler.getRandomLasso(hypothesis);
            Pair<Word, Word> word = result.getLeft();
            boolean acceptedHypo = false;
            if (result.getRight()) {
                acceptedHypo = true;
            } else {
                acceptedHypo = NBAOperations.accepts(hypothesis, word.getLeft(), word.getRight());
            }
            boolean acceptedTgt = NBAOperations.accepts(target, word.getLeft(), word.getRight());
            if(acceptedHypo != acceptedTgt) {
                // found a counterexample
                Query<HashableValue> ceQuery = new QuerySimple<>(word.getLeft(), word.getRight());
                ceQuery.answerQuery(new HashableValueBoolean(false));
                return ceQuery;
            }
        }
        // sample from target
        for (int i = 0; i < numOfSamples; i++) {
            Pair<Pair<Word, Word>, Boolean> result = MonteCarloSampler.getRandomLasso(target);
            Pair<Word, Word> word = result.getLeft();
            boolean acceptedTgt = false;
            if (result.getRight()) {
                acceptedTgt = true;
            } else {
                acceptedTgt = NBAOperations.accepts(target, word.getLeft(), word.getRight());
            }
            boolean acceptedHypo = NBAOperations.accepts(hypothesis, word.getLeft(), word.getRight());
            if(acceptedHypo != acceptedTgt) {
                // found a counterexample
                // found a counterexample
                Query<HashableValue> ceQuery = new QuerySimple<>(word.getLeft(), word.getRight());
                ceQuery.answerQuery(new HashableValueBoolean(false));
                return ceQuery;
            }
        }
        Word wordEmpty = target.getAlphabet().getEmptyWord();
        // found a counterexample
        Query<HashableValue> ceQuery = new QuerySimple<>(wordEmpty, wordEmpty);
        ceQuery.answerQuery(new HashableValueBoolean(true));
        return ceQuery;
    }

}
