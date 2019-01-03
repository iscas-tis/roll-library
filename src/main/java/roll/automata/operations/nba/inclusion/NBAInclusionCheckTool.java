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

package roll.automata.operations.nba.inclusion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.function.Function;

import roll.automata.NBA;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * 1. Only can prove inclusion, and GOAL tends to be very slow
 *
 * 2. Not able to provide the witness for noninclusion and inequivalence
 * 
 * */

public class NBAInclusionCheckTool {
    
    // use spot to check whether A is included by B
    public static boolean isIncludedSpot(NBA A, NBA B) {
        String command = "autfilt --included-in=";
        boolean result = executeTool(command, true, "HOA", A, B);
        return result;
    }

    public static boolean isEquivalentSpot(NBA A, NBA B) {
        String command = "autfilt --equivalent-to=";
        boolean result = executeTool(command, true, "HOA", A, B);
        return result;
    }

    public static boolean isIncludedGoal(String goal, NBA A, NBA B) {
        String command = goal + " containment ";
        boolean result = executeTool(command, false, "(true", A, B);
        return result;
    }

    public static boolean isEquivalentGoal(String goal, NBA A, NBA B) {
        String command = goal + " equivalence ";
        boolean result = executeTool(command, false, "true", A, B);
        return result;
    }

    private static boolean executeTool(String cmd, boolean reverse, String pattern, NBA A, NBA B) {
        File fileA = new File("/tmp/A.hoa");
        File fileB = new File("/tmp/B.hoa");
        try {
            outputHOAStream(A, new PrintStream(new FileOutputStream(fileA)));
            outputHOAStream(B, new PrintStream(new FileOutputStream(fileB)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final Runtime rt = Runtime.getRuntime();
        String command = null;
        // add files
        if (reverse) {
            command = cmd + fileB.getAbsolutePath() + " " + fileA.getAbsolutePath();
        } else {
            command = cmd + fileA.getAbsolutePath() + " " + fileB.getAbsolutePath();
        }
        Process proc = null;
        try {
            proc = rt.exec(command);
            proc.waitFor();
        } catch (IOException | InterruptedException e1) {
            e1.printStackTrace();
        }
        System.out.println(command);

        final BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line = null;
        boolean result = false;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.contains(pattern)) {
                    result = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void outputHOAStream(NBA nba, PrintStream out) {
        int numBits = Integer.highestOneBit(nba.getAlphabetSize());
        Function<Integer, String> labelFunc = x -> translateInteger(x, numBits);
        Function<Integer, String> apList = x -> "a" + x;
        outputHOAStream(nba, out, numBits, apList, labelFunc);
    }

    public static void outputHOAStream(NBA nba, PrintStream out, int numAp, Function<Integer, String> apList,
            Function<Integer, String> labelFunc) {
        out.println("HOA: v1");
        out.println("tool: \"ROLL\"");
        out.println("properties: explicit-labels state-acc trans-labels ");

        out.println("States: " + nba.getStateSize());
        out.println("Start: " + nba.getInitialState());
        out.println("acc-name: Buchi");
        out.println("Acceptance: 1 Inf(0)");
        out.print("AP: " + numAp);
        for (int index = 0; index < numAp; index++) {
            out.print(" \"" + apList.apply(index) + "\"");
        }
        out.println();
        out.println("--BODY--");

        for (int stateNr = 0; stateNr < nba.getStateSize(); stateNr++) {
            out.print("State: " + stateNr);
            if (nba.isFinal(stateNr))
                out.print(" {0}");
            out.println();
            for (int letter = 0; letter < nba.getAlphabetSize(); letter++) {
                if (nba.getAlphabet().indexOf(Alphabet.DOLLAR) == letter)
                    continue;
                for (int succNr : nba.getSuccessors(stateNr, letter)) {
                    out.println("[" + labelFunc.apply(letter) + "]  " + succNr);
                }
            }
        }
        out.println("--END--");
    }

    private static String translateInteger(int value, int numBits) {
        StringBuilder builder = new StringBuilder();
        int bit = 1;
        for (int index = 0; index < numBits; index++) {
            if ((bit & value) == 0) {
                builder.append("!");
            }
            builder.append("" + index);
            if (index != numBits - 1) {
                builder.append("&");
            }
            bit <<= 1;
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA nba = new NBA(alphabet);
        nba.createState();
        nba.createState();

        nba.getState(0).addTransition(0, 0);
        nba.getState(0).addTransition(1, 0);
        nba.getState(0).addTransition(1, 1);
        nba.getState(1).addTransition(1, 1);
        nba.setInitial(0);
        nba.setFinal(1);

        NBA nba1 = new NBA(alphabet);
        nba1.createState();
        nba1.createState();

        nba1.getState(0).addTransition(0, 0);
        nba1.getState(0).addTransition(1, 1);
        nba1.getState(1).addTransition(1, 1);
        nba1.setInitial(0);
        nba1.setFinal(1);

        int numBits = Integer.highestOneBit(nba.getAlphabetSize());
        Function<Integer, String> labelFunc = x -> translateInteger(x, numBits);
        Function<Integer, String> apList = x -> "a" + x;
        NBAInclusionCheckTool.outputHOAStream(nba, System.out, numBits, apList, labelFunc);

        System.out.println("result : " + NBAInclusionCheckTool.isIncludedSpot(nba, nba));

        System.out.println("result : " + NBAInclusionCheckTool.isIncludedSpot(nba, nba1));
    }

}
