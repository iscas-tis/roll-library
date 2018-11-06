/* Copyright (c) 2016, 2017, 2018                                         */
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
package roll.jupyter;

import roll.automata.DFA;
import roll.automata.FDFA;
import roll.learner.LearnerBase;
import roll.main.IHTML;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class FDFALearner implements JupyterLearner<FDFA>, IHTML {

    private LearnerBase<FDFA> learner;
    private MembershipOracle<HashableValue> mqOracle;
    private Alphabet alphabet;
    public FDFALearner(Alphabet alphabet
            , LearnerBase<FDFA> learner
            , MembershipOracle<HashableValue> mqOracle) {
        assert learner != null;
        assert alphabet != null;
        assert mqOracle != null;
        this.alphabet = alphabet;
        this.learner = learner;
        this.mqOracle = mqOracle;
    }
    
    @Override
    public String toString() {
        return learner.toString();
    }
    
    @Override
    public String toHTML() {
        return learner.toHTML();
    }

    @Override
    public FDFA getHypothesis() {
        return learner.getHypothesis();
    }
    
    public void refineHypothesis(String stem, String loop) {

        Word p = alphabet.getWordFromString(stem);
        Word s = alphabet.getWordFromString(loop);
        FDFA hypothesis = getHypothesis();
        Pair<Word, Word> normForm = getNormalizedFactorization(hypothesis.getLeadingFA(), p, s);
        // now verify counterexample
        Word prefix = normForm.getLeft();
        Word suffix = normForm.getRight();
        boolean isInHypo = hypothesis.getAcc().accept(prefix, suffix);
        Query<HashableValue> ceQuery = new QuerySimple<>(prefix, suffix);
        HashableValue isInTarget = mqOracle.answerMembershipQuery(ceQuery);
        if(isInHypo && isInTarget.isAccepting()) {
            System.err.println("Invalid counterexample, both in hypothesis and target");
            return ;
        }
        
        if(!isInHypo && !isInTarget.isAccepting()) {
            System.err.println("Invalid counterexample, neither in hypothesis or target");
            return ;
        }
        ceQuery.answerQuery(null);
        learner.refineHypothesis(ceQuery);
    }
    
    
    private Pair<Word, Word> getNormalizedFactorization(DFA M, Word u, Word v) {
        int num = M.getStateSize() + 1;
        int i, j = 0;
        int uState = M.getSuccessor(u);
        boolean flag = false;
        // 0 <= i < j such that uv^i ~ uv^{i + j} and i + j < |M| + 1 
        for(i = 0; i <= num / 2; i ++) {
            int uvi = uState;
            int k = i;
            while(k > 0) {
                uvi = M.getSuccessor(uvi, v);
                k --;
            }
            for(j = i + 1; i + j < num; j ++) {
                k = j;
                int uvivj = uvi;
                while(k > 0) {
                    uvivj = M.getSuccessor(uvivj, v);
                    k --;
                }
                if(uvi == uvivj) {
                    flag = true;
                    break;
                }
            }
            if(flag) break;
        }
        // get i v
        Word prefix = u;
        while(i > 0) {
            prefix = prefix.concat(v);
            i --;
        }
        
        Word suffix = v;
        while(j > 1) {
            suffix = suffix.concat(v);
            j --;
        }
        
        return new Pair<>(prefix, suffix);
    }
}
