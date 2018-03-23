// @flow

import {
  ALPHABET_START,
  ALPHABET_END,
  stringToVector,
  vectorToString,
} from "./charvec";
import {popcnt} from "./util";

describe("charvec", () => {
  describe("stringToVector", () => {
    it("works on a simple case", () => {
      expect(stringToVector("adeg")).toEqual(0b1011001);
    });

    it("works on another simple case", () => {
      expect(stringToVector("aehj")).toEqual(0b1010010001);
    });

    it("works when the input is unsorted and has duplicate characters", () => {
      expect(stringToVector("gagged")).toEqual(0b1011001);
    });

    it("works on the empty string", () => {
      expect(stringToVector("")).toEqual(0);
    });

    it("works on the full alphabet", () => {
      const alphabet = Array(ALPHABET_END - ALPHABET_START + 1)
        .fill(null)
        .map((x, i) => String.fromCharCode(ALPHABET_START + i))
        .join("");
      const output = stringToVector(alphabet);
      const expected = (1 << (ALPHABET_END - ALPHABET_START + 1)) - 1;
      expect(output).toEqual(expected);
    });
  });

  describe("vectorToString", () => {
    it("works on the empty input", () => {
      expect(vectorToString(0)).toEqual("");
    });

    it("works on a singleton input", () => {
      expect(vectorToString(0b1)).toEqual("a");
    });

    it("works on another singleton input", () => {
      expect(vectorToString(0b1000)).toEqual("d");
    });

    it("works on a simple case", () => {
      expect(vectorToString(0b101)).toEqual("ac");
    });

    it("yields strings of the correct length (up to 0xFFFF)", () => {
      for (let i = 0; i <= 0xffff; i++) {
        const output = vectorToString(i);
        const actual = output.length;
        const expected = popcnt(i);
        if (actual !== expected) {
          const ctx = {input: i, output: output};
          expect({...ctx, value: actual}).toEqual({...ctx, value: expected});
        }
      }
    });
  });

  it("stringToVector is a left identity for vectorToString", () => {
    for (let i = 0; i <= 0xffff; i++) {
      const j = stringToVector(vectorToString(i));
      if (i !== j) {
        expect(j).toEqual(i);
      }
    }
  });
});
