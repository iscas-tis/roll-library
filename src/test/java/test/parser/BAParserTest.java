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

package test.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;

import automata.FiniteAutomaton;
import roll.automata.NBA;
import roll.automata.operations.NBAGenerator;
import roll.automata.operations.NBAOperations;
import roll.main.Options;
import roll.oracle.nba.rabit.UtilRABIT;
import roll.parser.ba.PairParserBA;
import roll.parser.ba.ParserBA;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class BAParserTest {
    @Test
    public void testBAParser() throws FileNotFoundException {
        Options options = new Options();
        final String dir = "src/main/resources/ba/";
        ParserBA parser = new ParserBA(options, dir + "A4.ba");
        NBA nba = parser.parse();
        File file = new File(dir + "A4-1.ba");
        parser.print(nba, new PrintStream(new FileOutputStream(file)));
        
        PairParserBA pp = new PairParserBA(options, dir + "A4.ba", dir + "B4.ba");
        NBA A = pp.getA();
        file = new File(dir + "A3-1.ba");
        parser.print(A, new PrintStream(new FileOutputStream(file)));
        NBA B = pp.getB();
        file = new File(dir + "B3-1.ba");
        parser.print(B, new PrintStream(new FileOutputStream(file)));
        System.out.println(B.toString());
        System.out.println("states: " + B.getStateSize());
    }
    
    private void print(NBA nba, OutputStream out) {
        PrintStream printer = new PrintStream(out);
        printer.print("[" + nba.getInitialState() + "]\n");
        if(nba.getFinalStates().isEmpty()) {
            int dead = nba.getInitialState() + 1;
            for(int letter = 0; letter < nba.getAlphabetSize(); letter ++) {
                if(nba.getAlphabet().indexOf(Alphabet.DOLLAR) == letter) continue;
                printer.print(nba.getAlphabet().getLetter(letter)
                        + "," + "[" + dead + "]->[" + dead + "]\n");
            }
            printer.print("[" + dead + "]\n");
            return ;
        }
        // transitions
        for(int stateNr = 0; stateNr < nba.getStateSize(); stateNr ++) {
            for(int letter = 0; letter < nba.getAlphabetSize(); letter ++) {
                if(nba.getAlphabet().indexOf(Alphabet.DOLLAR) == letter) continue;
                for(int succNr : nba.getSuccessors(stateNr, letter)) {
                    printer.print(nba.getAlphabet().getLetter(letter)
                            + "," + "[" + stateNr + "]->[" + succNr + "]\n");
                }
            }
        }
        for(final int finalNr : nba.getFinalStates()) {
            printer.print("[" + finalNr + "]\n");
        }
    }
    
    @Test
    public void testRandomNBA() throws FileNotFoundException {
        final int test = 5;
        final int state = 5;
        for(int i = 0; i < test; i ++) {
            NBA nba1 = NBAGenerator.getRandomNBA(state, 2);
            nba1 = NBAOperations.removeDeadStates(nba1);
            System.out.println("A: \n" + nba1.toString());
            Options options = new Options();
            options.inputA = "/tmp/A.ba";
            print(nba1, new FileOutputStream(options.inputA));
            
            FiniteAutomaton rA = new FiniteAutomaton(options.inputA);
            
            ParserBA pp = new ParserBA(options, options.inputA);
            NBA A = pp.parse();
            options.outputA = "/tmp/A1.ba";
            pp.print(A, new FileOutputStream(options.outputA));
            
            FiniteAutomaton lA = new FiniteAutomaton(options.outputA);
            
            boolean isEq1 = UtilRABIT.isIncluded(nba1.getAlphabet(), lA, rA) == null;
            isEq1 = isEq1 && (UtilRABIT.isIncluded(nba1.getAlphabet(), rA, lA) == null);
            assert isEq1 : "Wrong A";
        }

    }
    
    @Test
    public void testRandomNBAPair() throws FileNotFoundException {
        final int test = 5;
        final int state = 5;
        for(int i = 0; i < test; i ++) {
            NBA nba1 = NBAGenerator.getRandomNBA(state, 2);
            NBA nba2 = NBAGenerator.getRandomNBA(state, 2);
            nba1 = NBAOperations.removeDeadStates(nba1);
            nba2 = NBAOperations.removeDeadStates(nba2);
            System.out.println("A: \n" + nba1.toString());
            System.out.println("B: \n" + nba2.toString());
            Options options = new Options();
            options.inputA = "/tmp/A.ba";
            options.inputB = "/tmp/B.ba";
            print(nba1, new FileOutputStream(options.inputA));
            print(nba2, new FileOutputStream(options.inputB));
            
            FiniteAutomaton rA = new FiniteAutomaton(options.inputA);
            FiniteAutomaton rB = new FiniteAutomaton(options.inputB);
            
            PairParserBA pp = new PairParserBA(options, options.inputA, options.inputB);
            NBA A = pp.getA();
            NBA B = pp.getB();
            options.outputA = "/tmp/A1.ba";
            options.outputB = "/tmp/A2.ba";
            pp.print(A, new FileOutputStream(options.outputA));
            pp.print(B, new FileOutputStream(options.outputB));
            
            FiniteAutomaton lA = new FiniteAutomaton(options.outputA);
            FiniteAutomaton lB = new FiniteAutomaton(options.outputB);
            
            boolean isEq1 = UtilRABIT.isIncluded(nba1.getAlphabet(), lA, rA) == null;
            isEq1 = isEq1 && (UtilRABIT.isIncluded(nba1.getAlphabet(), rA, lA) == null);
            assert isEq1 : "Wrong A";
            boolean isEq2 = UtilRABIT.isIncluded(nba1.getAlphabet(), lB, rB) == null;
            isEq2 = isEq2 && (UtilRABIT.isIncluded(nba1.getAlphabet(), rB, lB) == null);
            assert isEq2 : "Wrong B";
        }

    }
}
