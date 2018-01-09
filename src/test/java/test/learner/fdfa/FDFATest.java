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

package test.learner.fdfa;

import java.util.ArrayList;

import org.junit.Test;

import roll.automata.DFA;
import roll.automata.FDFA;
import roll.learner.fdfa.LearnerFDFA;
import roll.learner.fdfa.table.LearnerFDFATablePeriodic;
import roll.learner.fdfa.table.LearnerFDFATableRecurrent;
import roll.learner.fdfa.table.LearnerFDFATableSyntactic;
import roll.main.Options;
import roll.oracle.fdfa.dk.TeacherFDFADK;
import roll.query.Query;
import roll.table.HashableValue;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class FDFATest {
    
    private DFA getLeadDFA() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        
        DFA leadDFA = new DFA(alphabet);
        leadDFA.createState();
        leadDFA.createState();
        leadDFA.createState();
        leadDFA.createState();
        leadDFA.createState();
        int fst = 0, snd = 1, thd = 2, fur = 3, fiv = 4;
        leadDFA.getState(fst).addTransition(0, snd);
        leadDFA.getState(fst).addTransition(1, thd);
        leadDFA.getState(snd).addTransition(0, fur);
        leadDFA.getState(snd).addTransition(1, fiv);
        leadDFA.getState(thd).addTransition(0, thd);
        leadDFA.getState(thd).addTransition(1, thd);
        leadDFA.getState(fur).addTransition(0, fur);
        leadDFA.getState(fur).addTransition(1, thd);
        leadDFA.getState(fiv).addTransition(0, thd);
        leadDFA.getState(fiv).addTransition(1, fiv);
        leadDFA.setInitial(fst);
        System.out.println(leadDFA.toString());
        return leadDFA;
    }
    
    private DFA getEpsilonDFA(Alphabet alphabet) {
        DFA proDFA = new DFA( alphabet);
        proDFA.createState();
        proDFA.createState();
        proDFA.createState();
        int fst = 0, snd = 1, thd = 2;
        
        proDFA.getState(fst).addTransition(0, snd);
        proDFA.getState(fst).addTransition(1, thd);
        proDFA.getState(snd).addTransition(0, snd);
        proDFA.getState(snd).addTransition(1, thd);
        proDFA.getState(thd).addTransition(0, thd);
        proDFA.getState(thd).addTransition(1, thd);
        proDFA.setInitial(fst);
        proDFA.setFinal(snd);
        return proDFA;
    }
    
    private DFA getADFA(Alphabet alphabet) {
        DFA proDFA = new DFA( alphabet);
        proDFA.createState();
        proDFA.createState();
        proDFA.createState();
        proDFA.createState();
        int fst = 0, snd = 1, thd = 2, fur = 3;
        
        proDFA.getState(fst).addTransition(0, snd);
        proDFA.getState(fst).addTransition(1, thd);
        proDFA.getState(snd).addTransition(0, snd);
        proDFA.getState(snd).addTransition(1, fur);
        proDFA.getState(thd).addTransition(0, fur);
        proDFA.getState(thd).addTransition(1, thd);
        proDFA.getState(fur).addTransition(0, fur);
        proDFA.getState(fur).addTransition(1, fur);
        proDFA.setInitial(fst);
        proDFA.setFinal(thd);
        proDFA.setFinal(snd);
        return proDFA;
    }
    
    private DFA getABDFA(Alphabet alphabet) {
        DFA proDFA = new DFA( alphabet);
        proDFA.createState();
        proDFA.createState();
        proDFA.createState();
        int fst = 0, snd = 1, thd = 2;
        
        proDFA.getState(fst).addTransition(0, snd);
        proDFA.getState(fst).addTransition(1, thd);
        proDFA.getState(snd).addTransition(0, snd);
        proDFA.getState(snd).addTransition(1, snd);
        proDFA.getState(thd).addTransition(0, snd);
        proDFA.getState(thd).addTransition(1, thd);
        proDFA.setInitial(fst);
        proDFA.setFinal(thd);
        return proDFA;
    }

    private DFA getEmptyDFA(Alphabet alphabet) {
        DFA leadDFA = new DFA( alphabet);
        leadDFA.createState();
        for(int letter = 0; letter < alphabet.getLetterSize(); letter ++) {
            leadDFA.getState(0).addTransition(letter, 0);
        }
        leadDFA.setInitial(0);
        return leadDFA;
    }
    
    private FDFA getFDFA() {
        DFA leadDFA = getLeadDFA();
        ArrayList<DFA> proDFAs = new ArrayList<>();
        proDFAs.add(getEmptyDFA(leadDFA.getAlphabet()));
        proDFAs.add(getADFA(leadDFA.getAlphabet()));
        proDFAs.add(getEmptyDFA(leadDFA.getAlphabet()));
        proDFAs.add(getEpsilonDFA(leadDFA.getAlphabet()));
        proDFAs.add(getABDFA(leadDFA.getAlphabet()));
        return new FDFA(leadDFA, proDFAs);
    }
    
    @Test
    public void testFDFAPeriodic() {
        FDFA fdfa = getFDFA();
        System.out.println("FDFA \n" + fdfa.toString());
        Alphabet alphabet = fdfa.getAlphabet();
        Options options = new Options();
        TeacherFDFADK teacher = new TeacherFDFADK(options, fdfa);
        LearnerFDFA learner = null;
        
        options.algorithm = Options.Algorithm.RECURRENT;
        if(options.algorithm == Options.Algorithm.PERIODIC) {
            learner = new LearnerFDFATablePeriodic(options, alphabet, teacher); 
        }else if(options.algorithm == Options.Algorithm.SYNTACTIC){
            learner = new LearnerFDFATableSyntactic(options, alphabet, teacher); 
        }else {
            learner = new LearnerFDFATableRecurrent(options, alphabet, teacher); 
        }
        System.out.println("starting learning");
        learner.startLearning();

        while(true) {
            System.out.println("Table is both closed and consistent\n" + learner.toString());
            FDFA model = learner.getHypothesis();
            System.out.println("FDFA \n" + model.toString());
            // along with ce
            Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(model);
            boolean isEq = ceQuery.getQueryAnswer().get();
            if(isEq) {
                System.out.println(model.toString());
                break;
            }
            ceQuery.answerQuery(null);
//          HashableValue val = teacher.answerMembershipQuery(ceQuery);
//          ceQuery.answerQuery(val);
            learner.refineHypothesis(ceQuery);
        }
    }

}
