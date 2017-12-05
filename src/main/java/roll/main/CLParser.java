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
            if(args[i].compareTo("-aut") == 0) {
                options.runningMode = Options.RunningMode.AUTOMATIC;
                continue;
            }
            if(args[i].compareTo("-int") == 0) {
                options.runningMode = Options.RunningMode.INTERACTIVE;
                continue;
            }
            
            if(args[i].compareTo("-test") == 0) {
                options.runningMode = Options.RunningMode.TEST;
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
                continue;
            }
            if(args[i].compareTo("-dfa") == 0) {
                options.algorithm = Options.Algorithm.DFA_COLUMN;
                continue;
            }
            if(args[i].compareTo("-ldollar") == 0) {
                options.algorithm = Options.Algorithm.NBA_LDOLLAR;
                continue;
            }
            if(args[i].compareTo("-syntactic") == 0) {
                options.algorithm = Options.Algorithm.NBA_SYNTACTIC;
                continue;
            }
            if(args[i].compareTo("-recurrent") == 0) {
                options.algorithm = Options.Algorithm.NBA_RECURRENT;
                continue;
            }
            if(args[i].compareTo("-periodic") == 0) {
                options.algorithm = Options.Algorithm.NBA_PERIODIC;
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
                options.buchi = Options.Buchi.LDBA;
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
        
        
        
    }
    
    
    private void printUsage() {
        options.log.println(
                "ROLL (Regular Omega Language Learning) library\n");
        
        options.log.println(
                "Usage: java -jar ROLL.jar [aut.ba, aut.hoa] [options]");

        options.log.println("Recommended use: java -jar ROLL.jar -int -lstar");
        options.log.println("             or: java -jar ROLL.jar aut.ba -aut -table -periodic -under");
        options.log.println("             or: java -jar ROLL.jar aut.hoa -aut -table -periodic -under");
        options.log.println("             or: java -jar ROLL.jar -test 3 3 -table -periodic -under");
        options.log.println("\noptions:");
        final int indent = 12;
        options.log.println("-h", indent, "Show this page");
        options.log.println("-v", indent, "Verbose mode");
        options.log.println("-out <file>", indent, "Output learned automaton in <file>");
        options.log.println("-dot", indent, "Output automaton in DOT format");
        options.log.println("-test k n", indent, "Test ROLL with k randomly generated Buechi automata of n states");
        options.log.println("-int", indent, "Test ROLL with k randomly generated Buechi automata of n states");
        options.log.println("-aut", indent, "Use RABIT or DK package tool as the teacher");
        options.log.println("-sameq e d", indent, "Sampling as the teacher to check equivalence of two BA automata");
        options.log.println("", indent + 4, "e - the probability that equivalence check is not correct");
        options.log.println("", indent + 4, "d - the probability of the confidence for equivalence check");
        options.log.println("-tree", indent, "Use tree-based data structure in learning (Default)");
        options.log.println("-table", indent, "Use table-based data structure in learning");
        options.log.println("-lstar", indent, "Use classic L* algorithm");
        options.log.println("-dfa", indent, "Use column based DFA learning algorithm");
        options.log.println("-ldollar", indent, "Use L$ automata to learn Omega regular language");
        options.log.println("-periodic", indent, "Use peridoc FDFA to learn Omega regular language");
        options.log.println("-recurrent", indent, "Use recurrent FDFA to learn Omega regular language");
        options.log.println("-syntactic", indent, "Use syntactic FDFA to learn Omega regular language");
        options.log.println("-over", indent, "Use over-approximation in Buechi automaton construction for FDFA (Default)");
        options.log.println("-under", indent, "Use under-approximation in Buechi automaton construction for FDFA");
        options.log.println("-bs", indent, "Use binary search to find counterexample");
        options.log.println("-lazyeq", indent, "Equivalence check as the last resort");
        options.log.println("-ldba", indent, "Output the learned Buechi as Limit Deterministic Buechi Automaton");
        System.exit(0);
    }
    

}
