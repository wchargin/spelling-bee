import java.util.Collection;

/**
 * An accessibility estimator that uses the solution word count as the metric.
 */
final class SolutionCountAccessibilityEstimator implements AccessibilityEstimator {
    @Override
    public double accessibility(Puzzle puzzle, Collection<String> solutions) {
        return solutions.size();
    }
}
