import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PuzzleDatumTest {
    @Test
    public void createDataset_representativeData() throws IOException {
        final String input = Stream.of(
                "# This is a comment line, and the next line is blank\n",
                "\n",
                "c,abdryz,2,3,4\n",
                "e,dghntl,3,4,5\n"
        ).collect(Collectors.joining());
        final InputStream stream = new ByteArrayInputStream(input.getBytes());

        final Map<Puzzle, Collection<String>> solutions = new HashMap<>();
        final Puzzle puzzle1 = new Puzzle(
                Puzzle.characterVector("cabdryz".toCharArray()),
                Puzzle.characterVector("c".toCharArray()));
        final Puzzle puzzle2 = new Puzzle(
                Puzzle.characterVector("edghntl".toCharArray()),
                Puzzle.characterVector("e".toCharArray()));
        solutions.put(puzzle1, Collections.unmodifiableList(
                Arrays.asList("abracadabrazy", "abrac", "barca", "barbar")));
        solutions.put(puzzle2, Collections.unmodifiableList(
                Arrays.asList("lengthened", "lengthen")));

        final List<PuzzleDatum> dataset = PuzzleDatum.createDataset(stream, solutions::get);

        Assert.assertEquals(2, dataset.size());

        Assert.assertEquals(puzzle1, dataset.get(0).puzzle);
        Assert.assertEquals(solutions.get(puzzle1), dataset.get(0).solutions);
        Assert.assertEquals(2, dataset.get(0).good);
        Assert.assertEquals(3, dataset.get(0).excellent);
        Assert.assertEquals(4, dataset.get(0).genius);

        Assert.assertEquals(puzzle2, dataset.get(1).puzzle);
        Assert.assertEquals(solutions.get(puzzle2), dataset.get(1).solutions);
        Assert.assertEquals(3, dataset.get(1).good);
        Assert.assertEquals(4, dataset.get(1).excellent);
        Assert.assertEquals(5, dataset.get(1).genius);
    }
}
