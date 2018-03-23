// @flow

import type {CharVec} from "./charvec";
import {stringToVector} from "./charvec";
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
});
