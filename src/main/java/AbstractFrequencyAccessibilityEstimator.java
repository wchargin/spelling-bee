import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An accessibility estimator that performs calculations based on the natural-language
 * frequencies of the words in the solution set.
 */
abstract class AbstractFrequencyAccessibilityEstimator implements AccessibilityEstimator {

    /**
     * The user-provided map of word frequencies.
     */
    private final Map<String, Double> wordFrequencies;

    /**
     * The smallest user-observed nonzero frequency of a word.
     */
    private final double minimumNonzeroFrequency;

    /**
     * Construct an estimator given the provided word frequencies. All reported frequencies must
     * be non-negative.
     *
     * @param wordFrequencies
     *         a mapping from each word to the frequency at which it appears
     * @throws IllegalArgumentException
     *         if {@code wordFrequencies} contains a negative value
     */
    protected AbstractFrequencyAccessibilityEstimator(Map<String, Double> wordFrequencies) {
        this.wordFrequencies = new HashMap<>(wordFrequencies);
        final List<Map.Entry<String, Double>> negativeEntries =
                this.wordFrequencies.entrySet().stream()
                        .filter(x -> x.getValue() < 0)
                        .collect(Collectors.toList());
        if (!negativeEntries.isEmpty()) {
            throw new IllegalArgumentException(
                    "negative probabilities reported: " + negativeEntries);
        }
        minimumNonzeroFrequency = this.wordFrequencies.values().stream()
                .mapToDouble(x -> x)
                .filter(x -> x > 0)
                .min()
                .orElse(1);  // arbitrary nonzero
    }

    /**
     * Estimate the frequency of a word. The result is always positive: if the word was never
     * observed, the minimum observed frequency will be reported.
     *
     * @param word
     *         a word whose frequency to estimate
     * @return a probability estimate for the word's frequency
     */
    protected double wordFrequency(String word) {
        final double raw = wordFrequencies.getOrDefault(word, 0.0);
        return raw == 0 ? minimumNonzeroFrequency : raw;
    }

}
