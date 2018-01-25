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

import java.io.OutputStream;

import roll.parser.Format;


/**
 * Command Line Parser
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class CLParser {
    
    private final Options options;
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
            if(args[i].compareTo("-h")==0) {
                printUsage();
            }
        }
        
        if(args.length < 2) {
            printUsage();
        }

        // for learning;
        for(int i = 0; i < args.length; i ++) {
            if(args[i].compareTo("-learn") == 0) {
                options.runningMode = Options.RunningMode.LEARNING;
                continue;
            }
            if(args[i].compareTo("-convert") == 0) {
                options.runningMode = Options.RunningMode.CONVERTING;
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
            if(args[i].compareTo("-play") == 0) {
                options.runningMode = Options.RunningMode.PLAYING;
                continue;
            }
            if(args[i].compareTo("-complement") == 0) {
                options.runningMode = Options.RunningMode.COMPLEMENTING;
                continue;
            }
            if(args[i].compareTo("-include") == 0) {
                options.runningMode = Options.RunningMode.INCLUDING;
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
            if(args[i].compareTo("-test") == 0) {
                options.runningMode = Options.RunningMode.TESTING;
                options.numOfTests = Integer.parseInt(args[i+1]);
                options.numOfStatesForTest = Integer.parseInt(args[i + 2]);
                i += 2;
                continue;
            }
            if(args[i].compareTo("-sameq") == 0) {
                options.runningMode = Options.RunningMode.SAMPLING;
                options.epsilon = Double.parseDouble(args[i+1]);
                options.delta = Double.parseDouble(args[i+2]);
                i += 2;
                continue;
            }
            if(args[i].compareTo("-v")==0){
                options.verbose=true;
                continue;
            }
            
            if(args[i].compareTo("-bs")==0) {
                options.binarySearch = true;
                continue;
            }
            if(args[i].compareTo("-out")==0){
                options.outputFile = args[i+1];
                i += 1;
                continue;
            }
            if(args[i].compareTo("-out2")==0){
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
        
        options.checkConsistency();
    }
    
    
    private void printUsage() {
        options.log.println(
                "ROLL (Regular Omega Language Learning) v" + version + "\n");
        
        options.log.println(
                "Usage: java -jar ROLL.jar [aut.ba, aut.hoa] [options]");
        final int indent = 20;
//        options.log.println("Recommended use", indent, "java -jar ROLL.jar -play -lstar");
        options.log.println("Recommended use", indent, "java -jar ROLL.jar -test 3 3 -table -syntactic -under");
        options.log.println("             or", indent, "java -jar ROLL.jar -learn B.ba -table -periodic -under");
        options.log.println("             or", indent, "java -jar ROLL.jar -complement B.hoa -table -syntactic");
        options.log.println("             or", indent, "java -jar ROLL.jar -include A.ba B.ba -table -syntactic");
        options.log.println("             or", indent, "java -jar ROLL.jar -convert A.ba B.ba -out A.hoa B.hoa");
        options.log.println("             or", indent, "java -jar ROLL.jar -play -table -syntatic");
        options.log.println("\noptions:");
        
        options.log.println("-h", indent, "Show this page");
        options.log.println("-v", indent, "Verbose mode");
        options.log.println("-out <A>", indent, "Output learned automaton in file <A>");
        options.log.println("-out2 <A> <B>", indent, "Output two automata in files <A> and <B>");
        options.log.println("-dot", indent, "Output automaton in DOT format");
        options.log.println("-test k n", indent, "Test ROLL with k randomly generated BAs of n states");
        options.log.println("-play", indent, "You play the role as a teacher");
        options.log.println("-convert [A] [B]", indent, "Convert two input automata to the other format");
        options.log.println("-learn", indent, "Use RABIT or DK package tool as the teacher to learn the input BA");
        options.log.println("-complement", indent, "Use learning algorithm to complement the input BA");
        options.log.println("-include [A] [B]", indent, "Use learning algorithm to test the inclusion between A and B");
        options.log.println("-sameq e d", indent, "Sampling as the teacher to check equivalence of two BAs");
        options.log.println("", indent + 4, "e - the probability that equivalence check is not correct");
        options.log.println("", indent + 4, "d - the probability of the confidence for equivalence check");
        options.log.println("-tree", indent, "Use tree-based data structure in learning");
        options.log.println("-table", indent, "Use table-based data structure in learning (Default)");
//        options.log.println("-lstar", indent, "Use classic L* algorithm");
//        options.log.println("-dfa", indent, "Use column based DFA learning algorithm");
        options.log.println("-ldollar", indent, "Use L$ automata to learn Omega regular language");
        options.log.println("-periodic", indent, "Use peridoc FDFA to learn Omega regular language");
        options.log.println("-recurrent", indent, "Use recurrent FDFA to learn Omega regular language");
        options.log.println("-syntactic", indent, "Use syntactic FDFA to learn Omega regular language");
        options.log.println("-over", indent, "Use over-approximation in BA construction for FDFA");
        options.log.println("-under", indent, "Use under-approximation in BA construction for FDFA (Default)");
//        options.log.println("-bs", indent, "Use binary search to find counterexample");
        options.log.println("-lazyeq", indent, "Equivalence check as the last resort");
        options.log.println("-ldba", indent, "Learning target is a limit deterministic BA");
//        options.log.println("-fdfa", indent, "Learning target is an FDFA");
//        options.log.println("-nba", indent, "Learning target is a BA");
        System.exit(0);
    }
    

}
