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

package roll.parser;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Function;

import roll.automata.NBA;
import roll.main.Options;
import roll.parser.ba.PairParserBA;
import roll.parser.ba.ParserBA;
import roll.parser.hoa.PairParserHOA;
import roll.parser.hoa.ParserHOA;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class UtilParser {
    
    
    public static void print(NBA nba, OutputStream out, Function<Integer, String> fun) {
        PrintStream printer = new PrintStream(out);
        printer.print("//nba \n");
        printer.print("digraph {\n");
        
        for(int stateNr = 0; stateNr < nba.getStateSize(); stateNr ++) {
            printer.print("  " + stateNr + " [label=\"" +  stateNr + "\"");
            if(nba.isFinal(stateNr)) printer.print(", shape = doublecircle");
            else printer.print(", shape = circle");
            printer.print("];\n");
            for(int letter = 0; letter < nba.getAlphabetSize(); letter ++) {
                for(int succNr : nba.getSuccessors(stateNr, letter)) {
                    printer.print("  " + stateNr + " -> " + succNr 
                            + " [label=\"" + fun.apply(letter)
                            + "\"];\n");
                }
            }
        }   
        printer.print("  " + nba.getStateSize() + " [label=\"\", shape = plaintext];\n");
        printer.print("  " + nba.getStateSize() + " -> " + nba.getInitialState() + " [label=\"\"];\n");
        printer.print("}\n\n");
    }
    
    public static Parser prepare(Options options, String file, Format format) {
    	checkInputFile(file);
        if(format == Format.BA) {
            return new ParserBA(options, file);
        }else if(format == Format.HOA) {
            return new ParserHOA(options, file);
        }
        
        return null;
    }
    
    private static void checkInputFile(String file) {
        if(file == null) {
        	throw new UnsupportedOperationException("No valid input file with suffix extension hoa or ba");
        }
    }
    
    private static void checkInputFiles(String fileA, String fileB) {
        if(fileA == null || fileB == null) {
        	throw new UnsupportedOperationException("No valid input files with suffix extension hoa or ba");
        }
    }
    
    public static PairParser prepare(Options options, String fileA, String fileB, Format format) {
    	checkInputFiles(fileA, fileB);
    	if(format == Format.BA) {
            return new PairParserBA(options, fileA, fileB);
        }else if(format == Format.HOA) {
            return new PairParserHOA(options, fileA, fileB);
        }
        
        return null;
    }

}
