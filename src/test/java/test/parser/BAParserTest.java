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
import java.io.PrintStream;

import org.junit.Test;

import roll.automata.NBA;
import roll.main.Options;
import roll.parser.ba.PairParserBA;
import roll.parser.ba.ParserBA;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class BAParserTest {
    @Test
    public void testBAParser() throws FileNotFoundException {
        Options options = new Options();
        final String dir = "/home/liyong/Downloads/RABIT244/Examples/";
        ParserBA parser = new ParserBA(options, dir + "mcsA.ba");
        NBA nba = parser.parse();
        File file = new File(dir + "mcsA1.ba");
        parser.print(nba, new PrintStream(new FileOutputStream(file)));
        
        PairParserBA pp = new PairParserBA(options, dir + "mcsA.ba", dir + "mcsB.ba");
        NBA A = pp.getA();
        file = new File(dir + "mcsA1.ba");
        parser.print(A, new PrintStream(new FileOutputStream(file)));
        NBA B = pp.getB();
        file = new File(dir + "mcsB1.ba");
        parser.print(B, new PrintStream(new FileOutputStream(file)));
        System.out.println("states: " + B.getStateSize());
    }
}
