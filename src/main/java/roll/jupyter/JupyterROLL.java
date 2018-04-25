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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import automata.FiniteAutomaton;
import dk.brics.automaton.Automaton;
import jupyter.Displayer;
import jupyter.Displayers;
import jupyter.MIMETypes;
import mainfiles.RABIT;
import roll.automata.DFA;
import roll.automata.FASimple;
import roll.automata.NBA;
import roll.automata.operations.DFAOperations;
import roll.learner.LearnerBase;
import roll.learner.dfa.table.LearnerDFATableColumn;
import roll.learner.dfa.table.LearnerDFATableLStar;
import roll.learner.dfa.tree.LearnerDFATreeColumn;
import roll.learner.dfa.tree.LearnerDFATreeKV;
import roll.learner.nba.ldollar.LearnerNBALDollar;
import roll.learner.nba.lomega.LearnerNBALOmega;
import roll.main.IHTML;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.oracle.TeacherAbstract;
import roll.oracle.dfa.TeacherDFA;
import roll.oracle.dfa.dk.TeacherDFADK;
import roll.oracle.nba.TeacherNBA;
import roll.oracle.nba.rabit.TeacherNBARABIT;
import roll.oracle.nba.rabit.UtilRABIT;
import roll.query.Query;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Alphabet;

/**
 * @author Jianlin Li, Yong Li (liyong@ios.ac.cn)
 * */

public class JupyterROLL {
    static {
        register();
    }
    
    private static void register(){
        Displayers.register(IHTML.class, new Displayer<IHTML>() {
            @Override
            public Map<String, String> display(IHTML html) {
                return new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put(MIMETypes.HTML, html.toHTML());
                    }
                };
            }
        });
    }
    
    public static Alphabet alphabet;
    
    public static void createAlphabet(List<Character> array) {
        alphabet = new Alphabet();
        for(Character letter : array) {
            alphabet.addLetter(letter);
        }
    }
    
    private static void verifyAlphabet() {
        if(alphabet == null) throw new UnsupportedOperationException("Alphabet is empty, use createAlphabet function");
    }
    
    public static NBA createNBA() {
        verifyAlphabet();
        return new NBA(alphabet);
    }
    
    public static DFA createDFA() {
        verifyAlphabet();
        return new DFA(alphabet);
    }
    
    // ==============================================================================================================
    
    public static List<Triple> learningSeq(
            String algo, String structure, FASimple target) {
        // first choose learning algorithm and teacher
        Options options = parseOptions(algo, structure);
        TeacherAbstract<? extends FASimple> teacher = getTeacher(options, target);
        LearnerBase<? extends FASimple> learner = getLearner(options, target.getAlphabet(), teacher);
        ArrayList<Triple> sequence = new ArrayList<>();
        // learning loop
        learner.startLearning();
        Query<HashableValue> ceQuery = null;
        while(true) {
            // along with ce
            FASimple hypothesis = learner.getHypothesis();
            Triple triple = null;
            String learnerStr = learner.toHTML();
            triple = new Triple(learnerStr,
                    hypothesis.toHTML(), ceQuery);
            if(hypothesis instanceof NBA) {
                TeacherNBA teacherNBA = (TeacherNBA)teacher;
                ceQuery = teacherNBA.answerEquivalenceQuery((NBA)hypothesis);
            }else {
                TeacherDFA teacherDFA = (TeacherDFA)teacher;
                ceQuery = teacherDFA.answerEquivalenceQuery((DFA)hypothesis);
            }
            sequence.add(triple);
            boolean isEq = ceQuery.getQueryAnswer().get();
            if(isEq) {
                break;
            }
            ceQuery.answerQuery(null);
            learner.refineHypothesis(ceQuery);
        }
        return sequence;
    }
    
    private static Options parseOptions(String algo, String structure) {
        Options options = new Options();
        switch(algo) {
        case "periodic":
            options.algorithm = Options.Algorithm.PERIODIC;
            options.automaton = Options.TargetAutomaton.NBA;
            break;
        case "syntactic":
            options.algorithm = Options.Algorithm.SYNTACTIC;
            options.automaton = Options.TargetAutomaton.NBA;
            break;
        case "recurrent":
            options.algorithm = Options.Algorithm.RECURRENT;
            options.automaton = Options.TargetAutomaton.NBA;
            break;
        case "ldollar":
            options.algorithm = Options.Algorithm.NBA_LDOLLAR;
            options.automaton = Options.TargetAutomaton.NBA;
            break;
        case "lstar":
            options.algorithm = Options.Algorithm.DFA_LSTAR;
            options.automaton = Options.TargetAutomaton.DFA;
            break;
        case "kv":
            options.algorithm = Options.Algorithm.DFA_KV;
            options.automaton = Options.TargetAutomaton.DFA;
            break;
        case "column":
            options.algorithm = Options.Algorithm.DFA_COLUMN;
            options.automaton = Options.TargetAutomaton.DFA;
            break;
        default:
                throw new UnsupportedOperationException("Unknown learning algorithm");
        }
        switch(structure) {
        case "table":
            options.structure = Options.Structure.TABLE;
            break;
        case "tree":
            options.structure = Options.Structure.TREE;
            break;
        default:
            throw new UnsupportedOperationException("Unknown data structure");
        }
        
        return options;
    }
    
    private static TeacherAbstract<? extends FASimple> getTeacher(Options options, FASimple target) {
        if((target instanceof NBA) && (options.algorithm == Options.Algorithm.NBA_LDOLLAR
                || options.algorithm == Options.Algorithm.PERIODIC
                || options.algorithm == Options.Algorithm.SYNTACTIC
                || options.algorithm == Options.Algorithm.RECURRENT)
            ) {
               return new TeacherNBARABIT(options, (NBA)target);
           }else if((target instanceof DFA) && (options.algorithm == Options.Algorithm.DFA_COLUMN
                || options.algorithm == Options.Algorithm.DFA_LSTAR
                || options.algorithm == Options.Algorithm.DFA_KV)) {
               return new TeacherDFADK(options, (DFA)target);
           }else {
               throw new UnsupportedOperationException("Unsupported Learning Target");
           }
    }
    
    private static LearnerBase<? extends FASimple> getLearner(Options options, Alphabet alphabet,
            MembershipOracle<HashableValue> teacher) {
        LearnerBase<? extends FASimple> learner = null;
        if(options.algorithm == Options.Algorithm.NBA_LDOLLAR) {
            learner = (LearnerBase<? extends FASimple>)new LearnerNBALDollar(options, alphabet, teacher);
        }else if(options.algorithm == Options.Algorithm.PERIODIC
             || options.algorithm == Options.Algorithm.SYNTACTIC
             || options.algorithm == Options.Algorithm.RECURRENT) {
            learner = new LearnerNBALOmega(options, alphabet, teacher);
        }else if(options.algorithm == Options.Algorithm.DFA_COLUMN) {
            if(options.structure == Options.Structure.TABLE) {
                learner = new LearnerDFATableColumn(options, alphabet, teacher);
            }else {
                learner = new LearnerDFATreeColumn(options, alphabet, teacher);
            }
        }else if(options.algorithm == Options.Algorithm.DFA_LSTAR) {
            learner = new LearnerDFATableLStar(options, alphabet, teacher);
        }else if(options.algorithm == Options.Algorithm.DFA_KV) {
            learner = new LearnerDFATreeKV(options, alphabet, teacher);
        }else {
            throw new UnsupportedOperationException("Unsupported Learner");
        }
        
        return learner;
    }
    // ==============================================================================================================
    // interactive learning
    // create NBA learner
    public static NBALearner createNBALearner(String algo, String structure, BiFunction<String, String, Boolean> mqFunc) {
        Options options = parseOptions(algo, structure);
        if(options.automaton != Options.TargetAutomaton.NBA) {
            throw new UnsupportedOperationException("Unsupported BA learner");
        }
        verifyAlphabet();
        MembershipOracle<HashableValue> mqOracle = new MQOracle(mqFunc);
        @SuppressWarnings("unchecked")
        LearnerBase<NBA> learner = (LearnerBase<NBA>) getLearner(options, alphabet, mqOracle);
        learner.startLearning();
        return new NBALearner(alphabet, learner, mqOracle);
    }
    
    // create DFA learner
    public static DFALearner createDFALearner(String algo, String structure, Function<String, Boolean> mqFunc) {
        Options options = parseOptions(algo, structure);
        if(options.automaton != Options.TargetAutomaton.DFA) {
            throw new UnsupportedOperationException("Unsupported DFA learner");
        }
        verifyAlphabet();
        MembershipOracle<HashableValue> mqOracle = new MQOracle(mqFunc);
        @SuppressWarnings("unchecked")
        LearnerBase<DFA> learner = (LearnerBase<DFA>) getLearner(options, alphabet, mqOracle);
        learner.startLearning();
        return new DFALearner(alphabet, learner, mqOracle);
    }
    
    private static class MQOracle implements MembershipOracle<HashableValue> {
        private Function<Query<HashableValue>,Boolean>  delegate;

        MQOracle(BiFunction<String, String, Boolean> f) {
            this.delegate = (query -> f.apply(
                    query.getPrefix().toStringWithAlphabet(),
                    query.getSuffix().toStringWithAlphabet()
                )
            );
        }

        MQOracle(Function<String, Boolean> f) {
            this.delegate = (query -> f.apply(
                    query.getQueriedWord().toStringWithAlphabet()
                )
            );
        }

        @Override
        public HashableValue answerMembershipQuery(Query<HashableValue> query) {
            return new HashableValueBoolean(this.delegate.apply(query));
        }


    }
    
    // ==============================================================================================================
    // complement Buchi automaton
    public static NBA complement(String algo, String structure, NBA nba) {
        Options options = parseOptions(algo, structure);
        if(options.automaton != Options.TargetAutomaton.NBA) {
            throw new UnsupportedOperationException("Unsupported BA learner");
        }
        if(options.algorithm != Options.Algorithm.PERIODIC
        && options.algorithm != Options.Algorithm.RECURRENT
        && options.algorithm != Options.Algorithm.SYNTACTIC) {
            throw new UnsupportedOperationException("Unsupported BA learner");
        }
        
        return roll.main.ROLL.complement(options, nba);
    }
    
    // check equivalence
    public static String checkEquivalence(DFA A, DFA B) {
        Automaton dkA = DFAOperations.toDkDFA(A);
        Automaton dkB = DFAOperations.toDkDFA(B);
        Automaton result = dkA.clone().minus(dkB.clone());
        String counterexample = result.getShortestExample(true);
        if(A.getAlphabet() != B.getAlphabet()) {
            System.err.println("A and B donot share the same alphabet");
            return null;
        }
        
        if(counterexample != null) {
            return counterexample;
        }
        
        result = dkB.clone().minus(dkA.clone());
        counterexample = result.getShortestExample(true);
        if(counterexample != null) {
            return counterexample;
        }
        return null;
    }
    
    // check equivalence
    public static Pair<String, String> checkEquivalence(NBA A, NBA B) {
        FiniteAutomaton rabitA = UtilRABIT.toRABITNBA(A);
        FiniteAutomaton rabitB = UtilRABIT.toRABITNBA(B);
        boolean inclusion = RABIT.isIncluded(rabitA, rabitB);
        if(inclusion) {
            inclusion = RABIT.isIncluded(rabitB, rabitA);
            if(inclusion) return null;
        }
        String prefixStr = RABIT.getPrefix();
        String suffixStr = RABIT.getSuffix();
        
        return new Pair<>(prefixStr, suffixStr);
    }
}
