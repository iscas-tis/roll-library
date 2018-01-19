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
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.TObjectCharMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.map.hash.TObjectCharHashMap;
import roll.automata.NBA;
import roll.automata.operations.NBAOperations;
import roll.main.Options;
import roll.parser.Parser;
import roll.parser.UtilParser;
import roll.words.Alphabet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
 
public class ParserBA implements Parser {
	
	protected TCharObjectMap<String> charStrMap ; // char -> str
	protected TObjectCharMap<String> strCharMap ; // str -> char
	protected Map<String, State> strStateMap = new HashMap<>();
	protected final Alphabet alphabet;
	// this class only allow the characters
	protected Automaton automaton;
	protected final Options options;
	protected NBA nba;
	
	public ParserBA(Options options, String file) {
	    this.options = options;
		this.strCharMap = new TObjectCharHashMap<>();
		this.charStrMap = new TCharObjectHashMap<>();
		this.automaton = new Automaton();
		this.alphabet = new Alphabet();
		try {
			FileInputStream inputStream = new FileInputStream(new File(file));
			JBAParser parser = new JBAParser(inputStream);
			parser.parse(this);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	protected ParserBA(Options options) {
	    this.options = options;
	    this.alphabet = new Alphabet();
	    this.strCharMap = new TObjectCharHashMap<>();
        this.charStrMap = new TCharObjectHashMap<>();
	}
	
	// share alphabet with other parser
	public ParserBA(Options options, String file, ParserBA otherParser) {
	    this.options = options;
		this.charStrMap = otherParser.charStrMap;
		this.strCharMap = otherParser.strCharMap;
		this.automaton = new Automaton();
		this.alphabet = new Alphabet();
		try {
			FileInputStream inputStream = new FileInputStream(new File(file));
			JBAParser parser = new JBAParser(inputStream);
			parser.parse(this);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public NBA parse() {
		return nba; //NBAOperations.fromDkNBA(automaton, alphabet);
	}

	@Override
	public void print(NBA nba, OutputStream out) {
		PrintStream printer = new PrintStream(out);
		if(options.dot) {
		    Function<Integer, String> fun = index -> charStrMap.get(nba.getAlphabet().getLetter(index));
		    UtilParser.print(nba, out, fun);
		}else {
			// first output initial state
			printer.print("[" + nba.getInitialState() + "]\n");
			// transitions
			for(int stateNr = 0; stateNr < nba.getStateSize(); stateNr ++) {
			    for(int letter = 0; letter < nba.getAlphabetSize(); letter ++) {
			        if(nba.getAlphabet().indexOf(Alphabet.DOLLAR) == letter) continue;
                    for(int succNr : nba.getSuccessors(stateNr, letter)) {
                        printer.print(charStrMap.get(nba.getAlphabet().getLetter(letter))
                                + "," + "[" + stateNr + "]->[" + succNr + "]\n");
                    }
			    }
			}
			
			for(final int finalNr : nba.getFinalStates()) {
				printer.print("[" + finalNr + "]\n");
			}
		}
	}

	@Override
	public void close() {		
	}
	
	protected void setInitial(String state) {
		State init = getState(state);
		automaton.setInitialState(init);
	}
	
	protected void addTransition(String source, String target, String ap) {
		State st = getState(source);
		State tg = getState(target);
		char ch = getCharFromString(ap);
		st.addTransition(new Transition(ch, tg));
	}
	
	protected void setAccepting(String state) {
		State fin = getState(state);
		fin.setAccept(true);
	}
	
	protected void parseBegin() {
		
	}
	
	protected void parseEnd() {
		nba = NBAOperations.fromDkNBA(automaton, alphabet);
		automaton = null; // empty this automaton
	}
	
	// we reserve '$' sign for L dollar automaton
	protected char getCharFromString(String label) {
		char ch = 0;
		if(strCharMap.containsKey(label)) {
			return strCharMap.get(label);
		}

		ch = (char) strCharMap.size();
		if(ch >= '$' )   ch ++; // reserve '$' sign
		strCharMap.put(label, ch);
		charStrMap.put(ch, label);
		alphabet.addLetter(ch); // add characters
		return ch;
	}
	
	protected State getState(String str) {
	    State state = strStateMap.get(str);
		if(state == null) {
			state = new State();
			strStateMap.put(str, state);
		}
		return state;
	}
	
//	public String translate(List<String> strs) {
//		StringBuilder builder = new StringBuilder();
//		for(String str : strs) {
//			builder.append(charStrMap.get(str));
//		}
//		return builder.toString();
//	}
//	
//	public String translate(String strs) {
//		StringBuilder builder = new StringBuilder();
//		for(int i = 0; i < strs.length(); i ++) {
//			builder.append(charStrMap.get("" + strs.charAt(i)));
//		}
//		return builder.toString();
//	}

}
