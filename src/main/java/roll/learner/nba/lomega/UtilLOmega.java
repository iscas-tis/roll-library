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

package roll.learner.nba.lomega;

import dk.brics.automaton.Automaton;
import roll.automata.FDFA;
import roll.automata.NBA;
import roll.automata.operations.FDFAOperations;
import roll.automata.operations.NBAOperations;
import roll.learner.fdfa.LearnerFDFA;
import roll.learner.fdfa.table.LearnerFDFATablePeriodic;
import roll.learner.fdfa.table.LearnerFDFATableRecurrent;
import roll.learner.fdfa.table.LearnerFDFATableSyntactic;
import roll.learner.nba.lomega.translator.TranslatorFDFA;
import roll.learner.nba.lomega.translator.TranslatorFDFAOver;
import roll.learner.nba.lomega.translator.TranslatorFDFAUnder;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.table.HashableValue;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class UtilLOmega {
    
    static LearnerFDFA getLearnerFDFA(Options options, Alphabet alphabet
            , MembershipOracle<HashableValue> membershipOracle) {
        LearnerFDFA fdfaLearner = null;
        if(options.structure.isTable()) {
            switch(options.algorithm) {
            case PERIODIC:
                fdfaLearner = new LearnerFDFATablePeriodic(options, alphabet, membershipOracle);
                break;
            case SYNTACTIC:
                fdfaLearner = new LearnerFDFATableSyntactic(options, alphabet, membershipOracle);
                break;
            case RECURRENT:
                fdfaLearner = new LearnerFDFATableRecurrent(options, alphabet, membershipOracle);
                break;
            default:
                throw new UnsupportedOperationException("Unknown FDFA learner");
            }
        }else {
            switch(options.algorithm) {
            case PERIODIC:
                fdfaLearner = null;
                break;
            case SYNTACTIC:
                fdfaLearner = null;
                break;
            case RECURRENT:
                fdfaLearner = null;
                break;
            default:
                throw new UnsupportedOperationException("Unknown FDFA learner");
            }
        }
        return fdfaLearner;
    }
    
    public static NBA constructNBA(Options options, FDFA fdfa) {
        Automaton dkAut = null;
        Alphabet alphabet = fdfa.getAlphabet();
        if(options.approximation == Options.Approximation.OVER) {
            dkAut = FDFAOperations.buildOverNBA(fdfa);
        }else if(options.approximation == Options.Approximation.UNDER){
            dkAut = FDFAOperations.buildUnderNBA(fdfa);
        }else {
            throw new UnsupportedOperationException("Unknown approximation for fdfa");
        }
        NBA nba = NBAOperations.fromDkNBA(dkAut, alphabet);
        return nba;
    }
    
    public static TranslatorFDFA getTranslator(Options options
            , LearnerFDFA fdfaLearner, MembershipOracle<HashableValue> membershipOracle) {
        TranslatorFDFA translator = null;
        if(options.approximation == Options.Approximation.OVER) {
            translator = new TranslatorFDFAOver(fdfaLearner, membershipOracle);
        }else if(options.approximation == Options.Approximation.UNDER){
            translator = new TranslatorFDFAUnder(fdfaLearner);
        }else {
            throw new UnsupportedOperationException("Unknown approximation for fdfa");
        }
        return translator;
    }

}
