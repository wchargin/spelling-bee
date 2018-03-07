import java.util.Collection;

/**
 * <p>
 * A Spelling Bee puzzle represented with character vectors.
 * </p>
 * <p>
 * The <em>character vector</em> for a set of characters is the integer whose {@code i}th bit is
 * set exactly if the {@code i}th letter of the alphabet is included in the character set. All
 * indices are zero-based, and the least significant bits are numbered first. The alphabet is fixed
 * to the 26 lowercase Latin characters as used in the English alphabet. For instance, the
 * character vector for {@code {'a', 'd', 'e', 'g'}} is {@code 0b1011001}, or {@code 89}.
 * </p>
 * <p>
 * The character vector for a word is defined to be the character vector for the set of
 * characters in that string. For instance, the character vector for {@code "adage"} is {@code 89},
 * which is also the character vector for {@code "gagged"}. Strings like {@code "France"} and
 * {@code "vis-à-vis"} do not have character vectors, because they contain characters outside the
 * alphabet.
 * </p>
 * <p>
 * {@code Puzzle}s are immutable and have logical identity.
 * </p>
 */
final class Puzzle {

    /**
     * The minimum number of letters that a word must have for it to be considered acceptable in
     * this game.
     */
    static final int MINIMUM_WORD_LENGTH = 5;

    /**
     * The size of the "pot" of letters that the player is allowed to use to form words.
     */
    static final int POT_SIZE = 7;

    /**
     * The point value of a "bingo" (a word that uses every letter in the pot). A non-bingo is
     * always worth one point.
     */
    static final int BINGO_SCORE = 3;

    /**
     * The {@linkplain Puzzle character vector} corresponding to the set of all letters that
     * <em>may</em> be used to construct a word in this puzzle, including those that
     * <em>must</em> be used.
     *
     * @see #requiredVector
     */
    final int potVector;

    /**
     * The {@linkplain Puzzle character vector} corresponding to the set of all letters that
     * <em>must</em> be used in any word that is valid for this puzzle. This should be a subvector
     * of {@link #potVector}: i.e., it should be true that
     * {@code (requiredVector & potVector) == potVector}.
     *
     * @see #potVector
     */
    final int requiredVector;

    /**
     * Construct a {@code Puzzle} with the given {@link #potVector} and {@link #requiredVector}.
     * Behavior is undefined if {@code requiredVector} is not a subvector of {@code potVector}.
     *
     * @param potVector
     *         the {@code potVector} for this puzzle
     * @param requiredVector
     *         {@code requiredVector} for this puzzle
     */
    Puzzle(int potVector, int requiredVector) {
        assert (requiredVector & potVector) == requiredVector : String.format(
                "requiredVector (%d) must be a subvector of potVector (%d)",
                requiredVector, potVector);
        this.potVector = potVector;
        this.requiredVector = requiredVector;
    }

    /**
     * <p>
     * Convert a set of characters, represented as an array, to its corresponding
     * {@linkplain Puzzle character vector}.
     * </p>
     * <p>
     * This is a right inverse for {@link #characterUnvector(int)}, (up to type conversions with
     * {@link String#String(char[])} and {@link String#toCharArray()}), but not a left inverse:
     * order and multiplicity are not preserved.
     * </p>
     *
     * @param cs
     *         a character array, which must contain only characters from {@code 'a'} to
     *         {@code 'z'}, inclusive; duplicate characters are okay
     * @return a bit vector as described above
     */
    static int characterVector(char[] cs) {
        int result = 0;
        for (char c : cs) {
            int ordinal = c - 'a';
            result |= (1 << ordinal);
        }
        return result;
    }

    /**
     * <p>
     * Convert a {@linkplain Puzzle character vector} to a string that has that vector as its
     * character vector. The string will be as short as possible: its length will be the number
     * of set bits in {@code vector}, so each character will appear at most once. The order of
     * the characters in the string is not specified.
     * </p>
     * <p>
     * This is a left inverse for {@link #characterUnvector(int)}, (up to type conversions with
     * {@link String#String(char[])} and {@link String#toCharArray()}), but not a right inverse:
     * order and multiplicity are not preserved.
     * </p>
     *
     * @param vector
     *         an arbitrary bit vector
     * @return a string as described above
     */
    static String characterUnvector(int vector) {
        char[] cs = new char[Integer.bitCount(vector)];
        for (int i = 0; i < cs.length; i++) {
            int mask = Integer.lowestOneBit(vector);
            cs[i] = (char) ('a' + Integer.numberOfTrailingZeros(mask));
            vector ^= mask;
        }
        return new String(cs);
    }

    /**
     * <p>
     * Compute the score of solution to a puzzle. Each word in the solution is worth one point,
     * unless it contains {@value #POT_SIZE} distinct letters, in which case it is worth
     * {@value #BINGO_SCORE} points.
     * </p>
     * <p>
     * Words are not checked for validity, but they must have valid
     * {@linkplain Puzzle character vectors}, and so they must contain only characters from the
     * lowercase Latin alphabet.
     * </p>
     *
     * @param words
     *         the set of words to score
     * @return the integer score for the provided collection
     */
    static int score(Collection<String> words) {
        return words.stream().mapToInt(w ->
                Integer.bitCount(characterVector(w.toCharArray())) >= POT_SIZE ?
                BINGO_SCORE : 1
        ).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Puzzle)) return false;
        Puzzle that = (Puzzle) o;
        return this.potVector == that.potVector && this.requiredVector == that.requiredVector;
    }

    @Override
    public int hashCode() {
        return 31 * potVector + requiredVector;
    }

    @Override
    public String toString() {
        return String.format("VectorPuzzle{pot=0x%s, required=0x%s}",
                Integer.toHexString(potVector),
                Integer.toHexString(requiredVector));
    }
}
