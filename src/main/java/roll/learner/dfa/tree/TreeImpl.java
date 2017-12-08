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

import roll.tree.Node;
import roll.tree.Tree;
import roll.tree.TreeBinaryExpoterDOT;

public class TreeImpl implements Tree<ValueNode> {


	private final Node<ValueNode> root;
	private Node<ValueNode> leafLambda;
	
	public TreeImpl(Node<ValueNode> root) {
		this.root = root;
	}
	
	public void setLamdaLeaf(Node<ValueNode> lamda) {
		this.leafLambda = lamda;
	}
	
	public Node<ValueNode> getLamdaLeaf() {
		return leafLambda;
	}

	@Override
	public Node<ValueNode> getRoot() {
		return root;
	}
	
	public String toString() {
		return TreeBinaryExpoterDOT.toString(this);
	}

}
