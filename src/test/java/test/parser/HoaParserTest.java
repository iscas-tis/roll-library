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

import org.junit.Test;

import roll.automata.NBA;
import roll.main.Options;
import roll.parser.hoa.PairParserHOA;
import roll.parser.hoa.ParserHOA;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class HoaParserTest {
    
    @Test
    public void testHOAParser() {
        Options options = new Options();
        final String dir = "src/main/resources/inclusion/";
        ParserHOA parser = new ParserHOA(options, dir + "A.hoa");
        NBA nba = parser.parse();
        parser.print(nba, System.out);
//        HOAParser parser2 = new HOAParser(options, "/home/liyong/workspace-neon/roll-library/src/main/resources/inclusion/A.hoa", parser);
//        parser2.parse();
        PairParserHOA pairParser = new PairParserHOA(options, dir + "A.hoa", dir + "B.hoa");
        NBA A = pairParser.getA();
        parser.print(A, System.out);
        NBA B = pairParser.getB();
        parser.print(B, System.out);
    }

}
