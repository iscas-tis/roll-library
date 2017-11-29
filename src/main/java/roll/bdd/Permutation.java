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

package roll.bdd;

import java.util.Collections;
import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDPairing;

/**
 * Adapted from EPMC tool code
 * 
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class Permutation {
	
	private final List<BDD> presVars;
	private final List<BDD> nextVars;
	private final BDDManager bdd;
	private final BDDPairing bddPair;
	
	public Permutation(BDDManager bdd, List<BDD> pres, List<BDD> nexts) {
		assert pres.size() == nexts.size();
		presVars = pres;
		nextVars = nexts;
		this.bdd = bdd;
		this.bddPair = bdd.makeBDDPair(pres, nexts);
	}
	
	public BDDPairing getBDDPairing() {
		return bddPair;
	}
	
	public List<BDD> getPresVars() {
		return Collections.unmodifiableList(presVars);
	}
	
	public List<BDD> getNextVars() {
		return Collections.unmodifiableList(nextVars);
	}
	
	public BDDManager getBDDManager() {
		return bdd;
	}

}
