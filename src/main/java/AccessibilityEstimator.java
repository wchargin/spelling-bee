import java.util.Collection;

/**
 * An estimator for {@link Puzzle}s' <em>accessibility</em>: the ease of finding solutions to the
 * puzzle. A puzzle for which it is easy to find many words is highly accessible, and a puzzle
 * for which it is hard to find words is inaccessible. An accessible puzzle may still be
 * perceived as difficult if the ranking thresholds are steep, and vice versa.
 */
interface AccessibilityEstimator {
    /**
     * Estimate how accessible a puzzle is, given its full set of solutions.
     *
     * @param puzzle
     *         a puzzle
     * @param solutions
     *         all solutions to the puzzle
     * @return a value for this estimator's accessibility metric
     */
    double accessibility(Puzzle puzzle, Collection<String> solutions);
}
