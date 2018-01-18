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

import roll.automata.FDFA;
import roll.automata.NBA;
import roll.automata.operations.NBAGenerator;
import roll.automata.operations.NBAOperations;
import roll.learner.fdfa.LearnerFDFA;
import roll.learner.nba.lomega.UtilLOmega;
import roll.learner.nba.lomega.translator.TranslatorFDFA;
import roll.learner.nba.lomega.translator.TranslatorFDFAUnder;
import roll.main.complement.TeacherNBAComplement;
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
            runTestingMode(options);
            break;
        case PLAYING:
            runPlayingMode(options);
            break;
        case COMPLEMENTING:
            runComplementingMode(options);
            break;
        case INCLUDING:
            runIncludingMode(options);
            break;
        case LEARNING:
            runLearningMode(options, false);
            break;
        case SAMPLING:
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
    
    public static void runPlayingMode(Options options) {
        throw new UnsupportedOperationException("Not yet implmenented");
//        PlayExecution.execute();
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

    public static void runComplementingMode(Options options) {
        
        Timer timer = new Timer();
        timer.start();
        // prepare the parser
        Parser parser = UtilParser.prepare(options, options.inputFile, options.format);
        NBA input = parser.parse();
        options.stats.numOfLetters = input.getAlphabetSize();
        options.stats.numOfStatesInTraget = input.getStateSize();
        
        TeacherNBAComplement teacher = new TeacherNBAComplement(options, input);
        LearnerFDFA learner = UtilLOmega.getLearnerFDFA(options, input.getAlphabet(), teacher);
        options.log.println("Initializing learner...");
        
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
        // output target automaton
        if(options.outputFile != null) {
            try {
                parser.print(options.stats.hypothesis, new FileOutputStream(new File(options.outputFile)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            options.log.println("\ntarget automaton:");
            parser.print(input, options.log.getOutputStream());
            options.log.println("\nhypothesis automaton:");
            parser.print(options.stats.hypothesis, options.log.getOutputStream());
        }
        parser.close();
        // output statistics
        options.stats.numOfStatesInHypothesis = options.stats.hypothesis.getStateSize();
        options.stats.numOfTransInTraget = NBAOperations.getNumberOfTransitions(input);
        options.stats.numOfTransInHypothesis = NBAOperations.getNumberOfTransitions(options.stats.hypothesis);
        timer.stop();
        options.stats.timeInTotal = timer.getTimeElapsed();
        
        options.stats.print();
        teacher.print();
    }
    
    
    public static void runIncludingMode(Options options) {
        
        
        
    }

}
