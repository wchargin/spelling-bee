import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Entry point to solve a specific puzzle given by the user.
 */
final class PuzzleSolver {

    static void printUsage() {
        final List<String> args = Arrays.asList(
                PuzzleSolver.class.getName(),
                "<words_file>",
                "<required>",
                "<optional>");
        System.out.println("Usage: java " + args.stream().collect(Collectors.joining(" ")));
    }

    static int parseVector(String name, String s) {
        for (char c : s.toCharArray()) {
            final char lower = String.valueOf(c).toLowerCase(Locale.US).charAt(0);
            if (lower < 'a' || lower > 'z') {
                throw new IllegalArgumentException(String.format(
                        "Invalid character in '%s': '%s' (%x)", name, c, (int) c));
            }
        }
        return Puzzle.characterVector(s.toLowerCase(Locale.US).toCharArray());
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            printUsage();
            System.exit(1);
        }
        final String wordsFilename = args[0];
        final int requiredVector = parseVector("required", args[1]);
        final int optionalVector = parseVector("optional", args[2]);
        final int potVector = requiredVector | optionalVector;
        final Puzzle puzzle = new Puzzle(potVector, requiredVector);

        System.out.println("Reading dictionary...");
        final List<String> words = Files.readAllLines(Paths.get(wordsFilename));

        System.out.println("Compiling generic puzzle data...");
        final PuzzleMaster pm = new PuzzleMaster(words);

        System.out.println("Solving puzzle...");
        final Set<String> solutions = pm.solutionsTo(puzzle);

        final ToIntFunction<String> uniqueLetters =
                (String x) -> Integer.bitCount(Puzzle.characterVector(x.toCharArray()));
        final Comparator<String> mostUniqueLettersFirst =
                Comparator.comparingInt(uniqueLetters).reversed();
        final Comparator<String> longestFirst =
                Comparator.comparingInt(String::length).reversed();

        System.out.println("Solutions:");
        solutions.stream()
                .sorted(mostUniqueLettersFirst.thenComparing(longestFirst))
                .forEachOrdered(word -> {
                    final int letters = uniqueLetters.applyAsInt(word);
                    final int value = letters >= Puzzle.POT_SIZE ? Puzzle.BINGO_SCORE : 1;
                    System.out.printf("  - %s (%s)%n", word, value);
                });
        System.out.println("Score: " + Puzzle.score(solutions));
    }

}
