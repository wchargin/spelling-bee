import java.util.Collection;
import java.util.Map;

/**
 * An accessibility estimator that sums the frequencies of the valid solution words. If these
 * frequencies represented the probability that a person knows a particular word, then this
 * would represent the expected number of words that a person knows (but of course this
 * assumption is not at all true).
 */
final class AdditiveFrequencyAccessibilityEstimator
        extends AbstractFrequencyAccessibilityEstimator {
    /**
     * Construct an estimator given the provided word frequencies. All reported frequencies must
     * be non-negative.
     *
     * @param wordFrequencies
     *         a mapping from each word to the frequency at which it appears
     * @throws IllegalArgumentException
     *         if {@code wordFrequencies} contains a negative value
     */
    AdditiveFrequencyAccessibilityEstimator(Map<String, Double> wordFrequencies) {
        super(wordFrequencies);
    }

    @Override
    public double accessibility(Puzzle puzzle, Collection<String> solutions) {
        return solutions.stream().mapToDouble(this::wordFrequency).sum();
    }
}
