import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * An accessibility estimator that sums the frequencies of the valid solution words, after
 * passing them through a function that is roughly like the reciprocal of the negated logarithm.
 * (There are a softplus and some tuning parameters thrown in there.) This attempts to capture
 * the fact that most words should count as approximately the same value, except for those words
 * that are really rare and that a player might not think of.
 * </p>
 * <p>
 * For the purposes of this class, the <em>rarity</em> of a word is given by −log(<i>f</i>), where
 * <i>f</i> is the frequency with which the word occurs, and log is the natural logarithm.
 * </p>
 */
final class InverseLogarithmicFrequencyAccessibilityEstimator
        extends AbstractFrequencyAccessibilityEstimator {

    /**
     * The point around which rarity starts to matter. For instance, most people probably know all
     * the words with rarity at most 6, so it doesn't matter that "their" appears more frequently
     * than "sheep". We want to differentiate these words from words with rarities more like 20,
     * such as "afforests" (a real word!).
     */
    final double cotail;

    /**
     * The degree to which rectified rarity matters. A value of {@code 0} makes this a constant
     * estimator; a high value means that even moderately rare words basically make no difference
     * to the accessibility estimate.
     */
    final double falloff;

    /**
     * Construct an estimator given the provided word frequencies. All reported frequencies must
     * be non-negative.
     *
     * @param wordFrequencies
     *         a mapping from each word to the frequency at which it appears
     * @param cotail
     *         see {@link #cotail}
     * @param falloff
     *         see {@link #falloff}
     * @throws IllegalArgumentException
     *         if {@code wordFrequencies} contains a negative value
     */
    InverseLogarithmicFrequencyAccessibilityEstimator(
            Map<String, Double> wordFrequencies, double cotail, double falloff) {
        super(wordFrequencies);
        this.cotail = cotail;
        this.falloff = falloff;
    }

    @Override
    public double accessibility(Puzzle puzzle, Collection<String> solutions) {
        return solutions.stream().mapToDouble(x -> {
            final double rarity = -Math.log(wordFrequency(x));
            final double rectifiedRarity = Math.log1p(Math.exp(rarity - cotail));
            return 1.0 / (1 + rectifiedRarity * falloff);
        }).sum();
    }
}
