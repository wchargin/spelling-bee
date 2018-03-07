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
