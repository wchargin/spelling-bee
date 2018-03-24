// @flow

import React from "react";
import {RadioGroup, Radio} from "react-radio-group";

import PuzzleMaster from "./puzzleMaster";
import type {WordData, WordListReference} from "./wordData";

type Props = {
  radioGroupName: string,
  references: $ReadOnlyArray<WordListReference>,
  onChange: (WordData) => void,
};
type WordListState =
  | {|+state: "NOT_LOADED"|}
  | {|+state: "FAILED"|}
  | {|+state: "LOADING"|}
  | {|+state: "PROCESSING"|}
  | {|
      +state: "LOADED",
      +wordData: WordData,
    |};
type State = {
  selectedWordList: ?string,
  desiredWordList: ?string,
  wordLists: {[id: string]: WordListState},
};

function initialWordListState(): WordListState {
  return {
    state: "NOT_LOADED",
  };
}

export default class WordListManager extends React.Component<Props, State> {
  constructor(props: Props) {
    super();
    this.state = {
      selectedWordList: null,
      desiredWordList: null,
      wordLists: {},
    };
  }

  loadFirstIfPresent() {
    if (this.props.references.length > 0) {
      this.selectWordList(this.props.references[0]);
    }
  }

  componentDidMount() {
    this.loadFirstIfPresent();
  }

  componentWillReceiveProps() {
    if (this.props.references.length === 0) {
      this.loadFirstIfPresent();
    }
  }

  getReferenceState(id: string): WordListState {
    return this.state.wordLists[id] || initialWordListState();
  }

  selectWordList(ref: WordListReference) {
    const state = this.getReferenceState(ref.id);
    switch (state.state) {
      case "NOT_LOADED":
      case "FAILED":
        fetch(ref.url)
          .then((resp) => {
            if (!resp.ok) {
              return Promise.reject(resp.statusText);
            } else {
              return resp;
            }
          })
          .then((resp) => resp.text())
          .then((text) => {
            this.processResponse(ref, text);
          })
          .catch((err) => {
            this.processError(ref, err);
          });
        this.setState((state) => ({
          desiredWordList: ref.id,
          wordLists: {
            ...state.wordLists,
            [ref.id]: {state: "LOADING"},
          },
        }));
        break;
      case "LOADING":
      case "PROCESSING":
        this.setState({desiredWordList: ref.id});
        break;
      case "LOADED":
        this.props.onChange(state.wordData);
        this.setState({
          selectedWordList: ref.id,
          desiredWordList: null,
        });
        break;
      default:
        throw new Error(`Unexpected case: ${state.state}`);
    }
  }

  processResponse(ref: WordListReference, text: string) {
    this.setState((state) => {
      const wordLists = {
        ...state.wordLists,
        [ref.id]: {state: "PROCESSING"},
      };
      return {wordLists};
    });
    new Promise((resolve, reject) => {
      const words = text.split("\n").filter((x) => x.length > 0);
      const puzzleMaster = new PuzzleMaster(words);
      resolve({
        id: ref.id,
        name: ref.name,
        words,
        puzzleMaster,
      });
    }).then((wordData: WordData) => {
      this.props.onChange(wordData);
      this.setState((state) => {
        const wordLists = {
          ...state.wordLists,
          [ref.id]: {state: "LOADED", wordData},
        };
        if (state.desiredWordList === ref.id) {
          return {
            selectedWordList: ref.id,
            desiredWordList: null,
            wordLists,
          };
        } else {
          return {wordLists};
        }
      });
    });
  }

  processError(ref: WordListReference, err: string) {
    console.error("Error fetching word list:", err);
    this.setState((state) => {
      const wordLists = {
        ...state.wordLists,
        [ref.id]: {state: "FAILED"},
      };
      if (state.desiredWordList === ref.id) {
        return {
          desiredWordList: null,
          wordLists,
        };
      } else {
        return {wordLists};
      }
    });
  }

  render() {
    return (
      <div>
        <h2>Select word list</h2>
        <RadioGroup
          name={this.props.radioGroupName}
          selectedValue={this.state.selectedWordList}
          onChange={(e) => {
            const ref = this.props.references.find((x) => x.id === e);
            if (ref == null) {
              throw new Error(`dangling reference: ${e}`);
            }
            this.selectWordList(ref);
          }}
        >
          {this.props.references.map((ref) => {
            const state = this.getReferenceState(ref.id);
            const inProgress =
              state.state === "LOADING" || state.state === "PROCESSING";
            return (
              <label key={ref.id} style={{display: "block"}}>
                <Radio value={ref.id} disabled={inProgress} />
                {ref.name}
                {(() => {
                  const state = this.getReferenceState(ref.id).state;
                  switch (state) {
                    case "LOADING":
                      return <strong> (loading)</strong>;
                    case "PROCESSING":
                      return <strong> (processing)</strong>;
                    case "FAILED":
                      return <strong> (failed to load)</strong>;
                    default:
                      return null;
                  }
                })()}
              </label>
            );
          })}
        </RadioGroup>
      </div>
    );
  }
}
