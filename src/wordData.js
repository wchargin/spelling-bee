// @flow

import type PuzzleMaster from "./puzzleMaster";

export type WordListReference = {|
  +id: string,
  +name: string,
  +url: string,
|};

export type WordData = {|
  +id: string,
  +name: string,
  +words: string[],
  +puzzleMaster: PuzzleMaster,
|};
