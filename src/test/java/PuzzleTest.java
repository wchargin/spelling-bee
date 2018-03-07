import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class PuzzleTest {

    @Test
    public void characterVector_simple1() {
        Assert.assertEquals(
                0b1011001, Puzzle.characterVector("adeg".toCharArray()));
    }

    @Test
    public void characterVector_simple2() {
        Assert.assertEquals(
                0b1010010001, Puzzle.characterVector("aehj".toCharArray()));
    }

    @Test
    public void characterVector_unsortedWithDuplicates() {
        Assert.assertEquals(
                0b1011001, Puzzle.characterVector("gagged".toCharArray()));
    }

    @Test
    public void characterVector_empty() {
        Assert.assertEquals(
                0, Puzzle.characterVector("".toCharArray()));
    }

    @Test
    public void characterVector_fullAlphabet() {
        final String alphabet = "abcdefghijklmnopqrstuvwxyz";
        final int expected = (1 << 26) - 1;
        Assert.assertEquals(
                expected, Puzzle.characterVector(alphabet.toCharArray()));
    }

    @Test
    public void characterUnvector_empty() {
        Assert.assertEquals("", Puzzle.characterUnvector(0));
    }

    @Test
    public void characterUnvector_singleton1() {
        Assert.assertEquals("a", Puzzle.characterUnvector(0b1));
    }

    @Test
    public void characterUnvector_singleton2() {
        Assert.assertEquals("d", Puzzle.characterUnvector(0b1000));
    }

    @Test
    public void characterUnvector_simple() {
        final int input = 0b101;
        final String output = Puzzle.characterUnvector(input);
        if (!output.equals("ac") && !output.equals("ca")) {
            Assert.fail(output);
        }
    }

    @Test
    public void characterUnvector_lengthCorrect() {
        for (int vector = 0; vector < 0xFFFF; vector++) {
            final String unvector = Puzzle.characterUnvector(vector);
            Assert.assertEquals(unvector, Integer.bitCount(vector), unvector.length());
        }
    }

    @Test
    public void characterVector_characterUnvector_inverseProperties() {
        for (int vector = 0; vector < 0xFFFF; vector++) {
            final int roundtrip =
                    Puzzle.characterVector(Puzzle.characterUnvector(vector).toCharArray());
            Assert.assertEquals(vector, roundtrip);
        }
    }

    @Test
    public void testScore_emptyInput() {
        Assert.assertEquals(0, Puzzle.score(Collections.emptySet()));
    }

    @Test
    public void testScore_noBingos() {
        Assert.assertEquals(5, Puzzle.score(Arrays.asList(
                "tenet", "teeth", "legged", "length", "deleted")));
    }

    @Test
    public void testScore_someBingos() {
        Assert.assertEquals(8, Puzzle.score(Arrays.asList(
                "tenet", "teeth", "legged", "length", "deleted", "lengthened")));
    }

}
