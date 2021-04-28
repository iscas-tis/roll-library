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
import java.io.PrintStream;

import roll.parser.Format;
import roll.parser.PairParser;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class Options {
    
    // running mode
    public RunningMode runningMode = null; 
    
    // learning data structure
    public Structure structure = Structure.TABLE;
    
    // learning algorithm
    public Algorithm algorithm = Algorithm.SYNTACTIC;
    
    // approximation method for the ultimately periodic words of FDFA
    public Approximation approximation = Approximation.UNDER;
    
    // optimization for learning
    public Optimization optimization = Optimization.NONE;
    
    // the type of learned buchi
    public TargetAutomaton automaton = TargetAutomaton.NBA;
    
    // sampling precision
    public double epsilon;
    public double delta;
    public double stopProb = -1;
    public int numOfVisits;
    
    // number of test cases and size of automaton for each case
    public int numOfTests;
    public int numOfStatesForTest;
    
    // output mode
    public int verbose = 0; // 0 for silent, 1 for normal and 2 for verbose
    
    // output mode
    public boolean silent = false;
    
    // only noninclusion
    public boolean nonIncusion = false;
    
    public boolean onlyCongr = false;
    
    // search method for counterexample
    public boolean binarySearch = false;
    
    // output file for learned automaton
    public String outputFile = null;
    // input file
    public String inputFile = null;
    
    // input A and B for inclusion testing
    public String inputA = null;
    public String inputB = null;
    
    // output A and B for format conversion
    public String outputA = null;
    public String outputB = null;
    
    public boolean dot = false;
    
    public boolean spot = false;

    public boolean parallel = false;
    
    public int numWorkers = 4;
    // use congruence simulation algorithm for inclusion checking
    public boolean congruence = false;
    public boolean simulation = false;
    public boolean fwSimulation = false;
    public boolean bwSimulation = false;
    public boolean congrOpt = false;
    // minimize graphs
    public boolean minimization = false;
    
    // NCSB complementation
    public boolean lazyS = false;
    public boolean lazyB = true;

    // use the complement teacher to learn the target BA
    public boolean reverse = false;
    
    //  format
    public Format format;
    
    // output 
    public Log log;
    
    // statistics during learning procedure
    public Statistics stats;
    
    // for ltl to ldba or dpa
    public PairParser parser;
    
    public String ltl = null;
    
    public boolean finite = false;
    
    public Options(OutputStream out) {
        this.log = new Log(this, new PrintStream(out));
        this.stats = new Statistics(this);
    }
    
    public Options() {
        this.log = new Log(this, new PrintStream(System.out));
        this.stats = new Statistics(this);
    }
    
    protected void setOutputStream(OutputStream out) {
        this.log = new Log(this, new PrintStream(out));
        this.stats = new Statistics(this);
    }
    
    public static enum RunningMode {
    	TRANSLATING,
        TESTING,
        PLAYING,
        CONVERTING,    // convert BA to Hanoi format or vice versa
        LEARNING,      // learning automata
        SAMPLING,
        COMPLEMENTING, // complement input BA
        INCLUDING;     // inclusion testing for input BAs
    	
        
        boolean isTestMode() {
            return this == TESTING;
        }
    }
    
    public static enum Structure {
        TREE,
        TABLE;
        public boolean isTable() {
            return this == TABLE;
        }
    }
    
    public static enum Algorithm {
        DFA_LSTAR,
        DFA_KV,
        DFA_COLUMN,
        NFA_COLUMN,
        NFA_RDSTAR,
        NFA_NLSTAR,
        NBA_LDOLLAR,
        PERIODIC,
        SYNTACTIC,
        RECURRENT;
        
        boolean isTargetDFA() {
            return this == DFA_LSTAR || this == DFA_COLUMN|| this == DFA_KV;
        }
        
        boolean isTargetNFA() {
            return this == NFA_NLSTAR || this == NFA_COLUMN || this == NFA_RDSTAR;
        }
        
        boolean isTargetFDFA() {
            return this == PERIODIC || this == SYNTACTIC|| this == RECURRENT;
        }
    }
    
    public static enum Approximation {
        UNDER,
        OVER
    }
    
    public static enum Optimization {
        NONE,
        LAZY_EQ,
        SIMULATION,
        MINIMIZATION
    }
    
    public static enum TargetAutomaton {
        DFA,
        NFA,
        FDFA,
        NBA,
        LDBA;
        
        boolean isBA() {
            return this == NBA || this == LDBA;
        }
        
        boolean isDFA() {
            return this == DFA;
        }
        
        boolean isNFA() {
            return this == NFA;
        }
        
        
        boolean isFDFA() {
            return this == FDFA;
        }
        
        boolean isLDBA() {
            return this == LDBA;
        }
    }
    
    public boolean verbose() {
        return verbose == 2;
    }
    
    public boolean silent() {
        return verbose == 0;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(runningMode + ",");
        builder.append(structure + ",");
        builder.append(algorithm + ",");
        builder.append(optimization + ",");
        builder.append(automaton + ",");
        if(automaton.isBA()) {
            builder.append(approximation + ",");
        }
        // not yet supported
        if(runningMode == RunningMode.SAMPLING) {
            builder.append("e=" + epsilon + "," + "d=" + delta + ",");
        }
        if(runningMode == RunningMode.TESTING) {
            builder.append("k=" + numOfTests + "," + "n=" + numOfStatesForTest + ",");
        }
        builder.append("verbose=" + verbose + ",");
        builder.append("bs=" + binarySearch + ",");
        builder.append("dot=" + dot + ",");
        builder.append("inputfile=" + inputFile + ",");
        builder.append("outputfile=" + outputFile + ",");
        builder.append("outputA=" + outputA + ",");
        builder.append("outputB=" + outputB + ",");
        builder.append("congruence=" + congruence + ",");
        builder.append("fwSimulation=" + fwSimulation + ",");
        builder.append("bwSimulation=" + bwSimulation + "\n");
        return builder.toString();
    }
    
    protected void checkConsistency() {
        
        if((algorithm.isTargetDFA() && ! automaton.isDFA())
          || (algorithm.isTargetNFA() && ! automaton.isNFA())
          || (!algorithm.isTargetDFA() && automaton.isDFA())) {
            throw new UnsupportedOperationException("algorithm and target automaton are not consistent");
        }
        if(runningMode.isTestMode() 
          && (numOfTests == 0 || numOfStatesForTest == 0)) {
            throw new UnsupportedOperationException("arguments for test mode are illegal");
        }
        if(runningMode == RunningMode.COMPLEMENTING
                && (!algorithm.isTargetFDFA())) {
                  throw new UnsupportedOperationException("arguments for test mode are illegal");
        }
        if(congruence && (runningMode != RunningMode.INCLUDING && runningMode != RunningMode.COMPLEMENTING)) {
        	throw new UnsupportedOperationException("congruence is only valid for include mode");
        }
        
    }


}
