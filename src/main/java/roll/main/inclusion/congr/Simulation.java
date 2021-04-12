package roll.main.inclusion.congr;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import datastructure.Pair;
import roll.automata.NBA;
import roll.automata.StateNFA;
import roll.automata.operations.StateContainer;
import roll.main.Options;
import roll.parser.ba.PairParserBA;
import roll.util.Timer;
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
		// transition function of first automaton
		for(int i = 0; i < fst.getStateSize(); i ++) {
			StateNFA st = fst.getState(i);
			for (int letter = 0; letter < fst.getAlphabetSize(); letter++) {
				for (int succ : st.getSuccessors(letter)) {
					map[i].addSuccessor(letter, succ);
					map[succ].addPredecessor(letter, i);
				}
			}
		}
		// transition function of second automaton
		// every state has to be incremented by base
		for(int i = 0; i < snd.getStateSize(); i ++) {
			StateNFA st = snd.getState(i);
			for (int letter = 0; letter < snd.getAlphabetSize(); letter++) {
				for (int succ : st.getSuccessors(letter)) {
					map[i + base].addSuccessor(letter, succ);
					map[succ + base].addPredecessor(letter, i );
				}
			}
		}
		// forward simulation
		boolean[][] fsim = new boolean[numStates][numStates];
		
		if((long)numStates * (long)numStates * (long)fst.getAlphabetSize() <= (long)800000000) {
			computeFastFwSimilation(fst.getAlphabetSize(), map, fsim);
		}else {
			return computeBitSetFwSimilation(fst.getAlphabetSize(), map, fsim);
		}
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
		return computeFastHHKFwSimilation(aut, fsim, autPres);
	}
	
	private static boolean[][] computeBitSetFwSimilation(int numSymbols, StateMap[] map, boolean[][] fsim) {
		int numStates = map.length;
//		FAState[] states = all_states.toArray(new FAState[0]);

		// Reverse mapping of states to their index numbers
		// System.out.println("Construct reverse mapping");
				
		int[] W = new int[((numStates*numStates)>>5) + 1];
		// boolean[][] W2 = new boolean[n_states][n_states];
		
//		{
//		ArrayList<String> symbols=new ArrayList<String>(alphabet);
//
		boolean[] isFinal = new boolean[numStates];
		for (int i = 0; i < numStates; i++) {
			isFinal[i] = map[i].isFinal();
		}
//		
		int[][][] post = new int[numSymbols][numStates][];
		int[][] post_len = new int[numSymbols][numStates];

		// System.out.println("Construct post");
		for (int s = 0; s < numSymbols; s++) {
			for (int p = 0; p < numStates; p++) {
				post_len[s][p] = 0;
				ISet next = map[p].getSuccessors(s);
				if (!next.isEmpty()) {
					post[s][p] = new int[map[p].getSuccessors(s).cardinality()];
					/*
					 * for(int q=0; q<n_states; q++) { if(next.contains(states[q])) {
					 * post[s][p][post_len[s][p]++] = q; } }
					 */
					for (int q : next) {
						post[s][p][post_len[s][p]++] = q;
					}
				}
			}
		}
//
//		// System.out.println("Initialize result matrix");
//		// Initialize result. This will shrink by least fixpoint iteration.
//		
		for (int p = 0; p < numStates; p++)
			for (int q = 0; q < numStates; q++) {
				if (isFinal[p] && !isFinal[q]) {
					int k = p * numStates + q;
					int rem = k & 0x1F;
					int dev = k >> 5;
					W[dev] = W[dev] & (~(1 << rem));
					// W2[p][q]=false;
					continue;
				}
				int k = p * numStates + q;
				int rem = k & 0x1F;
				int dev = k >> 5;
				W[dev] = W[dev] | (1 << rem);
				// W2[p][q]=true;
				for (int s = 0; s < numSymbols; s++)
					if (post_len[s][p] > 0 && post_len[s][q] == 0) {
						// W2[p][q]=false;
						// p can do action s, but q cannot
						k = p * numStates + q;
						rem = k & 0x1F;
						dev = k >> 5;
						W[dev] = W[dev] & (~(1 << rem));
					}
			}
//
//		/*
//		for(int p=0; p<n_states; p++)
//		    for(int q=0; q<n_states; q++){
//			int k = p*n_states + q;
//			int rem = k & 0x1F;
//			int dev = k >> 5;
//			boolean flag = ((W[dev] & (1 << rem)) != 0);
//			if(flag != W2[p][q]) System.out.println("Diff at "+p+","+q+" flag="+flag+" W2="+W2[p][q]);
//		    }
//		*/
//			
//			
		Bitset_DirectSimRelNBW_refine(numStates, numSymbols, post, post_len, W);
//
//		}
//
//
//		/* W[p][q] means in relation. Now collect and return the result. */
//
//		Set<Pair<FAState,FAState>> FSim2 = new TreeSet<Pair<FAState,FAState>>(new StatePairComparator());
		for(int p=0; p<numStates; p++)	
		    for(int q=0; q<numStates; q++){
			// if(W[p][q]) FSim2.add(new Pair<FAState, FAState>(states[p],states[q]));
			int k = p*numStates + q;
			int rem = k & 0x1F;
			int dev = k >> 5;
			if((W[dev] & (1<<rem)) !=0) fsim[p][q] = true;
		    }
		return fsim;
	}
	
	private static void Bitset_DirectSimRelNBW_refine(int numStates, int numSymbols, int[][][] post, int[][] post_len,
			int[] W) {
		boolean changed=true;
		while(changed){
		    // System.out.println("Bitset sim refize: States: "+n_states+" Matrix: "+count_bitset(W, n_states));
		    changed = (Bitset_single_DirectSimRelNBW_refine(0,numStates,0,numStates,numStates,numSymbols,post,post_len, W) >0);
		}
	}

	private static int Bitset_single_DirectSimRelNBW_refine(int p1, int p2, int q1, int q2, int n_states, int n_symbols, int[][][] post, int[][] post_len, int[] W)
	     {
		 boolean changed=false;
			    for(int p=p1; p<p2; p++)	
				for(int q=q1; q<q2; q++){
				    // if(W[p][q]){  
				    int k = p*n_states + q;
				    int rem = k & 0x1F;
				    int dev = k >> 5;
				    if((W[dev] & (1<<rem)) !=0){
					if(Bitset_DirectSimRelNBW_Fail(p, q, n_states, n_symbols, post, post_len, W)) {
					// W[p][q]=false;
					W[dev] = W[dev] & (~(1 << rem));
					changed=true;
				    }
				    }
				}
			    if(changed) return(1); else return(0);
	     }
	    
	    private static boolean Bitset_DirectSimRelNBW_Fail(int p, int q, int n_states, int n_symbols, int[][][] post, int[][] post_len, int[] X)
        {
	    boolean trapped=false;
	    for(int a=0; a<n_symbols; a++)
		if(post_len[a][p]>0){
		    for(int r=0; r<post_len[a][p]; r++){ 
			trapped=true;
			if(post_len[a][q]>0) for(int t=0; t<post_len[a][q]; t++){
						 // if(X[post[a][p][r]][post[a][q][t]]) { trapped=false; break; }
				int k = (post[a][p][r] * n_states) + post[a][q][t];
				int rem = k & 0x1F;
				int dev = k >> 5;
				if((X[dev] & (1 << rem)) != 0) { trapped=false; break; }
			    }
			if(trapped) return true;
		    }
		}
	    return false;
	}

	private static boolean[][] computeFastFwSimilation(int numSymbols, StateMap[] map, boolean[][] fsim) {
		//implement the HHK algorithm
//		int numStates = all_states.size();
//		int numSymbols = alphabet.size();
//		FAState[] states = all_states.toArray(new FAState[0]);
//		ArrayList<String> symbols=new ArrayList<String>(alphabet);

		// fsim[u][v]=true iff v in fsim(u) iff v forward-simulates u
		int numStates = map.length;
		
		boolean[] isFinal = new boolean[numStates];
		boolean[] isInit = new boolean[numStates];
		// forward simulation
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
//		System.out.println("Size : " + ((long)numSymbols * (long)numStates * (long)numStates));
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
	private static boolean[][] computeFastHHKFwSimilation(NBA aut, boolean[][] fsim, StateContainer[] autPres) {
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
		
		String path = args[0];
		File file = new File(path);
		List<Pair<File, File>> lists = new LinkedList<>();
		for(File fsA : file.listFiles() ) {
			if(fsA.isDirectory() ) continue;
			String fileNameA = fsA.getName();
			if(!fileNameA.endsWith("_A.ba")) continue;
			int indexA = fileNameA.indexOf("_A.ba");
			String prefA = fileNameA.substring(0, indexA);
			if(fileNameA.endsWith("_A.ba")) {
				for(File fsB : file.listFiles() ) {
					if(fsA.isDirectory()) continue;
					String fileNameB = fsB.getName();
					if(!fileNameB.endsWith("_B.ba")) continue;
					if(fileNameB.equals(prefA + "_B.ba")) {
						lists.add(new Pair<>(fsA, fsB));
						System.out.println("Added a pair: " + fsA.getName() + ", " + fsB.getName());
						break;
					}else continue;
				}
			}else {
				continue;
			}
		}
		long ts1 = 0;
		long ts2 = 0;
		long ts3 = 0;
		for(Pair<File, File> pair : lists) {
			Options options = new Options();
			PairParserBA pairParser = new PairParserBA(options, pair.getLeft().getAbsolutePath(), pair.getRight().getAbsolutePath());
			NBA A = pairParser.getA();
			NBA B = pairParser.getB();
			System.out.println("#A = " + A.getStateSize() + ", #B = " + B.getStateSize());
			System.out.println("Process A = " + pair.getLeft().getName() + " B = " + pair.getRight().getName());
			
			int numStates = A.getStateSize() + B.getStateSize();
//			if(pair.getLeft().getName().contains("email_spec11_product40_true-unreach-call_true-termination.cil.c_Iteration3_A")
//					|| pair.getLeft().getName().contains("s3_clnt_2_false-unreach-call_true-termination.cil.c_Iteration28_A")
//					|| pair.getLeft().getName().contains("s3_clnt_2_true-unreach-call_true-termination.cil.c_Iteration27")
//					|| pair.getLeft().getName().contains("s3_clnt_3.cil_true-unreach-call_true-termination.c_Iteration27")
//					|| pair.getLeft().getName().contains("s3_srvr_1a_true-unreach-call_false-termination.cil.c_Iteration9")
//					|| pair.getLeft().getName().contains("bist_cell_true-unreach-call_false-termination.cil.c_Iteration26")
//					|| pair.getLeft().getName().contains("bist_cell_true-unreach-call_false-termination.cil.c_Iteration27")
//					|| pair.getLeft().getName().contains("email_spec11_product40_true-unreach-call_true-termination.cil.c_Iteration4")
//					|| pair.getLeft().getName().contains("elevator_spec1_product27_true-unreach-call_true-termination.cil.c_Iteration3")) {
//				continue;
//			}
			StateMap[] map = new StateMap[numStates];
			for(int i = 0; i < A.getStateSize(); i ++) {
				map[i] = new StateMap(i, 0, A);
			}
			int base = A.getStateSize();
			for(int i = base; i < numStates; i ++) {
				map[i] = new StateMap(i - base, base, B);
			}
			// transition function of first automaton
			for(int i = 0; i < A.getStateSize(); i ++) {
				StateNFA st = A.getState(i);
				for (int letter = 0; letter < A.getAlphabetSize(); letter++) {
					for (int succ : st.getSuccessors(letter)) {
						map[i].addSuccessor(letter, succ);
						map[succ].addPredecessor(letter, i);
					}
				}
			}
			// transition function of second automaton
			// every state has to be incremented by base
			for(int i = 0; i < B.getStateSize(); i ++) {
				StateNFA st = B.getState(i);
				for (int letter = 0; letter < B.getAlphabetSize(); letter++) {
					for (int succ : st.getSuccessors(letter)) {
						map[i + base].addSuccessor(letter, succ);
						map[succ + base].addPredecessor(letter, i );
					}
				}
			}
			// forward simulation
			Timer timer1 = new Timer();
			Timer timer2 = new Timer();
			timer1.start();
			
			boolean[][] fsim1 = new boolean[numStates][numStates];
			//computeFastFwSimilation(A.getAlphabetSize(), map, fsim1);
			timer1.stop();
			ts1 += timer1.getTimeElapsed();
			timer2.start();
			boolean[][] fsim2 = new boolean[numStates][numStates];
//			computeBitSetFwSimilation(A.getAlphabetSize(), map, fsim2);
			timer2.stop();
			ts2 += timer2.getTimeElapsed();
			fsim1 = fsim2;
			for(int i = 0; i < numStates; i ++) {
				for (int j = 0; j < numStates; j++) {
					if(fsim1[i][j] != fsim2[i][j]) {
						System.out.println("Error " + pair.getLeft().getAbsolutePath() );
						System.out.println("Error " + pair.getRight().getAbsolutePath() );
						System.out.println("Error (i,j) = " + i + ", " + j);
						System.out.println("Error hhk = " + fsim1[i][j]);
						System.out.println("Error bit = " + fsim2[i][j]);
						System.exit(-1);
					}
				}
			}
			System.out.println("OOK " );
			
			numStates = 2 * B.getStateSize();
			map = new StateMap[numStates];
			StateContainer[] bStates = new StateContainer[B.getStateSize()];

			for(int i = 0; i < B.getStateSize(); i ++) {
				map[i] = new StateMap(i, 0, B);
				bStates[i] = new StateContainer(i, B);
			}
			base = B.getStateSize();
			for(int i = base; i < numStates; i ++) {
				map[i] = new StateMap(i - base, base, B);
			}
			// transition function of first automaton
			for(int i = 0; i < B.getStateSize(); i ++) {
				StateNFA st = B.getState(i);
				for (int letter = 0; letter < B.getAlphabetSize(); letter++) {
					for (int succ : st.getSuccessors(letter)) {
						map[i].addSuccessor(letter, succ);
						map[succ].addPredecessor(letter, i);
						bStates[succ].addPredecessors(letter, i);
					}
				}
			}
			// transition function of second automaton
			// every state has to be incremented by base
			for(int i = 0; i < B.getStateSize(); i ++) {
				StateNFA st = B.getState(i);
				for (int letter = 0; letter < B.getAlphabetSize(); letter++) {
					for (int succ : st.getSuccessors(letter)) {
						map[i + base].addSuccessor(letter, succ);
						map[succ + base].addPredecessor(letter, i );
					}
				}
			}
			// forward simulation
			timer1 = new Timer();
		    timer2 = new Timer();
			timer1.start();
			
			fsim1 = new boolean[numStates][numStates];
			computeFastFwSimilation(B.getAlphabetSize(), map, fsim1);
			timer1.stop();
			ts1 += timer1.getTimeElapsed();
			timer2.start();
			fsim2 = new boolean[numStates][numStates];
			computeBitSetFwSimilation(B.getAlphabetSize(), map, fsim2);
			timer2.stop();
			ts2 += timer2.getTimeElapsed();
			timer2.start();
			boolean[][] fsim3 = computeForwardSimilation(B, bStates);
			timer2.stop();
			ts3 += timer2.getTimeElapsed();
			for(int i = 0; i < numStates; i ++) {
				for (int j = 0; j < numStates; j++) {
					if(fsim1[i][j] != fsim2[i][j]) {
						System.out.println("Error " + pair.getLeft().getAbsolutePath() );
						System.out.println("Error " + pair.getRight().getAbsolutePath() );
						System.out.println("Error (i,j) = " + i + ", " + j);
						System.out.println("Error hhk = " + fsim1[i][j]);
						System.out.println("Error bit = " + fsim2[i][j]);
						System.exit(-1);
					}
					if(i < B.getStateSize() && j >= B.getStateSize()
							&& fsim1[i][j] != fsim3[i][j - B.getStateSize()]) {
						System.out.println("Error " + pair.getLeft().getAbsolutePath() );
						System.out.println("Error " + pair.getRight().getAbsolutePath() );
						System.out.println("Error (i,j) = " + i + ", " + j);
						System.out.println("Error hhk = " + fsim1[i][j]);
						System.out.println("Error bit = " + fsim3[i][j - B.getStateSize()]);
						System.exit(-1);
					}
				}
			}
			System.out.println("OOK " );
			

		}
		System.out.println("HHK " + ts1 );
		System.out.println("Bit " + ts2 );
		System.out.println("SIN " + ts3 );
		
		
		
//		StateContainer[] bStates = new StateContainer[B.getStateSize()];
//		// compute the predecessors and successors
//		for(int i = 0; i < B.getStateSize(); i ++) {
//			bStates[i] = new StateContainer(i, B);
//		}
//		// initialize the information for B
//		for (int i = 0; i < B.getStateSize(); i++) {
//			StateNFA st = bStates[i].getState();
//			for (int letter = 0; letter < B.getAlphabetSize(); letter++) {
//				for (int succ : st.getSuccessors(letter)) {
//					//aStates[i].addSuccessors(letter, succ);
//					bStates[succ].addPredecessors(letter, i);
//				}
//			}
//		}
//		
//		// compute forward simulation of B
//		//Simulation sim = new Simulation();
//		boolean[][] bSims = Simulation.computeForwardSimilation(B, bStates);
//		for(int f : B.getFinalStates()) {
//			System.out.println("Final states: " + f);
//		}
//		
//		StateContainer[] aStates = new StateContainer[A.getStateSize()];
//		// compute the predecessors and successors
//		for(int i = 0; i < A.getStateSize(); i ++) {
//			aStates[i] = new StateContainer(i, A);
//		}
//		// initialize the information for B
//		for (int i = 0; i < A.getStateSize(); i++) {
//			StateNFA st = aStates[i].getState();
//			for (int letter = 0; letter < A.getAlphabetSize(); letter++) {
//				for (int succ : st.getSuccessors(letter)) {
//					//aStates[i].addSuccessors(letter, succ);
//					aStates[succ].addPredecessors(letter, i);
//				}
//			}
//		}
//		
//		// compute forward simulation of B
//		Simulation.computeForwardSimilation(A, aStates);
//		for(int f : A.getFinalStates()) {
//			System.out.println("Final states: " + f);
//		}
//		
//		
//        boolean[][] abSims = Simulation.computeForwardSimulation(A, B);
//        for(int i = 0; i <  B.getStateSize(); i ++) {
//        	for(int j = 0; j <  B.getStateSize(); j ++) {
//        		if(abSims[i + A.getStateSize()][j+A.getStateSize()] != bSims[i][j]) {
//        			System.out.println("Error : i=" + i + ", j=" + j + " bsim=" + bSims[i][j]);
//        		}
//        	}
//        }
//        
//        boolean[][] ddSims = Simulation.computeForwardSimulation(B, B);
//        for(int i = 0; i <  B.getStateSize(); i ++) {
//        	for(int j = 0; j <  B.getStateSize(); j ++) {
//        		if(ddSims[i][j+B.getStateSize()]) {
//        			System.out.println("i=" + i + ", j=" + j + " bsim= " + ddSims[i][j+B.getStateSize()] );
//        		}
//        	}
//        }
        

	}
	




}
