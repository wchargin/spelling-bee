// @flow

import React from "react";

import type {WordData} from "./wordData";
import {ALPHABET_START, ALPHABET_END, stringToVector} from "./charvec";
import {BINGO_SCORE, POT_SIZE} from "./puzzle";
import {popcnt} from "./util";

type Props = {
  +wordData: ?WordData,
};
type State = {|
  required: string,
  optional: string,
  revealed: boolean,
|};

function sanitize(s: string) {
  return Array.from(s)
    .map((c) => {
      c = c.toLowerCase();
      const ordinal = c.charCodeAt(0);
      return ordinal < ALPHABET_START || ordinal > ALPHABET_END ? null : c;
    })
    .filter((c) => c != null)
    .join("");
}

export default class SolutionViewer extends React.Component<Props, State> {
  constructor() {
    super();
    this.state = {
      required: "",
      optional: "",
      revealed: false,
    };
  }

  render() {
    return (
      <div>
        <h2>Puzzle solver</h2>
        <div>
          <h3>Specify puzzle</h3>
          <ul>
            <li>
              <label>
                Required letter(s):{" "}
                <input
                  value={this.state.required}
                  onChange={(e) => {
                    this.setState({
                      required: sanitize(e.target.value),
                      revealed: false,
                    });
                  }}
                />{" "}
                (usually just one letter)
              </label>
            </li>
            <li>
              <label>
                Optional letter(s):{" "}
                <input
                  value={this.state.optional}
                  onChange={(e) => {
                    this.setState({
                      optional: sanitize(e.target.value),
                      revealed: false,
                    });
                  }}
                />
              </label>
            </li>
          </ul>
          {this.renderSolutions()}
        </div>
      </div>
    );
  }

  renderSolutions() {
    if (!this.state.revealed) {
      return (
        <button
          type="submit"
          onClick={() => {
            this.setState({revealed: true});
          }}
          disabled={this.props.wordData == null}
        >
          Reveal solutions
        </button>
      );
    }
    if (!this.props.wordData) {
      return null;
    }
    const {puzzleMaster} = this.props.wordData;
    const puzzle = {
      required: stringToVector(this.state.required),
      optional: stringToVector(this.state.optional),
    };
    const solutions = Array.from(puzzleMaster.solutionsTo(puzzle));
    function cmp(x: string, y: string): 1 | 0 | -1 {
      const bingoX = popcnt(stringToVector(x)) >= POT_SIZE;
      const bingoY = popcnt(stringToVector(y)) >= POT_SIZE;
      if (bingoX !== bingoY) {
        return bingoX ? -1 : 1;
      }
      return x > y ? 1 : x < y ? -1 : 0;
    }

    function score(x: string) {
      return popcnt(stringToVector(x)) >= POT_SIZE ? BINGO_SCORE : 1;
    }

    function solutionEntry(word: string) {
      const name = score(word) > 1 ? <strong>{word}</strong> : word;
      const definitionLink = (
        <a
          href={`https://www.thefreedictionary.com/${word}`}
          target="_blank"
          rel="noopener noreferrer"
        >
          {name}
        </a>
      );
      return (
        <li key={word}>
          {definitionLink} ({score(word)})
        </li>
      );
    }

    const totalScore = solutions
      .map((x) => score(x))
      .reduce((x, y) => x + y, 0);
    return (
      <div>
        <h3>Solutions</h3>
        {solutions.length > 0 ? (
          <p>Words ({solutions.length} total):</p>
        ) : (
          <p>No valid solutions.</p>
        )}
        <ul style={{lineHeight: 1.2}}>
          {solutions.sort(cmp).map((word) => solutionEntry(word))}
        </ul>
        <p>Total score: {totalScore}.</p>
      </div>
    );
  }
}
