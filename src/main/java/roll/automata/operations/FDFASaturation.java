package roll.automata.operations;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import roll.automata.DFA;
import roll.automata.FDFA;
import roll.util.Pair;
import roll.words.Word;
import roll.automata.operations.FDFAOperations.SaturationWitness;

/**
 * Polynomial saturation test; see FDFAOperations.checkSaturation.
 * Based on https://doi.org/10.4230/LIPIcs.ICALP.2025.146, Lemmas 2-4.
 */
final class FDFASaturation {
    private FDFASaturation() { }

    static SaturationWitness check(FDFA fdfa) {
        DFA leading = fdfa.getLeadingFA();
        validate(leading);
        for (int q = 0; q < leading.getStateSize(); q++) {
            if (fdfa.getProgressFA(q).getAlphabet() != fdfa.getAlphabet())
                throw new IllegalArgumentException("FDFA alphabets must agree");
            validate(fdfa.getProgressFA(q));
        }
        Word empty = fdfa.getAlphabet().getEmptyWord();
        Word[] access = new Word[leading.getStateSize()];
        Queue<Integer> queue = new ArrayDeque<>();
        access[leading.getInitialState()] = empty;
        queue.add(leading.getInitialState());
        while (!queue.isEmpty()) {
            int q = queue.remove();
            for (int a = 0; a < leading.getAlphabetSize(); a++) {
                int next = leading.getSuccessor(q, a);
                if (access[next] == null) {
                    access[next] = access[q].append(a);
                    queue.add(next);
                }
            }
        }
        // Lemma 3: compare (u, ax) and (ua, xa). Track both leading
        // endpoints as well as both progress runs; x is allowed to be empty.
        for (int q = 0; q < access.length; q++) {
            if (access[q] == null) continue;
            final int base = q;
            final DFA left = fdfa.getProgressFA(q);
            for (int a = 0; a < leading.getAlphabetSize(); a++) {
                final int letter = a;
                final int shifted = leading.getSuccessor(q, a);
                final DFA right = fdfa.getProgressFA(shifted);
                Word x = search(empty, new int[] {shifted,
                        left.getSuccessor(left.getInitialState(), a), right.getInitialState()},
                        s -> s[0] == base && left.isFinal(s[1]) !=
                                right.isFinal(right.getSuccessor(s[2], letter)),
                        s -> successors(leading, left, right, s));
                if (x != null) {
                    Word ax = x.preappend(a);
                    return new SaturationWitness(fdfa, new Pair<>(access[q], ax),
                            new Pair<>(access[q].append(a), x.append(a)));
                }
            }
        }
        // Lemma 4: minimize the language of accepted loops first. Keeping a
        // separate leading endpoint below restores the leading-state refinement
        // assumption without requiring it of the caller's progress automata.
        // For loops x,y with the same minimized state, xz and yz agree for
        // every loop z. Loopshift stability also gives zx ~ zy (rotate z to
        // the end), hence this equivalence is a two-sided congruence on loops.
        // It follows inductively that x^i and y^i agree for every i. One short
        // nonempty representative per (leading, minimized) state thus suffices.
        for (int q = 0; q < access.length; q++) {
            if (access[q] == null) continue;
            DFA progress = fdfa.getProgressFA(q);
            Automaton loops = DFAOperations.toDkDFA(progress).intersection(
                    DFAOperations.toDkDFA(leading, q, q));
            loops.minimize();
            List<State> states = new ArrayList<>(loops.getStates());
            Map<State, Integer> ids = new IdentityHashMap<>();
            for (int i = 0; i < states.size(); i++) ids.put(states.get(i), i);
            // brics uses an implicit rejecting sink, represented here by -1.
            final int base = q;
            final int initial = ids.get(loops.getInitialState());
            Map<List<Integer>, Word> representatives = new HashMap<>();
            Queue<int[]> pending = new ArrayDeque<>();
            int[] start = {q, initial};
            // Seed with letters to retain nonempty words returning to start.
            for (int a = 0; a < leading.getAlphabetSize(); a++) {
                int[] next = loopStep(leading, states, ids, start, a);
                if (representatives.putIfAbsent(key(next), empty.append(a)) == null) pending.add(next);
            }
            while (!pending.isEmpty()) {
                int[] current = pending.remove();
                Word r = representatives.get(key(current));
                if (current[0] == base) {
                    int p = progress.getSuccessor(r);
                    boolean accepts = progress.isFinal(p);
                    boolean[] seen = new boolean[progress.getStateSize()];
                    Word power = r;
                    while (!seen[p]) {
                        seen[p] = true;
                        if (progress.isFinal(p) != accepts)
                            return new SaturationWitness(fdfa, new Pair<>(access[q], r),
                                    new Pair<>(access[q], power));
                        p = progress.getSuccessor(p, r);
                        power = power.concat(r);
                    }
                }
                for (int a = 0; a < leading.getAlphabetSize(); a++) {
                    int[] next = loopStep(leading, states, ids, current, a);
                    if (representatives.putIfAbsent(key(next), r.append(a)) == null) pending.add(next);
                }
            }
        }
        return null;
    }

    private static int[] loopStep(DFA leading, List<State> states, Map<State, Integer> ids,
            int[] s, int a) {
        State next = s[1] < 0 ? null : states.get(s[1]).step(leading.getAlphabet().getLetter(a));
        return new int[] {leading.getSuccessor(s[0], a), next == null ? -1 : ids.get(next)};
    }

    private static int[][] successors(DFA leading, DFA left, DFA right, int[] s) {
        int[][] result = new int[leading.getAlphabetSize()][];
        for (int a = 0; a < result.length; a++) result[a] = new int[] {
                leading.getSuccessor(s[0], a), left.getSuccessor(s[1], a), right.getSuccessor(s[2], a)};
        return result;
    }

    private static List<Integer> key(int[] state) {
        List<Integer> result = new ArrayList<>(state.length);
        for (int s : state) result.add(s);
        return result;
    }

    private static Word search(Word empty, int[] start, Predicate<int[]> goal,
            Function<int[], int[][]> step) {
        Map<List<Integer>, Word> words = new HashMap<>();
        Queue<int[]> pending = new ArrayDeque<>();
        words.put(key(start), empty);
        pending.add(start);
        while (!pending.isEmpty()) {
            int[] s = pending.remove();
            Word word = words.get(key(s));
            if (goal.test(s)) return word;
            int[][] next = step.apply(s);
            for (int a = 0; a < next.length; a++) {
                if (words.putIfAbsent(key(next[a]), word.append(a)) == null) pending.add(next[a]);
            }
        }
        return null;
    }

    private static void validate(DFA dfa) {
        if (dfa.getInitialState() < 0 || dfa.getInitialState() >= dfa.getStateSize())
            throw new IllegalArgumentException("A DFA must have an initial state");
        for (int q = 0; q < dfa.getStateSize(); q++) {
            for (int a = 0; a < dfa.getAlphabetSize(); a++) {
                if (dfa.getState(q).getSuccessors(a).cardinality() != 1)
                    throw new IllegalArgumentException("Saturation requires total deterministic DFAs");
            }
        }
    }
}
