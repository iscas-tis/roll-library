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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import roll.automata.FDFA;
import roll.automata.NBA;
import roll.automata.operations.NBAGenerator;
import roll.automata.operations.NBAOperations;
import roll.automata.operations.nba.inclusion.NBAInclusionCheckTool;
import roll.learner.fdfa.LearnerFDFA;
import roll.learner.nba.lomega.UtilLOmega;
import roll.learner.nba.lomega.translator.TranslatorFDFA;
import roll.learner.nba.lomega.translator.TranslatorFDFAUnder;
import roll.main.complement.TeacherNBAComplement;
import roll.main.inclusion.NBAInclusionCheck;
import roll.parser.PairParser;
import roll.parser.Parser;
import roll.parser.UtilParser;
import roll.query.Query;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Timer;

/**
 * 
 * Main entry of the tool Regular Omega Language Learning Library
 * 
 * 
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public final class ROLL {
    
    public static void main(String[] args) {
        // select mode to execute
        CLParser clParser = new CLParser();
        clParser.prepareOptions(args);
        Options options = clParser.getOptions();
        options.log.println("\n" + options.toString());
        switch(options.runningMode) {
        case TESTING:
            options.log.info("Testing ROLL...");
            runTestingMode(options);
            break;
        case PLAYING:
            options.log.info("ROLL for interactive play...");
            runPlayingMode(options);
            break;
        case CONVERTING:
            options.log.info("ROLL for format conversion...");
            runConvertingMode(options);
            break;
        case COMPLEMENTING:
            options.log.info("ROLL for BA complementation...");
            runComplementingMode(options);
            break;
        case INCLUDING:
            options.log.info("ROLL for BA inclusion testing...");
            runIncludingMode(options);
            break;
        case LEARNING:
            options.log.info("ROLL for BA learning via rabit...");
            runLearningMode(options, false);
            break;
        case SAMPLING:
            options.log.info("ROLL for BA learning via sampling...");
            runLearningMode(options, true);
            break;
        default :
                options.log.err("Incorrect running mode.");
        }
    }


    private static void runTestingMode(Options options) {
        final int numLetter = 2;
        for(int n = 0; n < options.numOfTests; n ++) {
            options.log.println("Testing case " + (n + 1) + " ...");
            NBA nba = NBAGenerator.getRandomNBA(options.numOfStatesForTest, numLetter);
            try{
                System.out.println("target: \n" + nba.toString());
                Executor.executeRABIT(options, nba);
            }catch (Exception e)
            {
                e.printStackTrace();
                options.log.err("Exception occured, Learning aborted...");
                System.out.println(nba.toString());
                System.exit(-1);
            }
            options.log.info("Done for case " + (n + 1));
        }
    }
    
    private static void runConvertingMode(Options options) {
        // prepare the parser
        PairParser parser = UtilParser.prepare(options, options.inputA, options.inputB, options.format);
        NBA A = parser.getA();
        NBA B = parser.getB();
        OutputStream stream = options.log.getOutputStream();
        PrintStream out = new PrintStream(stream);
        options.log.println("\nA input automaton:");
        parser.print(A, options.log.getOutputStream());
        options.log.println("\nB input automaton:");
        parser.print(B, options.log.getOutputStream());
        options.log.println("\noutput automata:");
        PrintStream outA = null, outB = null;
        if(options.outputA != null && options.outputB != null) {
            try {
                outA = new PrintStream(new FileOutputStream(options.outputA));
                outB = new PrintStream(new FileOutputStream(options.outputB));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            outA = out;
            outB = out;
        }
        switch (options.format) {
        case BA:
            NBAInclusionCheckTool.outputHOAStream(A, outA); // BA to HOA
            out.println("\n");
            NBAInclusionCheckTool.outputHOAStream(B, outB); // BA to HOA
            break;
        case HOA:
            outA.println(A.toBA());
            out.println("\n");
            outB.println(B.toBA());
            break;
        default:
            throw new UnsupportedOperationException("Unknow input format");
        }
        if(options.outputA != null && options.outputB != null) {
            outA.close();
            outB.close();
            out.close();
        }else {
            out.close();
        }
    }
    
    public static void runPlayingMode(Options options) {
        InteractiveMode.interact(options);
    }
    
    public static void runLearningMode(Options options, boolean sampling) {

        Timer timer = new Timer();
        timer.start();
        // prepare the parser
        Parser parser = UtilParser.prepare(options, options.inputFile, options.format);
        NBA target = parser.parse();
        options.stats.numOfLetters = target.getAlphabetSize();
        options.stats.numOfStatesInTraget = target.getStateSize();
        // learn the target automaton
        
        if(sampling) {
            Executor.executeSampler(options, target);
        }else {
            Executor.executeRABIT(options, target);
        }
        timer.stop();
        options.stats.timeInTotal = timer.getTimeElapsed();
        // output target automaton
        if(options.outputFile != null) {
            try {
                parser.print(options.stats.hypothesis, new FileOutputStream(new File(options.outputFile)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            options.log.println("\ntarget automaton:");
            parser.print(target, options.log.getOutputStream());
            options.log.println("\nhypothesis automaton:");
            parser.print(options.stats.hypothesis, options.log.getOutputStream());
        }
        parser.close();
        // output statistics
        options.stats.numOfTransInTraget = NBAOperations.getNumberOfTransitions(target);
        options.stats.numOfTransInHypothesis = NBAOperations.getNumberOfTransitions(options.stats.hypothesis);
        options.stats.print();
        
    }
    
    public static NBA complement(Options options, NBA input) {
        // starting to complement
        options.stats.numOfLetters = input.getAlphabetSize();
        options.stats.numOfStatesInTraget = input.getStateSize();
        
        TeacherNBAComplement teacher = new TeacherNBAComplement(options, input);
        LearnerFDFA learner = UtilLOmega.getLearnerFDFA(options, input.getAlphabet(), teacher);
        options.log.println("Initializing learner...");
        Timer timer = new Timer();
        long t = timer.getCurrentTime();
        learner.startLearning();
        t = timer.getCurrentTime() - t;
        options.stats.timeOfLearner += t;
        FDFA hypothesis = null;
        while(true) {
            options.log.verbose("Table/Tree is both closed and consistent\n" + learner.toString());
            hypothesis = learner.getHypothesis();
            // along with ce
            options.log.println("Resolving equivalence query for hypothesis...  ");
            Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(hypothesis);
            boolean isEq = ceQuery.getQueryAnswer().getLeft();
            if(isEq) {
                // store statistics
                options.stats.numOfStatesInLeading = hypothesis.getLeadingDFA().getStateSize();
                for(int state = 0; state < hypothesis.getLeadingDFA().getStateSize(); state ++) {
                    options.stats.numOfStatesInProgress.add(hypothesis.getProgressDFA(state).getStateSize());
                }
                break;
            }
            // counterexample analysis
            ceQuery.answerQuery(new HashableValueBoolean(ceQuery.getQueryAnswer().getRight()));
            TranslatorFDFA translator = new TranslatorFDFAUnder(learner);
            translator.setQuery(ceQuery);
            while(translator.canRefine()) {
                ceQuery = translator.translate();
                options.log.verbose("Counterexample is: " + ceQuery.toString());
                t = timer.getCurrentTime();
                options.log.println("Refining current hypothesis...");
                learner.refineHypothesis(ceQuery);
                t = timer.getCurrentTime() - t;
                options.stats.timeOfLearner += t;
                if(options.optimization != Options.Optimization.LAZY_EQ) break;
            }            
        }
        options.log.println("Learning completed...");
        
        teacher.print();
        return options.stats.hypothesis;
    }

    public static void runComplementingMode(Options options) {
        
        Timer timer = new Timer();
        timer.start();
        // prepare the parser
        Parser parser = UtilParser.prepare(options, options.inputFile, options.format);
        NBA input = parser.parse();
        NBA complement = complement(options, input);
        // output target automaton
        if(options.outputFile != null) {
            try {
                parser.print(complement, new FileOutputStream(new File(options.outputFile)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            options.log.println("\ntarget automaton:");
            parser.print(input, options.log.getOutputStream());
            options.log.println("\nhypothesis automaton:");
            parser.print(complement, options.log.getOutputStream());
        }
        parser.close();
        // output statistics
        options.stats.numOfStatesInHypothesis = options.stats.hypothesis.getStateSize();
        options.stats.numOfTransInTraget = NBAOperations.getNumberOfTransitions(input);
        options.stats.numOfTransInHypothesis = NBAOperations.getNumberOfTransitions(options.stats.hypothesis);
        timer.stop();
        options.stats.timeInTotal = timer.getTimeElapsed();
        
        options.stats.print();
    }
    
    
    public static void runIncludingMode(Options options) {
        
        // a bit complicated so move the code to another file
        NBAInclusionCheck.execute(options);
        
    }

}
