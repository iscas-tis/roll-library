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

package roll.oracle.nba.sampler;

import roll.automata.NBA;
import roll.main.Options;
import roll.oracle.nba.TeacherNBA;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class TeacherNBASampler extends TeacherNBA {
    
    private final SamplerIndexedMonteCarlo sampler;
    
    public TeacherNBASampler(Options options, NBA target) {
        super(options, target);
        this.sampler = new SamplerIndexedMonteCarlo(options.epsilon, options.delta);
    }
    
    private boolean isEmptyNBA(NBA nba) {
        if(nba.getStateSize() > 1) {
            return false;
        }
        return nba.getState(0).getEnabledLetters().cardinality() == 0;
    }

    @Override
    protected Query<HashableValue> checkEquivalence(NBA hypothesis) {
        // sample words from hypothesis
        Query<HashableValue> ceQuery = null;
        NBA A, B;
        if(target.getStateSize() > hypothesis.getStateSize()) {
            A = hypothesis;
            B = target;
        }else {
            A = target;
            B = hypothesis;
        }
        
        if(!isEmptyNBA(A)) {
            sampler.K = B.getStateSize();
            ceQuery = NBAInclusionSampler.isIncluded(A, B, sampler);
        }
        if(ceQuery != null) return ceQuery;
        
        if(!isEmptyNBA(B)) {
            sampler.K = A.getStateSize();
            ceQuery = NBAInclusionSampler.isIncluded(B, A, sampler);
        }
        if(ceQuery != null) return ceQuery;
        
        Word wordEmpty = target.getAlphabet().getEmptyWord();
        // found a counterexample
        ceQuery = new QuerySimple<>(wordEmpty, wordEmpty);
        ceQuery.answerQuery(new HashableValueBoolean(true));
        return ceQuery;
    }

}
