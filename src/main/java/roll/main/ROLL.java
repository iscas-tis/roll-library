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

import roll.automata.NBA;
import roll.parser.Parser;
import roll.parser.UtilParser;
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
        System.out.println("\n" + options.toString());
        switch(options.runningMode) {
        case TEST:
            runTestMode(options);
            break;
        case INTERACTIVE:
            runInteractiveMode(options);
            break;
        case AUTOMATIC:
            runAutomaticMode(options);
            break;
        case SAMPLING:
            throw new UnsupportedOperationException("Unsupported for sampling as teacher");
        default :
                options.log.err("Incorrect running mode.");
        }
    }
    
    private static void runTestMode(Options options) {
        
        for(int n = 0; n < options.numOfTests; n ++) {
//            Statistics.target = aut;
//            try{
//                AutoExecution.execute(aut);
//            }catch (Exception e)
//            {
//                e.printStackTrace();
//                Options.log.err("Exception occured, Learning aborted...");
//                aut.saveAutomaton("bug-test.ba");
//                System.exit(-1);
//            }
            options.log.info("Done for case " + (n + 1));
        }
    }
    
    private static void runInteractiveMode(Options options) {
//        PlayExecution.execute();
    }
    
    private static void runAutomaticMode(Options options) {
//        Statistics.reset();
//        
        Timer timer = new Timer();
        timer.start();
        // prepare the parser
        Parser parser = UtilParser.prepare(options, options.inputFile, options.format);
        NBA target = parser.parse();
        parser.print(target, System.out);
        // learn the target automaton
//        AutoExecution.execute(target);
//        
        timer.stop();
//        Statistics.timeTotal = timer.getTimeElapsed();
//        // output target automaton
//        if(Options.outputFile != null) {
//            try {
//                parser.print(Statistics.hypothesis, new FileOutputStream(new File(Options.outputFile)));
//            } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }else {
//            Options.log.println("target automaton:");
//            parser.print(Statistics.target, Options.log.getOutputStream());
//            Options.log.println("hypothesis automaton:");
//            parser.print(Statistics.hypothesis, Options.log.getOutputStream());
//        }
        parser.close();
//
        // output statistics
//        Statistics.print();
        
    }

}
