import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class PuzzleMasterTest {

    private static PuzzleMaster createSmallInstance() {
        final List<String> words = Arrays.asList(
                "abracadabrazy",
                "abrac",
                "barca",
                "barbar",
                "zzzzz",
                "zzzzzzzz",
                "lengthened",
                "lengthen",
                "then",
                "vis-a-vis",
                "Caps"
        );
        return new PuzzleMaster(words);
    }

    @Test
    public void constructor_onSmallData_createsWords() {
        Assert.assertEquals(
                new HashSet<>(Arrays.asList(
                        "abracadabrazy",
                        "abrac",
                        "barca",
                        "barbar",
                        "zzzzz",
                        "zzzzzzzz",
                        "lengthened",
                        "lengthen")),
                createSmallInstance().words);
    }

    @Test
    public void constructor_onSmallData_createsPots() {
        Assert.assertEquals(
                new HashSet<>(Arrays.asList(
                        Puzzle.characterVector("abrcdzy".toCharArray()),
                        Puzzle.characterVector("lengthd".toCharArray()))),
                createSmallInstance().pots);
    }

    @Test
    public void constructor_onSmallData_createsWordsByVector() {
        final PuzzleMaster pm = createSmallInstance();
        Assert.assertEquals(new HashSet<>(Arrays.asList(
                Puzzle.characterVector("abrcdzy".toCharArray()),
                Puzzle.characterVector("abrc".toCharArray()),
                Puzzle.characterVector("bar".toCharArray()),
                Puzzle.characterVector("z".toCharArray()),
                Puzzle.characterVector("lengthd".toCharArray()),
                Puzzle.characterVector("length".toCharArray()))
        ), pm.wordsByVector.keySet());
        Assert.assertEquals(
                new HashSet<>(Arrays.asList("abracadabrazy")),
                pm.wordsByVector.get(Puzzle.characterVector("abrcdzy".toCharArray())));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList("abrac", "barca")),
                pm.wordsByVector.get(Puzzle.characterVector("abrc".toCharArray())));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList("barbar")),
                pm.wordsByVector.get(Puzzle.characterVector("bar".toCharArray())));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList("zzzzz", "zzzzzzzz")),
                pm.wordsByVector.get(Puzzle.characterVector("z".toCharArray())));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList("lengthened")),
                pm.wordsByVector.get(Puzzle.characterVector("lengthd".toCharArray())));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList("lengthen")),
                pm.wordsByVector.get(Puzzle.characterVector("length".toCharArray())));
    }

    @Test
    public void constructor_onSmallData_createsPuzzles() {
        final PuzzleMaster pm = createSmallInstance();
        Assert.assertEquals(14, pm.puzzles.size());
        for (String pot : new String[] { "abrcdzy", "lengthd" }) {
            for (char required : pot.toCharArray()) {
                final Puzzle p = new Puzzle(
                        Puzzle.characterVector(pot.toCharArray()),
                        Puzzle.characterVector(new char[] { required }));
                Assert.assertTrue(p.toString(), pm.puzzles.contains(p));
            }
        }
    }

    @Test
    public void solutionsTo_simple1() {
        final PuzzleMaster pm = createSmallInstance();
        final Puzzle puzzle = new Puzzle(
                Puzzle.characterVector("abrcdzy".toCharArray()),
                Puzzle.characterVector("c".toCharArray()));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList("abracadabrazy", "abrac", "barca")),
                pm.solutionsTo(puzzle));
    }

    public void solutionsTo_simple2() {
        final PuzzleMaster pm = createSmallInstance();
        final Puzzle puzzle = new Puzzle(
                Puzzle.characterVector("lengthd".toCharArray()),
                Puzzle.characterVector("e".toCharArray()));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList("lengthened", "lengthen")),
                pm.solutionsTo(puzzle));
    }

    @Test
    public void solutionsTo_noSolutions() {
        final PuzzleMaster pm = createSmallInstance();
        final Puzzle puzzle = new Puzzle(
                Puzzle.characterVector("abcdleg".toCharArray()),
                Puzzle.characterVector("c".toCharArray()));
        Assert.assertEquals(Collections.emptySet(), pm.solutionsTo(puzzle));
    }

    @Test
    public void solutionsTo_unknownLetters() {
        final PuzzleMaster pm = createSmallInstance();
        final Puzzle puzzle = new Puzzle(
                Puzzle.characterVector("jkopqsu".toCharArray()),
                Puzzle.characterVector("q".toCharArray()));
        for (String word : pm.words) {
            Assert.assertTrue("test data mismatch",
                    (puzzle.potVector & Puzzle.characterVector(word.toCharArray())) == 0);
        }
        Assert.assertEquals(Collections.emptySet(), pm.solutionsTo(puzzle));
    }

}
