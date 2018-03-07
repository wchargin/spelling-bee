import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class for generating and solving puzzles.
 */
final class PuzzleMaster {

    /**
     * All words that might appear in a puzzle. This includes all words composed only of
     * characters in the alphabet (i.e., the 26 lowercase Latin characters), with length at least
     * {@value Puzzle#MINIMUM_WORD_LENGTH}, and with at most {@value Puzzle#POT_SIZE} distinct
     * letters.
     */
    final Set<String> words;

    /**
     * The {@linkplain #words set of all valid words}, segmented by their
     * {@linkplain #characterVector(char[]) character vectors}. Two words will be in the same
     * bucket if and only if they contain the same sets of characters, not considering
     * multiplicity. The union of all the values equals the full word set.
     */
    final Map<Integer, Set<String>> wordsByVector;

    /**
     * The set of all character vectors that could represent a pot: i.e., the set of all
     * character vectors some permutation of which is a valid word with exactly {@value
     * Puzzle#POT_SIZE} distinct letters.
     */
    final Set<Integer> pots;

    /**
     * The set of all potential puzzles. This is trivially formed from {@link #pots} by choosing
     * each potential required letter for each potential pot.
     */
    final List<Puzzle> puzzles;

    /**
     * <p>
     * Create a {@code PuzzleMaster} using the given set of words as the dictionary. The words in
     * the dictionary may be arbitrary: they need not be valid words, and they may contain
     * characters from outside the alphabet.
     * </p>
     * <p>
     * <strong>Note:</strong> this constructor does non-trivial work; assuming that the length
     * of a word is bounded, the work is linear in the length of {@code allWords}.
     * </p>
     *
     * @param allWords
     *         the collection of valid words
     */
    PuzzleMaster(Collection<String> allWords) {
        words = new HashSet<>();
        wordsByVector = new HashMap<>();
        pots = new HashSet<>();
        puzzles = new ArrayList<>();
        outer:
        for (final String word : allWords) {
            if (word.length() < Puzzle.MINIMUM_WORD_LENGTH) {
                continue;
            }
            for (char c : word.toCharArray()) {
                if (c < 'a' || c > 'z') {
                    continue outer;
                }
            }
            final int vector = Puzzle.characterVector(word.toCharArray());
            final int distinctLetterCount = Integer.bitCount(vector);
            if (distinctLetterCount > Puzzle.POT_SIZE) {
                continue;
            }
            words.add(word);
            wordsByVector.computeIfAbsent(vector, (k) -> new HashSet<>()).add(word);
            if (distinctLetterCount == Puzzle.POT_SIZE) {
                if (pots.add(vector)) {
                    puzzles.addAll(puzzlesForPot(vector));
                }
            }
        }
    }

    /**
     * Construct all puzzles whose pot vector is the given vector and that have exactly one
     * required letter.
     *
     * @param pot
     *         a character vector, which should have at least one bit set
     * @return all {@link Puzzle}s whose {@link Puzzle#potVector} is {@code pot} and whose
     * {@link Puzzle#requiredVector} has exactly one bit set
     */
    static List<Puzzle> puzzlesForPot(int pot) {
        final List<Puzzle> puzzles = new ArrayList<>();
        int decayingPot = pot;
        while (decayingPot != 0) {
            int required = Integer.lowestOneBit(decayingPot);
            decayingPot ^= required;
            puzzles.add(new Puzzle(pot, required));
        }
        return puzzles;
    }

}
