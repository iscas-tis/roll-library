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

package roll.learner.dfa.tree;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.tree.Node;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Word;

/**
 * Value in node 
 * */
public class ValueNode {
	
	public final int id;
	public Node<ValueNode> node;
	public final Word label;
	public TIntObjectMap<ISet> predecessors; // use more efficient way to store
	
	public ValueNode(int id, Word label) {
		this.id = id;
		this.label = label;
		predecessors = new TIntObjectHashMap<>();
	}
	
	public void addPredecessor(int source, int letter) {
		if(predecessors.containsKey(letter)) {
			ISet states = predecessors.get(letter);
			states.set(source);
		}else {
			ISet states = UtilISet.newISet();
			states.set(source);
			predecessors.put(letter, states);
		}
	}
	
	public String toString() {
		return id + " : " + label.toStringWithAlphabet();
	}

}
