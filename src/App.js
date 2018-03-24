// @flow

import React from "react";

import type {WordData} from "./wordData";
import standardWordLists from "./standardWordLists";
import WordListManager from "./WordListManager";
import WordInspector from "./WordInspector";

type State = {|
  wordData: ?WordData,
|};

export default class App extends React.Component<{}, State> {
  constructor() {
    super();
    this.state = {
      wordData: null,
    };
  }

  render() {
    return (
      <div style={{margin: "auto", maxWidth: 800}}>
        <header>
          <h1>Spelling Bee puzzle tools</h1>
        </header>
        <WordListManager
          radioGroupName="word_list"
          references={standardWordLists()}
          onChange={(wordData) => {
            this.setState({wordData});
          }}
        />
        <WordInspector wordData={this.state.wordData} />
      </div>
    );
  }
}
