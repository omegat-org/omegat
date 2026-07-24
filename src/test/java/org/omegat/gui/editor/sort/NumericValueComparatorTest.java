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

package org.omegat.gui.editor.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

/**
 * Tests for {@link NumericValueComparator}: value-based ordering of the first
 * number in the text (across numbering systems), with text fallback.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumericValueComparatorTest {

    private final NumericValueComparator cmp = new NumericValueComparator(Collator.getInstance(Locale.ENGLISH));

    private int c(String a, String b) {
        return Integer.signum(cmp.compare(a, b));
    }

    @Test
    public void embeddedDecimalByValue() {
        assertTrue(c("item2", "item10") < 0);
        assertTrue(c("Section 10", "Section 9") > 0);
    }

    @Test
    public void nonDecimalSystemsByValue() {
        assertTrue("Kapitel XII > Kapitel III", c("Kapitel XII", "Kapitel III") > 0);
        assertTrue("十二 > 三", c("十二", "三") > 0);
    }

    @Test
    public void germanSourceLocaleReadsCommaDecimals() {
        NumericValueComparator de = new NumericValueComparator(Collator.getInstance(Locale.GERMANY), true,
                Locale.GERMANY);
        // "9,90" is nine point nine in a German source, so it sorts after 9.
        assertTrue(Integer.signum(de.compare("Das Modul kostet 9,90€ pro Monat.",
                "Die BSNR muss genau 9 Ziffern haben.")) > 0);
        assertTrue(Integer.signum(de.compare("Preis 1.234,56 gesamt", "Preis 1234 gesamt")) > 0);
    }

    @Test
    public void romanFreeModeTreatsRomanAsPlainText() {
        NumericValueComparator noRoman = new NumericValueComparator(Collator.getInstance(Locale.ENGLISH),
                false);
        // Default mode: "XL" is 40, so it carries a number and sorts before the
        // numberless word; Roman-free mode sees two numberless strings and
        // falls back to collation (spare < XL).
        assertTrue(c("XL", "spare") < 0);
        assertTrue(Integer.signum(noRoman.compare("XL", "spare")) > 0);
        // Single-codepoint Roman forms are excluded too: Ⅻ is numberless, so it
        // sorts after the numbered string instead of as 12 < 20.
        assertTrue(c("Ⅻ", "20") < 0);
        assertTrue(Integer.signum(noRoman.compare("Ⅻ", "20")) > 0);
        // All other numerals keep working in Roman-free mode.
        assertTrue(Integer.signum(noRoman.compare("item2", "item10")) < 0);
        assertTrue(Integer.signum(noRoman.compare("十二", "三")) > 0);
    }

    @Test
    public void stringsWithNumberSortBeforeStringsWithout() {
        assertTrue(c("item2", "hello") < 0);
        assertTrue(c("hello", "item2") > 0);
    }

    @Test
    public void numberlessStringsUseTextCollation() {
        assertTrue(c("apple", "banana") < 0);
        assertEquals(0, c("apple", "apple"));
    }

    @Test
    public void hugeValuesNoOverflow() {
        String huge = "item" + "9".repeat(40);
        assertTrue(c(huge, "item99") > 0);
    }

    @Test
    public void equalValueFallsBackToTextCollation() {
        // equal numeric value (2), different text -> non-zero, consistent with the collator
        assertTrue(c("item2", "chapter2") != 0);
        assertEquals(
                Integer.signum(Collator.getInstance(Locale.ENGLISH).compare("item2", "chapter2")),
                c("item2", "chapter2"));
        assertEquals(0, c("Section 2", "Section 2"));
    }

    @Test
    public void sortsAMixedListByFirstNumber() {
        List<String> list = new ArrayList<>(Arrays.asList("Chapter III", "Chapter 1", "Chapter 十", "Chapter II"));
        list.sort(cmp);
        // values: 1, 2 (II), 3 (III), 10 (十)
        assertEquals(Arrays.asList("Chapter 1", "Chapter II", "Chapter III", "Chapter 十"), list);
    }

    @Test
    public void japaneseComposedSevenDigitValuesSortByValue() {
        List<String> list = new ArrayList<>(Arrays.asList(
                "Betrag 五百二十万四十三 Yen.",   // 5200043
                "Betrag 二百五十万三十四 Yen.",   // 2500034
                "Betrag 四百三十万五十二 Yen.",   // 4300052
                "Betrag 三百四十万二十五 Yen."));  // 3400025
        list.sort(cmp);
        assertEquals(Arrays.asList(
                "Betrag 二百五十万三十四 Yen.",   // 2500034
                "Betrag 三百四十万二十五 Yen.",   // 3400025
                "Betrag 四百三十万五十二 Yen.",   // 4300052
                "Betrag 五百二十万四十三 Yen."),  // 5200043
                list);
    }

    @Test
    public void higherValueMixedScriptsByValue() {
        // 100 (百) < 1984 (roman) < 10000 (Ethiopic ፼) < 10^8 (一億)
        assertTrue(c("Zeile 百", "Jahr MCMLXXXIV") < 0);
        assertTrue(c("Jahr MCMLXXXIV", "Posten ፼") < 0);
        assertTrue(c("Posten ፼", "Summe 一億") < 0);
    }

    // --- negatives, decimals, fractions, vulgar fractions --------------------

    @Test
    public void theRequiredMixedOrdering() {
        // -5 < ¼ < ½ < 1 < 1.5 < 2 < 10 < 十二(=12), interleaved with a CJK numeral.
        List<String> list = new ArrayList<>(Arrays.asList(
                "10", "2", "¼", "-5", "½", "1.5", "十二", "1"));
        list.sort(cmp);
        assertEquals(Arrays.asList("-5", "¼", "½", "1", "1.5", "2", "10", "十二"), list);
    }

    @Test
    public void negativesSortBelowZeroAndByValue() {
        // -2 < -3/2 < -1 < -1/2 (nearer zero is larger)
        List<String> list = new ArrayList<>(Arrays.asList("-1", "-1/2", "-2", "-3/2"));
        list.sort(cmp);
        assertEquals(Arrays.asList("-2", "-3/2", "-1", "-1/2"), list);
    }

    @Test
    public void improperFractionsSortBetweenIntegers() {
        List<String> list = new ArrayList<>(Arrays.asList("1", "7/4", "2", "3/2"));
        list.sort(cmp);
        assertEquals(Arrays.asList("1", "3/2", "7/4", "2"), list);
    }

    @Test
    public void fractionVersusNearbyDecimals() {
        // 0.25 (¼) < 0.3 < 0.333... (⅓)
        List<String> list = new ArrayList<>(Arrays.asList("0.3", "¼", "⅓"));
        list.sort(cmp);
        assertEquals(Arrays.asList("¼", "0.3", "⅓"), list);
    }

    @Test
    public void numbersSortBeforeNonNumbersNegativesIncluded() {
        List<String> list = new ArrayList<>(Arrays.asList("hello", "item2", "world", "-5", "¾"));
        list.sort(cmp);
        assertEquals(Arrays.asList("-5", "¾", "item2", "hello", "world"), list);
    }

    @Test
    public void divisionByZeroDoesNotBreakTheSort() {
        // "1/0" is not a valid whole number, so it falls back to its first embedded
        // integer (1), exactly like a date/version. The point is that the sort never
        // throws and stays a valid total order; no Infinity/NaN ever reaches compare.
        List<String> list = new ArrayList<>(Arrays.asList("1/0", "2", "1", "1/2"));
        list.sort(cmp); // must not throw
        for (int i = 0; i + 1 < list.size(); i++) {
            assertTrue("ordered at " + i, c(list.get(i), list.get(i + 1)) <= 0);
        }
        // 1/2 (=0.5) is the smallest; 2 is the largest.
        assertEquals("1/2", list.get(0));
        assertEquals("2", list.get(list.size() - 1));
    }

    @Test
    public void equalValueDifferentSpellingFallsBackToCollation() {
        // ½, 2/4 and 0.5 are the same value -> non-zero, decided only by the collator.
        assertTrue("equal value, different text -> non-zero via collator", c("½", "2/4") != 0);
        assertEquals(Integer.signum(Collator.getInstance(Locale.ENGLISH).compare("1.5", "3/2")),
                c("1.5", "3/2"));
        assertEquals("identical strings compare equal", 0, c("3/2", "3/2"));
    }

    @Test
    public void totalOrderContractHoldsUnderStress() {
        // A shuffled mix of ties, signed zero, absents, huge values, negatives and
        // fractions must sort without "Comparison method violates its general contract".
        List<String> list = new ArrayList<>(Arrays.asList(
                "-0", "0", "2/4", "0.5", "1/3", "0.3333333", "-5", "十二", "12",
                "1" + "0".repeat(40), "hello", "1/0", "-3/2", "7/4", "¾", "world"));
        list.sort(cmp); // must not throw
        for (int i = 0; i + 1 < list.size(); i++) {
            assertTrue("ordered at " + i, cmp.compare(list.get(i), list.get(i + 1)) <= 0);
        }
    }

    // --- enclosed / historic single-code-point numerals (Mechanism C) --------

    @Test
    public void enclosedAndHistoricNumeralsSortByValue() {
        List<String> list = new ArrayList<>(Arrays.asList(
                "㉕", "𐄎", "⑼", "½", "-5", "𐄡"));
        list.sort(cmp);
        // -5 < ½(0.5) < 𐄎(8) < ⑼(9) < ㉕(25) < 𐄡(900)
        assertEquals(Arrays.asList("-5", "½", "𐄎", "⑼", "㉕", "𐄡"), list);
    }

    @Test
    public void mathematicalDigitsAndRomanShareValue() {
        // 𝟵 (math nine) == 9; Ⅷ (roman eight) == 8; both numeric, ordered by value.
        assertTrue(c("Ⅷ", "𝟵") < 0); // 8 < 9
        assertTrue(c("𝟵", "㉕") < 0); // 9 < 25
    }

    @Test
    public void emojiWithoutValueSortAsNonNumbers() {
        assertTrue(c("💯", "5") > 0);  // 💯 has no value -> after numbers
        assertTrue(c("🔟", "5") > 0);  // 🔟 keycap ten -> not a number
        assertTrue(c("5", "💯") < 0);
    }


    // --- alphabetical order differs from numeric order (the point of numeric sort) ---

    @Test
    public void romanAlphabeticalOrderDiffersFromNumeric() {
        Collator alpha = Collator.getInstance(Locale.ENGLISH);
        List<String> byNumeric = new ArrayList<>(Arrays.asList(
                "Norm IV", "Norm V", "Norm VIII", "Norm IX", "Norm X"));
        List<String> byAlpha = new ArrayList<>(byNumeric);
        byNumeric.sort(cmp);
        byAlpha.sort(alpha);
        // numeric: by value 4, 5, 8, 9, 10
        assertEquals(Arrays.asList("Norm IV", "Norm V", "Norm VIII", "Norm IX", "Norm X"), byNumeric);
        // alphabetical: IX sorts before V and VIII, so the order genuinely differs
        assertEquals(Arrays.asList("Norm IV", "Norm IX", "Norm V", "Norm VIII", "Norm X"), byAlpha);
        assertNotEquals(byNumeric, byAlpha);
        // crisp pairwise flip: numeric VIII < IX, but alphabetical IX < VIII
        assertTrue(c("Norm VIII", "Norm IX") < 0);
        assertTrue(alpha.compare("Norm IX", "Norm VIII") < 0);
    }

    @Test
    public void decimalAlphabeticalOrderDiffersFromNumeric() {
        Collator alpha = Collator.getInstance(Locale.ENGLISH);
        List<String> byNumeric = new ArrayList<>(Arrays.asList(
                "Schritt 2", "Schritt 10", "Schritt 21", "Schritt 100"));
        List<String> byAlpha = new ArrayList<>(byNumeric);
        byNumeric.sort(cmp);
        byAlpha.sort(alpha);
        // numeric: by value 2, 10, 21, 100
        assertEquals(Arrays.asList("Schritt 2", "Schritt 10", "Schritt 21", "Schritt 100"), byNumeric);
        // alphabetical: shorter/leading-digit order 10, 100, 2, 21
        assertEquals(Arrays.asList("Schritt 10", "Schritt 100", "Schritt 2", "Schritt 21"), byAlpha);
        assertNotEquals(byNumeric, byAlpha);
        // crisp pairwise flip: numeric 2 < 10, but alphabetical "10" < "2"
        assertTrue(c("Schritt 2", "Schritt 10") < 0);
        assertTrue(alpha.compare("Schritt 10", "Schritt 2") < 0);
    }

}
