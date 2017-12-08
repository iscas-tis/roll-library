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


package roll.tree;

import roll.table.HashableValue;

/** not only keeps the information of LCA, 
 * but also store the branching information
 *  */
public class LCA<V> {
	
	public Node<V> commonAncestor;
	public HashableValue firstBranch;
	public HashableValue secondBranch;
	public Node<V> firstChild;
	public Node<V> secondChild;
	
	public LCA() {
		
	}
	
	public void setChild(boolean first, Node<V> child, HashableValue branch) {
		assert child != null && branch != null;
		if(first) {
			firstChild = child;
			firstBranch = branch;
		}else {
			secondChild = child;
			secondBranch = branch;
		}
		
	}
	
}
