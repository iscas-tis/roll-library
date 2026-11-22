package roll.automata.operations;

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import org.junit.Test;
import roll.automata.DFA;
import roll.automata.FDFA;
import roll.automata.operations.FDFAOperations.SaturationWitness;
import roll.query.Query;
import roll.table.HashableValue;
import roll.table.HashableValueBoolean;
import roll.util.Pair;
import roll.words.Alphabet;
import roll.words.Word;

public class FDFASaturationTest {
    private Alphabet alphabet() {
        Alphabet a = new Alphabet();
        a.addLetter('a');
        a.addLetter('b');
        return a;
    }

    private DFA dfa(Alphabet a, int[][] delta, int... finals) {
        DFA d = new DFA(a);
        for (int i = 0; i < delta.length; i++) d.createState();
        d.setInitial(0);
        for (int q = 0; q < delta.length; q++)
            for (int c = 0; c < a.getLetterSize(); c++) d.getState(q).addTransition(c, delta[q][c]);
        for (int f : finals) d.setFinal(f);
        return d;
    }

    private FDFA family(Alphabet a, DFA... progress) {
        return new FDFA(dfa(a, new int[][] {{0, 0}}), Arrays.asList(progress));
    }

    @Test public void powersAndRefinementLabels() {
        Alphabet a = alphabet();
        // All words of odd length: loopshift-stable, but a and aa disagree.
        FDFA f = family(a, dfa(a, new int[][] {{1, 1}, {0, 0}}, 1));
        certify(f, FDFAOperations.checkSaturation(f));
        for (boolean target : new boolean[] {false, true}) {
            int[] calls = {0};
            Query<HashableValue> ce = FDFAOperations.findSaturationCounterexample(f, query -> {
                calls[0]++;
                return new HashableValueBoolean(target);
            });
            assertEquals(1, calls[0]);
            assertTrue(f.isNormalized(ce.getPrefix(), ce.getSuffix()));
            assertFalse(ce.getSuffix().isEmpty());
            assertEquals(target, (boolean) ce.getQueryAnswer().get());
            assertEquals(!target, f.getAcc().accept(ce.getPrefix(), ce.getSuffix()));
        }
    }

    @Test public void loopshiftAcrossProgressAutomataAndUnreachableStates() {
        Alphabet a = alphabet();
        DFA leading = dfa(a, new int[][] {{1, 1}, {0, 0}, {2, 2}});
        DFA yes = dfa(a, new int[][] {{0, 0}}, 0);
        DFA no = dfa(a, new int[][] {{0, 0}});
        FDFA f = new FDFA(leading, Arrays.asList(yes, no, yes));
        certify(f, FDFAOperations.checkSaturation(f));
        FDFA saturated = new FDFA(leading, Arrays.asList(yes, yes,
                dfa(a, new int[][] {{1, 1}, {0, 0}}, 1)));
        assertNull(FDFAOperations.checkSaturation(saturated));
    }

    @Test public void nonminimalAndEmptyWordAcceptance() {
        Alphabet a = alphabet();
        // Only epsilon is rejected. All nonempty representations agree.
        FDFA f = family(a, dfa(a, new int[][] {{1, 2}, {2, 1}, {1, 2}}, 1, 2));
        assertNull(FDFAOperations.checkSaturation(f));
        assertNull(FDFAOperations.findSaturationCounterexample(f, query -> {
            fail("Saturated input must not query the target");
            return null;
        }));
        assertNull(FDFAOperations.checkSaturation(family(a, dfa(a, new int[][] {{0, 0}}))));
    }

    @Test public void powerFailureCanRequireMoreThanSquaring() {
        Alphabet a = alphabet();
        FDFA f = family(a, dfa(a, new int[][] {{1, 1}, {2, 2}, {0, 0}}, 1, 2));
        SaturationWitness witness = FDFAOperations.checkSaturation(f);
        certify(f, witness);
        assertEquals(3, witness.getRejected().getRight().length());
    }

    @Test(expected = IllegalArgumentException.class) public void rejectsPartialInput() {
        Alphabet a = alphabet();
        DFA partial = new DFA(a);
        partial.createState();
        partial.setInitial(0);
        FDFAOperations.checkSaturation(family(a, partial));
    }

    @Test public void agreesWithExactTransitionMonoidOracle() {
        Random random = new Random(20251122);
        for (int sample = 0; sample < 1000; sample++) {
            Alphabet a = alphabet();
            DFA leading = randomDfa(a, 1 + random.nextInt(2), random);
            List<DFA> progress = new ArrayList<>();
            for (int q = 0; q < leading.getStateSize(); q++)
                progress.add(randomDfa(a, 1 + random.nextInt(2), random));
            FDFA f = new FDFA(leading, progress);
            SaturationWitness witness = FDFAOperations.checkSaturation(f);
            assertEquals("sample " + sample, exactSaturated(f), witness == null);
            if (witness != null) certify(f, witness);
        }
    }

    private DFA randomDfa(Alphabet a, int n, Random random) {
        int[][] delta = new int[n][2];
        List<Integer> finals = new ArrayList<>();
        for (int q = 0; q < n; q++) {
            for (int c = 0; c < 2; c++) delta[q][c] = random.nextInt(n);
            if (random.nextBoolean()) finals.add(q);
        }
        return dfa(a, delta, finals.stream().mapToInt(Integer::intValue).toArray());
    }

    private void certify(FDFA f, SaturationWitness w) {
        assertNotNull(w);
        Pair<Word, Word> yes = w.getAccepted(), no = w.getRejected();
        for (Pair<Word, Word> pair : Arrays.asList(yes, no)) {
            assertFalse(pair.getRight().isEmpty());
            assertTrue(f.isNormalized(pair.getLeft(), pair.getRight()));
        }
        assertTrue(f.getAcc().accept(yes.getLeft(), yes.getRight()));
        assertFalse(f.getAcc().accept(no.getLeft(), no.getRight()));
        assertEquals(Alphabet.getNormalForm(yes.getLeft(), yes.getRight()),
                Alphabet.getNormalForm(no.getLeft(), no.getRight()));
    }

    // Exponential reference algorithm: enumerate every simultaneous word-induced
    // transformation, not just the short state representatives used in production.
    private boolean exactSaturated(FDFA f) {
        List<DFA> machines = new ArrayList<>();
        machines.add(f.getLeadingFA());
        for (int q = 0; q < f.getLeadingFA().getStateSize(); q++) machines.add(f.getProgressFA(q));
        List<Integer> identity = new ArrayList<>();
        for (DFA d : machines) for (int p = 0; p < d.getStateSize(); p++) identity.add(p);
        Map<List<Integer>, Word> transformations = new HashMap<>();
        Queue<List<Integer>> pending = new ArrayDeque<>();
        Word empty = f.getAlphabet().getEmptyWord();
        pending.add(identity);
        // Don't mark identity yet: it must also be enumerated if a nonempty
        // word induces it, since periods cannot be empty.
        while (!pending.isEmpty()) {
            List<Integer> t = pending.remove();
            Word prefix = transformations.getOrDefault(t, empty);
            for (int a = 0; a < 2; a++) {
                List<Integer> next = new ArrayList<>();
                int offset = 0;
                for (DFA d : machines) {
                    for (int p = 0; p < d.getStateSize(); p++) next.add(d.getSuccessor(t.get(offset + p), a));
                    offset += d.getStateSize();
                }
                if (!transformations.containsKey(next)) {
                    transformations.put(next, prefix.append(a));
                    pending.add(next);
                }
            }
        }
        DFA leading = f.getLeadingFA();
        boolean[] reachable = new boolean[leading.getStateSize()];
        reachable[leading.getInitialState()] = true;
        for (Word w : transformations.values()) reachable[leading.getSuccessor(w)] = true;
        for (int q = 0; q < reachable.length; q++) {
            if (!reachable[q]) continue;
            DFA p = f.getProgressFA(q);
            for (Word w : transformations.values()) {
                if (leading.getSuccessor(q, w) == q) {
                    int state = p.getSuccessor(w);
                    boolean expected = p.isFinal(state);
                    boolean[] seen = new boolean[p.getStateSize()];
                    while (!seen[state]) {
                        seen[state] = true;
                        if (p.isFinal(state) != expected) return false;
                        state = p.getSuccessor(state, w);
                    }
                }
                for (int a = 0; a < 2; a++) {
                    Word aw = w.preappend(a);
                    if (leading.getSuccessor(q, aw) != q) continue;
                    DFA other = f.getProgressFA(leading.getSuccessor(q, a));
                    if (p.isFinal(p.getSuccessor(aw)) != other.isFinal(other.getSuccessor(w.append(a))))
                        return false;
                }
            }
        }
        return true;
    }
}
