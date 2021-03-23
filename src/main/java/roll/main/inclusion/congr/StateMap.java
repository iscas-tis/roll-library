package roll.main.inclusion.congr;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.StateContainer;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;

public class StateMap {
	
	int number;
	int state;
	int base;
	NBA nba;
	
	StateContainer[] autPres;
	TIntObjectMap<ISet> succMap;
	TIntObjectMap<ISet> predMap;
	
	public StateMap(int s, int b, NBA aut) {
		this.number = s + b;
		this.base = b;
		this.state = s;
		this.nba = aut;
		this.succMap = new TIntObjectHashMap<>();
		this.predMap = new TIntObjectHashMap<>();
	}
	
	public int getStateId() {
		return state;
	}
	
	public StateNFA getState() {
		return nba.getState(state);
	}
	
	public boolean isInitial() {
		return getStateId() == nba.getInitialState();
	}
	
	public boolean isFinal() {
		return nba.isFinal(getStateId());
	}
	
	public void addSuccessor(int letter, int state) {
		ISet result;
		if(succMap.containsKey(letter)) {
			result = succMap.get(letter);
		}else {
			result = UtilISet.newISet();
		}
		result.set(state + base);
		succMap.put(letter, result);
	}
	
	public void addPredecessor(int letter, int state) {
		ISet result;
		if(predMap.containsKey(letter)) {
			result = predMap.get(letter);
		}else {
			result = UtilISet.newISet();
		}
		result.set(state + base);
		predMap.put(letter, result);
	}
	
	public ISet getSuccessors(int letter) {
		ISet result = succMap.get(letter);
		if(result != null) {
			return result;
		}else {
			return UtilISet.newISet();
		}
	}
	
	public ISet getPredecessors(int letter) {
		ISet result = predMap.get(letter);
		if(result != null) {
			return result;
		}else {
			return UtilISet.newISet();
		}
	}
	
	public boolean forwardCovers(StateNFA other) {
		return this.getState().forwardCovers(other);
	}
	
}
