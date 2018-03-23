// @flow

import type {CharVec} from "./charvec";

/**
 * A Spelling Bee puzzle. The `required` field is the vector of all
 * characters that are required to be in every word (usually just one).
 * The `optional` field is the vector of all characters that are not
 * required, but are allowed to be in any word. These sets should be
 * disjoint: we should have `required & optional === 0`.
 */
export type Puzzle = {|
  +required: CharVec,
  +optional: CharVec,
|};

/**
 * The minimum number of letters that a word must have for it to be
 * considered acceptable in this game.
 */
export const MINIMUM_WORD_LENGTH: number = 5;

/**
 * The size of the "pot" of letters that the player is allowed to use to
 * form words.
 */
export const POT_SIZE: number = 7;

/**
 * The point value of a "bingo" (a word that uses every letter in the
 * pot). A non-bingo is always worth one point.
 */
export const BINGO_SCORE: number = 3;
