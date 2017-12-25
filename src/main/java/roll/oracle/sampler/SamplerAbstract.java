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

package roll.oracle.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import gnu.trove.procedure.TIntProcedure;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.util.Pair;
import roll.util.sets.ISet;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public abstract class SamplerAbstract implements Sampler {
    
    protected NBA nba;
    protected final long numOfSamples;
    
    public SamplerAbstract(double epsilon, double delta) {
        this.numOfSamples = computeSampleSize(epsilon, delta);
    }
    
    protected long computeSampleSize(double epsilon, double delta) {
        double result = Math.log(delta) / (1.0 * Math.log(1 - epsilon));
        long num = Math.round(result);
        return num;
    }
    
    protected Pair<Integer, StateNFA> rNext(NBA nba, int s) {
        // uniformly pick the successor
        List<Pair<Integer, StateNFA>> nexts = new ArrayList<>();
        StateNFA curr = nba.getState(s);
        TIntProcedure procedure = new TIntProcedure() {
            @Override
            public boolean execute(int letter) {
                ISet succs = curr.getSuccessors(letter);
                for (int succ : succs) {
                    nexts.add(new Pair<>(letter, nba.getState(succ)));
                }
                // for every key
                return true;
            }
        };
        // add every pair of outgoing transitions
        curr.forEachEnabledLetter(procedure);
        // pick a state randomly
        if(nexts.size() <= 0) {
            throw new UnsupportedOperationException("Every state should have at least one successor");
        }
        int sNr = ThreadLocalRandom.current().nextInt(0, nexts.size());
        assert sNr < nexts.size();
        return nexts.get(sNr);
    }
    
    public void setNBA(NBA nba) {
        this.nba = nba;
    }
    
    @Override
    public long getSampleSize() {
        return numOfSamples;
    }

    @Override
    public NBA getNBA() {
        return nba;
    }

}
