// @flow

import {ntz, popcnt} from "./util";

describe("util", () => {
  const testRanges = [
    {
      name: "[0, 0xFFFF]",
      create: () =>
        Array(0x10000)
          .fill(null)
          .map((_, i) => i),
    },
    {
      name: "[0, 0xFFFF] << 16",
      create: () =>
        Array(0x10000)
          .fill(null)
          .map((_, i) => i << 16),
    },
    {
      name: "([0, 0xFFFF] << 16) | 0xFFFF",
      create: () =>
        Array(0x10000)
          .fill(null)
          .map((_, i) => (i << 16) | 0xffff),
    },
  ];

  function compare(reference, functionUnderTest) {
    testRanges.forEach((range) => {
      it(`is correct on ${range.name}`, () => {
        for (const input of range.create()) {
          const expected = reference(input);
          const actual = functionUnderTest(input);
          if (actual !== expected) {
            expect({input, output: actual}).toEqual({input, output: expected});
          }
        }
      });
    });
  }

  describe("popcnt", () => {
    function popcntNaive(x: number): number {
      let count = 0;
      while (x) {
        if (x & 1) {
          count++;
        }
        x >>>= 1;
      }
      return count;
    }
    compare(popcntNaive, popcnt);
  });

  describe("ntz", () => {
    function ntzNaive(x: number): number {
      if ((x | 0) === 0) {
        return 32;
      }
      let count = 0;
      while (!(x & 1)) {
        count++;
        x >>>= 1;
      }
      return count;
    }
    compare(ntzNaive, ntz);
  });
});
