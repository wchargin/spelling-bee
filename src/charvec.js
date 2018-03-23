// @flow

import {ntz} from "./util";

/**
 * A character vector is a set of characters in the alphabet, stored as
 * a bit vector. Position `i` (zero-indexed from the LSB) is on in the
 * vector if and only if the character of ordinal `i` (zero-indexed from
 * the start of the alphabet) appears in the set. The alphabet consists
 * of those characters from `ALPHABET_START` to `ALPHABET_END`,
 * inclusive.
 */
export type CharVec = number;

export const ALPHABET_START: number = "a".charCodeAt(0);
export const ALPHABET_END: number = "z".charCodeAt(0);

/**
 * Convert a string to the character vector for its character set. All
 * characters in the provided string _must_ be within the alphabet:
 * i.e., must have code unit at least ALPHABET_START and at most
 * ALPHABET_END.
 */
export function stringToVector(s: string): CharVec {
  let result: number = 0;
  for (let i = 0; i < s.length; i++) {
    const ordinal = s.charCodeAt(i) - ALPHABET_START;
    result |= 1 << ordinal;
  }
  return result;
}

/**
 * Convert a character vector to a representative string with the
 * character set represented by the vector. The resulting string's
 * characters will be distinct and in sorted order.
 */
export function vectorToString(v: CharVec): string {
  const chars: string[] = [];
  while (v) {
    const i = v & -v;
    const ordinal = ntz(i);
    chars.push(String.fromCharCode(ALPHABET_START + ordinal));
    v ^= i;
  }
  return chars.join("");
}
