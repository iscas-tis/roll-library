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

package roll.automata.operations;

import roll.automata.NBA;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * 
 * */

public class NBASimulation {
    
//    public static boolean isForwardSimulated(NBA fst, NBA snd) {
//        assert fst != snd;
//        StateContainer[] states = new StateContainer[fst.getStateSize() + snd.getStateSize()];
//        for(int i = 0; i < fst.getStateSize(); i ++) {
//            states[i] = new StateContainer(i, fst);
//        }
//        for(int i = 0; i < fst.getStateSize(); i ++) {
//            StateNFA st = states[i].getState();
//            for(int letter = 0; letter < fst.getAlphabetSize(); letter ++) {
//                for(int succ : st.getSuccessors(letter)) {
//                    states[i].addSuccessors(letter, succ);
//                    states[succ].addPredecessors(letter, i);
//                }
//            }
//        }
//        
//        int n = fst.getStateSize();
//        for(int i = 0; i < snd.getStateSize(); i ++) {
//            states[i + n] = new StateContainer(i, snd);
//        }
//        for(int i = 0; i < snd.getStateSize(); i ++) {
//            StateNFA st = states[i + n].getState();
//            for(int letter = 0; letter < snd.getAlphabetSize(); letter ++) {
//                for(int succ : st.getSuccessors(letter)) {
//                    states[i + n].addSuccessors(letter, succ);
//                    states[succ + n].addPredecessors(letter, i);
//                }
//            }
//        }
//            
//        boolean[] isFinal = new boolean[states.length];
//        boolean[] isInit = new boolean[states.length];
//        boolean[][] fsim = new boolean[states.length][states.length];
//        for (int i = 0; i < states.length; i++) {
//            isFinal[i] = states[i].getFA().isFinal(states[i].getId());
//            isInit[i] = states[i].getFA().getInitialState() == states[i].getId();
//        }
//        for (int i = 0; i < states.length; i++) {
//            for (int j = i; j < states.length; j++) {
//                fsim[i][j] = (!isFinal[i] || isFinal[j]) && states[j].isForwardCovered(states[i]);
//                fsim[j][i] = (isFinal[i] || !isFinal[j]) && states[i].isForwardCovered(states[j]);
//            }
//        }
//        return isForwardSimulated(fst, snd, fsim, states);
//    }
//    
//    public static boolean isForwardSimulated(NBA fst, NBA snd, boolean[][] fsim, StateContainer[] states) {
//        
//        Alphabet alphabet = fst.getAlphabet();
//        //implement the HHK algorithm
//        // fsim[u][v]=true iff v in fsim(u) iff v forward-simulates u
//        int[][][] pre = new int[alphabet.getLetterSize()][states.length][];
//        int[][][] post = new int[alphabet.getLetterSize()][states.length][];
//        int[][] pre_len = new int[alphabet.getLetterSize()][states.length];
//        int[][] post_len = new int[alphabet.getLetterSize()][states.length];
//
//        // Initialize memory of pre/post
//        for (int c = 0; c < alphabet.getLetterSize(); c++) {
//            for (int s = 0; s < states.length; s++) {
//                Set<StateNFA> next = states[s].getSuccessors(c);
//                post_len[c][s] = 0;
//                if (!next.isEmpty())
//                    post[c][s] = new int[next.size()];
//                Set<StateNFA> prev = states[s].getPredecessors(c);
//                pre_len[c][s] = 0;
//                if (!prev.isEmpty())
//                    pre[c][s] = new int[prev.size()];
//            }
//        }
//
//        //state[post[s][q][r]] is in post_s(q) for 0<=r<adj_len[s][q]
//        //state[pre[s][q][r]] is in pre_s(q) for 0<=r<adj_len[s][q]
//        for (int c = 0; c < alphabet.getLetterSize(); c++) {
//            for (int p = 0; p < states.length; p++) {
//                Set<StateNFA> next = states[p].getSuccessors(c);
//                if (!next.isEmpty()) {
//                    for (int q = 0; q < states.length; q++) {
//                        if (next.contains(states[q].getState())) {
//                            // if p --a--> q, then p is in pre_a(q), q is in
//                            // post_a(p)
//                            pre[c][q][pre_len[c][q]++] = p;
//                            post[c][p][post_len[c][p]++] = q;
//                        }
//                    }
//                }
//            }
//        }
//
//        int[] todo = new int[alphabet.getLetterSize() * states.length];
//        int todo_len = 0;
//        
//        int[][][] remove = new int[alphabet.getLetterSize()][states.length][states.length];
//        int[][] remove_len = new int[alphabet.getLetterSize()][states.length];
//        for(int a=0; a<alphabet.getLetterSize(); a++)
//        {
//            for(int p=0; p<states.length; p++)
//                if(pre_len[a][p]>0) // p is in a_S
//                {   
//                    Sharpen_S_a:
//                    for(int q=0; q<states.length; q++)   // {all q} --> S_a 
//                    {
//                            if(post_len[a][q]>0)    /// q is in S_a 
//                            {   
//                                for(int r=0; r<post_len[a][q]; r++) 
//                                    if(fsim[p][post[a][q][r]])  // q is in pre_a(sim(p))
//                                        continue Sharpen_S_a;   // skip q                       
//                                remove[a][p][remove_len[a][p]++] = q;
//                            }
//                    }
//                    if(remove_len[a][p]>0)
//                        todo[todo_len++] = a*states.length + p;
//                }
//        }
//        int[] swap = new int[states.length];
//        int swap_len = 0;
//        boolean using_swap = false;
//        
//        while(todo_len>0)
//        {
//            todo_len--;
//            int v = todo[todo_len] % states.length;
//            int a = todo[todo_len] / states.length;
//            int len = (using_swap? swap_len : remove_len[a][v]);
//            remove_len[a][v] = 0;
//            
//            for(int j=0; j<pre_len[a][v]; j++)
//            {
//                int u = pre[a][v][j];
//                
//                for(int i=0; i<len; i++)            
//                {
//                    int w = (using_swap? swap[i] : remove[a][v][i]);
//                    if(fsim[u][w]) 
//                    {
//                        fsim[u][w] = false;                 
//                        for(int b=0; b<alphabet.getLetterSize(); b++)
//                            if(pre_len[b][u]>0)
//                            {
//                                Sharpen_pre_b_w:
//                                for(int k=0; k<pre_len[b][w]; k++)
//                                {   
//                                    int ww = pre[b][w][k];
//                                    for(int r=0; r<post_len[b][ww]; r++) 
//                                        if(fsim[u][post[b][ww][r]])     // ww is in pre_b(sim(u))
//                                            continue Sharpen_pre_b_w;   // skip ww
//                                    
//                                    if(b==a && u==v && !using_swap)
//                                        swap[swap_len++] = ww;
//                                    else{                                       
//                                        if(remove_len[b][u]==0)
//                                            todo[todo_len++] = b*states.length + u;
//                                        remove[b][u][remove_len[b][u]++] = ww;
//                                    }
//                                    
//                                }
//                            }
//                    }//End of if(fsim[u][w])
//                }               
//            }           
//            if(swap_len>0)
//            {   
//                if(!using_swap)
//                {   
//                    todo[todo_len++] = a*states.length + v;  
//                    using_swap = true; 
//                }else{
//                    swap_len = 0;
//                    using_swap = false;
//                }
//            }
//            
//        }
//
//        for(int p=0; p<states.length; p++)   
//            for(int q=0; q<states.length; q++)
//                if(fsim[p][q]) {
//                    // q is in sim(p), q simulates p
//                    if(states[p].getId() == fst.getInitialState()
//                     && states[q].getId() == snd.getInitialState()) {
//                        return true;
//                    }
//                }
//                    
//        return false;
//    }
    
    public static boolean isDelayedSimulated(NBA fst, NBA snd) {
        return false;
    }

}
