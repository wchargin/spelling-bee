// @flow

import React from "react";

import type {WordData} from "./wordData";
import {ALPHABET_START, ALPHABET_END, stringToVector} from "./charvec";
import {MINIMUM_WORD_LENGTH, POT_SIZE} from "./puzzle";
import {popcnt} from "./util";

type Props = {
  +wordData: ?WordData,
};
type State = {|
  word: string,
|};

export default class WordInspector extends React.Component<Props, State> {
  constructor() {
    super();
    this.state = {
      word: "",
    };
  }

  render() {
    return (
      <div>
        <h2>Word lookup</h2>
        <p>
          <label>
            Look up word:{" "}
            <input
              value={this.state.word}
              onChange={(e) => {
                this.setState({word: e.target.value});
              }}
            />
          </label>
        </p>
        {this.renderWordInfo()}
      </div>
    );
  }

  renderWordInfo() {
    const word = this.state.word.trim();
    if (!word) {
      return null;
    }
    if (!this.props.wordData) {
      return (
        <div>
          <em>Cannot show data: no active word list.</em>
        </div>
      );
    }
    const {words, puzzleMaster} = this.props.wordData;
    if (!words.includes(word)) {
      return (
        <p>
          This word <strong>does not appear</strong> in the word list.
        </p>
      );
    }
    const elements = [];
    elements.push(
      <li key="appearance">
        This word <strong>appears</strong> in the base word list.
      </li>
    );
    if (puzzleMaster.words.includes(word)) {
      elements.push(
        <li key="validity">
          Furthermore, this word is potentially valid in a puzzle.
        </li>
      );
    } else {
      elements.push(
        <li key="validity">
          However, this word is <strong>not considered valid</strong> as a
          puzzle solution.
        </li>
      );
    }
    if (
      Array.from(word).some((x) => {
        const cc = x.charCodeAt(0);
        return cc < ALPHABET_START || cc > ALPHABET_END;
      })
    ) {
      elements.push(
        <li key="invalid-characters">
          This word has characters outside of the approved alphabet (the 26
          lowercase Latin characters only).
        </li>
      );
    } else {
      if (word.length < MINIMUM_WORD_LENGTH) {
        const noun = word.length === 1 ? "letter" : "letters";
        const nounPhrase = (
          <strong>
            {word.length} {noun}
          </strong>
        );
        elements.push(
          <li key="too-short">
            This word has only {nounPhrase}, and is thus too short to count as a
            valid word.
          </li>
        );
      }
      const vec = stringToVector(word);
      const distinctLetterCount = popcnt(vec);
      const noun = distinctLetterCount === 1 ? "letter" : "letters";
      const nounPhrase = (
        <strong>
          {distinctLetterCount} distinct {noun}
        </strong>
      );
      if (distinctLetterCount < POT_SIZE) {
        elements.push(<li key="letter-count">This word has {nounPhrase}.</li>);
      } else if (distinctLetterCount > POT_SIZE) {
        elements.push(
          <li key="letter-count">
            This word has {nounPhrase}, and so it can never appear in any
            puzzle.
          </li>
        );
      } else {
        elements.push(
          <li key="letter-count">
            This word has {nounPhrase}, using all possible letters in any puzzle
            in which it should appear.
          </li>
        );
      }
    }
    return (
      <div>
        <p>Results:</p>
        <ul>{elements}</ul>
      </div>
    );
  }
}
