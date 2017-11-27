/*
 * Written by Yong Li (liyong@ios.ac.cn)
 * This file is part of the Buchi.
 * 
 * Buchi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Buchi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Buchi. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package roll.util;

//In order to keep the original code as much as possible, we
//use the interface of BitSet
public interface ISet extends Iterable<Integer> {

    ISet clone();

    void andNot(ISet set);

    void and(ISet set);

    void or(ISet set);

    boolean get(int value);

    void set(int value);

    void clear(int value);

    void clear();

    boolean isEmpty();

    int cardinality();

    int hashCode();

    String toString();

    default boolean overlap(ISet set) {
        ISet a = null;
        ISet b = null;

        if (this.cardinality() <= set.cardinality()) {
            a = this;
            b = set;
        } else {
            a = set;
            b = this;
        }

        for (final int elem : a) {
            if (b.get(elem))
                return true;
        }
        return false;
    }

    boolean subsetOf(ISet set);

    boolean contentEq(ISet set);
    
    Object get();

}
