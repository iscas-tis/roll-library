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

package roll.parser.ba;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import dk.brics.automaton.Automaton;
import roll.automata.NBA;
import roll.main.Options;
import roll.parser.PairParser;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class PairParserBA extends ParserBA implements PairParser {
    protected NBA A;
    protected NBA B;
    public PairParserBA(Options options, String fileA, String fileB) {
        super(options);
        try {
            this.automaton = new Automaton();
            this.strStateMap.clear();
            FileInputStream inputStream = new FileInputStream(new File(fileA));
            JBAParser parser = new JBAParser(inputStream);
            parser.parse(this);
            final int numLetters = strCharMap.size();
            this.A = nba;
            this.automaton = new Automaton();
            this.strStateMap.clear();
            inputStream = new FileInputStream(new File(fileB));
            this.calledAcc = false;
            parser = new JBAParser(inputStream);
            parser.parse(this);
            this.B = nba;
            if(strCharMap.size() != numLetters) {
                throw new UnsupportedOperationException("Alphabet not the same between A and B");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
    }

    @Override
    public NBA getA() {
        return A;
    }

    @Override
    public NBA getB() {
        return B;
    }
    
    

}
