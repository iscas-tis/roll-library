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

import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.tree.Node;
import roll.tree.NodeAbstract;
import roll.words.Word;

public class NodeImpl extends NodeAbstract<ValueNode> {

	public NodeImpl(Node<ValueNode> parent, HashableValue branch, ExprValue exprValue) {
		super(parent, branch, exprValue);
	}

	private boolean isAccepting = false;

	@Override
	public void setAcceting() {
		isAccepting = true;
	}

	@Override
	public boolean isAccepting() {
		return isAccepting;
	} 
	
	public Node<ValueNode> getLeftNode() {
		return null;
	}
	
	public Node<ValueNode> getRightNode() {
		return null;
	}
	
	public String toString() {
		if (getLabel().get() instanceof Word) {
			Word label = getLabel().get();  
			return label.toStringWithAlphabet() + ":" + isLeaf() + ":" + getDepth();
		}else {
			return getLabel().get().toString();
		}
		
	}
}
