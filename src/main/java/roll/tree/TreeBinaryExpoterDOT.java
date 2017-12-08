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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import roll.table.HashableValueBoolean;


/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public class TreeBinaryExpoterDOT {
	 
	public static void export(Tree<?> tree, OutputStream stream) {
		
        PrintStream out = new PrintStream(stream);
        out.println("// Binary tree");
        out.println("digraph BST {");
        export(tree.getRoot(), out, 0);
        out.println("}");
        
	}
	
	public static int export(Node<?> node, PrintStream out, int id) {
		out.print(" " + id + " [ label=\"" + node.getLabel().toString() + "\"");
		if(node.isLeaf()) {
			out.print(", shape = box ");
			if(node.isAccepting()) {
				out.print(", style=filled ");
			}
		}else {
			out.print(", shape = circle ");
		}
		out.println(" ];");

		if(! node.isLeaf()) {// have two children
			
			int child = id + 1;
			Node<?> nodeChild = node.getChild(new HashableValueBoolean(false));
			if(nodeChild != null) {
				out.println(" " + id + " -> " + child + " [ style = dashed ];");
				child = export(nodeChild, out, child);
			}
			nodeChild = node.getChild(new HashableValueBoolean(true));
			if(nodeChild != null) {
				out.println(" " + id + " -> " + child + " [ style = solid ];");
				child = export(nodeChild, out, child);
			}
			
			return child;
		}
		
		return id + 1;
	}
	
    public static String toString(Tree<?> tree) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            export(tree, out);
            return out.toString();
        } catch (Exception e) {
            return "ERROR";
        }
    }

}
