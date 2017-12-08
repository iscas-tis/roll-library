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
import java.util.HashMap;
import java.util.Map;

import roll.table.ExprValue;
import roll.table.HashableValue;


public abstract class NodeAbstract<V> implements Node<V> {
	
	protected final Node<V> parent;
	protected final ExprValue exprValue;
	protected Map<HashableValue, Node<V>> children; 
	protected V value;
	private boolean isAccepting = false;
	protected final HashableValue branch; // store branch info
	private final int depth;   // for compute LCA
	
	public NodeAbstract(Node<V> parent, HashableValue branch, ExprValue exprValue) {
		this.parent = parent;
		this.exprValue  = exprValue;
		this.children = new HashMap<>();
		this.branch = branch;
		this.depth = parent == null ? 0 : parent.getDepth() + 1;
	}
	
	public Node<V> getParent() {
		return parent;
	}
	
	public ExprValue getLabel() {
		return exprValue;
	}
	
	public Collection<Node<V>> getChildren() {
		return children.values();
	}
	public Node<V> getChild(HashableValue value) {
		return children.get(value);
	}
	
	public V getValue() {
		return value;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public HashableValue fromBranch() {
		return branch;
	}
	
	public void setValue(V v) {
		this.value = v;
	}
	
	public void addChild(HashableValue v, Node<V> n) {
		children.put(v, n);
	}
	
	public void setAcceting() {
		isAccepting = true;
	}
	
	public boolean isAccepting() {
		return isAccepting;
	}

}
