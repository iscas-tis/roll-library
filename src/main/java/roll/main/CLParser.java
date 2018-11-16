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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import roll.parser.Format;


/**
 * Command Line Parser
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class CLParser {
    
    private Options options;
    private final String version = "1.0";
    
    public CLParser(OutputStream out) {
        options = new Options(out);
    }
    
    public CLParser() {
        options = new Options();
    }
    
    public Options getOptions() {
        return options;
    }
      
    public void prepareOptions(String []args) {
                
        // parse input arguments
        for(int i = 0; i < args.length; i ++) {
            if(args[i].compareTo("-h")==0 || args[i].compareTo("help")==0 ) {
                printUsage();
            }
        }
        
        if(args.length < 2) {
            printUsage();
        }

        // for learning;
        for(int i = 0; i < args.length; i ++) {
            if(args[i].compareTo("-log") == 0) {
                String file = args[i + 1];
                try {
                    options.setOutputStream(new FileOutputStream(file));
                } catch (FileNotFoundException e) {
                    throw new UnsupportedOperationException("Invalid log file name: " + file);
                }
                i += 1;
                continue;
            }
            if(args[i].compareTo("learn") == 0) {
                options.runningMode = Options.RunningMode.LEARNING;
                continue;
            }
            if(args[i].compareTo("convert") == 0) {
                options.runningMode = Options.RunningMode.CONVERTING;
                if(i + 2 >= args.length) {
                    throw new UnsupportedOperationException("convert should be followed by two files");
                }
                options.inputA = args[i + 1];
                options.inputB = args[i + 2];
                if(args[i + 1].endsWith(".ba") && args[i +2].endsWith(".ba")) {
                    options.format = Format.BA;
                }else if(args[i + 1].endsWith(".hoa") && args[i +2].endsWith(".hoa")){
                    options.format = Format.HOA;
                }else {
                    throw new UnsupportedOperationException("Unsupported input format");
                }
                i += 2;
            }
            if(args[i].compareTo("play") == 0) {
                options.runningMode = Options.RunningMode.PLAYING;
                continue;
            }
            if(args[i].compareTo("-complement") == 0) {
                options.runningMode = Options.RunningMode.COMPLEMENTING;
                continue;
            }
            if(args[i].compareTo("include") == 0) {
                options.runningMode = Options.RunningMode.INCLUDING;
                if(i + 2 >= args.length) {
                    throw new UnsupportedOperationException("include should be followed by two files");
                }
                options.inputA = args[i + 1];
                options.inputB = args[i + 2];
                if(args[i + 1].endsWith(".ba") && args[i +2].endsWith(".ba")) {
                    options.format = Format.BA;
                }else if(args[i + 1].endsWith(".hoa") && args[i +2].endsWith(".hoa")){
                    options.format = Format.HOA;
                }else {
                    throw new UnsupportedOperationException("Unsupported input format");
                }
                i += 2;
                continue;
            }
            if(args[i].compareTo("test") == 0) {
                options.runningMode = Options.RunningMode.TESTING;
                if(i + 2 >= args.length) {
                    throw new UnsupportedOperationException("include should be followed by two integers");
                }
                options.numOfTests = parseInt(args[i + 1], "test");
                options.numOfStatesForTest = parseInt(args[i + 2], "test");
                i += 2;
                continue;
            }
            if(args[i].compareTo("sameq") == 0) {
                options.runningMode = Options.RunningMode.SAMPLING;
                if(i + 2 >= args.length) {
                    throw new UnsupportedOperationException("sameq should be followed by two doubles");
                }
                options.epsilon = parseDouble(args[i+1], "sameq");
                options.delta = parseDouble(args[i+2], "sameq");
                i += 2;
                continue;
            }
            if(args[i].compareTo("-v")==0){
            	if(args.length > i + 1) {
            		options.verbose = parseInt(args[i+1], "-v");
            		i += 1;
            	}else {
            		options.verbose = 1;
            	}
                continue;
            }
            
            if(args[i].compareTo("-bs")==0) {
                options.binarySearch = true;
                continue;
            }
            if(args[i].compareTo("-out")==0){
                if(i + 1 >= args.length) {
                    throw new UnsupportedOperationException("-out should be followed by a file name");
                }
                options.outputFile = args[i+1];
                i += 1;
                continue;
            }
            if(args[i].compareTo("-out2")==0){
                if(i + 2 >= args.length) {
                    throw new UnsupportedOperationException("-out2 should be followed by two file names");
                }
                options.outputA = args[i+1];
                options.outputB = args[i+2];
                i += 2;
                continue;
            }
            if(args[i].compareTo("-table") == 0) {
                options.structure = Options.Structure.TABLE;
                continue;
            }
            if(args[i].compareTo("-tree") == 0) {
                options.structure = Options.Structure.TREE;
                continue;
            }
            if(args[i].compareTo("-lstar") == 0) {
                options.algorithm = Options.Algorithm.DFA_LSTAR;
                options.automaton = Options.TargetAutomaton.DFA;
                continue;
            }
            if(args[i].compareTo("-dfa") == 0) {
                options.algorithm = Options.Algorithm.DFA_COLUMN;
                options.automaton = Options.TargetAutomaton.DFA;
                continue;
            }
            if(args[i].compareTo("-nfa") == 0) {
                options.algorithm = Options.Algorithm.NFA_NLSTAR;
                options.automaton = Options.TargetAutomaton.NFA;
                continue;
            }
            if(args[i].compareTo("-ldollar") == 0) {
                options.algorithm = Options.Algorithm.NBA_LDOLLAR;
                continue;
            }
            if(args[i].compareTo("-syntactic") == 0) {
                options.algorithm = Options.Algorithm.SYNTACTIC;
                continue;
            }
            if(args[i].compareTo("-recurrent") == 0) {
                options.algorithm = Options.Algorithm.RECURRENT;
                continue;
            }
            if(args[i].compareTo("-periodic") == 0) {
                options.algorithm = Options.Algorithm.PERIODIC;
                continue;
            }
            if(args[i].compareTo("-under") == 0) {
                options.approximation = Options.Approximation.UNDER;
                continue;
            }
            if(args[i].compareTo("-over") == 0) {
                options.approximation = Options.Approximation.OVER;
                continue;
            }
            if(args[i].compareTo("-lazyeq") == 0) {
                options.optimization = Options.Optimization.LAZY_EQ;
                continue;
            }
            if(args[i].compareTo("-ldba") == 0) {
                options.automaton = Options.TargetAutomaton.LDBA;
                continue;
            }
            if(args[i].compareTo("-nba") == 0) {
                options.automaton = Options.TargetAutomaton.NBA;
                continue;
            }
            if(args[i].compareTo("-fdfa") == 0) {
                options.automaton = Options.TargetAutomaton.FDFA;
                continue;
            }
            if(args[i].compareTo("-dot") == 0) {
                options.dot = true;
                continue;
            }
            if(args[i].endsWith(".ba")) {
                options.inputFile = args[i];
                options.format = Format.BA;
                continue;
            }
            if(args[i].endsWith(".hoa")) {
                options.inputFile = args[i];
                options.format = Format.HOA;
                continue;
            }

        }
        
        if(options.runningMode == null) {
            options.log.err("No running mode specified in the command line");
            System.exit(-1);
        }
        
        options.checkConsistency();
    }
    
    private int parseInt(String str, String option) {
        int num;
        try {
            num = Integer.parseInt(str);
        }catch(NumberFormatException e) {
            throw new UnsupportedOperationException("Invalid input integers: " + str + " followed by " + option);
        }
        return num;
    }
    
    private double parseDouble(String str, String option) {
        double num;
        try {
            num = Double.parseDouble(str);
        }catch(NumberFormatException e) {
            throw new UnsupportedOperationException("Invalid input doubles: " + str + " followed by " + option);
        }
        return num;
    }
    
    
    private void printUsage() {
        options.log.print(
                "ROLL (Regular Omega Language Learning) v" + version + "\n\n");
        
        options.log.print(
                "Usage: java -jar ROLL.jar <learn|complement> <aut.ba|aut.hoa> [options]\n"
              + "       java -jar ROLL.jar test <k> <n> [options]\n"
              + "       java -jar ROLL.jar play [options]\n"
              + "       java -jar ROLL.jar include <A> <B> [options]\n"
              + "       java -jar ROLL.jar convert <A> <B> -out2 <AO> <BO>\n\n");
        final int indent = 20;
//        options.log.println("Recommended use", indent, "java -jar ROLL.jar -play -lstar");
        options.log.println("Recommended use", indent, "java -jar ROLL.jar test 3 3");
        options.log.println("             or", indent, "java -jar ROLL.jar learn B.ba");
        options.log.println("             or", indent, "java -jar ROLL.jar complement B.hoa");
        options.log.println("             or", indent, "java -jar ROLL.jar include A.ba B.ba");
        options.log.println("             or", indent, "java -jar ROLL.jar convert A.ba B.ba -out2 A.hoa B.hoa");
        options.log.println("             or", indent, "java -jar ROLL.jar play");
        
        options.log.print("\ncommands:\n");
        options.log.println("test k n", indent, "Test ROLL with k randomly generated BAs of n states");
        options.log.println("play", indent, "You play the role as a teacher");
        options.log.println("convert <A> <B>", indent, "Convert two input automata to the other format");
        options.log.println("learn", indent, "Use RABIT or DK package tool as the teacher to learn the input BA");
        options.log.println("complement", indent, "Use learning algorithm to complement the input BA");
        options.log.println("include <A> <B>", indent, "Use learning algorithm to test the inclusion between A and B");
        options.log.println("sameq e d", indent, "Sampling as the teacher to check equivalence of two BAs");
        options.log.println("", indent + 4, "e - the probability that equivalence check is not correct");
        options.log.println("", indent + 4, "d - the probability of the confidence for equivalence check");
        options.log.println("help", indent, "Show help page, same as the -h option");

        options.log.print("\noptions:\n");
        
        options.log.println("-h", indent, "Show this page");
        options.log.println("-log <file>", indent, "Output log to <file>");
        options.log.println("-v i", indent, "0 for silent (minimal output), 1 for normal (default, output stages in learning) and");
        options.log.println("", indent, "2 for verbose (output internal data structures and may output unprintable characters)");
        options.log.println("-out <A>", indent, "Output learned automaton in file <A>");
        options.log.println("-out2 <A> <B>", indent, "Output two automata in files <A> and <B>");
        options.log.println("-dot", indent, "Output automaton in DOT format");
        options.log.println("-tree", indent, "Use tree-based data structure in learning");
        options.log.println("-table", indent, "Use table-based data structure in learning (Default)");
//        options.log.println("-lstar", indent, "Use classic L* algorithm");
//        options.log.println("-dfa", indent, "Use column based DFA learning algorithm");
//        options.log.println("-nfa", indent, "Use column based NFA learning algorithm");
//        options.log.println("-rdfa", indent, "Use reverse DFA learning algorithm");
        options.log.println("-ldollar", indent, "Use L$ automata to learn Omega regular language");
        options.log.println("-periodic", indent, "Use peridoc FDFA to learn Omega regular language");
        options.log.println("-recurrent", indent, "Use recurrent FDFA to learn Omega regular language");
        options.log.println("-syntactic", indent, "Use syntactic FDFA to learn Omega regular language (Default)");
        options.log.println("-over", indent, "Use over-approximation in BA construction for FDFA");
        options.log.println("-under", indent, "Use under-approximation in BA construction for FDFA (Default)");
        options.log.println("-bs", indent, "Use binary search to find counterexample");
        options.log.println("-lazyeq", indent, "Equivalence check as the last resort");
        options.log.println("-ldba", indent, "Output learned BA as a limit deterministic BA");
//        options.log.println("-fdfa", indent, "Learning target is an FDFA");
//        options.log.println("-nba", indent, "Learning target is a BA");
        System.exit(0);
    }
    

}
