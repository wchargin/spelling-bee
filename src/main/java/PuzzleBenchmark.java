import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entry point to generate and solve all possible puzzles. The results aren't saved anywhere, so
 * this is useful only for timing purposes (or for counting how many puzzles there are).
 */
final class PuzzleBenchmark {

    static void printUsage() {
        final List<String> args = Arrays.asList(
                PuzzleBenchmark.class.getName(),
                "<words_file>");
        System.out.println("Usage: java " + args.stream().collect(Collectors.joining(" ")));
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            printUsage();
            System.exit(1);
        }
        final String wordsFilename = args[0];

        System.out.println("Reading dictionary...");
        final List<String> words = Files.readAllLines(Paths.get(wordsFilename));

        System.out.println("Compiling generic puzzle data...");
        final PuzzleMaster pm = new PuzzleMaster(words);

        System.out.println("Puzzle count: " + pm.puzzles.size());

        System.out.println("Solving puzzles...");
        final List<Set<String>> solutions =
                pm.puzzles.stream()
                        .parallel()  // only about a 15% speedup due to small dataset
                        .map(pm::solutionsTo)
                        .collect(Collectors.toList());

        System.out.println("Solved.");
        System.out.println("Proof: " + solutions.hashCode());
    }

}
