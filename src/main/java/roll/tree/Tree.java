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

/* later add balanced binary tree */
public interface Tree<V> {
	
	Node<V> getRoot();
	
	//Node<V> sift(Word r);
	
	//void update(Word r);
	
	default Node<V> getLeastCommonAncestor(Node<V> p, Node<V> q) {
		assert p != q && p != null && q != null;
		
		Node<V> lowerNode, higherNode;
		if(p.getDepth() - q.getDepth() >= 0) {
			higherNode = p;
			lowerNode = q;
		}else {
			higherNode = q;
			lowerNode = p;
		}
		// to the same level
		while(higherNode.getDepth() != lowerNode.getDepth()) {
			higherNode = higherNode.getParent();
		}
		// go to common ancestor with same speed
		while(higherNode != lowerNode) {
			higherNode = higherNode.getParent();
			lowerNode = lowerNode.getParent();
		}
		
		return higherNode;
	}
	
	default LCA<V> getLCA(Node<V> p, Node<V> q) {
		assert p != q && p != null && q != null;
		
		Node<V> lowerNode, higherNode;
		LCA<V> lca = new LCA<>();
		
		boolean isPHiger = p.getDepth() - q.getDepth() >= 0 ? true : false;
		
		if(isPHiger) {
			higherNode = p;
			lowerNode = q;
		}else {
			higherNode = q;
			lowerNode = p;
		}
		
		HashableValue highBranch = higherNode.fromBranch()
				, lowBranch = lowerNode.fromBranch();
		// to the same level
		while(higherNode.getDepth() != lowerNode.getDepth()) {
			highBranch = higherNode.fromBranch();     // before go to parent, record current branch
			higherNode = higherNode.getParent();
		}
		// go to common ancestor with same speed
		while(higherNode != lowerNode) {
			highBranch = higherNode.fromBranch();
			lowBranch = lowerNode.fromBranch();
			higherNode = higherNode.getParent();
			lowerNode = lowerNode.getParent();
		}
		
		lca.commonAncestor = higherNode;
		
		if(isPHiger) {
			lca.setChild(false, p, highBranch);
			lca.setChild(true, q, lowBranch);
		}else {
			lca.setChild(false, q, highBranch);
			lca.setChild(true, p, lowBranch);
		}
		
		return lca;
	}

}
