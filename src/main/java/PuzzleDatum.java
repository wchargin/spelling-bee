import java.util.ArrayList;
import java.util.Collection;

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
}
