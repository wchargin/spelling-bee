#!/usr/local/bin/MathematicaScript -script
(* Mathematica script to batch-compute word frequencies *)

(*
 * words.csv should be a plain text file containing one word per line.
 * Mathematica returns a list of singleton lists, so we extract the
 * unique word on each line.
 *)
words = First /@ Import["words.csv"];

(*
 * The WordFrequencyData function takes a while to run. To avoid putting
 * all our eggs in one basket, we'll chunk the data and write it after
 * each chunk (in case the job gets killed or something).
 *)
chunkSize = 100;
wordCount = Length@words;
chunkCount = Ceiling[wordCount / chunkSize];
chunks = Table[
    words[[(i - 1) * chunkSize + 1 ;; Min[i * chunkSize, wordCount]]],
    {i, 1, chunkCount}
];

(*
 * Processing the chunk at a given index involves computing the
 * frequency data, and writing it to the file with a short header. We
 * re-open streams every time, just to be safe. (This doesn't run
 * anywhere near fast enough for that to be a problem.)
 *)
processChunk[idx_] := Module[{chunk, out, data},
    chunk = chunks[[idx]];
    out = OpenAppend["out.csv"];
    WriteString[out,
        "# chunk " <> ToString[idx] <> " of " <> ToString[chunkCount] <> "\n"];
    data = List@@@Normal@(WordFrequencyData@chunk /. Missing[_] -> 0);
    Export[out, data, "CSV"];
    WriteString[out, "\n"];  (* finish last line; this is not a blank line *)
    Close[out]
];

(* Now, sequentially process the chunks. *)
processChunk /@ Range@Length@chunks;
