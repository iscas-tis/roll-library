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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.javabdd.BDD;

//only support at most two copies
/**
 * currently only support for integer variable Adapted from EPMC tool code
 * 
 * @author Yong Li (liyong@ios.ac.cn)
 */

public class VariableBDD {

    private final int copies;
    private final List<List<BDD>> ddVariables;
    private final BDDManager bdd;
    private final String name;
    private final int lower;
    private final int upper;
    private final String UNDERSCORE = "_";

    public VariableBDD(BDDManager bdd, int copies, String name, int lower, int upper) {
        assert bdd != null;
        assert copies > 0;
        assert name != null;

        this.bdd = bdd;
        this.copies = copies;
        this.ddVariables = new ArrayList<>(copies);
        this.name = name;
        this.lower = lower;
        this.upper = upper;

        prepareBDDVariables();
    }

    public int getLower() {
        return lower;
    }

    public int getUpper() {
        return upper;
    }

    private void prepareBDDVariables() {
        final int numValues = getUpper() - getLower() + 1;
        final int numBits = Integer.SIZE - Integer.numberOfLeadingZeros(numValues - 1);
        for (int copy = 0; copy < copies; copy++) {
            this.ddVariables.add(new ArrayList<>());
        }

        int varNum = bdd.getNumVars();
        bdd.addExtraVarNum(numBits * copies);
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            for (int copy = 0; copy < copies; copy++) {
                BDD dd = bdd.ithVar(varNum);
                String ddName = name + UNDERSCORE + bitNr + UNDERSCORE + copy;
                bdd.setVariableName(varNum, ddName);
                this.ddVariables.get(copy).add(dd);
                ++varNum;
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getNumCopies() {
        return copies;
    }

    public List<BDD> getBDDVariables(int copy) {
        return Collections.unmodifiableList(ddVariables.get(copy));
    }

    /** only contain the variables in VariableBDD */
    public int toInteger(BDD dd) {
        int value = 0;
        int bit = 1;
        for (BDD bitVar : getBDDVariables(0)) {
            BDD result = bitVar.and(dd);
            if (!result.isZero()) {
                value |= bit;
            }
            result.free();
            bit <<= 1;
        }
        value += lower;
        return value;
    }

    public BDD newValue(int copy, int value) {
        assert copy >= 0;
        assert copy < getNumCopies();
        assert value >= getLower();
        assert value <= getUpper();

        value -= getLower();
        BDD dd = bdd.getOne();
        int bit = 1;
        for (BDD bitVar : getBDDVariables(copy)) {
            BDD oldDD = dd;
            BDD bitVarNot = bitVar.not();
            dd = dd.and((value & bit) != 0 ? bitVar : bitVarNot);
            oldDD.free();
            bitVarNot.free();
            bit <<= 1;
        }
        return dd;
    }

    public BDD getCube(int copy) {
        BDD cube = bdd.getOne();
        for (BDD dd : getBDDVariables(copy)) {
            BDD temp = cube.and(dd);
            cube.free();
            cube = temp;
        }

        return cube;
    }

    public void close() {
        for (List<BDD> vars : ddVariables) {
            for (BDD var : vars) {
                var.free();
            }
        }
    }

}
