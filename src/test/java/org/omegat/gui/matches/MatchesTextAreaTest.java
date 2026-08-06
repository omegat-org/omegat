/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.matches;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneJapaneseTokenizer;
import org.omegat.util.TestPreferencesInitializer;

public class MatchesTextAreaTest {

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
    }

    @Test
    public void testReplaceNumbers() {
        ITokenizer tok = new DefaultTokenizer();

        // Simple case
        String source = "chapter 5";
        String srcMatch = "chapter 1";
        String trgMatch = "foo 1";
        assertEquals("foo 5", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Double
        source = "chapter 5.5";
        srcMatch = "chapter 1.1";
        trgMatch = "foo 1.1";
        assertEquals("foo 5.5", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Different order
        source = "hoge 9 fuga 8 piyo 7";
        srcMatch = "foo 1 bar 2 baz 3";
        trgMatch = "bing 3 bang 2 bop 1";
        assertEquals("bing 7 bang 8 bop 9",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // User-reported problem string (duplicate numbers)
        source = "Point C : Recommence les \u00E9tapes 16 \u00E0 21 \u2013 pages 16 et 17";
        srcMatch = "Point B : Recommence les \u00E9tapes 9 \u00E0 15 \u2013 page 14 et 15";
        trgMatch = "Point B: Repeat steps 9 to 15 \u2013 pages 14 and 15";
        assertEquals("Point B: Repeat steps 16 to 21 \u2013 pages 16 and 17",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Substitution not possible: differing number counts
        source = "hoge 9 fuga 8 piyo 7";
        srcMatch = "foo 1 bar 2 baz";
        trgMatch = "bing 3 bang 2 bop 1";
        assertEquals("bing 3 bang 2 bop 1", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Substitution not possible: differing number counts
        source = "hoge 9 fuga 8 piyo";
        srcMatch = "foo 1 bar 2 baz 3";
        trgMatch = "bing 3 bang 2 bop 1";
        assertEquals("bing 3 bang 2 bop 1", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Substitution not possible: differing number counts
        source = "hoge 9 fuga 8 piyo 7";
        srcMatch = "foo 1 bar 2 baz 3";
        trgMatch = "bing 3 bang 2 bop";
        assertEquals("bing 3 bang 2 bop", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Substitution not possible: differing number counts
        source = "hoge 9 fuga 8 piyo 7";
        srcMatch = "foo 1 bar 2 baz 3 3";
        trgMatch = "bing 3 bang 2 bop 1";
        assertEquals("bing 3 bang 2 bop 1", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Substitution not possible: differing numbers
        source = "hoge 9 fuga 8 piyo 7";
        srcMatch = "foo 1 bar 2 baz 33";
        trgMatch = "bing 3 bang 2 bop 1";
        assertEquals("bing 3 bang 2 bop 1", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * Full-width (ASCII) digits (U+FF10-U+FF19), common in Japanese text,
     * must be treated as equivalent to their half-width counterparts when
     * substituting numbers into a fuzzy match, and the inserted number must
     * adopt the digit width used by the target match. Feature request #1193.
     */
    @Test
    public void testReplaceNumbersFullwidth() {
        ITokenizer tok = new DefaultTokenizer();

        // Reported case: full-width source number, half-width (Latin) target.
        // The inserted number must be converted to half-width to match the target.
        String source = "これは例文９です";
        String srcMatch = "これは例文8です";
        String trgMatch = "This is a sample sentence 8";
        assertEquals("This is a sample sentence 9",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Equivalence: the match's own source uses a full-width digit while its
        // target uses a half-width one. They must still be recognized as the
        // same number so the substitution is applied.
        source = "chapter ５";
        srcMatch = "chapter ９";
        trgMatch = "foo 9";
        assertEquals("foo 5", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // A full-width target keeps full-width digits: the half-width source
        // number is converted to full-width to match the target convention.
        source = "chapter 5";
        srcMatch = "chapter 9";
        trgMatch = "第９章";
        assertEquals("第５章",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Full-width digits on both sides of a purely CJK segment.
        source = "第５０章";
        srcMatch = "第１２章";
        trgMatch = "第１２章";
        assertEquals("第５０章",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Regression guard: pure ASCII substitution is unaffected.
        source = "chapter 5";
        srcMatch = "chapter 1";
        trgMatch = "foo 1";
        assertEquals("foo 5", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * Edge cases and documented scope limits of the full-width digit handling
     * from feature request #1193.
     */
    @Test
    public void testReplaceNumbersWidthEdgeCases() {
        ITokenizer tok = new DefaultTokenizer();

        // Several numbers at once, full-width source into half-width target.
        String source = "x ９ y ８";
        String srcMatch = "x ３ y ４";
        String trgMatch = "p 3 q 4";
        assertEquals("p 9 q 8", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Each substituted number independently follows the width of the
        // target token it replaces (mixed widths in one target).
        source = "x ９ y ８";
        srcMatch = "x ３ y ４";
        // Target mixes a half-width and a full-width digit.
        trgMatch = "p 3 q ４";
        assertEquals("p 9 q ８",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Documented limit: a full-width DECIMAL is not a single number, because
        // the full-width full stop is not read as a decimal separator, so no
        // substitution happens. This pins the current scope; extending to
        // full-width decimals is a possible follow-up.
        source = "x ５．５";
        srcMatch = "x １．１";
        trgMatch = "foo 1.1";
        assertEquals("foo 1.1", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Arabic-Indic digits (U+0660-U+0669) carry the same values as their
        // ASCII counterparts, and the inserted number follows the digit script
        // of the target token it replaces, so a Latin target receives ASCII
        // digits.
        source = "x ٩";
        srcMatch = "x 8";
        trgMatch = "foo 8";
        assertEquals("foo 9",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // ... and the other way round: an Arabic-Indic target keeps its script.
        source = "x 9";
        srcMatch = "x 8";
        trgMatch = "foo ٨";
        assertEquals("foo ٩",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * Numbers are compared and paired by value, so the same number written in a
     * different numeral system counts as the same number. Roman numerals are the
     * exception, and only where they are written with Latin letters: see
     * {@link #testUppercaseRomanWordsAreNotNumbers()}.
     */
    @Test
    public void testReplaceNumbersAcrossNumeralSystems() {
        ITokenizer tok = new DefaultTokenizer();

        // A Roman numeral written with the dedicated code points is unambiguous
        // and counts, so the numbers pair up and the source number is inserted.
        String source = "Kapitel 7";
        String srcMatch = "Kapitel 4";
        String trgMatch = "Chapter Ⅳ";
        assertEquals("Chapter 7",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Written with Latin letters the same numeral is not a number here, so
        // the counts disagree and the match is left alone.
        trgMatch = "Chapter IV";
        assertEquals("Chapter IV",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Han numerals are numbers as well, including multi-character ones.
        source = "chapter 五";
        srcMatch = "chapter 十二";
        trgMatch = "Kapitel 12";
        assertEquals("Kapitel 5",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Where a number begins and ends is the tokenizer's decision, not ours:
        // the default tokenizer keeps a run of ideographs together, so the
        // numeral inside the Han word below never becomes a token of its own and
        // the segment holds no number at all.
        source = "第五章";
        srcMatch = "第八章";
        trgMatch = "Chapter 8";
        assertEquals("Chapter 8",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Ordinary words that look like numerals must not be touched: "mix" is
        // not a lowercase Roman 1009, and "civil" is not 106. The segment
        // carries a real number as well, so the assertion tells the two cases
        // apart: were "mix" counted, the number counts would disagree and no
        // substitution would happen at all.
        source = "the mix of 3 civil laws";
        srcMatch = "the mix of 5 civil laws";
        trgMatch = "die Mischung aus 5 Zivilgesetzen";
        assertEquals("die Mischung aus 3 Zivilgesetzen",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * Values are exact and unbounded: numbers far beyond the range and the
     * precision of a double are substituted digit for digit, and two numbers
     * that differ only beyond that precision are recognized as different.
     */
    @Test
    public void testReplaceNumbersArbitraryPrecision() {
        ITokenizer tok = new DefaultTokenizer();

        // A 40-digit integer is inserted verbatim, with no rounding and no
        // exponent notation.
        String big = "1234567890123456789012345678901234567890";
        String source = "id " + big;
        String srcMatch = "id 7";
        String trgMatch = "Kennung 7";
        assertEquals("Kennung " + big,
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Two 20-digit numbers that a double would round to the same value are
        // different numbers, so the guard refuses the substitution.
        source = "id 99999999999999999999";
        srcMatch = "id 99999999999999999998";
        trgMatch = "Kennung 99999999999999999997";
        assertEquals("Kennung 99999999999999999997",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // The same for decimals: a difference in the twentieth decimal place is
        // a real difference, and it is not lost.
        source = "value 1.00000000000000000001";
        srcMatch = "value 1.00000000000000000002";
        trgMatch = "Wert 1.00000000000000000003";
        assertEquals("Wert 1.00000000000000000003",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // A long decimal that does match is carried over exactly.
        source = "value 2.71828182845904523536028747135266249776";
        srcMatch = "value 3.14159265358979323846264338327950288420";
        trgMatch = "Wert 3.14159265358979323846264338327950288420";
        assertEquals("Wert 2.71828182845904523536028747135266249776",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * The numbers of a match may appear in any order in its target, and they may
     * repeat. Every target number must receive the source number that belongs to
     * it, whatever the permutation.
     */
    @Test
    public void testReplaceNumbersPermutations() {
        ITokenizer tok = new DefaultTokenizer();

        // A cyclic permutation: the target order 2, 3, 1 must pick up the source
        // numbers that stand where those numbers stand in the match's source.
        String source = "a 7 b 8 c 9";
        String srcMatch = "a 1 b 2 c 3";
        String trgMatch = "x 2 y 3 z 1";
        assertEquals("x 8 y 9 z 7",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // The same numbers in different multiplicities are not the same set of
        // numbers; refusing here also keeps every target number mapped.
        source = "a 7 b 8 c 9";
        srcMatch = "a 1 b 1 c 2";
        trgMatch = "x 1 y 2 z 2";
        assertEquals("x 1 y 2 z 2",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // A repeated number is substituted in every place it occurs.
        source = "a 7 b 7 c 9";
        srcMatch = "a 1 b 1 c 2";
        trgMatch = "x 2 y 1 z 1";
        assertEquals("x 9 y 7 z 7",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // A permutation that is not its own inverse. Reversing a pair or a
        // triple hides a mapping that runs in the wrong direction, because
        // those permutations equal their inverse; a four-element cycle does
        // not, so this is the case that pins the direction of the mapping.
        source = "a 10 b 20 c 30 d 40";
        srcMatch = "a 1 b 2 c 3 d 4";
        trgMatch = "w 2 x 3 y 4 z 1";
        assertEquals("w 20 x 30 y 40 z 10",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * What counts as a number here is decided token by token, so a number glued
     * to a symbol is only reached when the tokenizer happens to separate the
     * two. The cases below pin today's reach. The ones marked as gaps are not
     * intended behaviour: a percentage, a currency amount, a dotted date and an
     * ordinal are all values the number conversion window already reads and
     * writes locale by locale, and match insertion should end up using the same
     * notion of a number rather than the bare token parser.
     */
    @Test
    public void testReplaceNumbersReachOfTheTokenNotion() {
        ITokenizer tok = new DefaultTokenizer();

        // Separated by a space, the amount is an ordinary number token and is
        // substituted; the unit travels along untouched.
        String source = "Preis 30 EUR für 4 Stück";
        String srcMatch = "Preis 50 EUR für 6 Stück";
        String trgMatch = "Price 50 EUR for 6 items";
        assertEquals("Price 30 EUR for 4 items",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // GAP: glued to a percent or a currency sign it is one token with no
        // value, so the match's own figure survives although the source
        // disagrees - the translator is handed 50% where the segment says 30%.
        source = "Rabatt 30% auf 4 Artikel";
        srcMatch = "Rabatt 50% auf 6 Artikel";
        trgMatch = "50% discount on 6 items";
        assertEquals("50% discount on 4 items",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        source = "Preis $30 für 4";
        srcMatch = "Preis $50 für 6";
        trgMatch = "Price $50 for 4";
        assertEquals("Price $50 for 4",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // A date whose parts are separated becomes three numbers and is carried
        // over part by part - including across a different date order in the
        // target, which is exactly the case a mapping in the wrong direction
        // would scramble.
        source = "am 2024-03-07";
        srcMatch = "am 2024-01-06";
        trgMatch = "on 01/06/2024";
        assertEquals("on 03/07/2024",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // GAP: a dotted date is a single token with no value, so the stale date
        // of the match is left standing.
        source = "am 07.03.2024";
        srcMatch = "am 06.01.2024";
        trgMatch = "on 06.01.2024";
        assertEquals("on 06.01.2024",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // A time separated by a colon is two numbers and is carried over.
        source = "um 12:30 kamen 4";
        srcMatch = "um 14:45 kamen 6";
        trgMatch = "at 14:45 there were 6";
        assertEquals("at 12:30 there were 4",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // GAP: an ordinal indicator makes the token unreadable as a number on
        // the English side, so the counts disagree and nothing is substituted -
        // although "3." and "3rd" are the same ordinal, and the conversion
        // window already knows how to read and write both.
        source = "der 4. Absatz";
        srcMatch = "der 3. Absatz";
        trgMatch = "the 3rd paragraph";
        assertEquals("the 3rd paragraph",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * Numbers keep their surroundings intact: inline tags, punctuation directly
     * against a number, and a target that holds no number at all.
     */
    @Test
    public void testReplaceNumbersLeavesSurroundingsAlone() {
        ITokenizer tok = new DefaultTokenizer();

        // OmegaT inline tags are ordinary text here and must come through byte
        // for byte, including the digits inside the tag names.
        String source = "<f1>Chapter 3</f1>";
        String srcMatch = "<f1>Chapter 5</f1>";
        String trgMatch = "<f1>Kapitel 5</f1>";
        assertEquals("<f1>Kapitel 3</f1>",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        source = "Text <x0/> 3 items";
        srcMatch = "Text <x0/> 5 items";
        trgMatch = "Text <x0/> 5 Stück";
        assertEquals("Text <x0/> 3 Stück",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // A number pressed against punctuation and a percent sign.
        source = "50% of 3, no more.";
        srcMatch = "50% of 5, no more.";
        trgMatch = "50% von 5, nicht mehr.";
        assertEquals("50% von 3, nicht mehr.",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Empty input is not a special case, it simply holds no number.
        assertEquals("", MatchesTextArea.substituteNumbers("", "", "", tok, tok));
    }

    /**
     * The digit script of the target token is followed for every decimal script,
     * including one outside the basic multilingual plane. A target token written
     * in an algorithmic system that this code cannot write back (Han) falls back
     * to plain digits rather than to the wrong number.
     */
    @Test
    public void testReplaceNumbersDigitScripts() {
        ITokenizer tok = new DefaultTokenizer();

        // Osmanya digits (U+104A0..) are surrogate pairs; the substituted number
        // is written in that script, not in ASCII.
        String source = "n 7";
        String srcMatch = "n 5";
        String trgMatch = "n 𐒥";
        assertEquals("n 𐒧",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // Documented limit: there is no writer for the Han system here, so a Han
        // target receives the value spelled out in digits. The number is right,
        // the notation is not preserved.
        source = "chapter 7";
        srcMatch = "chapter 12";
        trgMatch = "第 十二 章";
        assertEquals("第 7 章",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * Regression guard for the substitution side: a token made only of Roman
     * letters is a numeral only in the fuzzy-matching score, where a false
     * positive costs a little similarity. Here it rewrites text the translator
     * gets handed, so an ordinary uppercase word must never be read as a number.
     *
     * These cases fail as long as the substitution side uses the same
     * unrestricted Roman gate as the match scorer. Fix the production code, not
     * the expectations.
     */
    @Test
    public void testUppercaseRomanWordsAreNotNumbers() {
        ITokenizer tok = new DefaultTokenizer();

        // The English pronoun "I" is a canonical Roman 1. Were it counted, it
        // would take part in the pairing and be rewritten as a numeral: the
        // target would read "II saw 8 birds 2 hours later."
        String source = "2 Stunden später sah ich 8 Vögel.";
        String srcMatch = "1 Stunde später sah ich 3 Vögel.";
        String trgMatch = "I saw 3 birds 1 hour later.";
        assertEquals("I saw 8 birds 2 hour later.",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // "X" as a size or a multiplier, and "MIX" as a word, are not 10 and 1009.
        source = "Größe 3";
        srcMatch = "Größe 10";
        trgMatch = "Size X";
        assertEquals("Size X", MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // The same rule the other way round: a Roman-looking word in the source
        // or in the match must not silently switch the whole feature off by
        // making the number counts disagree.
        source = "I have 3 apples";
        srcMatch = "I have 5 apples";
        trgMatch = "Ich habe 5 Äpfel";
        assertEquals("Ich habe 3 Äpfel",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));

        // "IV" reads as intravenous far more often than as 4, and "V" prefixes a
        // version far more often than it counts five.
        source = "Gabe von 3 ml";
        srcMatch = "Gabe von 4 ml";
        trgMatch = "IV administration of 4 ml";
        assertEquals("IV administration of 3 ml",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, tok, tok));
    }

    /**
     * The reported #1193 scenario on the real Japanese production path: the
     * source is tokenized with the Kuromoji-based Japanese tokenizer while the
     * target uses a Latin tokenizer. The full-width (ASCII) source digit must still
     * be recognized and inserted into the target as a half-width digit.
     */
    @Test
    public void testReplaceNumbersJapaneseTokenizer() {
        ITokenizer jaTok = new LuceneJapaneseTokenizer();
        ITokenizer enTok = new DefaultTokenizer();

        String source = "これは例文９です";
        String srcMatch = "これは例文8です";
        String trgMatch = "This is a sample sentence 8";
        assertEquals("This is a sample sentence 9",
                MatchesTextArea.substituteNumbers(source, srcMatch, trgMatch, jaTok, enTok));
    }
}
