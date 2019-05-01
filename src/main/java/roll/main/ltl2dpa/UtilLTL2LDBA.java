/* Copyright (c) 2016, 2017 -                                               */
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

package roll.main.ltl2dpa;

import java.io.IOException;
import java.io.InputStream;
import roll.automata.NBA;
import roll.main.Options;
import roll.parser.hoa.PairParserHOA;
import roll.util.Pair;

public class UtilLTL2LDBA {
	
	private UtilLTL2LDBA() {
		
	}
	
	public static Pair<NBA, NBA> translateLtl2BA(Options options, String ltl) {
        final Runtime rt = Runtime.getRuntime();
        Process process = null;
        try {
        	// ltl to A
        	process = rt.exec(new String[]{"ltl2tgba", "-f", ltl, "-B"});
        	process.waitFor();
        } catch (IOException | InterruptedException e1) {
            e1.printStackTrace();
        }
        // positive NBA
        InputStream streamA = process.getInputStream();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line = null;
//        try {
//            while ((line = reader.readLine()) != null ) {
//            	System.out.println(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        
        try {
        	// ltl to B
        	process = rt.exec(new String[]{"ltl2tgba", "-f", "!(" + ltl + ")", "-B"});
        	process.waitFor();
        } catch (IOException|InterruptedException e1) {
            e1.printStackTrace();
        }

        // negative NBA
        InputStream streamB = process.getInputStream();
//        reader = new BufferedReader(new InputStreamReader(streamB));
//        try {
//            while ((line = reader.readLine()) != null ) {
//            	System.out.println(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        PairParserHOA pairParser = new  PairParserHOA(options, streamA, streamB);
        NBA A = pairParser.getA();
        NBA B = pairParser.getB();
        options.parser = pairParser;
        
		return new Pair<>(A, B);
	}

}
