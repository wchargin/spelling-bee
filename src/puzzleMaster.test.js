// @flow

import type {CharVec} from "./charvec";
import {stringToVector, vectorToString} from "./charvec";
import PuzzleMaster from "./puzzleMaster";

describe("PuzzleMaster", () => {
  const words = () => [
    "abracadabrazy",
    "abrac",
    "barca",
    "barbar",
    "zzzzz",
    "zzzzzzzz",
    "lengthened",
    "lengthen",
    "then", // too short
    "vis-a-vis", // outside the alphabet
    "Caps", // outside the alphabet
    "abcdefgh", // too many distinct letters
  ];

  describe("constructor", () => {
    it("filters out invalid words", () => {
      const instance = new PuzzleMaster(words());
      expect(instance.words.slice().sort()).toEqual(
        [
          "abracadabrazy",
          "abrac",
          "barca",
          "barbar",
          "zzzzz",
          "zzzzzzzz",
          "lengthened",
          "lengthen",
        ].sort()
      );
    });

    it("identifies pots", () => {
      const instance = new PuzzleMaster(words());
      expect(instance.pots).toEqual(
        new Set([stringToVector("abrcdzy"), stringToVector("lengthd")])
      );
    });

    it("partitions words by vector", () => {
      function sorted<T>(xs: Iterable<T>): T[] {
        return Array.from(xs)
          .slice()
          .sort();
      }
      function really<T>(value: ?T): T {
        if (value == null) {
          throw new Error(String(value));
        } else {
          return value;
        }
      }
      const instance = new PuzzleMaster(words());
      const actual: Map<CharVec, string[]> = instance.wordsByVector;
      const expected: Map<CharVec, string[]> = new Map([
        [stringToVector("abrcdzy"), ["abracadabrazy"]],
        [stringToVector("abrc"), ["abrac", "barca"]],
        [stringToVector("bar"), ["barbar"]],
        [stringToVector("z"), ["zzzzz", "zzzzzzzz"]],
        [stringToVector("lengthd"), ["lengthened"]],
        [stringToVector("length"), ["lengthen"]],
      ]);
      expect(sorted(actual.keys())).toEqual(sorted(expected.keys()));
      for (const key of expected.keys()) {
        expect(sorted(really(actual.get(key)))).toEqual(
          sorted(really(expected.get(key)))
        );
      }
    });
  });

  describe("#solve", () => {
    it("solves a simple puzzle", () => {
      const instance = new PuzzleMaster(words());
      const puzzle = {
        required: stringToVector("c"),
        optional: stringToVector("abrdzy"),
      };
      const actual = instance.solutionsTo(puzzle);
      const expected = new Set(["abracadabrazy", "abrac", "barca"]);
      expect(actual).toEqual(expected);
    });

    it("solves another simple puzzle", () => {
      const instance = new PuzzleMaster(words());
      const puzzle = {
        required: stringToVector("e"),
        optional: stringToVector("lngthd"),
      };
      const actual = instance.solutionsTo(puzzle);
      const expected = new Set(["lengthened", "lengthen"]);
      expect(actual).toEqual(expected);
    });

    it("solves a puzzle with no solutions", () => {
      const instance = new PuzzleMaster(words());
      const puzzle = {
        required: stringToVector("c"),
        optional: stringToVector("abdleg"),
      };
      const actual = instance.solutionsTo(puzzle);
      const expected = new Set([]);
      expect(actual).toEqual(expected);
    });

    it("solves a puzzle with never-before-seen letters", () => {
      const instance = new PuzzleMaster(words());
      const puzzle = {
        required: stringToVector("q"),
        optional: stringToVector("jkouwx"),
      };

      // Self-check...
      {
        const pot = puzzle.required | puzzle.optional;
        for (const word of words()) {
          const overlap = stringToVector(word) & pot;
          if (overlap) {
            const overlapString = vectorToString(overlap);
            throw new Error(
              `word "${word}" contains forbidden letters: "${overlapString}"`
            );
          }
        }
      }

      const actual = instance.solutionsTo(puzzle);
      const expected = new Set([]);
      expect(actual).toEqual(expected);
    });
  });
});
