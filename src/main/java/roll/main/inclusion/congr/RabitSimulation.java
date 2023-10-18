package roll.main.inclusion.congr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import automata.FAState;
import automata.FiniteAutomaton;
import comparator.StatePairComparator;
import datastructure.HashSet;
import datastructure.Pair;

/**
 * 
 * @author Richard Mayr, Yu-Fang Chen and Chih-Duo Hong
 * 
 *         This is a copy of code of Simulation.java in RABIT
 *         http://www.languageinclusion.org/doku.php
 * 
 */
public class RabitSimulation {

	public Set<Pair<FAState, FAState>> ForwardSimRelNBW(FiniteAutomaton omega1, FiniteAutomaton omega2) {
		// If small enough <= 4GB then call HHK sim, else more memory efficient bitset
		// naive
		int n_states = omega1.states.size();
		if (omega2 != null)
			n_states = n_states + omega2.states.size();
		HashSet<String> alphabet = new HashSet<String>();
		alphabet.addAll(omega1.alphabet);
		if (omega2 != null)
			alphabet.addAll(omega2.alphabet);
		int n_symbols = alphabet.size();
		if ((long) n_states * (long) n_states * (long) n_symbols <= (long) 800000000)
			return (HHK_ForwardSimRelNBW(omega1, omega2));
		else
			return (Bitset_Naive_DirectSimRelNBW(omega1, omega2));
	}

	public Set<Pair<FAState, FAState>> HHK_ForwardSimRelNBW(FiniteAutomaton omega1, FiniteAutomaton omega2) {
		ArrayList<FAState> all_states = new ArrayList<FAState>();
		HashSet<String> alphabet = new HashSet<String>();

		all_states.addAll(omega1.states);
		alphabet.addAll(omega1.alphabet);

		if (omega2 != null) {
			all_states.addAll(omega2.states);
			alphabet.addAll(omega2.alphabet);
		}

		FAState[] states = all_states.toArray(new FAState[0]);

		boolean[] isFinal = new boolean[states.length];
		boolean[] isInit = new boolean[states.length];
		boolean[][] fsim = new boolean[states.length][states.length];
		for (int i = 0; i < states.length; i++) {
			isFinal[i] = states[i].getowner().F.contains(states[i]);
			isInit[i] = states[i].getowner().getInitialState().compareTo(states[i]) == 0;
		}
		for (int i = 0; i < states.length; i++) {
			for (int j = i; j < states.length; j++) {
				fsim[i][j] = (!isFinal[i] || isFinal[j]) && states[j].fw_covers(states[i]);
				fsim[j][i] = (isFinal[i] || !isFinal[j]) && states[i].fw_covers(states[j]);
			}
		}
		return FastFSimRelNBW(omega1, omega2, fsim);
	}

	public Set<Pair<FAState, FAState>> FastFSimRelNBW(FiniteAutomaton omega1, FiniteAutomaton omega2,
			boolean[][] fsim) {

		ArrayList<FAState> all_states = new ArrayList<FAState>();
		HashSet<String> alphabet = new HashSet<String>();

		all_states.addAll(omega1.states);
		alphabet.addAll(omega1.alphabet);

		if (omega2 != null) {
			all_states.addAll(omega2.states);
			alphabet.addAll(omega2.alphabet);
		}
		// implement the HHK algorithm
		int n_states = all_states.size();
		int n_symbols = alphabet.size();
		FAState[] states = all_states.toArray(new FAState[0]);
		ArrayList<String> symbols = new ArrayList<String>(alphabet);

		// fsim[u][v]=true iff v in fsim(u) iff v forward-simulates u

		int[][][] pre = new int[n_symbols][n_states][];
		int[][][] post = new int[n_symbols][n_states][];
		int[][] pre_len = new int[n_symbols][n_states];
		int[][] post_len = new int[n_symbols][n_states];

		// Initialize memory of pre/post
		for (int s = 0; s < n_symbols; s++) {
			String a = symbols.get(s);
			for (int p = 0; p < n_states; p++) {
				Set<FAState> next = states[p].getNext(a);
				post_len[s][p] = 0;
				if (next != null)
					post[s][p] = new int[states[p].getNext(a).size()];
				Set<FAState> prev = states[p].getPre(a);
				pre_len[s][p] = 0;
				if (prev != null)
					pre[s][p] = new int[states[p].getPre(a).size()];
			}
		}

		// state[post[s][q][r]] is in post_s(q) for 0<=r<adj_len[s][q]
		// state[pre[s][q][r]] is in pre_s(q) for 0<=r<adj_len[s][q]
		for (int s = 0; s < n_symbols; s++) {
			String a = symbols.get(s);
			for (int p = 0; p < n_states; p++) {
				Set<FAState> next = states[p].getNext(a);
				if (next != null) {
					for (int q = 0; q < n_states; q++) {
						if (next.contains(states[q])) {
							// if p --a--> q, then p is in pre_a(q), q is in post_a(p)
							pre[s][q][pre_len[s][q]++] = p;
							post[s][p][post_len[s][p]++] = q;
						}
					}
				}
			}
		}

		int[] todo = new int[n_states * n_symbols];
		int todo_len = 0;

		int[][][] remove = new int[n_symbols][n_states][n_states];
		int[][] remove_len = new int[n_symbols][n_states];
		for (int a = 0; a < n_symbols; a++) {
			for (int p = 0; p < n_states; p++)
				if (pre_len[a][p] > 0) // p is in a_S
				{
					Sharpen_S_a: for (int q = 0; q < n_states; q++) // {all q} --> S_a
					{
						if (post_len[a][q] > 0) /// q is in S_a
						{
							for (int r = 0; r < post_len[a][q]; r++)
								if (fsim[p][post[a][q][r]]) // q is in pre_a(sim(p))
									continue Sharpen_S_a; // skip q
							remove[a][p][remove_len[a][p]++] = q;
						}
					}
					if (remove_len[a][p] > 0)
						todo[todo_len++] = a * n_states + p;
				}
		}
		int[] swap = new int[n_states];
		int swap_len = 0;
		boolean using_swap = false;

		while (todo_len > 0) {
			todo_len--;
			int v = todo[todo_len] % n_states;
			int a = todo[todo_len] / n_states;
			int len = (using_swap ? swap_len : remove_len[a][v]);
			remove_len[a][v] = 0;

			for (int j = 0; j < pre_len[a][v]; j++) {
				int u = pre[a][v][j];

				for (int i = 0; i < len; i++) {
					int w = (using_swap ? swap[i] : remove[a][v][i]);
					if (fsim[u][w]) {
						fsim[u][w] = false;
						for (int b = 0; b < n_symbols; b++)
							if (pre_len[b][u] > 0) {
								Sharpen_pre_b_w: for (int k = 0; k < pre_len[b][w]; k++) {
									int ww = pre[b][w][k];
									for (int r = 0; r < post_len[b][ww]; r++)
										if (fsim[u][post[b][ww][r]]) // ww is in pre_b(sim(u))
											continue Sharpen_pre_b_w; // skip ww

									if (b == a && u == v && !using_swap)
										swap[swap_len++] = ww;
									else {
										if (remove_len[b][u] == 0)
											todo[todo_len++] = b * n_states + u;
										remove[b][u][remove_len[b][u]++] = ww;
									}

								}
							}
					} // End of if(fsim[u][w])
				}
			}
			if (swap_len > 0) {
				if (!using_swap) {
					todo[todo_len++] = a * n_states + v;
					using_swap = true;
				} else {
					swap_len = 0;
					using_swap = false;
				}
			}

		}

		Set<Pair<FAState, FAState>> FSim2 = new TreeSet<Pair<FAState, FAState>>(new StatePairComparator());
		for (int p = 0; p < n_states; p++)
			for (int q = 0; q < n_states; q++)
				if (fsim[p][q]) // q is in sim(p), q simulates p
					FSim2.add(new Pair<FAState, FAState>(states[p], states[q]));
		return FSim2;

	}

	// -------------------------------------------------- Bitset Naive version of fw
	// and bw simulation

	/**
	 * Compute direct simulation relation on a Buchi automaton, by naive fixpoint
	 * iteration
	 * 
	 * @param omega1: a Buchi automaton
	 *
	 * @return direct simulation
	 */

	public Set<Pair<FAState, FAState>> Bitset_Naive_DirectSimRelNBW(FiniteAutomaton omega1, FiniteAutomaton omega2) {
		ArrayList<FAState> all_states = new ArrayList<FAState>();
		HashSet<String> alphabet = new HashSet<String>();

		all_states.addAll(omega1.states);
		alphabet.addAll(omega1.alphabet);

		if (omega2 != null) {
			all_states.addAll(omega2.states);
			alphabet.addAll(omega2.alphabet);
		}

		int n_states = all_states.size();
		int n_symbols = alphabet.size();

		FAState[] states = all_states.toArray(new FAState[0]);

		// Reverse mapping of states to their index numbers
		// System.out.println("Construct reverse mapping");
		TreeMap<FAState, Integer> rev_map = new TreeMap<FAState, Integer>();
		for (int i = 0; i < n_states; i++)
			rev_map.put(states[i], i);

		int[] W = new int[((n_states * n_states) >> 5) + 1];
		// boolean[][] W2 = new boolean[n_states][n_states];

		{
			ArrayList<String> symbols = new ArrayList<String>(alphabet);

			boolean[] isFinal = new boolean[n_states];
			for (int i = 0; i < n_states; i++) {
				isFinal[i] = states[i].getowner().F.contains(states[i]);
			}

			int[][][] post = new int[n_symbols][n_states][];
			int[][] post_len = new int[n_symbols][n_states];

			// System.out.println("Construct post");
			for (int s = 0; s < n_symbols; s++) {
				String a = symbols.get(s);
				for (int p = 0; p < n_states; p++) {
					post_len[s][p] = 0;
					Set<FAState> next = states[p].getNext(a);
					if (next != null) {
						post[s][p] = new int[states[p].getNext(a).size()];
						/*
						 * for(int q=0; q<n_states; q++) { if(next.contains(states[q])) {
						 * post[s][p][post_len[s][p]++] = q; } }
						 */
						Iterator<FAState> state_it = next.iterator();
						while (state_it.hasNext()) {
							FAState state = state_it.next();
							post[s][p][post_len[s][p]++] = rev_map.get(state);
						}

					}
				}
			}

			// System.out.println("Initialize result matrix");
			// Initialize result. This will shrink by least fixpoint iteration.

			for (int p = 0; p < n_states; p++)
				for (int q = 0; q < n_states; q++) {
					if (isFinal[p] && !isFinal[q]) {
						int k = p * n_states + q;
						int rem = k & 0x1F;
						int dev = k >> 5;
						W[dev] = W[dev] & (~(1 << rem));
						// W2[p][q]=false;
						continue;
					}
					int k = p * n_states + q;
					int rem = k & 0x1F;
					int dev = k >> 5;
					W[dev] = W[dev] | (1 << rem);
					// W2[p][q]=true;
					for (int s = 0; s < n_symbols; s++)
						if (post_len[s][p] > 0 && post_len[s][q] == 0) {
							// W2[p][q]=false;
							// p can do action s, but q cannot
							k = p * n_states + q;
							rem = k & 0x1F;
							dev = k >> 5;
							W[dev] = W[dev] & (~(1 << rem));
						}
				}

			/*
			 * for(int p=0; p<n_states; p++) for(int q=0; q<n_states; q++){ int k =
			 * p*n_states + q; int rem = k & 0x1F; int dev = k >> 5; boolean flag = ((W[dev]
			 * & (1 << rem)) != 0); if(flag != W2[p][q])
			 * System.out.println("Diff at "+p+","+q+" flag="+flag+" W2="+W2[p][q]); }
			 */

			Bitset_DirectSimRelNBW_refine(n_states, n_symbols, post, post_len, W);

		}

		/* W[p][q] means in relation. Now collect and return the result. */

		Set<Pair<FAState, FAState>> FSim2 = new TreeSet<Pair<FAState, FAState>>(new StatePairComparator());
		for (int p = 0; p < n_states; p++)
			for (int q = 0; q < n_states; q++) {
				// if(W[p][q]) FSim2.add(new Pair<FAState, FAState>(states[p],states[q]));
				int k = p * n_states + q;
				int rem = k & 0x1F;
				int dev = k >> 5;
				if ((W[dev] & (1 << rem)) != 0)
					FSim2.add(new Pair<FAState, FAState>(states[p], states[q]));
			}
		return FSim2;
	}

	private void Bitset_DirectSimRelNBW_refine(int n_states, int n_symbols, int[][][] post, int[][] post_len, int[] W) {
		boolean changed = true;
		while (changed) {
			// System.out.println("Bitset sim refize: States: "+n_states+" Matrix:
			// "+count_bitset(W, n_states));
			changed = (Bitset_single_DirectSimRelNBW_refine(0, n_states, 0, n_states, n_states, n_symbols, post,
					post_len, W) > 0);
		}
	}

	private int Bitset_single_DirectSimRelNBW_refine(int p1, int p2, int q1, int q2, int n_states, int n_symbols,
			int[][][] post, int[][] post_len, int[] W) {
		boolean changed = false;
		for (int p = p1; p < p2; p++)
			for (int q = q1; q < q2; q++) {
				// if(W[p][q]){
				int k = p * n_states + q;
				int rem = k & 0x1F;
				int dev = k >> 5;
				if ((W[dev] & (1 << rem)) != 0) {
					if (Bitset_DirectSimRelNBW_Fail(p, q, n_states, n_symbols, post, post_len, W)) {
						// W[p][q]=false;
						W[dev] = W[dev] & (~(1 << rem));
						changed = true;
					}
				}
			}
		if (changed)
			return (1);
		else
			return (0);
	}

	private boolean Bitset_DirectSimRelNBW_Fail(int p, int q, int n_states, int n_symbols, int[][][] post,
			int[][] post_len, int[] X) {
		boolean trapped = false;
		for (int a = 0; a < n_symbols; a++)
			if (post_len[a][p] > 0) {
				for (int r = 0; r < post_len[a][p]; r++) {
					trapped = true;
					if (post_len[a][q] > 0)
						for (int t = 0; t < post_len[a][q]; t++) {
							// if(X[post[a][p][r]][post[a][q][t]]) { trapped=false; break; }
							int k = (post[a][p][r] * n_states) + post[a][q][t];
							int rem = k & 0x1F;
							int dev = k >> 5;
							if ((X[dev] & (1 << rem)) != 0) {
								trapped = false;
								break;
							}
						}
					if (trapped)
						return true;
				}
			}
		return false;
	}

	// ------------------------------------------- Improved Delayed Sim
	// -----------------------------

	/**
	 * Performance improved version of delayed simulation. Compute delayed (forward)
	 * simulation relation on/between two Buchi automata
	 * 
	 * @param omega1, omega2: two Buchi automata
	 *
	 * @return maximal delayed simulation relation
	 */

	public Set<Pair<FAState, FAState>> DelayedSimRelNBW(FiniteAutomaton omega1, FiniteAutomaton omega2) {
		ArrayList<FAState> all_states = new ArrayList<FAState>();
		HashSet<String> alphabet = new HashSet<String>();

		all_states.addAll(omega1.states);
		alphabet.addAll(omega1.alphabet);

		if (omega2 != null) {
			all_states.addAll(omega2.states);
			alphabet.addAll(omega2.alphabet);
		}

		int n_states = all_states.size();
		int n_symbols = alphabet.size();

		boolean[][] W = new boolean[n_states][n_states];

		FAState[] states = all_states.toArray(new FAState[0]);
		{
			ArrayList<String> symbols = new ArrayList<String>(alphabet);

			boolean[] isFinal = new boolean[n_states];
			for (int i = 0; i < n_states; i++) {
				isFinal[i] = states[i].getowner().F.contains(states[i]);
			}

			int[][][] post = new int[n_symbols][n_states][];
			int[][] post_len = new int[n_symbols][n_states];

			TreeMap<FAState, Integer> rev_map = new TreeMap<FAState, Integer>();
			for (int i = 0; i < n_states; i++)
				rev_map.put(states[i], i);
			for (int s = 0; s < n_symbols; s++) {
				String a = symbols.get(s);
				for (int p = 0; p < n_states; p++) {
					post_len[s][p] = 0;
					Set<FAState> next = states[p].getNext(a);
					if (next != null) {
						// store the successor states for symbol s and state p
						post[s][p] = new int[states[p].getNext(a).size()];
						/*
						 * for(int q=0; q<n_states; q++) { if(next.contains(states[q])) {
						 * post[s][p][post_len[s][p]++] = q; } }
						 */
						Iterator<FAState> state_it = next.iterator();
						while (state_it.hasNext()) {
							FAState state = state_it.next();
							post[s][p][post_len[s][p]++] = rev_map.get(state);
						}
					}
				}
			}
			// post[s][p] store the successors for (p, s)

			// Initialize result W (winning for spolier). This will grow by least fixpoint
			// iteration.
			// p better than q
			for (int p = 0; p < n_states; p++)
				for (int q = 0; q < n_states; q++) {
					W[p][q] = false;
					for (int s = 0; s < n_symbols; s++)
						if (post_len[s][p] > 0 && post_len[s][q] == 0)
							W[p][q] = true; // p can do action s, but q cannot
				}

			boolean[][] avoid = new boolean[n_states][n_states];

			boolean changed = true;
			while (changed) {
				changed = false;
				// avoid final ?
				get_avoid(avoid, isFinal, n_states, n_symbols, post, post_len, W);
				changed = get_W(avoid, isFinal, n_states, n_symbols, post, post_len, W);
			}
		}
		// Create final result as set of pairs of states
		Set<Pair<FAState, FAState>> FSim2 = new TreeSet<Pair<FAState, FAState>>(new StatePairComparator());
		for (int p = 0; p < n_states; p++)
			for (int q = 0; q < n_states; q++)
				if (!W[p][q]) // W is winning for spoiler here, so the result is the opposite.
					FSim2.add(new Pair<FAState, FAState>(states[p], states[q]));
		return FSim2;
	}

	private void get_avoid(boolean[][] avoid, boolean[] isFinal, int n_states, int n_symbols, int[][][] post,
			int[][] post_len, boolean[][] W) {
		// System.out.println("Computing getavoid.");
		for (int p = 0; p < n_states; p++)
			for (int q = 0; q < n_states; q++)
				// avoid p or q is not accepting
				// q cannot be better than p?
				avoid[p][q] = (W[p][q] || !isFinal[q]);

		int sincechanged = 0;
		while (true) {
			for (int p = 0; p < n_states; p++)
				for (int q = 0; q < n_states; q++) {
					++sincechanged;
					// p is not winning and p can avoid q?
					if (!W[p][q] && avoid[p][q]) {
						if (!CPre(p, q, n_symbols, post, post_len, avoid)) {
							avoid[p][q] = false;
							sincechanged = 0;
						}
					}
					if (sincechanged >= n_states * n_states)
						return;
				}
		}
	}
	// judge here, whether p simulates q
	private boolean CPre(int p, int q, int n_symbols, int[][][] post, int[][] post_len, boolean[][] X) {
		boolean trapped = false;
		for (int a = 0; a < n_symbols; a++)
			if (post_len[a][p] > 0) {
				for (int r = 0; r < post_len[a][p]; r++) {
					trapped = true;
					// q also has a
					if (post_len[a][q] > 0)
						for (int t = 0; t < post_len[a][q]; t++)
							// there exists a successor of p, the successor of q is winning
							if (!X[post[a][p][r]][post[a][q][t]]) {
								trapped = false;
								break;
							}
				    // q does not have a or no successor can win the successor
					if (trapped)
						return true;
				}
			}

		return false;
	}

	private boolean get_W(boolean[][] avoid, boolean[] isFinal, int n_states, int n_symbols, int[][][] post,
			int[][] post_len, boolean[][] W) {
		boolean changed = false;
		for (int p = 0; p < n_states; p++)
			for (int q = 0; q < n_states; q++) {
				if (W[p][q])
					continue;
				if (isFinal[p] && avoid[p][q]) {
					W[p][q] = true;
					changed = true;
				}
			}
		int sincechanged = 0;
		while (true) {
			for (int p = 0; p < n_states; p++)
				for (int q = 0; q < n_states; q++) {
					++sincechanged;
					if (!W[p][q]) {
						if (CPre(p, q, n_symbols, post, post_len, W)) {
							W[p][q] = true;
							changed = true;
							sincechanged = 0;
						}
					}
				}
			if (sincechanged >= n_states * n_states)
				return (changed);
		}
	}

}
