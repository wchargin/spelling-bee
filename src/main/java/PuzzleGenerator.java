import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

final class PuzzleGenerator {

    static void printUsage() {
        final List<String> args = Arrays.asList(
                PuzzleGenerator.class.getName(),
                "<words_file>",
                "<frequencies_file>",
                "<ratings_file>",
                "<n_weeks>",
                "<output_file>");
        System.out.println("Usage: java " + args.stream().collect(Collectors.joining(" ")));
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            printUsage();
            System.exit(1);
        }
        final String wordsFilename = args[0];
        final String frequenciesFilename = args[1];
        final String ratingsFilename = args[2];
        final int nWeeks = Integer.parseInt(args[3]);
        final String outputFilename = args[4];
        final Random rng = new Random(0);

        System.out.println("Reading dictionary...");
        final List<String> words = Files.readAllLines(Paths.get(wordsFilename));

        System.out.println("Compiling generic puzzle data...");
        final PuzzleMaster pm = new PuzzleMaster(words);

        System.out.println("Reading word frequencies...");
        final Map<String, Double> frequencies =
                AbstractFrequencyAccessibilityEstimator.parseFrequencies(
                        Files.newInputStream(Paths.get(frequenciesFilename)));

        // The following constants were chosen by inspection of the frequencies file for the word
        // list provided by the Ubuntu wamerican package,version 7.1 - 1. This frequency file is
        // available in <repo>/data/frequencies.
        final AccessibilityEstimator estimator =
                new InverseLogarithmicFrequencyAccessibilityEstimator(frequencies, 6, 0.08);

        System.out.println("Training models for good/excellent/genius levels...");
        final List<PuzzleDatum> trainingData = PuzzleDatum.createDataset(
                Files.newInputStream(Paths.get(ratingsFilename)),
                pm::solutionsTo);
        final SimpleOrdinaryLinearRegression<PuzzleDatum> goodRatingModel =
                new SimpleOrdinaryLinearRegression<>(
                        datum -> estimator.accessibility(datum.puzzle, datum.solutions),
                        datum -> datum.good);
        goodRatingModel.train(trainingData);
        final SimpleOrdinaryLinearRegression<PuzzleDatum> perLevelDeltaModel =
                new SimpleOrdinaryLinearRegression<>(
                        datum -> estimator.accessibility(datum.puzzle, datum.solutions),
                        datum -> 0.5 * (datum.genius - datum.good));
        perLevelDeltaModel.train(trainingData);

        System.out.println("Solving all puzzles...");
        final Map<Puzzle, Set<String>> solutions = pm.puzzles.stream().collect(
                Collectors.toMap(Function.identity(), pm::solutionsTo));

        // Choose only puzzles with a reasonable maximum possible score.
        final int scoreLowerBound = 14;
        final int scoreUpperBound = 28;
        System.out.printf("Selecting puzzles with scores between %s and %s, inclusive...%n",
                scoreLowerBound, scoreUpperBound);
        final List<Puzzle> puzzlesInScoreRange = pm.puzzles.stream()
                .filter(puzzle -> {
                    final int score = Puzzle.score(solutions.get(puzzle));
                    return scoreLowerBound <= score && score <= scoreUpperBound;
                })
                .collect(Collectors.toList());
        System.out.println("--- Puzzles in score range: " + puzzlesInScoreRange.size());

        System.out.println("Selecting at most one puzzle per pot...");
        final List<Puzzle> nonOverlappingPuzzles = puzzlesInScoreRange
                .stream()
                .collect(Collectors.groupingBy(puzzle -> puzzle.potVector))
                .values().stream()
                .map(list -> list.get(rng.nextInt(list.size())))
                .collect(Collectors.toCollection(ArrayList::new));
        System.out.println("--- Puzzles in pool: " + nonOverlappingPuzzles.size());

        final List<Puzzle> puzzles = nonOverlappingPuzzles;
        System.out.println("Sorting puzzles by decreasing accessibility...");
        puzzles.sort(Comparator.comparingDouble(
                (Puzzle puzzle) -> estimator.accessibility(puzzle, solutions.get(puzzle))
        ).reversed());

        System.out.println("Randomizing order within tripartitions...");
        final int bigStep = puzzles.size() / 3;
        Collections.shuffle(puzzles.subList(0, bigStep), rng);
        Collections.shuffle(puzzles.subList(bigStep, 2 * bigStep), rng);
        Collections.shuffle(puzzles.subList(2 * bigStep, puzzles.size()), rng);

        System.out.println("Estimating level ratings...");
        final Map<Puzzle, Integer> goodLevels = puzzles.stream().collect(Collectors.toMap(
                Function.identity(),
                puzzle -> (int) Math.round(goodRatingModel.predict(
                        estimator.accessibility(puzzle, solutions.get(puzzle))))));
        final Map<Puzzle, Integer> perLevelDeltas = puzzles.stream().collect(Collectors.toMap(
                Function.identity(),
                puzzle -> (int) Math.round(perLevelDeltaModel.predict(
                        estimator.accessibility(puzzle, solutions.get(puzzle))))));

        final File outFile = new File(outputFilename);
        System.out.printf("Writing TeX to '%s'...%n", outFile.getPath());
        final Function<Puzzle, PuzzleDatum> formDatum = (puzzle) ->
                new PuzzleDatum(
                        puzzle,
                        solutions.get(puzzle),
                        goodLevels.get(puzzle),
                        goodLevels.get(puzzle) + perLevelDeltas.get(puzzle),
                        goodLevels.get(puzzle) + 2 * perLevelDeltas.get(puzzle));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write("\\documentclass[12pt,letterpaper]{article}\n\n");
            writer.write("\\usepackage{spellingbee}\n\n");
            writer.write("\\begin{document}\n\n");
            for (int i = 0; i < nWeeks && i < bigStep; i++) {
                final Puzzle easy = puzzles.get(i);
                final Puzzle medium = puzzles.get(i + bigStep);
                final Puzzle hard = puzzles.get(i + 2 * bigStep);
                writer.write(pageTeX(
                        formDatum.apply(easy),
                        formDatum.apply(medium),
                        formDatum.apply(hard)));
            }
            writer.write("\\end{document}\n");
        }
        System.out.println("Done.");
    }

    static String pageTeX(PuzzleDatum easy, PuzzleDatum medium, PuzzleDatum hard) {
        final StringBuilder sb = new StringBuilder();
        sb.append(puzzleTeX(easy));
        sb.append("\\nextpuzzle\n");
        sb.append(puzzleTeX(medium));
        sb.append("\\nextpuzzle\n");
        sb.append(puzzleTeX(hard));
        sb.append("\\clearpage\n\n");
        return sb.toString();
    }

    static String puzzleTeX(PuzzleDatum datum) {
        final IntFunction<String> vectorToTeXGroup = (vector) ->
                Puzzle.characterUnvector(vector).toUpperCase()
                        .chars().boxed()
                        .map(x -> String.valueOf((char) x.intValue()))
                        .collect(Collectors.joining(",", "{", "}"));
        final StringBuilder sb = new StringBuilder();
        sb.append("\\puzzle{%\n");
        sb.append("  required=");
        final int required = datum.puzzle.requiredVector;
        sb.append(vectorToTeXGroup.apply(required));
        sb.append(",\n  optional=");
        final int optional = datum.puzzle.potVector & ~required;
        sb.append(vectorToTeXGroup.apply(optional));
        sb.append(",\n  good=");
        sb.append(datum.good);
        sb.append(",\n  excellent=");
        sb.append(datum.excellent);
        sb.append(",\n  genius=");
        sb.append(datum.genius);
        sb.append("\n}\n");
        return sb.toString();
    }

}
