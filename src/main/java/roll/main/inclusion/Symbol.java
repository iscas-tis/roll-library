package roll.main.inclusion;

import java.util.Iterator;
import java.util.Set;

import automata.FAState;
import automata.FiniteAutomaton;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectCharMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectCharHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.words.Alphabet;
import roll.words.Word;

public class Symbol {
    
    private final Alphabet alphabet;
    private TCharObjectMap<String> charStrMap ; // char -> str
    private TObjectCharMap<String> strCharMap ; // str -> char
    
    public Symbol() {
        this.alphabet = new Alphabet();
        this.charStrMap = new TCharObjectHashMap<>();
        this.strCharMap = new TObjectCharHashMap<>();
    }
    
    public Alphabet getAlphabet() {
        return alphabet;
    }
    
    public String getSymbol(int letter) {
        return getSymbol(alphabet.getLetter(letter));
    }
    
    public char addSymbol(String symb) {
        return getCharFromString(symb);
    }
    
    public int getSymbolIndex(String symb) {
        return alphabet.indexOf(strCharMap.get(symb));
    }
    
    // we reserve '$' sign for L dollar automaton
    private char getCharFromString(String label) {
        char ch = 0;
        if(strCharMap.containsKey(label)) {
            return strCharMap.get(label);
        }

        ch = (char) strCharMap.size();
        if(ch >= Alphabet.DOLLAR)   ch ++; // reserve '$' sign
        strCharMap.put(label, ch);
        charStrMap.put(ch, label);
        alphabet.addLetter(ch); // add characters
        return ch;
    }
    
    public char getChar(String symb) {
        if(strCharMap.containsKey(symb)) {
            return strCharMap.get(symb);
        }
        throw new UnsupportedOperationException("NO char is available for symbol " + symb);
    }
    
    public String getSymbol(char ch) {
        if(charStrMap.containsKey(ch)) {
            return charStrMap.get(ch);
        }
        throw new UnsupportedOperationException("NO char is available for char " + (int)ch);
    }
    
    private FAState getOrAddState(FiniteAutomaton result, FAState left, TIntObjectMap<FAState> map) {
        FAState right = map.get(left.id);
        if(right == null) {
            right = result.createState();
            map.put(left.id, right);
        }
        return right;
    }
    
    public FiniteAutomaton translateAutomaton(FiniteAutomaton aut) {
        FiniteAutomaton result = new FiniteAutomaton();
        TIntObjectMap<FAState> map = new TIntObjectHashMap<>();
        for(FAState st : aut.states) {
            FAState rSt = getOrAddState(result, st, map);
            // all successors
            Iterator<String> nextIt = st.nextIt();
            while(nextIt.hasNext()) {
                String symb = nextIt.next();
                Set<FAState> succs = st.getNext(symb);
                char ch = getChar(symb);
                if(succs == null) continue;
                for(FAState succ : succs) {
                    FAState rSucc = getOrAddState(result, succ, map);
                    result.addTransition(rSt, rSucc, ch + "");
                }
            }
            if(st.id == aut.getInitialState().id) {
                result.setInitialState(rSt);
            }
            if(aut.F.contains(st)) {
                result.F.add(rSt);
            }
        }
        return result;
    }
    
    private int getOrAddState(NBA nba, FAState left, TIntIntMap map) {
        int right = -1;
        if(map.containsKey(left.id)) {
            right = map.get(left.id);
        }
        if(right == -1) {
            StateNFA rtSt = nba.createState();
            right = rtSt.getId();
            map.put(left.id, right);
        }
        return right;
    }
    
    public NBA toNBA(FiniteAutomaton aut) {
        NBA result = new NBA(alphabet);
        TIntIntMap map = new TIntIntHashMap();
        for(FAState st : aut.states) {
            int rSt = getOrAddState(result, st, map);
            // all successors
            Iterator<String> nextIt = st.nextIt();
            while(nextIt.hasNext()) {
                String symb = nextIt.next();
                Set<FAState> succs = st.getNext(symb);
                char ch = getChar(symb);
                if(succs == null) continue;
                for(FAState succ : succs) {
                    int rSucc = getOrAddState(result, succ, map);
                    result.getState(rSt).addTransition(alphabet.indexOf(ch), rSucc);
                }
            }
            if(st.id == aut.getInitialState().id) {
                result.setInitial(rSt);
            }
            if(aut.F.contains(st)) {
                result.setFinal(rSt);
            }
        }
        return result;
    }
    
    public String translate(Word word) {
        StringBuilder builder = new StringBuilder();
        for (int letterNr = 0; letterNr < word.length(); letterNr ++) {
            builder.append(getSymbol(alphabet.getLetter(word.getLetter(letterNr))));
        }
        return builder.toString();
    }

}
