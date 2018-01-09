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

package roll.automata.operations;

import java.util.HashSet;
import java.util.Set;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.util.sets.ISet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class StateContainer {
    
    private final int state;
    private final NBA nba;
    private final TIntObjectMap<Set<StateNFA>> preds; // predecessors
    private final TIntObjectMap<Set<StateNFA>> succs; // predecessors
    
    public StateContainer(int state, NBA nba) {
        this.state = state;
        this.nba = nba;
        this.preds = new TIntObjectHashMap<>();
        this.succs = new TIntObjectHashMap<>();
    }
    
    public StateNFA getState() {
        return nba.getState(state);
    }
    
    public int getId() {
        return state;
    }
    
    public NBA getFA() {
        return nba;
    }
    
    public void addPredecessors(int letter, int pred) {
        Set<StateNFA> pres = preds.get(letter);
        if(pres == null) {
            pres = new HashSet<>();
        }
        pres.add(nba.getState(pred));
        preds.put(letter, pres);
    }
    
    public void addSuccessors(int letter, int succ) {
        Set<StateNFA> sucs = succs.get(letter);
        if(sucs == null) {
            sucs = new HashSet<>();
        }
        sucs.add(nba.getState(succ));
        succs.put(letter, sucs);
    }
    
    public Set<StateNFA> getPredecessors(int letter) {
        Set<StateNFA> pres = new HashSet<>();
        if(preds.containsKey(letter)) {
            pres.addAll(preds.get(letter));
        }
        return pres;
    }
    
    public Set<StateNFA> getSuccessors(int letter) {
        Set<StateNFA> sucs = new HashSet<>();
        if(succs.containsKey(letter)) {
            sucs.addAll(succs.get(letter));
        }
        return sucs;
    }
    
    public int getPredSize(int letter) {
        Set<StateNFA> pres = preds.get(letter);
        if(pres == null) return 0;
        return pres.size();
    }
    
    public int getSuccSize(int letter) {
        Set<StateNFA> sucs = succs.get(letter);
        if(sucs == null) return 0;
        return sucs.size();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(obj instanceof StateContainer) {
            StateContainer sc = (StateContainer)obj;
            return state == sc.state
              && nba == sc.nba;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return state;
    }
    
    public boolean isForwardCovered(StateContainer other) {
        for(int letter = 0; letter < nba.getAlphabetSize(); letter ++) {
            ISet sndSuccs = other.getState().getSuccessors(letter);
            if(sndSuccs.isEmpty()) continue;
            ISet fstSuccs = getState().getSuccessors(letter);
            if(fstSuccs.isEmpty()) return false;
        }
        return true;
    }

}
