/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Optional;

import org.junit.Test;
import org.omegat.util.NumeralValueParser.Rational;

import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.util.ULocale;

/**
 * Tests for {@link NumeralValueParser}: value parsing across many numbering
 * systems (via ICU), the "first number in text" scanner, and the guardrails
 * that keep ordinary words from being read as (e.g.) Roman numerals.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumeralValueParserTest {

    private static BigInteger whole(String s) {
        Optional<BigInteger> v = NumeralValueParser.parseWhole(s);
        assertTrue("expected a parsed value for: " + s, v.isPresent());
        return v.get();
    }

    private static String format(int type, String ruleSet, ULocale loc, long value) {
        RuleBasedNumberFormat f = new RuleBasedNumberFormat(loc, type);
        f.setDefaultRuleSet(ruleSet);
        return f.format(value);
    }

    /** Format each value in the given ICU system, then assert parseWhole round-trips it. */
    private static void assertRoundTrip(int type, String ruleSet, ULocale loc) {
        for (long v : new long[] { 1, 2, 7, 12, 20, 49, 88 }) {
            String text = format(type, ruleSet, loc, v);
            assertEquals(ruleSet + " '" + text + "'", BigInteger.valueOf(v),
                    NumeralValueParser.parseWhole(text).orElse(null));
        }
    }

    // --- must-work explicit cases -------------------------------------------

    @Test
    public void romanAndCjkAndEthiopicTwelve() {
        assertEquals(BigInteger.valueOf(12), whole("XII"));
        assertEquals(BigInteger.valueOf(12), whole("xii"));
        assertEquals(BigInteger.valueOf(12), whole("Ⅻ"));   // single codepoint U+216B, needs NFKC
        assertEquals(BigInteger.valueOf(12), whole("十二"));  // CJK ideographic
        assertEquals(BigInteger.valueOf(12), whole("፲፪"));   // Ethiopic
    }

    @Test
    public void decimalDigitsOfAnyScript() {
        assertEquals(BigInteger.valueOf(12), whole("12"));
        assertEquals(BigInteger.valueOf(12), whole("１２"));   // fullwidth
        assertEquals(BigInteger.valueOf(12), whole("١٢"));    // Arabic-Indic
        assertEquals(BigInteger.valueOf(12), whole("१२"));    // Devanagari
    }

    @Test
    public void sameValueDifferentSystemsAreEqual() {
        BigInteger twelve = BigInteger.valueOf(12);
        assertEquals(twelve, whole("XII"));
        assertEquals(twelve, whole("十二"));
        assertEquals(twelve, whole("12"));
        assertEquals(twelve, whole("፲፪"));
    }

    // --- round-trip across all supported ICU systems ------------------------

    @Test
    public void roundTripAllNumberingSystems() {
        int ns = RuleBasedNumberFormat.NUMBERING_SYSTEM;
        assertRoundTrip(ns, "%roman-upper", ULocale.ROOT);
        assertRoundTrip(ns, "%roman-lower", ULocale.ROOT);
        assertRoundTrip(ns, "%ethiopic", ULocale.ROOT);
        assertRoundTrip(ns, "%armenian-upper", ULocale.ROOT);
        assertRoundTrip(ns, "%armenian-lower", ULocale.ROOT);
        assertRoundTrip(ns, "%greek-upper", ULocale.ROOT);
        assertRoundTrip(ns, "%greek-lower", ULocale.ROOT);
        assertRoundTrip(ns, "%hebrew", ULocale.ROOT);
        assertRoundTrip(ns, "%tamil", ULocale.ROOT);
        assertRoundTrip(ns, "%georgian", ULocale.ROOT);
        assertRoundTrip(ns, "%cyrillic-lower", ULocale.ROOT);
    }

    @Test
    public void roundTripCjkSpellout() {
        int sp = RuleBasedNumberFormat.SPELLOUT;
        assertRoundTrip(sp, "%spellout-numbering", ULocale.forLanguageTag("zh"));
        assertRoundTrip(sp, "%spellout-numbering", ULocale.forLanguageTag("ja"));
        assertRoundTrip(sp, "%spellout-numbering", ULocale.forLanguageTag("zh-Hant"));
    }

    // --- non-numerals reject (fall back to text) ----------------------------

    @Test
    public void nonNumeralsReturnEmpty() {
        assertFalse(NumeralValueParser.parseWhole("hello").isPresent());
        assertFalse(NumeralValueParser.parseWhole("").isPresent());
        assertFalse(NumeralValueParser.parseWhole("   ").isPresent());
        assertFalse(NumeralValueParser.parseWhole("XIIabc").isPresent()); // partial -> rejected
    }

    // --- first number in text ----------------------------------------------

    @Test
    public void firstNumberFindsEmbeddedDecimal() {
        assertEquals(BigInteger.valueOf(2), NumeralValueParser.firstNumber("item2").orElse(null));
        assertEquals(BigInteger.valueOf(10), NumeralValueParser.firstNumber("item10").orElse(null));
        assertEquals(BigInteger.valueOf(3), NumeralValueParser.firstNumber("Section 3 and 4").orElse(null));
        assertEquals(BigInteger.valueOf(3), NumeralValueParser.firstNumber("٣ apples").orElse(null));
    }

    @Test
    public void firstNumberFindsDelimitedNonDecimalToken() {
        assertEquals(BigInteger.valueOf(12), NumeralValueParser.firstNumber("Kapitel XII").orElse(null));
        assertEquals(BigInteger.valueOf(12), NumeralValueParser.firstNumber("章 十二 節").orElse(null));
    }

    @Test
    public void edgeCasesSignDecimalAndSeparators() {
        // '-' is a separator, so a leading sign is dropped: "-5" -> 5
        assertEquals(BigInteger.valueOf(5), NumeralValueParser.firstNumber("-5").orElse(null));
        // '.' and ',' are separators: only the leading integer part is taken
        assertEquals(BigInteger.ONE, NumeralValueParser.firstNumber("1.5").orElse(null));
        assertEquals(BigInteger.ONE, NumeralValueParser.firstNumber("1,000").orElse(null));
        assertEquals(BigInteger.ZERO, NumeralValueParser.parseWhole("0").orElse(null));
    }

    @Test
    public void guardrailWordsAreNotMisreadAsNumerals() {
        // A whole word that is not a valid complete numeral must not be a hit.
        assertFalse(NumeralValueParser.firstNumber("the VILLA").isPresent());
        assertFalse(NumeralValueParser.firstNumber("hello world").isPresent());
        // Non-Nd numerals embedded inside an unsegmented run are not detected.
        assertFalse(NumeralValueParser.firstNumber("第十二章").isPresent());
    }

    // --- higher-value numerals across writing systems -----------------------

    @Test
    public void japaneseHigherValueUnits() {
        assertEquals(BigInteger.valueOf(100), whole("百"));       // hyaku
        assertEquals(BigInteger.valueOf(1000), whole("千"));      // sen
        assertEquals(BigInteger.valueOf(10000), whole("一万"));   // man (10^4)
        assertEquals(BigInteger.valueOf(100000000L), whole("一億")); // oku (10^8)
    }

    @Test
    public void japaneseComposedSevenDigitValues() {
        assertEquals(BigInteger.valueOf(2500034), whole("二百五十万三十四"));
        assertEquals(BigInteger.valueOf(3400025), whole("三百四十万二十五"));
        assertEquals(BigInteger.valueOf(4300052), whole("四百三十万五十二"));
        assertEquals(BigInteger.valueOf(5200043), whole("五百二十万四十三"));
    }

    @Test
    public void higherValueOtherScripts() {
        // Roman thousands/hundreds (M/D/C).
        assertEquals(BigInteger.valueOf(1984), whole("MCMLXXXIV"));
        assertEquals(BigInteger.valueOf(2024), whole("MMXXIV"));
        // Chinese higher unit for 10^8.
        assertEquals(BigInteger.valueOf(100000000L), whole("一亿"));
        // Ethiopic higher-value marks for 100 and 10000, and a composed value.
        assertEquals(BigInteger.valueOf(100), whole("፻"));
        assertEquals(BigInteger.valueOf(10000), whole("፼"));
        assertEquals(BigInteger.valueOf(2500034), whole("፪፻፶፼፴፬"));
    }

    @Test
    public void firstNumberFindsHigherValueTokenInSentence() {
        assertEquals(BigInteger.valueOf(2500034),
                NumeralValueParser.firstNumber("Betrag 二百五十万三十四 Yen.").orElse(null));
        assertEquals(BigInteger.valueOf(1984),
                NumeralValueParser.firstNumber("Baujahr MCMLXXXIV ist dokumentiert.").orElse(null));
        assertEquals(BigInteger.valueOf(10000),
                NumeralValueParser.firstNumber("Posten ፼ ist auf Lager.").orElse(null));
    }

    // ========================================================================
    // Real values: sign, decimals, fractions, mixed and vulgar fractions.
    // parseValue / firstValue return an exact reduced Rational.
    // ========================================================================

    private static Rational r(long num, long den) {
        return Rational.of(BigInteger.valueOf(num), BigInteger.valueOf(den));
    }

    /** Assert parseValue(input) is present and exactly equal to num/den. */
    private static void assertValue(String input, long num, long den) {
        Optional<Rational> v = NumeralValueParser.parseValue(input);
        assertTrue("expected a value for: " + input, v.isPresent());
        assertEquals("value of " + input, r(num, den), v.get());
    }

    private static void assertNoValue(String input) {
        assertFalse("expected no value for: " + input, NumeralValueParser.parseValue(input).isPresent());
    }

    @Test
    public void signedIntegers() {
        assertValue("5", 5, 1);
        assertValue("-5", -5, 1);
        assertValue("−5", -5, 1); // U+2212 MINUS SIGN
        assertValue("+5", 5, 1);
        assertValue("0", 0, 1);
    }

    @Test
    public void negativeZeroCollapsesToZero() {
        assertValue("-0", 0, 1);
        assertValue("-0.0", 0, 1);
        assertValue("↉", 0, 1); // ↉ zero thirds
        // canonical zero: numerator 0, denominator 1
        Rational z = NumeralValueParser.parseValue("-0").orElseThrow();
        assertEquals(BigInteger.ZERO, z.numerator());
        assertEquals(BigInteger.ONE, z.denominator());
    }

    @Test
    public void decimals() {
        assertValue("3.14", 157, 50);
        assertValue(".5", 1, 2);
        assertValue("1.", 1, 1);
        assertValue("1.0", 1, 1);
        assertValue("-1.5", -3, 2);
    }

    @Test
    public void asciiFractions() {
        assertValue("3/4", 3, 4);
        assertValue("7/4", 7, 4);   // improper
        assertValue("0/5", 0, 1);
        assertValue("-1/2", -1, 2);
        assertValue("2/4", 1, 2);   // reduces
    }

    @Test
    public void mixedNumbers() {
        assertValue("1 1/2", 3, 2);
        assertValue("2 3/4", 11, 4);
        assertValue("-1 1/2", -3, 2); // sign applies to the whole value
    }

    @Test
    public void vulgarFractions() {
        assertValue("¼", 1, 4);
        assertValue("½", 1, 2);
        assertValue("¾", 3, 4);
        assertValue("⅓", 1, 3);
        assertValue("⅔", 2, 3);
        assertValue("⅕", 1, 5);
        assertValue("⅛", 1, 8);
        assertValue("⅞", 7, 8);
        assertValue("⅐", 1, 7);
        assertValue("⅑", 1, 9);
        assertValue("⅒", 1, 10);
        assertValue("↉", 0, 1); // ↉
        assertValue("-¾", -3, 4);
    }

    @Test
    public void integerPlusVulgarFractionWithoutNfkcGluing() {
        // NFKC would turn the digit followed by the vulgar fraction into eleven
        // halves; the parser must read the glyph directly and yield 3/2.
        assertValue("1½", 3, 2);
        assertValue("2½", 5, 2);
        assertValue("-2½", -5, 2);
        assertValue("12¾", 51, 4); // 12 + 3/4
    }

    @Test
    public void unicodeFractionSlash() {
        assertValue("1⁄4", 1, 4);   // U+2044
        assertValue("3⁄8", 3, 8);
    }

    @Test
    public void exactRationalBeatsRoundedDecimal() {
        // 1/3 is strictly greater than 0.3333333 and strictly less than 0.3334;
        // an exact rational gets this right where a rounded decimal/double would tie.
        assertTrue(cmp("1/3", "0.3333333") > 0);
        assertTrue(cmp("1/3", "0.3334") < 0);
        assertTrue(cmp("⅐", "0.142857") > 0); // 1/7 > 0.142857
    }

    @Test
    public void divisionByZeroAndMalformedRejected() {
        assertNoValue("1/0");
        assertNoValue("0/0");
        assertNoValue("-1/0");
        assertNoValue("/");
        assertNoValue("1/");
        assertNoValue("/4");
        assertNoValue("1/2/3");
        assertNoValue("NaN");
        assertNoValue("∞"); // ∞
        assertNoValue("");
        assertNoValue("hello");
    }

    @Test
    public void commaIsNotADecimalOrGroupingSeparator() {
        // Deliberate, documented policy: comma is locale-ambiguous, so a comma
        // string is not a whole number; the first embedded integer is used.
        assertNoValue("1,000");
        assertNoValue("1,5");
        assertEquals(r(1, 1), NumeralValueParser.firstValue("1,000").orElse(null));
    }

    @Test
    public void hugeValuesAreExactNoOverflow() {
        String big = "1" + "0".repeat(40); // 10^40, overflows long
        assertTrue(cmp(big, "999999999999999999999") > 0);
        Rational v = NumeralValueParser.parseValue(big).orElseThrow();
        assertEquals(BigInteger.TEN.pow(40), v.numerator());
        assertEquals(BigInteger.ONE, v.denominator());
        // a huge fraction still cross-multiplies exactly
        assertTrue(cmp("100000000000000000000000000000000000000000/1",
                "999999999999999999999999999999999999999") > 0);
    }

    @Test
    public void firstValueFindsEmbeddedSignedAndFractionalNumbers() {
        assertEquals(r(2, 1), NumeralValueParser.firstValue("item2").orElse(null));
        assertEquals(r(-5, 1), NumeralValueParser.firstValue("Zeile -5 cm").orElse(null));
        assertEquals(r(3, 4), NumeralValueParser.firstValue("3/4 inch").orElse(null));
        assertEquals(r(1, 4), NumeralValueParser.firstValue("ca. ¼ Tasse").orElse(null));
        assertEquals(r(12, 1), NumeralValueParser.firstValue("Kapitel 十二 folgt").orElse(null));
    }

    private static int cmp(String a, String b) {
        return NumeralValueParser.parseValue(a).orElseThrow()
                .compareTo(NumeralValueParser.parseValue(b).orElseThrow());
    }

    // ========================================================================
    // Single-code-point Unicode numeric values (Mechanism C): enclosed,
    // parenthesized and historic script numerals that are neither decimal
    // digits nor algorithmic numerals. Only single, non-negative integers.
    // ========================================================================

    @Test
    public void enclosedAndParenthesizedNumbers() {
        assertValue("⑼", 9, 1);   // ⑼ PARENTHESIZED DIGIT NINE
        assertValue("㈨", 9, 1);   // ㈨ PARENTHESIZED IDEOGRAPH NINE
        assertValue("⒂", 15, 1);  // ⒂ PARENTHESIZED NUMBER FIFTEEN
        assertValue("⑰", 17, 1);  // ⑰ CIRCLED NUMBER SEVENTEEN
        assertValue("㉕", 25, 1);  // ㉕ CIRCLED NUMBER TWENTY FIVE
        assertValue("㉋", 40, 1);  // ㉋ CIRCLED NUMBER FORTY ON BLACK SQUARE
        assertValue("⓾", 10, 1);  // ⓾ DOUBLE CIRCLED NUMBER TEN
        assertValue("⓬", 12, 1);  // ⓬ NEGATIVE CIRCLED NUMBER TWELVE
        assertValue("⒔", 13, 1);  // ⒔ NUMBER THIRTEEN FULL STOP
        assertValue("①", 1, 1);   // ① CIRCLED DIGIT ONE
    }

    @Test
    public void historicScriptNumerals() {
        assertValue("𒐄", 6, 1);      // 𒐄 CUNEIFORM NUMERIC SIGN SIX ASH
        assertValue("𒐩", 7, 1);      // 𒐩 CUNEIFORM NUMERIC SIGN SEVEN SHAR2
        assertValue("𐡚", 3, 1);      // 𐡚 IMPERIAL ARAMAIC NUMBER THREE
        assertValue("𐤛", 3, 1);      // 𐤛 PHOENICIAN NUMBER THREE
        assertValue("𐄎", 8, 1);      // 𐄎 AEGEAN NUMBER EIGHT
        assertValue("𐄡", 900, 1);    // 𐄡 AEGEAN NUMBER NINE HUNDRED
        assertValue("𐄩", 8000, 1);   // 𐄩 AEGEAN NUMBER EIGHT THOUSAND
        assertValue("𐅰", 500, 1);    // 𐅰 GREEK ACROPHONIC NAXIAN FIVE HUNDRED
    }

    @Test
    public void mathematicalDigitFamiliesAndSingleRoman() {
        // Five mathematical "digit nine" families (all Nd) -> 9.
        assertValue("𝟗", 9, 1);   // 𝟗 MATHEMATICAL BOLD
        assertValue("𝟡", 9, 1);   // 𝟡 MATHEMATICAL DOUBLE-STRUCK
        assertValue("𝟫", 9, 1);   // 𝟫 MATHEMATICAL SANS-SERIF
        assertValue("𝟵", 9, 1);   // 𝟵 MATHEMATICAL SANS-SERIF BOLD
        assertValue("𝟿", 9, 1);   // 𝟿 MATHEMATICAL MONOSPACE
        assertValue("Ⅷ", 8, 1);         // Ⅷ ROMAN NUMERAL EIGHT (single code point)
        assertValue("Ⅻ", 12, 1);        // Ⅻ ROMAN NUMERAL TWELVE (single code point)
    }

    @Test
    public void superscriptSubscriptAndIdeographicZero() {
        assertValue("²", 2, 1);   // ² SUPERSCRIPT TWO (NFKC -> "2")
        assertValue("₃", 3, 1);   // ₃ SUBSCRIPT THREE (NFKC -> "3")
        assertValue("〇", 0, 1);   // 〇 IDEOGRAPHIC NUMBER ZERO
    }

    @Test
    public void symbolsWithoutNumericValueAreNotNumbers() {
        assertNoValue("💯"); // 💯 HUNDRED POINTS SYMBOL (emoji, no numeric value)
        assertNoValue("🔟"); // 🔟 KEYCAP TEN (emoji, no numeric value)
    }

    @Test
    public void firstValueFindsEnclosedAndHistoricNumbers() {
        assertEquals(r(25, 1), NumeralValueParser.firstValue("Nr. ㉕ folgt").orElse(null));
        assertEquals(r(8000, 1), NumeralValueParser.firstValue("Aegean 𐄩 hier").orElse(null));
        assertEquals(r(9, 1), NumeralValueParser.firstValue("Punkt ⑼.").orElse(null));
    }

    /**
     * Prose tokens must be rejected by the cheap pre-filter, not by failing
     * through all ICU rule sets: sorting a large project evaluates the parser
     * for (nearly) every segment, and without the pre-filter that froze the
     * UI for minutes.
     */
    @Test
    public void prosePreFilterKeepsParsingCheap() {
        NumeralValueParser.parseWhole("XII"); // warm up the ICU parsers
        long t0 = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            assertFalse(NumeralValueParser.parseWhole("Beispielwort" + (i % 97)).isPresent());
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertTrue("100k prose tokens must be rejected cheaply, took " + ms + " ms", ms < 2000);
    }

    /**
     * The pre-filter must not cut off separator-grouped digit runs: the
     * rule-based parsers read "99'999" via their loose number matching, and
     * NumberAutoConverter relies on that for foreign-grouping repair.
     */
    @Test
    public void separatorGroupedDigitsReachTheParsers() {
        assertEquals(Optional.of(BigInteger.valueOf(99999)), NumeralValueParser.parseWhole("99'999"));
        assertEquals(Optional.of(BigInteger.valueOf(1234567)),
                NumeralValueParser.parseWhole("1'234'567"));
        // "1.234" stays unpinned: the dot doubles as a decimal point, so its
        // whole-string value is locale-ambiguous by design.
    }

    /** Separator punctuation alone is prose, not a numeral. */
    @Test
    public void pureSeparatorPunctuationIsRejected() {
        assertFalse(NumeralValueParser.parseWhole("...").isPresent());
        assertFalse(NumeralValueParser.parseWhole(",").isPresent());
        assertFalse(NumeralValueParser.parseWhole("don't").isPresent());
    }

    /**
     * The token forms gate letter-only input: a word must not become a numeral
     * just because its letters happen to be Roman digits, while an unambiguous
     * numeral of any system is read.
     */
    @Test
    public void tokenFormsGateLetterOnlyInput() {
        // Words made of Roman letters stay words, whatever ICU would read.
        assertFalse(NumeralValueParser.parseTokenWhole("mix", true).isPresent());
        assertFalse(NumeralValueParser.parseTokenWhole("civil", true).isPresent());
        assertFalse(NumeralValueParser.parseTokenWhole("DID", true).isPresent());
        assertFalse(NumeralValueParser.parseTokenWhole("", true).isPresent());
        assertFalse(NumeralValueParser.parseTokenWhole(null, true).isPresent());

        // Unambiguous numerals are read, by value.
        assertEquals(Optional.of(BigInteger.valueOf(12)), NumeralValueParser.parseTokenWhole("XII", true));
        assertEquals(Optional.of(BigInteger.valueOf(12)), NumeralValueParser.parseTokenWhole("十二", true));
        assertEquals(Optional.of(BigInteger.valueOf(9)), NumeralValueParser.parseTokenWhole("٩", true));
        assertEquals(Optional.of(BigInteger.valueOf(5)), NumeralValueParser.parseTokenWhole("５", true));

        // The value form adds the non-integer numbers on top of the same gate.
        assertEquals(Optional.of("3/2"), NumeralValueParser.parseTokenValue("1.5", true).map(Object::toString));
        assertEquals(Optional.of("3/4"), NumeralValueParser.parseTokenValue("3/4", true).map(Object::toString));
        assertFalse(NumeralValueParser.parseTokenValue("mix", true).isPresent());

        // Deliberate boundary: an exponent and an enclosed number stay out of
        // the number comparisons even though they carry a numeric value; the
        // precomposed vulgar fractions are real numbers and count.
        assertFalse(NumeralValueParser.parseTokenValue("²", true).isPresent());
        assertEquals(Optional.of("3/4"), NumeralValueParser.parseTokenValue("¾", true).map(Object::toString));
        assertFalse(NumeralValueParser.parseTokenValue("⑩", true).isPresent());

        // Deliberate boundary: a Roman form without I, V or X is a unit or an
        // abbreviation more often than a number.
        assertFalse(NumeralValueParser.parseTokenWhole("L", true).isPresent());
        assertFalse(NumeralValueParser.parseTokenWhole("M", true).isPresent());
        assertFalse(NumeralValueParser.parseTokenWhole("CD", true).isPresent());

        // Latin-letter Roman numerals are the caller's decision, because there is
        // no telling them apart from ordinary uppercase words: the English
        // pronoun, a size, a version prefix and a handful of words read as
        // numbers where they are allowed and as prose where they are not.
        for (String roman : new String[] { "I", "V", "X", "XL", "MIX", "DIV" }) {
            assertTrue(roman, NumeralValueParser.parseTokenWhole(roman, true).isPresent());
            assertFalse(roman, NumeralValueParser.parseTokenWhole(roman, false).isPresent());
        }
        assertEquals(Optional.of(BigInteger.ONE), NumeralValueParser.parseTokenWhole("I", true));
        assertEquals(Optional.of(BigInteger.valueOf(1009)),
                NumeralValueParser.parseTokenWhole("MIX", true));

        // The switch is about Latin letters only: numerals of every other system,
        // the dedicated Roman code points included, read the same either way.
        for (String numeral : new String[] { "Ⅻ", "十二", "٩", "５", "12" }) {
            assertEquals(numeral, NumeralValueParser.parseTokenWhole(numeral, true),
                    NumeralValueParser.parseTokenWhole(numeral, false));
            assertTrue(numeral, NumeralValueParser.parseTokenWhole(numeral, false).isPresent());
        }

        // Arbitrary length: the value is exact, not a double.
        String big = "1234567890123456789012345678901234567890";
        assertEquals(Optional.of(new BigInteger(big)), NumeralValueParser.parseTokenWhole(big, true));
        assertNotEquals(NumeralValueParser.parseTokenValue("99999999999999999999", true),
                NumeralValueParser.parseTokenValue("99999999999999999998", true));
    }

    /**
     * The Number Forms block is never composed additively - forty is not four
     * twelves. The Roman rule sets write the value subtractively instead, in
     * the template's case, preferring the precomposed forms one to twelve.
     */
    @Test
    public void renderRomanFormsSubtractively() {
        assertEquals(Optional.of("ⅩⅬ"), NumeralValueParser.renderInSystemOf(BigInteger.valueOf(40), "Ⅳ"));
        assertEquals(Optional.of("ⅹⅼ"), NumeralValueParser.renderInSystemOf(BigInteger.valueOf(40), "ⅳ"));
        assertEquals(Optional.of("ⅯⅭⅯⅬⅩⅩⅩⅠⅤ"),
                NumeralValueParser.renderInSystemOf(BigInteger.valueOf(1984), "Ⅻ"));
        assertEquals(Optional.of("Ⅶ"), NumeralValueParser.renderInSystemOf(BigInteger.valueOf(7), "Ⅻ"));
        assertEquals(Optional.of("ⅶ"), NumeralValueParser.renderInSystemOf(BigInteger.valueOf(7), "ⅻ"));
    }

    /**
     * Counting rods are positional above the tens, so a greedy additive
     * spelling would be invalid notation: the block is read, never written.
     */
    @Test
    public void countingRodsAreReadButNotWritten() {
        String rods32 = new String(Character.toChars(0x1D36B)) + new String(Character.toChars(0x1D361));
        assertEquals(Optional.of(BigInteger.valueOf(32)),
                NumeralValueParser.parseTokenWhole(rods32, false));
        assertEquals(Optional.empty(),
                NumeralValueParser.renderInSystemOf(BigInteger.valueOf(432), rods32));
    }

    /**
     * A fraction value renders as the precomposed vulgar-fraction glyph when
     * the template is such a glyph and a glyph with exactly that value
     * exists; anything else stays unwritable.
     */
    @Test
    public void renderVulgarFractionsExactly() {
        NumeralValueParser.Rational quarter = NumeralValueParser.parseValue("1/4").orElseThrow();
        NumeralValueParser.Rational twoFifths = NumeralValueParser.parseValue("2/5").orElseThrow();
        NumeralValueParser.Rational fiveSevenths = NumeralValueParser.parseValue("5/7").orElseThrow();

        assertEquals(Optional.of("¼"), NumeralValueParser.renderVulgarFractionOf(quarter, "½"));
        assertEquals(Optional.of("⅖"), NumeralValueParser.renderVulgarFractionOf(twoFifths, "⅓"));
        // No precomposed glyph carries five sevenths.
        assertEquals(Optional.empty(), NumeralValueParser.renderVulgarFractionOf(fiveSevenths, "½"));
        // A non-fraction template is not written.
        assertEquals(Optional.empty(), NumeralValueParser.renderVulgarFractionOf(quarter, "12"));
    }

    /**
     * The sign numerals join the whole-token reading, so the match scorer
     * pairs them just like the insertion step does.
     */
    @Test
    public void wholeTokenFormReadsSignNumerals() {
        String mayanTwenty = new String(Character.toChars(0x1D2E1))
                + new String(Character.toChars(0x1D2E0));
        assertEquals(Optional.of(BigInteger.valueOf(20)),
                NumeralValueParser.parseTokenWhole(mayanTwenty, false));
        String aegeanLarge = new String(Character.toChars(0x10133));
        assertEquals(Optional.of(BigInteger.valueOf(90000)),
                NumeralValueParser.parseTokenWhole(aegeanLarge, false));
        // A fractional sign is a value, not a whole number.
        assertFalse(NumeralValueParser.parseTokenWhole("꠰", false).isPresent());
    }
}
