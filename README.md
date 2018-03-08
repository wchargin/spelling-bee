# spelling-bee

Generate, solve, and typeset “Spelling Bee” word puzzles, as created by
Frank Longo.

A “Spelling Bee” puzzle traditionally has one _required letter_ and six
_optional letters_—collectively, the _pot_. The goal is to form words of
at least five letters subject to the following constraints: a word is
valid if and only if it includes the required letter and does not
include any letters other than those in the pot. Repetition of letters
is allowed. Each word is worth one point, regardless of length, unless
it includes all seven letters from the pot; in this case, the word is
called a _bingo_ and is worth three points. Every “Spelling Bee” puzzle
admits at least one bingo.

For instance, the puzzle with required letter `G` and optional letters
`CENORV` has the following solutions, according to my word list:

  - convergence (3)
  - converge (3)
  - greengrocer (1)
  - governor (1)
  - govern (1)
  - evergreen (1)
  - revenge (1)
  - engorge (1)
  - groove (1)
  - grocer (1)
  - goner (1)
  - grove (1)
  - greener (1)
  - renege (1)
  - eggnog (1)
  - green (1)
  - roger (1)
  - verge (1)
  - gorge (1)
  - genre (1)

The maximum possible score for this puzzle is thus 24. (Again, this is
according to my word list; I think that the word list used by the
original author of these puzzles is larger than mine.)

Typically, puzzles are published with _rating thresholds_. The three
ratings are “good”, “excellent”, and “genius”, and each rating requires
a certain number of points to reach.

The above puzzle was assigned rating thresholds of 8, 14, and 20,
respectively, so we render it as follows:

![A rendering of the above puzzle, with required letter “G” and optional letters “C, E, N, O, R, V”](https://user-images.githubusercontent.com/4317806/37176155-5d17205c-22d0-11e8-9953-5f8f8f008056.png)

This repository includes code to
  - generate all valid puzzles, given a word list;
  - solve any particular puzzle;
  - estimate reasonable ranking thresholds for a puzzle, given training
    data in the form of ranking thresholds for existing puzzles; and
  - render puzzles as Ti*k*Z pictures in a print-friendly format.

## Building

You’ll need Gradle with Java 8+ to generate and solve puzzles, and a
LaTeX distribution to render them.

To build:

```shell
$ ./gradlew build
```

To run tests:

```shell
$ ./gradlew test
```

If tests pass, you’ll see “BUILD SUCCESSFUL”; if there are failures,
you’ll be given a `file://` link to a report indicating the failures.

Built classes are placed into `build/classes/main`. Run an application
with

```shell
$ java -cp build/classes/main <MainClassName> [ <args> ... ]
```

Most scripts require word lists, word frequency data, historical puzzle
data, or some combination of these. These can be found in `data/words/`,
`data/frequencies/`, and `data/ratings/`, respectively.

## Full pipeline

To generate and render a set of puzzles:

```shell
$ ./gradlew build
$ java -cp build/classes/main/ PuzzleGenerator \
>     data/words/words-ubuntu-wamerican-7.1-1.txt \
>     data/frequencies/frequencies-ubuntu-wamerican-7.1-1.csv \
>     data/ratings/ratings-20160103-20180304.csv \
>     52 \
>     tex/puzzles52.tex \
>     ;
$ (cd tex; pdflatex puzzles52.tex >/dev/null)
$ file tex/puzzles52.pdf
tex/puzzles52.pdf: PDF document, version 1.5
```

Here, the argument `52` to the Java program is the number of weeks’
worth of puzzles that you want to generate. Each week includes three
puzzles, which get harder as the week goes on.

The last argument to the Java program is the output filename. This
invocation creates a file `tex/puzzles52.pdf`, with one page per week.
After building, the whole process takes about ten seconds on my laptop,
which is a mid-2014 Thinkpad T440s with an Intel i5-4300U CPU @ 1.90GHz.
(Most of the time is spent in `pdflatex`.)

To solve a single puzzle:

```shell
$ java -cp build/classes/main/ PuzzleSolver \
>     data/words/words-ubuntu-wamerican-7.1-1.txt g cenorv
```

where `g` is the required letter and `cenorv` are the optional letters.
These are case-insensitive. Results will be printed to stdout in a
human-readable format.

## Benchmarks

This implementation can generate and solve all valid puzzles reasonably
quickly—under two seconds on my laptop:

```shell
$ ./gradlew build
$ time java -cp build/classes/main/ PuzzleBenchmark \
>     data/words/words-ubuntu-wamerican-7.1-1.txt
Reading dictionary...
Compiling generic puzzle data...
Puzzle count: 54733
Solving puzzles...
Solved.
Proof: 153564901

real    0m1.875s
user    0m6.456s
sys     0m0.152s
```

Using the Red Hat dictionary takes a bit longer:

```shell
$ ./gradlew build
$ time java -cp build/classes/main/ PuzzleBenchmark \
>     data/words/words-redhat-words-3.0-22.el7.noarch.txt
Reading dictionary...
Compiling generic puzzle data...
Puzzle count: 173040
Solving puzzles...
Solved.
Proof: -312488942

real    0m10.058s
user    0m35.660s
sys     0m0.712s
```

This is simply because the dictionary is a lot larger;

```shell
$ wc -l data/words/words-*
 479828 data/words/words-redhat-words-3.0-22.el7.noarch.txt
  99171 data/words/words-ubuntu-wamerican-7.1-1.txt
 578999 total
```

See `data/words/README.md` for a discussion of the differences between
the two word lists, and why I prefer the Ubuntu word list.

## Performance characteristics

The generation and solving algorithms proceed in two phases, both of
which live in `PuzzleMaster.java`. First, we preprocess the word list
file to index words by their _character sets_: e.g., the character set
for both _adage_ and _gagged_ is `{a, d, e, g}`. During this
preprocessing, we reject words with character sets of size greater than
7—as they could never be formed in a puzzle—and we mark down those
character sets of size exactly 7 as being potential pots of letters.
Once this preprocessing is complete, we can trivially generate all valid
puzzles: for each valid pot, just mark any particular letter as
required.

Second, we use this preprocessed data to solve a particular puzzle. For
a fixed pot size (i.e., 7), We can do this in time linear in the number
of solutions to the puzzle: take the power set of the puzzle’s optional
letters, and for each subset *S* use the precomputed tables to look up
all words with character set *S* U {*r*}, where *r* is the required
letter. Taking the union of these 128 sets completes the algorithm. Note
that this is exponential in the pot size, but the pot size is a small
constant and so this runs quite quickly. A trivial alternate algorithm
has time linear in the length of the full dictionary and
multiplicatively linear in the pot size, but I expect that it would be
slower empirically: the number of solutions to a puzzle is far smaller
than the number of words in the word list.

Assuming a fixed alphabet whose size is not greater than the width of a
machine word—such as the English alphabet, for which 26 < 64 and even 26
< 32—these algorithms admit particularly efficient implementations by
packing a character set into a single machine word. Set intersection,
union, and difference operations are all effectively free in this model.
We use this implementation: see the `characterVector` method in
`Puzzle.java`.

## Rating threshold estimation

The means by which puzzles should be assigned rating thresholds are not
clear. I’ve attempted to reverse-engineer the rating scheme used by the
114 most recent puzzles published by the _New York Times Magazine_ (from
2016-01-03 to 2018-03-04). Here are the results.

The first observation is that the rating thresholds for almost every
puzzle follow an arithmetic progression: that is, the difference between
“good” and “excellent” is the same as the difference between “excellent”
and “genius”. The puzzle published on 2016-09-11 is an exception, with
thresholds 7, 12, and 19. Ignoring this outlier, it suffices to estimate
two quantities: the “good” threshold, and the per-level delta.

It would be natural for these quantities to be somehow tied to how many
words the user is expected to find in a puzzle—call this the
_accessibility_ of the puzzle. We assume that the “good” threshold and
the per-level deltas are affine functions of some accessibility metric,
and so it remains only to define a good metric. We will propose various
metrics and evaluate them based on their 50%-cross-validated mean
squared output-rounded error (“crossMSRE”). By “output-rounded” we mean
that we compute the accessibility, pass it through the regression, and
round the result to the nearest integer before comparing it to the
training datum’s value; we round because it doesn’t make sense to
report, say, “you need to score at least 7.3 words to be ‘good’ at this
puzzle” as a final answer.

Two obvious first choices are the number of valid solutions and the
total score (which may differ non-trivially if there are multiple bingos
in a puzzle). These work reasonably well, with crossMSRE of 0.9649 for
the “good” threshold and 0.8860 for the per-level delta.

A second observation is that not all words should be treated equally.
It’s probably more likely that a player will recognize a word like
“sheep” than a word like “afforests”. Using word-frequency data, we can
ascribe to each word a _rarity_, given by −log(*p*), where *p* is the
word’s frequency in natural English text and log is the natural
logarithm. The most common words, like “their”, have rarities around 6;
words like “afforests” have rarities around 20. In the middle we have
words like “sheep”, with a rarity of 11.4—but just because “sheep” is
nominally rarer than “their” doesn’t mean that it’s significantly less
likely for a player to think of it; it’s still common enough. We
therefore introduce two tuning parameters: the cotail and the falloff.
Words with rarity values not much larger than the _cotail_ are all
considered approximately equal. Words significantly past the cotail fall
off harmonically according to the _falloff_. Most precisely, a word’s
accessibility is given by

> A = 1 / (1 + *f* · softplus(*r* − *c*)),

where *f* is the falloff, *c* is the cotail, and *r* is the word’s
rarity. The softplus function is given by

> softplus(*z*) = log(1 + exp(*z*)).

Using this formula, we say that a puzzle’s accessibility is the sum of
the accessibilities of the words in its solution set. When tuned, this
yields slightly better crossMSRE than before: for the “good” threshold,
0.9386 vs. 0.9649; and for the per-level delta, 0.8333 vs. 0.8860. This
is the model used in the final puzzle generator.

There is likely opportunity for further refinement. A natural next step
would be to lemmatize words—if you see “converge”, the probability that
you’ll also see “convergence” increases significantly, so bucketing
words together seems like a useful construct. This could be implemented
reasonably easily with Stanford’s Core NLP library. Transcribing
additional training data might help, too; it’s just a bit tedious, so I
stopped at just over two years’ worth of puzzles.

To pit the accessibility estimators against each other, check out the
`Calibrator` class. You can run it as follows:

```shell
$ java -cp build/classes/main/ Calibrator \
>     data/words/words-ubuntu-wamerican-7.1-1.txt \
>     data/frequencies/frequencies-ubuntu-wamerican-7.1-1.csv \
>     data/ratings/ratings-20160103-20180304.csv \
>     ;
```

This will evaluate each estimator on three quantities (”good”, “genius”,
and per-level delta), reporting results to stdout, and also output a
data file to `/tmp`, which you can easily plot with `gnuplot` or
similar. It should be very easy to add new estimators, and reasonably
easy to add new quantities.
