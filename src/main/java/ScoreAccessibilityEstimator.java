import java.util.Collection;

/**
 * An accessibility estimator that uses the solution score as the metric.
 */
final class ScoreAccessibilityEstimator implements AccessibilityEstimator {
    @Override
    public double accessibility(Puzzle puzzle, Collection<String> solutions) {
        return Puzzle.score(solutions);
    }
}
