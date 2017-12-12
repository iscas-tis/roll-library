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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import jhoafparser.ast.Atom;
import jhoafparser.ast.AtomLabel;
import jhoafparser.ast.BooleanExpression;
import jhoafparser.ast.BooleanExpression.Type;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import roll.parser.hoa.Valuation;
import roll.parser.hoa.ValuationIterator;

/**
 * TODO reimplement the BDD package and supports for clone BDD operation
 * 
 * @author Yong Li (liyong@ios.ac.cn)
 */
public final class BDDManager {

    private BDDFactory bdd;

    private final static int NUM_NODES = 125000;
    private final static int NUM_CACHE = 100000;
    private final static int NUM_INC = 10000;
    private final static String LIB = "jdd";

    private TIntObjectMap<String> varsMap = new TIntObjectHashMap<>();

    public BDDManager(int numNodes, int numCache) {
        bdd = BDDFactory.init(LIB, numNodes, numCache);
        bdd.setMaxIncrease(NUM_INC);
    }

    public BDDManager() {
        this(NUM_NODES, NUM_CACHE);
    }

    public void setNumVar(int numVar) {
        assert bdd != null;
        bdd.setVarNum(numVar);
    }

    public int getNumVars() {
        return bdd.varNum();
    }

    public void addExtraVarNum(int num) {
        bdd.extVarNum(num);
    }

    public BDD getOne() {
        return bdd.one();
    }

    public BDD getZero() {
        return bdd.zero();
    }

    public BDD ithVar(int index) {
        return bdd.ithVar(index);
    }

    // print one letter
    public String toString(BDD bdd) {
        if (bdd.isOne()) {
            return "t";
        } else if (bdd.isZero()) {
            return "f";
        } else {
            boolean first = false;
            String result = "";
            if (bdd.high().isOne()) {
                result += "" + bdd.var() + "";
                first = true;
            } else if (!bdd.high().isZero()) {
                result += "(" + bdd.var() + " & " + toString(bdd.high()) + ")";
                first = true;
            }

            if (bdd.low().isOne()) {
                result += (first ? "|" : "") + "!" + bdd.var() + "";
            } else if (!bdd.low().isZero()) {
                result += (first ? "|" : "") + "(!" + bdd.var() + "&" + toString(bdd.low()) + ")";
            }
            result = "(" + result + ")";
            return result;
        }
    }

    // change one letter to expression
    public BooleanExpression<Atom> toBoolExpr(BDD bdd) {
        if (bdd.isOne()) {
            return new BooleanExpression<>(true);
        } else if (bdd.isZero()) {
            return new BooleanExpression<>(false);
        } else {
            boolean first = false;
            BooleanExpression<Atom> atom = new BooleanExpression<>(AtomLabel.createAPIndex(bdd.var()));
            BooleanExpression<Atom> result = null;
            if (bdd.high().isOne()) {
                result = atom;
                first = true;
            } else if (!bdd.high().isZero()) {
                result = new BooleanExpression<>(Type.EXP_AND, atom, toBoolExpr(bdd.high()));
                first = true;
            }

            if (bdd.low().isOne()) {
                result = first ? new BooleanExpression<>(Type.EXP_OR, result, atom.not()) // high
                                                                                          // |
                                                                                          // !low
                        : atom.not();
            } else if (!bdd.low().isZero()) {
                BooleanExpression<Atom> lower = new BooleanExpression<>(Type.EXP_AND, atom.not(),
                        toBoolExpr(bdd.low()));
                result = first ? new BooleanExpression<>(Type.EXP_OR, result, lower) // high
                                                                                     // |
                                                                                     // low
                        : lower; // low
            }
            return result;
        }
    }

    public BDD fromBoolExpr(BooleanExpression<AtomLabel> boolExpr) {
        assert boolExpr != null;

        if (boolExpr.isTRUE()) {
            return bdd.one();
        } else if (boolExpr.isFALSE()) {
            return bdd.zero();
        } else if (boolExpr.isAtom()) {
            return bdd.ithVar(boolExpr.getAtom().getAPIndex()).andWith(bdd.one());
        } else if (boolExpr.isNOT()) {
            return fromBoolExpr(boolExpr.getLeft()).not();
        } else {
            BDD left = fromBoolExpr(boolExpr.getLeft());
            BDD right = fromBoolExpr(boolExpr.getRight());
            if (boolExpr.isAND()) {
                return left.andWith(right);
            } else {
                return left.orWith(right);
            }
        }
    }

    public BDD fromValuation(Valuation val) {
        BDD result = bdd.one();
        for (int i = 0; i < val.size(); i++) {
            if (val.get(i)) {
                result = result.andWith(bdd.ithVar(i).id());
            } else {
                result = result.andWith(bdd.ithVar(i).not());
            }
        }
        return result;
    }

    public Set<Valuation> toValuationSet(BDD ddSet, int size) {
        Set<Valuation> valSet = new HashSet<>();
        ValuationIterator valIter = new ValuationIterator(size);
        while (valIter.hasNext()) {
            Valuation val = valIter.next();
            BDD dd = fromValuation(val);
            dd = dd.and(ddSet);
            if (!dd.isZero()) {
                valSet.add(val);
            }
            dd.free();
        }
        return valSet;
    }

    public Valuation toOneFullValuation(BDD dd) {
        Valuation val = new Valuation(bdd.varNum());
        for (int i = 0; i < bdd.varNum(); i++) {
            BDD var = bdd.ithVar(i).id();
            var = var.andWith(dd.id());
            val.set(i, !var.isZero());
            var.free();
        }
        return val;
    }

    public Set<Valuation> toValuationSet(BDD ddSet) {
        Set<Valuation> valSet = new HashSet<>();

        BDD dd = ddSet.id();
        while (!dd.isZero()) {
            BDD oneSat = dd.fullSatOne();
            valSet.add(toOneFullValuation(oneSat));
            dd = dd.andWith(oneSat.not());
            oneSat.free();
        }

        dd.free();
        return valSet;
    }

    public Set<BDD> toValuationSetBDD(BDD ddSet, int size) {
        Set<BDD> valSet = new HashSet<>();
        ValuationIterator valIter = new ValuationIterator(size);
        while (valIter.hasNext()) {
            BDD dd = fromValuation(valIter.next());
            dd = dd.andWith(ddSet.not());
            if (!dd.isZero()) {
                valSet.add(dd);
            }
        }
        return valSet;
    }

    public void setVariableName(int var, String name) {
        varsMap.put(var, name);
    }

    // TODO
    public String toDot(BDD bdd) {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph {\n");
        builder.append("}\n");
        return builder.toString();
    }

    public String getVariableName(int var) {
        String name = varsMap.get(var);
        if (name != null)
            return name;
        return var + "";
    }

    public BDDPairing makeBDDPair(List<BDD> presVars, List<BDD> nextVars) {
        BDDPairing bddPair = bdd.makePair();
        for (int index = 0; index < presVars.size(); index++) {
            bddPair.set(presVars.get(index).var(), nextVars.get(index).var());
        }
        for (int index = 0; index < presVars.size(); index++) {
            bddPair.set(nextVars.get(index).var(), presVars.get(index).var());
        }
        return bddPair;
    }

    public void close() {
        bdd.done();
    }

}