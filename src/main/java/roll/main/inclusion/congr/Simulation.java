package roll.main.inclusion.congr;

import java.util.Set;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.StateContainer;
import roll.main.Options;
import roll.parser.ba.PairParserBA;
import roll.util.sets.ISet;

public class Simulation {
	
	public static boolean[][] computeForwardSimulation(NBA fst, NBA snd) {
		int numStates = fst.getStateSize() + snd.getStateSize();
		StateMap[] map = new StateMap[numStates];
		for(int i = 0; i < fst.getStateSize(); i ++) {
			map[i] = new StateMap(i, 0, fst);
		}
		int base = fst.getStateSize();
		for(int i = base; i < numStates; i ++) {
			map[i] = new StateMap(i - base, base, snd);
		}
		for(int i = 0; i < fst.getStateSize(); i ++) {
			StateNFA st = fst.getState(i);
			for (int letter = 0; letter < fst.getAlphabetSize(); letter++) {
				for (int succ : st.getSuccessors(letter)) {
					map[i].addSuccessor(letter, succ);
					map[succ].addPredecessor(letter, i);
				}
			}
		}
		for(int i = 0; i < snd.getStateSize(); i ++) {
			StateNFA st = snd.getState(i);
			for (int letter = 0; letter < snd.getAlphabetSize(); letter++) {
				for (int succ : st.getSuccessors(letter)) {
					map[i + base].addSuccessor(letter, succ);
					map[succ + base].addPredecessor(letter, i);
				}
			}
		}
		boolean[] isFinal = new boolean[numStates];
		boolean[] isInit = new boolean[numStates];
		// forward simulation
		boolean[][] fsim = new boolean[numStates][numStates];
		for (int i = 0; i < numStates; i++) {
			isFinal[i] = map[i].isFinal();
			isInit[i] = map[i].isInitial();
		}
		for (int i = 0; i < numStates; i++) {
			StateNFA iState = map[i].getState();
			for (int j = i; j < numStates; j++) {
				StateNFA jState = map[j].getState();
				fsim[i][j] =  (!isFinal[i] || isFinal[j])
						&& jState.forwardCovers(iState);
				fsim[j][i] =  (isFinal[i] || !isFinal[j])
						&& iState.forwardCovers(jState);
			}
		}
		computeFastFwSimilation(fst.getAlphabetSize(), map, fsim);
//		boolean[][] result = new boolean[fst.getStateSize()][snd.getStateSize()];
//		for(int i = 0; i < numStates; i ++) {
//			for(int j = 0; j < numStates; j ++) {
//					if(i < fst.getStateSize() && j >= base && fsim[i][j]) {
//						result[i][j - base] = fsim[i][j];
//						System.out.println((j-base) + " simulates " + i);
//					}
//					if(i >= base && j >= base && fsim[i][j] ) {
//						System.out.println((j-base) + " B-simulates " + (i-base) );
//					}
//			}
//		}
		return fsim;
	}
	
	public static boolean[][] computeForwardSimilation(NBA aut, StateContainer[] autPres) {
		final int size = aut.getStateSize();
		boolean[] isFinal = new boolean[size];
		boolean[] isInit = new boolean[size];
		// forward simulation
		boolean[][] fsim = new boolean[size][size];
		for (int i = 0; i < size; i++) {
			isFinal[i] = aut.isFinal(i);
			isInit[i] = (i == aut.getInitialState());
		}
		for (int i = 0; i < size; i++) {
			StateNFA iState = aut.getState(i);
			for (int j = i; j < size; j++) {
				StateNFA jState = aut.getState(j);
				fsim[i][j] =  (!isFinal[i] || isFinal[j])
						&& jState.forwardCovers(iState);
				fsim[j][i] =  (isFinal[i] || !isFinal[j])
						&& iState.forwardCovers(jState);
			}
		}
		return computeFastFwSimilation(aut, fsim, autPres);
	}
	
	private static boolean[][] computeFastFwSimilation(int numSymbols, StateMap[] map, boolean[][] fsim) {
		//implement the HHK algorithm
//		int numStates = all_states.size();
//		int numSymbols = alphabet.size();
//		FAState[] states = all_states.toArray(new FAState[0]);
//		ArrayList<String> symbols=new ArrayList<String>(alphabet);
//		
//
		// fsim[u][v]=true iff v in fsim(u) iff v forward-simulates u
		int numStates = map.length;
		int[][][] pre = new int[numSymbols][numStates][];
		int[][][] post = new int[numSymbols][numStates][];
		int[][] preLen = new int[numSymbols][numStates];
		int[][] postLen = new int[numSymbols][numStates];
//
		// Initialize memory of pre/post
		for (int a = 0; a < numSymbols; a++) {
			for (int p = 0; p < numStates; p++) {
				ISet next = map[p].getSuccessors(a);
				postLen[a][p] = 0;
				if (!next.isEmpty())
					post[a][p] = new int[next.cardinality()];
				ISet prev = map[p].getPredecessors(a);
				preLen[a][p] = 0;
				if (!prev.isEmpty())
					pre[a][p] = new int[prev.cardinality()];
			}
		}
//
		//state[post[s][q][r]] is in post_s(q) for 0<=r<adj_len[s][q]
		//state[pre[s][q][r]] is in pre_s(q) for 0<=r<adj_len[s][q]
		for (int a = 0; a < numSymbols; a++) {
			for (int p = 0; p < numStates; p++) {
				ISet next = map[p].getSuccessors(a);
				if (!next.isEmpty()) {
					for (int q = 0; q < numStates; q++) {
						if (next.get(q)) {
							// if p --a--> q, then p is in pre_a(q), q is in post_a(p)
							pre[a][q][preLen[a][q]++] = p;
							post[a][p][postLen[a][p]++] = q;
						}
					}
				}
			}
		}
//
		int[] todo = new int[numStates * numSymbols];
		int todoLen = 0;
//		
		int[][][] remove = new int[numSymbols][numStates][numStates];
		int[][] removeLen = new int[numSymbols][numStates];
		for(int a = 0; a < numSymbols; a++)
		{
			for(int p=0; p<numStates; p++)
				if(preLen[a][p]>0) // p is in a_S
				{	
					Sharpen_S_a:
					for(int q=0; q<numStates; q++)	// {all q} --> S_a 
					{
							if(postLen[a][q]>0)	/// q is in S_a 
							{	
								for(int r=0; r<postLen[a][q]; r++) 
									if(fsim[p][post[a][q][r]]) 	// q is in pre_a(sim(p))
										continue Sharpen_S_a;	// skip q						
								remove[a][p][removeLen[a][p]++] = q;
							}
					}
					if(removeLen[a][p]>0)
						todo[todoLen++] = a*numStates + p;
				}
		}
		int[] swap = new int[numStates];
		int swapLen = 0;
		boolean usingSwap = false;
		
		while(todoLen>0)
		{
			todoLen--;
			int v = todo[todoLen] % numStates;
			int a = todo[todoLen] / numStates;
			int len = (usingSwap? swapLen : removeLen[a][v]);
			removeLen[a][v] = 0;
			
			for(int j=0; j<preLen[a][v]; j++)
			{
				int u = pre[a][v][j];
				
				for(int i=0; i<len; i++)			
				{
					int w = (usingSwap? swap[i] : remove[a][v][i]);
					if(fsim[u][w]) 
					{
						fsim[u][w] = false;					
						for(int b=0; b<numSymbols; b++)
							if(preLen[b][u]>0)
							{
								Sharpen_pre_b_w:
								for(int k=0; k<preLen[b][w]; k++)
								{	
									int ww = pre[b][w][k];
									for(int r=0; r<postLen[b][ww]; r++) 
										if(fsim[u][post[b][ww][r]]) 	// ww is in pre_b(sim(u))
											continue Sharpen_pre_b_w;	// skip ww
									
									if(b==a && u==v && !usingSwap)
										swap[swapLen++] = ww;
									else{										
										if(removeLen[b][u]==0)
											todo[todoLen++] = b*numStates + u;
										remove[b][u][removeLen[b][u]++] = ww;
									}
									
								}
							}
					}//End of if(fsim[u][w])
				}				
			}			
			if(swapLen>0)
			{	
				if(!usingSwap)
				{	
					todo[todoLen++] = a*numStates + v;	
					usingSwap = true; 
				}else{
					swapLen = 0;
					usingSwap = false;
				}
			}
			
		}
//
//		Set<Pair<FAState,FAState>> FSim2 = new TreeSet<Pair<FAState,FAState>>(new StatePairComparator());
//		for(int p=0; p<numStates; p++)	
//			for(int q=0; q<numStates; q++)
//				if(fsim[p][q]) // q is in sim(p), q simulates p
//					System.out.println(map[q].getStateId() + ": " + q + " simulates " + map[p].getStateId() + ": " + p);
		return fsim;
	}

	
	// copy of the implementation in RABIT
	private static boolean[][] computeFastFwSimilation(NBA aut, boolean[][] fsim, StateContainer[] autPres) {
		//implement the HHK algorithm
//		int numStates = all_states.size();
//		int numSymbols = alphabet.size();
//		FAState[] states = all_states.toArray(new FAState[0]);
//		ArrayList<String> symbols=new ArrayList<String>(alphabet);
//		
//
		// fsim[u][v]=true iff v in fsim(u) iff v forward-simulates u
		int numSymbols = aut.getAlphabetSize();
		int numStates = aut.getStateSize();
		int[][][] pre = new int[numSymbols][numStates][];
		int[][][] post = new int[numSymbols][numStates][];
		int[][] preLen = new int[numSymbols][numStates];
		int[][] postLen = new int[numSymbols][numStates];
//
		// Initialize memory of pre/post
		for (int a = 0; a < numSymbols; a++) {
			for (int p = 0; p < numStates; p++) {
				ISet next = aut.getState(p).getSuccessors(a);
				postLen[a][p] = 0;
				if (!next.isEmpty())
					post[a][p] = new int[next.cardinality()];
				Set<StateNFA> prev = autPres[p].getPredecessors(a);
				preLen[a][p] = 0;
				if (!prev.isEmpty())
					pre[a][p] = new int[prev.size()];
			}
		}
//
		//state[post[s][q][r]] is in post_s(q) for 0<=r<adj_len[s][q]
		//state[pre[s][q][r]] is in pre_s(q) for 0<=r<adj_len[s][q]
		for (int a = 0; a < numSymbols; a++) {
			for (int p = 0; p < numStates; p++) {
				ISet next = aut.getState(p).getSuccessors(a);
				if (!next.isEmpty()) {
					for (int q = 0; q < numStates; q++) {
						if (next.get(q)) {
							// if p --a--> q, then p is in pre_a(q), q is in post_a(p)
							pre[a][q][preLen[a][q]++] = p;
							post[a][p][postLen[a][p]++] = q;
						}
					}
				}
			}
		}
//
		int[] todo = new int[numStates * numSymbols];
		int todoLen = 0;
//		
		int[][][] remove = new int[numSymbols][numStates][numStates];
		int[][] removeLen = new int[numSymbols][numStates];
		for(int a = 0; a < numSymbols; a++)
		{
			for(int p=0; p<numStates; p++)
				if(preLen[a][p]>0) // p is in a_S
				{	
					Sharpen_S_a:
					for(int q=0; q<numStates; q++)	// {all q} --> S_a 
					{
							if(postLen[a][q]>0)	/// q is in S_a 
							{	
								for(int r=0; r<postLen[a][q]; r++) 
									if(fsim[p][post[a][q][r]]) 	// q is in pre_a(sim(p))
										continue Sharpen_S_a;	// skip q						
								remove[a][p][removeLen[a][p]++] = q;
							}
					}
					if(removeLen[a][p]>0)
						todo[todoLen++] = a*numStates + p;
				}
		}
		int[] swap = new int[numStates];
		int swapLen = 0;
		boolean usingSwap = false;
		
		while(todoLen>0)
		{
			todoLen--;
			int v = todo[todoLen] % numStates;
			int a = todo[todoLen] / numStates;
			int len = (usingSwap? swapLen : removeLen[a][v]);
			removeLen[a][v] = 0;
			
			for(int j=0; j<preLen[a][v]; j++)
			{
				int u = pre[a][v][j];
				
				for(int i=0; i<len; i++)			
				{
					int w = (usingSwap? swap[i] : remove[a][v][i]);
					if(fsim[u][w]) 
					{
						fsim[u][w] = false;					
						for(int b=0; b<numSymbols; b++)
							if(preLen[b][u]>0)
							{
								Sharpen_pre_b_w:
								for(int k=0; k<preLen[b][w]; k++)
								{	
									int ww = pre[b][w][k];
									for(int r=0; r<postLen[b][ww]; r++) 
										if(fsim[u][post[b][ww][r]]) 	// ww is in pre_b(sim(u))
											continue Sharpen_pre_b_w;	// skip ww
									
									if(b==a && u==v && !usingSwap)
										swap[swapLen++] = ww;
									else{										
										if(removeLen[b][u]==0)
											todo[todoLen++] = b*numStates + u;
										remove[b][u][removeLen[b][u]++] = ww;
									}
									
								}
							}
					}//End of if(fsim[u][w])
				}				
			}			
			if(swapLen>0)
			{	
				if(!usingSwap)
				{	
					todo[todoLen++] = a*numStates + v;	
					usingSwap = true; 
				}else{
					swapLen = 0;
					usingSwap = false;
				}
			}
			
		}
//
//		Set<Pair<FAState,FAState>> FSim2 = new TreeSet<Pair<FAState,FAState>>(new StatePairComparator());
//		for(int p=0; p<numStates; p++)	
//			for(int q=0; q<numStates; q++)
//				if(fsim[p][q]) // q is in sim(p), q simulates p
//					System.out.println(q + " B-simulates " + p);
		return fsim;
	}
	
	
	
	public static void main(String []args) {
		
		Options options = new Options();
		PairParserBA pairParser = new PairParserBA(options, args[0], args[1]);
		NBA A = pairParser.getA();
		NBA B = pairParser.getB();
		System.out.println("#A = " + A.getStateSize() + ", #B = " + B.getStateSize());
		
		StateContainer[] bStates = new StateContainer[B.getStateSize()];
		// compute the predecessors and successors
		for(int i = 0; i < B.getStateSize(); i ++) {
			bStates[i] = new StateContainer(i, B);
		}
		// initialize the information for B
		for (int i = 0; i < B.getStateSize(); i++) {
			StateNFA st = bStates[i].getState();
			for (int letter = 0; letter < B.getAlphabetSize(); letter++) {
				for (int succ : st.getSuccessors(letter)) {
					//aStates[i].addSuccessors(letter, succ);
					bStates[succ].addPredecessors(letter, i);
				}
			}
		}
		
		// compute forward simulation of B
		//Simulation sim = new Simulation();
		boolean[][] bSims = Simulation.computeForwardSimilation(B, bStates);
		for(int f : B.getFinalStates()) {
			System.out.println("Final states: " + f);
		}
		
		StateContainer[] aStates = new StateContainer[A.getStateSize()];
		// compute the predecessors and successors
		for(int i = 0; i < A.getStateSize(); i ++) {
			aStates[i] = new StateContainer(i, A);
		}
		// initialize the information for B
		for (int i = 0; i < A.getStateSize(); i++) {
			StateNFA st = aStates[i].getState();
			for (int letter = 0; letter < A.getAlphabetSize(); letter++) {
				for (int succ : st.getSuccessors(letter)) {
					//aStates[i].addSuccessors(letter, succ);
					aStates[succ].addPredecessors(letter, i);
				}
			}
		}
		
		// compute forward simulation of B
		Simulation.computeForwardSimilation(A, aStates);
		for(int f : A.getFinalStates()) {
			System.out.println("Final states: " + f);
		}
		
		
        boolean[][] abSims = Simulation.computeForwardSimulation(A, B);
        for(int i = 0; i <  B.getStateSize(); i ++) {
        	for(int j = 0; j <  B.getStateSize(); j ++) {
        		if(abSims[i + A.getStateSize()][j+A.getStateSize()] != bSims[i][j]) {
        			System.out.println("Error : i=" + i + ", j=" + j + " bsim=" + bSims[i][j]);
        		}
        	}
        }
	}
}
