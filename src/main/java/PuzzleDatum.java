import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Data and metadata about a puzzle, used to train a model to estimate the good/excellent/genius
 * thresholds from just the solution count and score.
 */
final class PuzzleDatum {
    final Puzzle puzzle;
    final Collection<String> solutions;
    final int good;
    final int excellent;
    final int genius;

    PuzzleDatum(
            Puzzle puzzle, Collection<? extends String> solutions,
            int good, int excellent, int genius) {
        this.puzzle = puzzle;
        this.solutions = new ArrayList<>(solutions);
        this.good = good;
        this.excellent = excellent;
        this.genius = genius;
    }

    /**
     * <p>
     * Create a set of {@code PuzzleDatum} objects by reading the puzzles and rating thresholds
     * from a file (or other input source), and computing the solution sets on the fly.
     * </p>
     * <p>
     * The file should be in CSV format, with the following columns (in the following order):
     * </p>
     * <ul>
     * <li>
     * required character: the single lowercase character required in each word of the
     * puzzle
     * </li>
     * <li>
     * optional characters: the other lowercase characters that may be used in words
     * </li>
     * <li>
     * "good" threshold: the minimum number of points to earn a rating of "good"
     * </li>
     * <li>
     * "excellent" threshold: the minimum number of points to earn a rating of "excellent"
     * </li>
     * <li>
     * "genius" threshold: the minimum number of points to earn a rating of "genius"
     * </li>
     * </ul>
     * <p>
     * Whitespace-only lines and lines whose first non-whitespace character is a hash ({@code #})
     * are ignored. Extraneous columns will be ignored.
     * </p>
     *
     * @param inputStream
     *         a stream containing a CSV file described above
     * @param solver
     *         a function to generate all valid solutions to a given puzzle
     * @return a list of {@code PuzzleData} objects, whose puzzles and ratings are drawn from the
     * given CSV file and whose solutions are computed by the given function
     * @throws IOException
     *         if thrown while reading from the input stream
     */
    static List<PuzzleDatum> createDataset(
            InputStream inputStream,
            Function<Puzzle, ? extends Collection<String>> solver) throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream)).lines()
                .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                .map(line -> {
                    final String[] parts = line.trim().split(",");
                    final int requiredVector = Puzzle.characterVector(parts[0].toCharArray());
                    final int optionalVector = Puzzle.characterVector(parts[1].toCharArray());
                    final int potVector = requiredVector | optionalVector;
                    final Puzzle puzzle = new Puzzle(potVector, requiredVector);
                    final Collection<String> solutions = solver.apply(puzzle);
                    final int goodThreshold = Integer.parseInt(parts[2]);
                    final int excellentThreshold = Integer.parseInt(parts[3]);
                    final int geniusThreshold = Integer.parseInt(parts[4]);
                    return new PuzzleDatum(
                            puzzle, solutions, goodThreshold, excellentThreshold, geniusThreshold);
                }).collect(Collectors.toList());
    }
}
