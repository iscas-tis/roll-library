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

package test.learner.nba;

import roll.automata.NBA;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class NBAStore {
    
    public static NBA getNBA1() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        target.createState();
        target.createState();
        
        // a^w + ab^w
        int fst = 0, snd = 1, thd = 2;
        target.getState(fst).addTransition(alphabet.indexOf('a'), snd);
        target.getState(fst).addTransition(alphabet.indexOf('a'), thd);
        target.getState(snd).addTransition(alphabet.indexOf('a'), snd);
        target.getState(thd).addTransition(alphabet.indexOf('b'), thd);
        target.setInitial(fst);
        target.setFinal(snd);
        target.setFinal(thd);
        
        return target;
    }
    
    public static NBA getNBA2() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        target.createState();
        
        // a^w + ab^w
        int fst = 0, snd = 1;
        target.getState(fst).addTransition(alphabet.indexOf('a'), snd);
        target.getState(fst).addTransition(alphabet.indexOf('b'), fst);
        target.getState(snd).addTransition(alphabet.indexOf('b'), snd);
        target.setInitial(fst);
        target.setFinal(snd);
        
        return target;
    }
    
    public static NBA getNBA3() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        target.createState();
        target.createState();
        
        
        int fst = 0, snd = 1, thd = 2;
        target.getState(fst).addTransition(alphabet.indexOf('b'), thd);
        target.getState(snd).addTransition(alphabet.indexOf('a'), fst);
        target.getState(thd).addTransition(alphabet.indexOf('b'), fst);
        target.getState(thd).addTransition(alphabet.indexOf('b'), snd);
        target.getState(thd).addTransition(alphabet.indexOf('a'), thd);
        target.getState(thd).addTransition(alphabet.indexOf('a'), snd);

        target.setInitial(fst);
        target.setFinal(snd);
        
        return target;
    }
    
    public static NBA getNBA4() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        target.createState();
        target.createState();
        
        
        int fst = 0, snd = 1, thd = 2;
        target.getState(fst).addTransition(alphabet.indexOf('b'), thd);
        target.getState(snd).addTransition(alphabet.indexOf('a'), fst);
        target.getState(thd).addTransition(alphabet.indexOf('b'), fst);
        target.getState(thd).addTransition(alphabet.indexOf('b'), snd);
        target.getState(thd).addTransition(alphabet.indexOf('a'), thd);
        target.getState(thd).addTransition(alphabet.indexOf('a'), snd);

        target.setInitial(fst);
        target.setFinal(snd);
        
        return target;
    }
    
    public static NBA getNBA5() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        target.createState();
        
        
        int fst = 0, snd = 1;
        target.getState(fst).addTransition(alphabet.indexOf('b'), fst);
        target.getState(fst).addTransition(alphabet.indexOf('a'), fst);
        target.getState(fst).addTransition(alphabet.indexOf('b'), snd);
        target.getState(fst).addTransition(alphabet.indexOf('a'), snd);
        target.getState(snd).addTransition(alphabet.indexOf('a'), fst);
        target.getState(snd).addTransition(alphabet.indexOf('b'), fst);

        target.setInitial(fst);
        target.setFinal(snd);
        
        return target;
    }
    
    public static NBA getNBA6() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        target.createState();
        
        
        int fst = 0, snd = 1;
        target.getState(fst).addTransition(alphabet.indexOf('b'), fst);
        target.getState(fst).addTransition(alphabet.indexOf('a'), fst);
        target.getState(fst).addTransition(alphabet.indexOf('b'), snd);
        target.getState(snd).addTransition(alphabet.indexOf('b'), snd);

        target.setInitial(fst);
        target.setFinal(snd);
        
        return target;
    }
    
    public static NBA getNBA7() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        target.createState();
        target.createState();
        target.createState();
        
        
        int fst = 0, snd = 1, thd = 2, fur = 3;
        target.getState(fst).addTransition(alphabet.indexOf('b'), thd);
        target.getState(fst).addTransition(alphabet.indexOf('b'), fst);
        target.getState(fst).addTransition(alphabet.indexOf('a'), snd);
        target.getState(snd).addTransition(alphabet.indexOf('a'), fst);
        target.getState(fur).addTransition(alphabet.indexOf('a'), snd);

        target.setInitial(fst);
        target.setFinal(snd);
        
        return target;
    }
    
    public static NBA getNBA8() {
        Alphabet alphabet = new Alphabet();
        alphabet.addLetter('a');
        alphabet.addLetter('b');
        NBA target = new NBA(alphabet);
        target.createState();
        
        int fst = 0;
        target.getState(fst).addTransition(alphabet.indexOf('b'), fst);
        target.getState(fst).addTransition(alphabet.indexOf('a'), fst);

        target.setInitial(fst);
        target.setFinal(fst);
        
        return target;
    }

}
