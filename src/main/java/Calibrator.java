import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

final class Calibrator {

    static void printUsage() {
        System.out.printf("Usage: java %s <words_file> <frequencies_file> <puzzles_file>%n",
                Calibrator.class.getName());
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            printUsage();
            System.exit(1);
        }
        final String wordsFilename = args[0];
        final String frequenciesFilename = args[1];
        final String puzzlesFilename = args[2];

        System.out.println("Reading dictionary...");
        final List<String> words = Files.readAllLines(Paths.get(wordsFilename));

        System.out.println("Compiling puzzle information...");
        final PuzzleMaster pm = new PuzzleMaster(words);

        System.out.println("Reading frequencies...");
        final Map<String, Double> frequencies =
                AbstractFrequencyAccessibilityEstimator.parseFrequencies(
                        Files.newInputStream(Paths.get(frequenciesFilename)));

        System.out.println("Reading and solving puzzles...");
        final List<PuzzleDatum> puzzleData = PuzzleDatum.createDataset(
                Files.newInputStream(Paths.get(puzzlesFilename)),
                pm::solutionsTo);

        final Map<String, AccessibilityEstimator> estimators = new LinkedHashMap<>();
        estimators.put("solution_count", new SolutionCountAccessibilityEstimator());
        estimators.put("score", new ScoreAccessibilityEstimator());
        estimators.put("additive_freq",
                new AdditiveFrequencyAccessibilityEstimator(frequencies));
        // The following constants were chosen by inspection of the frequencies file for the
        // word list provided by the Ubuntu wamerican package, version 7.1-1. This frequency file
        // is available in <repo>/data/frequencies.
        estimators.put("inverse_log_freq",
                new InverseLogarithmicFrequencyAccessibilityEstimator(frequencies, 6, 0.08));

        final Map<String, ToDoubleFunction<PuzzleDatum>> quantities = new LinkedHashMap<>();
        quantities.put("good_threshold", datum -> datum.good);
        quantities.put("genius_threshold", datum -> datum.genius);
        quantities.put("average_level_delta",
                datum -> (datum.genius - datum.good) / 2);

        final File outfile = File.createTempFile("spelling-bee-", ".dat");
        System.out.printf("Writing gnuplot data to '%s'...%n", outfile.getPath());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile))) {
            writer.write(String.format("%s %s %s%n",
                    "puzzle_id",
                    estimators.keySet().stream().collect(Collectors.joining(" ")),
                    quantities.keySet().stream().collect(Collectors.joining(" "))));
            final Random rnd = new Random(0);
            // Perturb data slightly so that we can tell how many exactly overlapping points
            // there are at some location (without having to deal with transparent points, etc.).
            final DoubleUnaryOperator perturb = (d) ->
                    d + (rnd.nextDouble() * 2 - 1) * Math.abs(d * 0.01);
            for (PuzzleDatum datum : puzzleData) {
                final Collection<String> solutions = datum.solutions;
                writer.write(String.format("[%s]%s %s %s%n",
                        Puzzle.characterUnvector(datum.puzzle.requiredVector),
                        Puzzle.characterUnvector(
                                datum.puzzle.potVector & ~datum.puzzle.requiredVector),
                        estimators.values().stream()
                                .mapToDouble(estimator ->
                                        estimator.accessibility(datum.puzzle, solutions))
                                .mapToObj(Double::toString)
                                .collect(Collectors.joining(" ")),
                        quantities.values().stream()
                                .mapToDouble(q -> q.applyAsDouble(datum))
                                .map(Math::round)
                                .map(perturb)
                                .mapToObj(Double::toString)
                                .collect(Collectors.joining(" "))));
            }
        }

        quantities.forEach((name, quantity) -> {
            System.out.printf("Evaluating estimators for %s...%n", name);
            evaluateEstimators(puzzleData, estimators, quantity);
        });

        System.out.println("Done.");
    }

    static void evaluateEstimators(
            List<PuzzleDatum> data,
            Map<String, AccessibilityEstimator> estimators,
            ToDoubleFunction<PuzzleDatum> yAccessor) {
        final List<PuzzleDatum> h1 = data.subList(0, data.size() / 2);
        final List<PuzzleDatum> h2 = data.subList(data.size() / 2, data.size());
        estimators.forEach((name, estimator) -> {
            final ToDoubleFunction<PuzzleDatum> xAccessor = (datum) ->
                    estimator.accessibility(datum.puzzle, datum.solutions);
            final SimpleOrdinaryLinearRegression<PuzzleDatum> fullRegression =
                    new SimpleOrdinaryLinearRegression<>(xAccessor, yAccessor)
                            .train(data);
            final SimpleOrdinaryLinearRegression<PuzzleDatum> h1Regression =
                    new SimpleOrdinaryLinearRegression<>(xAccessor, yAccessor)
                            .train(h1);
            final SimpleOrdinaryLinearRegression<PuzzleDatum> h2Regression =
                    new SimpleOrdinaryLinearRegression<>(xAccessor, yAccessor)
                            .train(h2);
            final double fullMSRE = fullRegression.msre(data);
            final double crossMSE = 0.5 * (h1Regression.mse(h2) + h2Regression.mse(h1));
            final double crossMSRE = 0.5 * (h1Regression.msre(h2) + h2Regression.msre(h1));
            System.out.printf(
                    "--- %s: trainMSRE=%.04f, crossMSRE=%.04f, crossMSE=%.04f, avgval=%e%n",
                    name, fullMSRE, crossMSRE, crossMSE,
                    data.stream().mapToDouble(xAccessor).average().getAsDouble());
        });
    }

}
