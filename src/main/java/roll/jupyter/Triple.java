/* Copyright (c) 2016, 2017, 2018                                         */
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

package roll.jupyter;

import roll.main.IHTML;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.HashableValue;

/**
 * @author Yong Li
 * */
public class Triple implements IHTML {
    String learner;              // the data structure of the learner
    String hypothesis;           // the automaton of the learner
    Query<HashableValue> query;  // the counterexample used in last time
    
    public Triple(String learner, String hypothesis, Query<HashableValue> query) {
        this.learner = learner;
        this.hypothesis = hypothesis;
        this.query = query;
    }
    
    public String getLeft() {
        return learner;
    }
    
    public String getMiddle() {
        return hypothesis;
    }
    
    public Query<HashableValue> getRight() {
        return query;
    }

    @Override
    public String toHTML() {
        String html =
                "<table border=\"1\" cellspacing=\"0\" bordercolor=\"#000000\"  style=\"border-collapse:collapse;\">\n" +
                "  <tr>\n" +
                "    <th><center> Learner</center></th>\n" +
                "    <th><center>  Hypothesis </center></th>\n" +
                "    <th><center> Counterexample </center></th>\n" +
                "  </tr>\n" +
                "  <tr>\n"  +
                "    <td>%s</td>\n" +
                "    <td><center>%s</center></td>\n" +
                "    <td><center>%s</center></td>\n" +
                "  </tr>\n" +
                "</table>";
        String learner = getLeft();
        String hypothesis = getMiddle();
        QuerySimple<HashableValue> query = (QuerySimple<HashableValue>) getRight();
        return String.format(html, learner, hypothesis, query == null ? "" : "$" + query.toHTML() + "$");
    }
}