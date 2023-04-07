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

package roll.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import dk.brics.automaton.Automaton;
import roll.automata.NFA;
import roll.automata.operations.FDFAOperations;
import roll.learner.LearnerBase;
import roll.learner.dfa.table.LearnerDFATableColumn;
import roll.learner.dfa.table.LearnerDFATableLStar;
import roll.learner.dfa.tree.LearnerDFATreeColumn;
import roll.learner.dfa.tree.LearnerDFATreeKV;
import roll.learner.nba.ldollar.LearnerNBALDollar;
import roll.learner.nba.lomega.LearnerNBALOmega;
import roll.learner.nfa.nlstar.LearnerNFANLStar;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.words.Alphabet;
import roll.words.Word;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class InteractiveMode {
    
    public static void interact(Options options) {
        // prepare the alphabet
        Alphabet alphabet = prepareAlphabet(options);
        KnowledgeBase kb = new KnowledgeBase();
        MembershipOracle<HashableValue> teacher = getMembershipOracle(options, kb);
        LearnerBase<? extends NFA> learner = getLearner(options, alphabet, teacher);
        
        options.log.println("Initializing learning...");
        learner.startLearning();
        boolean result = false;
        while(! result ) {
            options.log.verbose("Table/Tree is both closed and consistent\n" + learner.toString());
            NFA hypothesis = learner.getHypothesis();
            // along with ce
            System.out.println("Resolving equivalence query for hypothesis (#Q=" + hypothesis.getStateSize() + ")...  ");
            Query<HashableValue> ceQuery = answerEquivalenceQuery(hypothesis);
            boolean isEq = ceQuery.getQueryAnswer().get();
            if(isEq == true) break;
            ceQuery = getCounterexample(options, alphabet);
            ceQuery.answerQuery(null);
            learner.refineHypothesis(ceQuery);
        }
        
        System.out.println("Congratulations! Learning completed...");
    }
    
    public static MembershipOracle<HashableValue> getMembershipOracle(Options options, KnowledgeBase kb) {
        if(options.algorithm == Options.Algorithm.NBA_LDOLLAR
             || options.algorithm == Options.Algorithm.PERIODIC
             || options.algorithm == Options.Algorithm.SYNTACTIC
             || options.algorithm == Options.Algorithm.RECURRENT
             || options.algorithm == Options.Algorithm.LIMIT) {
            return new MQNBAInteractive(kb);
        }else if(options.algorithm == Options.Algorithm.DFA_COLUMN
             || options.algorithm == Options.Algorithm.DFA_LSTAR
             || options.algorithm == Options.Algorithm.DFA_KV
             || options.algorithm == Options.Algorithm.NFA_NLSTAR) {
            return new MQDFAInteractive(kb);
        }else {
            throw new UnsupportedOperationException("Unsupported Learner");
        }
    }
    
    public static Query<HashableValue> getCounterexample(Options options, Alphabet alphabet) {
        if(options.algorithm == Options.Algorithm.NBA_LDOLLAR
                || options.algorithm == Options.Algorithm.PERIODIC
                || options.algorithm == Options.Algorithm.SYNTACTIC
                || options.algorithm == Options.Algorithm.RECURRENT
                || options.algorithm == Options.Algorithm.LIMIT) {
               return getOmegaCeWord(alphabet);
           }else if(options.algorithm == Options.Algorithm.DFA_COLUMN
                || options.algorithm == Options.Algorithm.DFA_LSTAR
                || options.algorithm == Options.Algorithm.DFA_KV
                || options.algorithm == Options.Algorithm.NFA_NLSTAR) {
               return getFiniteCeWord(alphabet);
           }else {
               throw new UnsupportedOperationException("Unsupported Learning Target");
           }
    }
    
    public static LearnerBase<? extends NFA> getLearner(Options options, Alphabet alphabet,
            MembershipOracle<HashableValue> teacher) {
        LearnerBase<? extends NFA> learner = null;
        if(options.algorithm == Options.Algorithm.NBA_LDOLLAR) {
            learner = (LearnerBase<? extends NFA>)new LearnerNBALDollar(options, alphabet, teacher);
        }else if(options.algorithm == Options.Algorithm.PERIODIC
             || options.algorithm == Options.Algorithm.SYNTACTIC
             || options.algorithm == Options.Algorithm.RECURRENT
             || options.algorithm == Options.Algorithm.LIMIT) {
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
        }else if(options.algorithm == Options.Algorithm.NFA_NLSTAR){
            learner = new LearnerNFANLStar(options, alphabet, teacher);
        }else {
            throw new UnsupportedOperationException("Unsupported BA Learner");
        }
        
        return learner;
    }
    
    private static Alphabet prepareAlphabet(Options options) {
        Alphabet alphabet = new Alphabet();
        System.out.println("Please input the number of letters ('a'-'z'): ");
        int numLetters = getInteger();
        while(numLetters < 1 || numLetters > 26) {
            System.out.println("Illegal input, it should in [1..26], try again!");
            numLetters = getInteger();
        }
        for(int letterNr = 0; letterNr < numLetters; letterNr ++) {
            System.out.println("Please input the " + (letterNr + 1) + "th letter: ");
            char letter = getLetter(alphabet);
            alphabet.addLetter(letter);
        }
        return alphabet;
    }
    
    private static char getLetter(Alphabet alphabet) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        char letter = 0;
        do {
            try {
                String line = reader.readLine();
                letter = line.charAt(0);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (letter < 'a' || letter > 'z')
                System.out.println("Illegal input, try again!");
            else if(alphabet.indexOf(letter) >= 0){
                System.out.println("Duplicate input letter, try again!");
            }else {
                break;
            }
        } while (true);
        return letter;
    }
    
    

    private static boolean getInputAnswer() {
        boolean answer = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            boolean finished = false;
            while(! finished) {
                String input = reader.readLine();
                if(input.equals("1")) {
                    answer = true;
                    finished = true;
                }else if(input.equals("0")) {
                    answer = false;
                    finished = true;
                }else {
                    System.out.println("Illegal input, try again!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }
    
    private static Query<HashableValue> getFiniteCeWord(Alphabet alphabet) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Word word = null;
        try {
            do {
                System.out.println("please input counterexample: ");
                String input = reader.readLine();
                input = input.trim();
                boolean valid = true;
                for(int i = 0; i < input.length(); i ++) {
                    int letter = alphabet.indexOf(input.charAt(i));
                    if(letter < 0) {
                        valid = false;
                        break;
                    }
                }
                if(valid) {
                    word = alphabet.getWordFromString(input);
                }else  {
                    System.out.println("Illegal input, try again!");
                }
            }while(word == null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new QuerySimple<HashableValue>(word, alphabet.getEmptyWord());
    }
    
    private static int getInteger() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int numLetters = -1;
        
            do {
                String input = null;
                try {
                    input = reader.readLine();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    numLetters = Integer.parseInt(input);
                    if(numLetters < 0) {
                        numLetters = -1;
                    }
                } catch (Exception e) {
                    numLetters = -1;
                }
                if(numLetters == -1)    System.out.println("Illegal input, try again!");
            }while(numLetters == -1);
            
        return numLetters;
    }
   
    
    private static Query<HashableValue> getOmegaCeWord(Alphabet alphabet) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Word prefix = null, suffix = null;
        System.out.println("Now you have to input a counterexample for inequivalence.");
        try {
            do {
                System.out.println("please input stem: ");
                String input = reader.readLine();
                input = input.trim();
                boolean valid = true;
                for(int i = 0; i < input.length(); i ++) {
                    int letter = alphabet.indexOf(input.charAt(i));
                    if(letter < 0) {
                        valid = false;
                        break;
                    }
                }
                if(valid) {
                    prefix = alphabet.getWordFromString(input);
                }else  {
                    System.out.println("Illegal input, try again!");
                }
            }while(prefix == null);
            System.out.println("You input a stem: " + prefix.toStringWithAlphabet());
            do {
                System.out.println("please input loop: ");
                String input = reader.readLine();
                input = input.trim();
                boolean valid = true;
                for(int i = 0; i < input.length(); i ++) {
                    int letter = alphabet.indexOf(input.charAt(i));
                    if(letter < 0) {
                        valid = false;
                        break;
                    }
                }
                if(valid) {
                    suffix = alphabet.getWordFromString(input);
                } else  {
                    System.out.println("Illegal input, try again!");
                }
            }while(suffix == null);
            System.out.println("You input a loop: " + suffix.toStringWithAlphabet());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return new QuerySimple<HashableValue>(prefix, suffix);
    }
    
    private static class MQNBAInteractive implements MembershipOracle<HashableValue> {
    	
    	KnowledgeBase kb;
    	
    	public MQNBAInteractive(KnowledgeBase kb) {
    		this.kb = kb;
    	}

        @Override
        public HashableValue answerMembershipQuery(Query<HashableValue> query) {
            Word prefix = query.getPrefix();
            Word suffix = query.getSuffix();
            if(suffix.isEmpty()) {
                return new HashableValueBoolean(false);
            }
            // check whether the word is in knowledge base
            Boolean memeq = kb.isInKnowledgeBase(prefix, suffix);
            if(memeq != null) {
            	return new HashableValueBoolean(memeq);
            }else {
            	System.out.println("Is w-word (" + prefix.toStringWithAlphabet() + ", " + suffix.toStringWithAlphabet()  + ") in the unknown languge: 1/0");
                boolean answer = getInputAnswer();
                kb.add(prefix, suffix, answer);
                HashableValue result = new HashableValueBoolean(answer);
                query.answerQuery(result);
                return result;
            }
        }
    }
    
    private static class MQDFAInteractive implements MembershipOracle<HashableValue> {
    	
    	KnowledgeBase kb;
    	
    	public MQDFAInteractive(KnowledgeBase kb) {
    		this.kb = kb;
    	}

        @Override
        public HashableValue answerMembershipQuery(Query<HashableValue> query) {
            Word word = query.getQueriedWord();
            Boolean memeq = kb.isInKnowledgeBase(word);
            if(memeq != null) {
            	return new HashableValueBoolean(memeq);
            }else {
            	System.out.println("Is word " + word.toStringWithAlphabet() + " in the unknown languge: 1/0");
                boolean answer = getInputAnswer();
                kb.add(word, answer);
                HashableValue result = new HashableValueBoolean(answer);
                query.answerQuery(result);
                return result;
            }
        }
    }
    
    private static Query<HashableValue> answerEquivalenceQuery(NFA hypothesis) {
        if(hypothesis != null) {
            List<String> apList = new ArrayList<>();
            for(int i = 0; i < hypothesis.getAlphabetSize(); i ++) {
                apList.add(hypothesis.getAlphabet().getLetter(i) + "");
            }
            System.out.println("Is following automaton the unknown automaton: 1/0?");
            System.out.println(hypothesis.toString(apList));
        }else {
            System.out.println("Is above automaton the unknown automaton: 1/0?");
        }
        boolean answer = getInputAnswer();
        Word wordEmpty = hypothesis.getAlphabet().getEmptyWord();
        Query<HashableValue> ceQuery = new QuerySimple<>(wordEmpty, wordEmpty);
        ceQuery.answerQuery(new HashableValueBoolean(answer));
        return ceQuery;
    }
    
    private static class KnowledgeBase {
    	
    	Automaton positives;
    	Automaton negatives;
    	
    	public KnowledgeBase() {
    		this.positives = new Automaton();
    		this.negatives = new Automaton();
    	}
    	
    	public Boolean isInKnowledgeBase(Word prefix, Word suffix) {
    		String word = prefix.toStringExact() + Alphabet.DOLLAR + suffix.toStringExact();
    		if(positives.run(word)) {
    			return true;
    		}
    		if(negatives.run(word)) {
    			return false;
    		}
			return null;
    	}
    	
    	public Boolean isInKnowledgeBase(Word word) {
    		if(positives.run(word.toStringExact())) {
    			return true;
    		}
    		if(negatives.run(word.toStringExact())) {
    			return false;
    		}
    		return null;
    	}
    	
    	public void add(Word prefix, Word suffix, boolean result) {
    		Automaton ddollar = FDFAOperations.buildDDollar(prefix, suffix);
    		if(result) {
    			positives = positives.union(ddollar);
    		}else {
    			negatives = negatives.union(ddollar);
    		}
    	}
    	
    	public void add(Word word, boolean result) {
    		if(result) {
    			positives = positives.union(Automaton.makeString(word.toStringExact()));
    		}else {
    			negatives = negatives.union(Automaton.makeString(word.toStringExact()));
    		}
    	}
    	
    }

}
