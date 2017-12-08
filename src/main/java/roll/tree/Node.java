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

import java.util.Collection;

import roll.table.ExprValue;
import roll.table.HashableValue;


/**
 * Node of tree:
 * V is the key used to get the child node
 * ExprValue is the experiment to distinguish different nodes
 * */
public interface Node<V> {
	
	//gets
	Node<V> getParent();
	ExprValue getLabel(); // the function L_n
	Collection<Node<V>> getChildren();
	Node<V> getChild(HashableValue value); // function L_e
	V getValue();	                       // 
	HashableValue fromBranch();            // which branch?
	
	// specially for binary tree
	default Node<V> getLeftNode() {
		return null;
	}
	
	default Node<V> getRightNode() {
		return null;
	}
	
	int getDepth();

	//sets
	void setValue(V v);
	void addChild(HashableValue v, Node<V> n);
	void setAcceting();
	//public void setLabel(ExprValue v); should be final once it has been instantiated
	
	//tests
	default boolean isRoot() {
		return getParent() == null;
	}
	
	default boolean isLeaf() {
		return getChildren().isEmpty();
	}
	//boolean equals(Object obj);
	boolean isAccepting();

}
