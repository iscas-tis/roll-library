package roll.main.inclusion.run;
/* Copyright (c) since 2016                                               */
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

// The code is borrowed from omega library


public class LassoRun {
    
    private final Run stem;
    private final Run loop;
    
    public LassoRun(Run stem, Run loop) {
        this.stem = stem;
        this.loop = loop;
    }
    
    public Run getStem() {
        return stem;
    }
    
    public Run getLoop() {
        return loop;
    }
    
    @Override
    public String toString() {
        return "(" + stem + ", " + loop + ")";
    }
    

}
