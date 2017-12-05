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

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public final class Options {
    
    // running mode
    public RunningMode runningMode; 
    
    // learning data structure
    public Structure structure;
    
    // learning algorithm
    public Algorithm algorithm;
    
    // approximation method for the ultimately periodic words of FDFA
    public Approximation approximation = Approximation.UNDER;
    
    // optimization for learning
    public Optimization optimization = Optimization.NONE;
    
    // the type of learned buchi
    public Buchi buchi = Buchi.NBA;
    
    // sampling precision
    public double epsilon;
    public double delta;
    
    // number of test cases and size of automaton for each case
    public int numOfTests;
    public int numOfStatesForTest;
    
    // output mode
    public boolean verbose = true;
    
    // search method for counterexample
    public boolean binarySearch = false;
    
    // output file for learned automaton
    public String outputFile = null;
    // input file
    public String inputFile = null;
    
    public boolean dot = false;
    
    //  format
    public Format format;
    
    // output 
    public Log log;
    
    // statistics during learning procedure
    public Statistics stats;
    
    public Options(OutputStream out) {
        this.log = new Log(this, new PrintStream(out));
    }
    
    public Options() {
        this.log = new Log(this, new PrintStream(System.out));
    }
    
    public static enum RunningMode {
        TEST,
        INTERACTIVE,
        AUTOMATIC,
        SAMPLING
    }
    
    public static enum Structure {
        TREE,
        TABLE
    }
    
    public static enum Algorithm {
        DFA_LSTAR,
        DFA_COLUMN,
        NBA_LDOLLAR,
        NBA_PERIODIC,
        NBA_SYNTACTIC,
        NBA_RECURRENT
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
    
    public static enum Buchi {
        NBA,
        LDBA
    }


}
