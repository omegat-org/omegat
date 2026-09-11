# Exotic numerals demo project

A minimal OmegaT project (English to German, `match_numbers` enabled in `omegat.project`)
illustrating how far the value-based fuzzy match number handling of SF-465
reaches across the numeral systems encoded in Unicode. Every example was
verified against `NumeralValueParser` before it went into this project.

Open the project folder in OmegaT and step through the segments of
`source/exotic-numerals.txt`: each segment has a translation-memory twin in
`tm/exotic-numerals-demo.tmx` that differs only in the number value, so the
Matches pane demonstrates the value comparison and substitution on scripts
far beyond ASCII digits.

Project doubles as fixture of acceptance test
`NumeralMatchesSubstitutionTest`, verifying substitution on several segments
below. Note: `source/beyond-coverage.txt` sorts before
`source/exotic-numerals.txt`, so editor entry numbers run two ahead of
segment numbers in this file.

## Covered systems (one segment each)

| Segment | System | Base | Codepoints used | Value in source / TM |
|---|---|---|---|---|
| 1 | Western digits | 10 | ASCII | 1984 / 1750 |
| 2 | Khmer digits | 10 | U+17E0 block | 123456 / 98765 |
| 3 | Ethiopic (multiplicative-additive) | 10, no zero | U+1369 block | 1976 / 1974 |
| 4 | Cuneiform (Sumero-Akkadian) | 60 | U+12433, U+12432 | 432000 / 216000 |
| 5 | Aegean (Linear A/B) | 10 | U+10133, U+10132 | 90000 / 80000 |
| 6 | Mayan digit | 20 | U+1D2F3, U+1D2EB | 19 / 11 |
| 7 | Kaktovik digit (Inupiaq, 1990s) | 20 | U+1D2D3, U+1D2C4 | 19 / 4 |
| 8 | Roman (subtractive-additive) | mixed | ASCII letters | 1984 / 2026 |
| 9 | CJK ideographic (multiplicative) | 10 | myriad grouping | 35000 / 1984 |
| 10 | Meroitic (Kingdom of Kush) | 10 | U+109C0 block | 900000 / 1000 |
| 11 | Pahawh Hmong | 10 | U+16B5B block | 10^12 / 10000 |
| 12 | Greek acrophonic (Attic) | 5/10 | U+10147, U+10146 | 50000 / 5000 |
| 13 | Ottoman Siyaq | 10 | U+1ED01 block | 90000 / 10000 |
| 14 | Vulgar fractions | - | U+00BC, U+00BD | 1/4 / 1/2 |
| 15 | Adlam digits (1980s, Guinea) | 10 | U+1E950 block | 1984 / 2026 |
| 16 | Medefaidrin digit (Nigeria) | 20 | U+16E80 block | 19 / 7 |

## Parsing versus substitution

All sixteen numbers above parse to the right value and enter the
value-based match comparison. The insertion step substitutes them as well
(verified with both the default and the Lucene tokenizers): digit systems
render in the target's digit script, sign numerals - Ethiopic
composites, cuneiform, Mayan and Kaktovik digits - are spelled out as
digits when the target writes digits, and a vulgar fraction (segment 14)
becomes the precomposed glyph of its exact value, so a quarter replaces a
half glyph for glyph.

Two exclusions are deliberate and stay: Roman numerals written with Latin
letters (segment 8) are never rewritten because "I", "MIX" or "DIV" cannot
be told apart from words, and the remaining presentation forms carrying a
compatibility decomposition - superscript exponents and enclosed numbers -
must never be touched by a substitution.

A related boundary holds for the protected-parts marking: Han numerals
(segment 9) are read by value but stay out of the default custom-tag
pattern, because their characters double as ordinary words in CJK prose
and the untokenized pattern would flood such projects with false tags.
A project that cannot meet Han characters outside numbers can add the
class to the pattern under tag processing preferences.

## Cross-system conversion (segments 17 to 19)

When the match target writes its number in a numeral system rather than in
digits, the source value is written in that system, whatever system the
source itself uses. Three segments demonstrate the three writers:

| Segment | Source system and value | Target system | Written as |
|---|---|---|---|
| 17 | Ethiopic 840 | Aegean | additive composition, 800 sign plus 40 sign |
| 18 | Ethiopic 40 | Mayan digits | positional base twenty, two signs |
| 19 | Meroitic 900000 | Ethiopic | ICU rule set writes the composite |

Only an exact composition is inserted; otherwise the value falls back to
plain digits.

## Sign sequences read as one number (segments 20 and 21)

The reading side composes sign sequences from the named numeral blocks as
well: a Mayan two-digit column reads positionally in base twenty (segment
20, value 20), and a largest-first cuneiform sign pair reads as an
additive sum (segment 21, value 11). Single fraction signs - the
cuneiform two-thirds at U+1245B, the North Indic quarter at U+A830, the
Kharoshthi half at U+10A48 - resolve to their exact rational as well.

## Valid notation only (segments 22 and 23)

A composition is only inserted when it is valid notation in the target's
system. The Roman code points of the Number Forms block are written
subtractively through the ICU rule sets in the template's case (year 40
becomes ⅩⅬ, never a pile of twelves), and the counting rods - positional
above the tens - are read but never composed, so a rod-written target
receives the value in plain digits instead.

## Beyond coverage: debugging candidates

`source/beyond-coverage.txt` keeps the remaining verified misses:

1. Tamil multiplicative-additive numerals: ICU's rule set writes them but
   cannot read them back, and the additive reader deliberately rejects
   their ascending unit-multiplier order rather than misread it as a sum.
2. Suzhou/Hangzhou numerals (U+3021 block) are positional decimal, but
   their block mixes in the ten/twenty/thirty signs, so a digit run is
   not composed.

## Not representable in Unicode

The Inca quipu records numbers as knot positions on cords; there is no
Unicode encoding for it, so it cannot appear in a text-based demo. The
Egyptian hieroglyph numerals are encoded but carry no Unicode numeric
properties, which keeps them outside the parser's code-point path as well.
