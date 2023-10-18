/* Copyright (c) 2022                                                     */
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

package roll.parser.hoa;

import java.util.ArrayList;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import net.sf.javabdd.BDD;

// Explicit graph with BDD labels on the edge
public class GraphBDD {
	
	int initialState;
	TIntObjectMap<ArrayList<Pair<BDD, Integer>>> transitions;
	ISet finalStates;
	
	public GraphBDD()
	{
		transitions = new TIntObjectHashMap<>();
		finalStates = UtilISet.newISet();
	}
	
	
	
	void addTransition(int src, BDD cond, int dst)
	{
		if (! transitions.containsKey(src))
		{
			transitions.put(src, new ArrayList<>());
		}
		transitions.get(src).add(new Pair<>(cond, dst));
	}
	
	void setInitial(int init)
	{
		this.initialState = init;
	}
	
	void setFinal(int f)
	{
		this.finalStates.set(f);
	}
	
	void free()
	{
		for (ArrayList<Pair<BDD, Integer>> edges: transitions.valueCollection())
		{
			for (Pair<BDD, Integer> edge : edges)
			{
				edge.getLeft().free();
			}
		}
	}

}
