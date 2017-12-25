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
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBAInclusionSampler {
    
    private NBAInclusionSampler() {
        
    }
    
    public static Query<HashableValue> isIncluded(NBA A, NBA B, Sampler sampler) {
        sampler.setNBA(A);
        for (int i = 0; i < sampler.getSampleSize(); i++) {
            Pair<Pair<Word, Word>, Boolean> result = sampler.getRandomLasso();
            Pair<Word, Word> word = result.getLeft();
            boolean needCheck = false;
            if (result.getRight()) {
                needCheck = true;
            } else {
                needCheck = NBAOperations.accepts(A, word.getLeft(), word.getRight());
            }

            if(needCheck) {
                // found a counterexample
                boolean acc = NBAOperations.accepts(B, word.getLeft(), word.getRight());
                if(! acc) {
                    Query<HashableValue> ceQuery = new QuerySimple<>(word.getLeft(), word.getRight());
                    ceQuery.answerQuery(new HashableValueBoolean(false));
                    return ceQuery;
                }
            }
        }
        sampler.setNBA(B);
        for (int i = 0; i < sampler.getSampleSize(); i++) {
            Pair<Pair<Word, Word>, Boolean> result = sampler.getRandomLasso();
            Pair<Word, Word> word = result.getLeft();
            boolean needCheck = false;
            if (result.getRight()) {
                needCheck = false;
            } else {
                needCheck = !NBAOperations.accepts(B, word.getLeft(), word.getRight());
            }

            if(needCheck) {
                // found a counterexample
                boolean acc = NBAOperations.accepts(A, word.getLeft(), word.getRight());
                if(acc) {
                    Query<HashableValue> ceQuery = new QuerySimple<>(word.getLeft(), word.getRight());
                    ceQuery.answerQuery(new HashableValueBoolean(false));
                    return ceQuery;
                }
            }
        }
        return null;
    }
}
