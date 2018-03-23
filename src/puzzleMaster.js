// @flow

import type {CharVec} from "./charvec";
import type {Puzzle} from "./puzzle";
import {ALPHABET_START, ALPHABET_END, stringToVector} from "./charvec";
import {MINIMUM_WORD_LENGTH, POT_SIZE} from "./puzzle";
import {popcnt} from "./util";

export default class PuzzleMaster {
  words: string[];
  wordsByVector: Map<CharVec, string[]>;
  pots: Set<CharVec>;

  constructor(words: string[]) {
    this.words = [];
    this.wordsByVector = new Map();
    this.pots = new Set();
    outer: for (const word of words) {
      if (word.length < MINIMUM_WORD_LENGTH) {
        continue;
      }
      for (let i = 0; i < word.length; i++) {
        const c = word.charCodeAt(i);
        if (c < ALPHABET_START || c > ALPHABET_END) {
          continue outer;
        }
      }
      const vec: CharVec = stringToVector(word);
      const distinctLetterCount: number = popcnt(vec);
      if (distinctLetterCount > POT_SIZE) {
        continue;
      }
      this.words.push(word);
      let bucket = this.wordsByVector.get(vec);
      if (bucket == null) {
        bucket = [];
        this.wordsByVector.set(vec, bucket);
      }
      bucket.push(word);
      if (distinctLetterCount === POT_SIZE) {
        this.pots.add(vec); // may be redundant; no problem
      }
    }
  }

  solutionsTo(puzzle: Puzzle): Set<string> {
    const result: Set<string> = new Set();
    this._addSolutions(result, puzzle.required, puzzle.optional);
    return result;
  }

  _addSolutions(result: Set<string>, required: CharVec, optional: CharVec) {
    if (!optional) {
      const bucket = this.wordsByVector.get(required);
      if (bucket) {
        for (const solution of bucket) {
          result.add(solution);
        }
      }
    } else {
      const oneHot: CharVec = optional & -optional;
      const nextOptional: CharVec = optional ^ oneHot;
      this._addSolutions(result, required, nextOptional);
      this._addSolutions(result, required | oneHot, nextOptional);
    }
  }
}
