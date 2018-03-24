// @flow

import type {WordListReference} from "./wordData";

if (process.env.PUBLIC_URL == null) {
  throw new Error(
    "Expected process.env.PUBLIC_URL to be set, but found: " +
      String(process.env.PUBLIC_URL)
  );
}
const WORDS_PATH = `${process.env.PUBLIC_URL}/static/words`;

export default function standardWordLists(): WordListReference[] {
  return [
    {
      id: "ubuntu-wamerican-7.1-1",
      name: "Standard dictionary (99K words)",
      url: `${WORDS_PATH}/words-ubuntu-wamerican-7.1-1.txt`,
    },
    {
      id: "redhat-words-3.0-22.el7.noarch.txt",
      name: "Large dictionary (479K words, some questionable)",
      url: `${WORDS_PATH}/words-redhat-words-3.0-22.el7.noarch.txt`,
    },
  ];
}
