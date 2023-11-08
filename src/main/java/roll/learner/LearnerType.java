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

package roll.learner;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */
public enum LearnerType {
	
	DFA_LSTAR,
	DFA_COLUMN_TREE,
	DFA_COLUMN_TABLE,
	DFA_KV,
	NFA_NLSTAR,
	NFA_RDSTAR,
	NFA_RDFA,
	WEIGHT,
	NBA_FDFA,
	DPA_FDFA,
	NBA_LDOLLAR,
	NBA_MP,
	TDBA_FDFA,
	
	SDFA,
	
	// FDFA
	
	FDFA,
	FNFA,
	
	FDFA_LEADING_TABLE,
	FDFA_LEADING_TREE,
	
	FDFA_PERIODIC_TABLE,
	FDFA_SYNTACTIC_TABLE,
	FDFA_RECURRENT_TABLE,
	FDFA_LIMIT_TABLE,
	
	FDFA_PERIODIC_TREE,
	FDFA_SYNTACTIC_TREE,
	FDFA_RECURRENT_TREE,
	FDFA_LIMIT_TREE
	
}
