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

package roll.util.sets;

import java.util.Iterator;

public class UtilISet {
    private UtilISet() {
        
    }
    
    public static ISet newISet() {
        return new ISetTreeSet();
    }
    
    public static boolean compare(ISet set1, ISet set2) {
    	if(set1.cardinality() < set2.cardinality()) {
    		return true;
    	}
    	if(set1.cardinality() > set2.cardinality()) {
    		return false;
    	}
    	Iterator<Integer> iter1 = set1.iterator();
    	Iterator<Integer> iter2 = set2.iterator();
    	while(iter1.hasNext() && iter2.hasNext()) {
    		int first = iter1.next();
    		int second = iter2.next();
    		if(first < second) {
    			return true;
    		}else if(first > second) {
    			return false;
    		}
    	}
		return true;
    }

}
