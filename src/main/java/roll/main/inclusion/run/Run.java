/* Copyright (c) since 2016                                               */
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

// The code is borrowed from omega library

package roll.main.inclusion.run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roll.words.Alphabet;
import roll.words.Word;

public class Run {
    
    protected List<Integer> run;
    protected Word word;
    protected final Alphabet alphabet;
    
    public Run(Alphabet alphabet) {
        this.alphabet = alphabet;
        run = new ArrayList<>();
        word = alphabet.getEmptyWord();
    }
    
    public Alphabet getAphabet() {
        return alphabet;
    }
    
    public Run(Run otherrun) {
        this(otherrun.alphabet);
        this.run.addAll(otherrun.run);
        this.word = otherrun.word;
    }
    
    public void append(int source, int letter, int target) {
        if(run.isEmpty()) {
            run.add(source);
            run.add(target);
            word = word.append(letter);
            return ;
        }
        // if it is not empty
        checkLastStateConsistency(source);
        run.add(target);
        word = word.append(letter);
    }
    
    public void preappend(int source, int letter, int target) {
        if(run.isEmpty()) {
            run.add(source);
            run.add(target);
            word = word.append(letter);
            return ;
        }
        // if it is not empty
        checkFirstStateConsistency(target);
        List<Integer> newrun = new ArrayList<>();
        newrun.add(source);
        newrun.addAll(run);
        run = newrun;
        word = word.preappend(letter);;
    }
    
    public void concatenate(Run otherrun) {
        checkConsistency(getLastState(), otherrun.getFirstState());
        List<Integer> states = new ArrayList<>();
        states.addAll(run);
        states.remove(run.size() - 1);
        states.addAll(otherrun.run);
        run = states;
        word = word.concat(otherrun.word);
    }
    
    public int getLastState() {
        return getStateAt(run.size() - 1);
    }
    
    public int getLastLetter() {
        return getLetterAt(word.length() - 1);
    }
    
    public int getStateAt(int index) {
        return getStateAt(run, index);
    }
    
    public int getLetterAt(int index) {
        return getAt(word, index);
    }
    
    public int getFirstState() {
        return getStateAt(0);
    }
    
    public int getFirstLetter() {
        return getLetterAt(0);
    }
    
    public Word getWord() {
        return word;
    }
    
    public List<Integer> getSequence() {
        return Collections.unmodifiableList(run);
    }
    
    private int getAt(Word word, int index) {
        if(index < 0 || index >= word.length()) 
            return -1;
        return word.getLetter(index);
    }
    
    private int getStateAt(List<Integer> list, int index) {
        if(index < 0 || index >= list.size()) 
            return -1;
        return list.get(index);
    }
    
    private void checkFirstStateConsistency(int state) {
        checkConsistency(getFirstState(), state);
    }
    
    private void checkLastStateConsistency(int state) {
        checkConsistency(getLastState(), state);
    }
    
    private void checkConsistency(int fstState, int sndState) {
        if(fstState != sndState)
            throw new RuntimeException("States not consistent");
    }
    
    public boolean isEmpty() {
        return run.size() == 0;
    }
    
    public int stateSize() {
        return run.size();
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if(isEmpty()) return "[]";
        builder.append("[ s" + getFirstState());
        for(int i = 0; i < stateSize() - 1; i ++) {
            builder.append("-L" + getLetterAt(i) + "-> s" + getStateAt(i + 1));
        }
        builder.append("]");
        return builder.toString();
    }

}

