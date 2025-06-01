/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
               2016 Aaron Madlon-Kay
               2018 Thomas Cordonnier
               2025 Hiroshi Miura
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for (some) static utility methods.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Thomas Cordonnier
 */
public class StringUtilTest {
    @Test
    public void testIsSubstringAfter() {
        assertFalse(StringUtil.isSubstringAfter("123456", 5, "67"));
        assertTrue(StringUtil.isSubstringAfter("123456", 5, "6"));
        assertTrue(StringUtil.isSubstringAfter("123456", 4, "56"));
        assertTrue(StringUtil.isSubstringAfter("123456", 0, "12"));
        assertTrue(StringUtil.isSubstringAfter("123456", 1, "23"));
    }

    @Test
    public void testIsTitleCase() {
        assertFalse(StringUtil.isTitleCase("foobar"));
        assertFalse(StringUtil.isTitleCase("fooBar"));
        assertFalse(StringUtil.isTitleCase("f1obar"));
        assertFalse(StringUtil.isTitleCase("FooBar"));
        assertTrue(StringUtil.isTitleCase("Fo1bar"));
        assertTrue(StringUtil.isTitleCase("Foobar"));
        // LATIN CAPITAL LETTER L WITH SMALL LETTER J (U+01C8)
        assertTrue(StringUtil.isTitleCase("\u01C8bcd"));
        assertFalse(StringUtil.isTitleCase("a\u01C8bcd"));

        // LATIN CAPITAL LETTER L WITH SMALL LETTER J (U+01C8)
        assertTrue(StringUtil.isTitleCase("\u01c8"));
        // LATIN CAPITAL LETTER LJ (U+01C7)
        assertFalse(StringUtil.isTitleCase("\u01c7"));
        // LATIN SMALL LETTER LJ (U+01C9)
        assertFalse(StringUtil.isTitleCase("\u01c9"));
    }

    @Test
    public void testIsSubstringBefore() {
        assertFalse(StringUtil.isSubstringBefore("123456", 1, "01"));
        assertTrue(StringUtil.isSubstringBefore("123456", 1, "1"));
        assertTrue(StringUtil.isSubstringBefore("123456", 2, "12"));
        assertTrue(StringUtil.isSubstringBefore("123456", 6, "56"));
        assertTrue(StringUtil.isSubstringBefore("123456", 5, "45"));
    }

    @Test
    public void testUnicodeNonBMP() {
        // MATHEMATICAL BOLD CAPITAL A (U+1D400)
        String test = "\uD835\uDC00";
        assertTrue(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertTrue(StringUtil.isTitleCase(test));

        // MATHEMATICAL BOLD CAPITAL A (U+1D400) x2
        test = "\uD835\uDC00\uD835\uDC00";
        assertTrue(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));

        // MATHEMATICAL BOLD SMALL A (U+1D41A)
        test = "\uD835\uDC1A";
        assertFalse(StringUtil.isUpperCase(test));
        assertTrue(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));

        // MATHEMATICAL BOLD CAPITAL A + MATHEMATICAL BOLD SMALL A
        test = "\uD835\uDC00\uD835\uDC1A";
        assertFalse(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertTrue(StringUtil.isTitleCase(test));

        // MATHEMATICAL BOLD SMALL A + MATHEMATICAL BOLD CAPITAL A
        test = "\uD835\uDC1A\uD835\uDC00";
        assertFalse(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));
    }

    @Test
    public void testAlphanumericStringCase() {
        String test = "MQL5";
        assertTrue(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));
        assertFalse(StringUtil.isMixedCase(test));

        test = "mql5";
        assertFalse(StringUtil.isUpperCase(test));
        assertTrue(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));
        assertFalse(StringUtil.isMixedCase(test));

        test = "Mql5";
        assertFalse(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertTrue(StringUtil.isTitleCase(test));
        assertFalse(StringUtil.isMixedCase(test));

        test = "mQl5";
        assertFalse(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));
        assertTrue(StringUtil.isMixedCase(test));
    }

    @Test
    public void testEmptyStringCase() {
        String test = null;
        try {
            StringUtil.isUpperCase(test);
            fail("Should throw an NPE");
        } catch (NullPointerException ex) {
            // OK
        }
        try {
            StringUtil.isLowerCase(test);
            fail("Should throw an NPE");
        } catch (NullPointerException ex) {
            // OK
        }
        try {
            StringUtil.isTitleCase(test);
            fail("Should throw an NPE");
        } catch (NullPointerException ex) {
            // OK
        }
        try {
            StringUtil.toTitleCase(test, Locale.ENGLISH);
            fail("Should throw an NPE");
        } catch (NullPointerException ex) {
            // OK
        }

        test = "";
        assertFalse(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isTitleCase(test));
        assertEquals("", StringUtil.toTitleCase("", Locale.ENGLISH));
    }

    @Test
    public void testIsWhiteSpace() {
        try {
            StringUtil.isWhiteSpace(null);
            fail("Should throw an NPE");
        } catch (NullPointerException ex) {
            // OK
        }
        assertFalse(StringUtil.isWhiteSpace(""));
        assertTrue(StringUtil.isWhiteSpace(" "));
        assertFalse(StringUtil.isWhiteSpace(" a "));
        // SPACE (U+0020) + IDEOGRAPHIC SPACE (U+3000)
        assertTrue(StringUtil.isWhiteSpace(" \u3000"));
        // We consider whitespace but Character.isWhiteSpace(int) doesn't:
        // NO-BREAK SPACE (U+00A0) + FIGURE SPACE (U+2007) + NARROW NO-BREAK
        // SPACE (U+202F)
        assertTrue(StringUtil.isWhiteSpace("\u00a0\u2007\u202f"));
    }

    @Test
    public void testIsMixedCase() {
        assertTrue(StringUtil.isMixedCase("ABc"));
        assertTrue(StringUtil.isMixedCase("aBc"));
        // This is title case, not mixed:
        assertFalse(StringUtil.isMixedCase("Abc"));
        // Non-letter characters should not affect the result:
        assertTrue(StringUtil.isMixedCase(" {ABc"));
    }

    @Test
    public void testNonWordCase() {
        String test = "{";
        assertFalse(StringUtil.isLowerCase(test));
        assertFalse(StringUtil.isUpperCase(test));
        assertFalse(StringUtil.isTitleCase(test));
        assertFalse(StringUtil.isMixedCase(test));
    }

    @Test
    public void testToTitleCase() {
        Locale locale = Locale.ENGLISH;
        assertEquals("Abc", StringUtil.toTitleCase("abc", locale));
        assertEquals("Abc", StringUtil.toTitleCase("ABC", locale));
        assertEquals("Abc", StringUtil.toTitleCase("Abc", locale));
        assertEquals("Abc", StringUtil.toTitleCase("abc", locale));
        assertEquals("Abc", StringUtil.toTitleCase("aBC", locale));
        assertEquals("A", StringUtil.toTitleCase("a", locale));
        // LATIN SMALL LETTER NJ (U+01CC) -> LATIN CAPITAL LETTER N WITH SMALL
        // LETTER J (U+01CB)
        assertEquals("\u01CB", StringUtil.toTitleCase("\u01CC", locale));
        // LATIN SMALL LETTER I (U+0069) -> LATIN CAPITAL LETTER I WITH DOT
        // ABOVE (U+0130) in Turkish
        assertEquals("\u0130jk", StringUtil.toTitleCase("ijk", new Locale("tr")));
        // Non-letters in front
        assertEquals("'Good day, sir.'", StringUtil.toTitleCase("'GOOD DAY, SIR.'", locale));
        // No letters at all
        String test = "!@#$%^&*()-=\"\\";
        assertEquals(test, StringUtil.toTitleCase(test, locale));
    }

    @Test
    public void testCompressSpace() {
        assertEquals("One Two Three Four Five", StringUtil.compressSpaces(" One Two\nThree   Four\r\nFive "));
        assertEquals("Six seven", StringUtil.compressSpaces("Six\tseven"));
    }

    @Test
    public void testIsValidXMLChar() {
        assertFalse(StringUtil.isValidXMLChar(0x01));
        assertTrue(StringUtil.isValidXMLChar(0x09));
        assertTrue(StringUtil.isValidXMLChar(0x0A));
        assertTrue(StringUtil.isValidXMLChar(0x0D));

        assertTrue(StringUtil.isValidXMLChar(0x21));
        assertFalse(StringUtil.isValidXMLChar(0xD800));

        assertTrue(StringUtil.isValidXMLChar(0xE000));
        assertFalse(StringUtil.isValidXMLChar(0xFFFE));

        assertTrue(StringUtil.isValidXMLChar(0x10000));
        assertFalse(StringUtil.isValidXMLChar(0x110000));
    }

    @Test
    public void testCapitalizeFirst() {
        Locale locale = Locale.ENGLISH;
        assertEquals("Abc", StringUtil.capitalizeFirst("abc", locale));
        assertEquals("ABC", StringUtil.capitalizeFirst("ABC", locale));
        assertEquals("Abc", StringUtil.capitalizeFirst("Abc", locale));
        assertEquals("Abc", StringUtil.capitalizeFirst("abc", locale));
        assertEquals("AbC", StringUtil.capitalizeFirst("abC", locale));
        assertEquals("A", StringUtil.capitalizeFirst("a", locale));
        // LATIN SMALL LETTER NJ (U+01CC) -> LATIN CAPITAL LETTER N WITH SMALL
        // LETTER J (U+01CB)
        assertEquals("\u01CB", StringUtil.capitalizeFirst("\u01CC", locale));
        // LATIN SMALL LETTER I (U+0069) -> LATIN CAPITAL LETTER I WITH DOT
        // ABOVE (U+0130) in Turkish
        assertEquals("\u0130jk", StringUtil.capitalizeFirst("ijk", new Locale("tr")));
        // Empty string -> empty string (like toLowerCase, toUpperCase)
        assertEquals("", StringUtil.capitalizeFirst("", locale));
    }

    @Test
    public void testMatchCapitalization() {
        Locale locale = Locale.ENGLISH;
        String text = "foo";
        // matchTo is empty -> return original text
        assertEquals(text, StringUtil.matchCapitalization(text, null, locale));
        assertEquals(text, StringUtil.matchCapitalization(text, "", locale));
        // text is empty -> return original text
        assertEquals("", StringUtil.matchCapitalization("", "Foo", locale));
        // text starts with matchTo -> return original text
        assertEquals(text, StringUtil.matchCapitalization(text, text + "BAR", locale));
        // matchTo is title case
        assertEquals("Foo", StringUtil.matchCapitalization(text, "Abc", locale));
        assertEquals("Foo", StringUtil.matchCapitalization(text, "A", locale));
        // matchTo is lower case
        assertEquals("foo", StringUtil.matchCapitalization("FOO", "lower", locale));
        assertEquals("foo", StringUtil.matchCapitalization("fOo", "l", locale));
        // matchTo is upper case
        assertEquals("FOO", StringUtil.matchCapitalization(text, "UPPER", locale));
        assertEquals("FOO", StringUtil.matchCapitalization("fOo", "UP", locale));
        // Interpreted as title case
        assertEquals("FOo", StringUtil.matchCapitalization("fOo", "U", locale));
        // matchTo is mixed or not cased
        assertEquals(text, StringUtil.matchCapitalization(text, "bAzZ", locale));
        assertEquals(text, StringUtil.matchCapitalization(text, ".", locale));
    }

    @Test
    public void testFirstN() {
        // MATHEMATICAL BOLD CAPITAL A (U+1D400) x2
        String test = "\uD835\uDC00\uD835\uDC00";
        assertTrue(StringUtil.firstN(test, 0).isEmpty());
        assertEquals("\uD835\uDC00", StringUtil.firstN(test, 1));
        assertEquals(test, StringUtil.firstN(test, 2));
        assertEquals(test, StringUtil.firstN(test, 100));
    }

    @Test
    public void testTruncateString() {
        // MATHEMATICAL BOLD CAPITAL A (U+1D400) x3
        String test = "\uD835\uDC00\uD835\uDC00\uD835\uDC00";
        try {
            StringUtil.truncate(test, 0);
            fail();
        } catch (IndexOutOfBoundsException ex) {
            // Ignore
        }
        assertEquals(String.valueOf(StringUtil.TRUNCATE_CHAR), StringUtil.truncate(test, 1));
        assertEquals("\uD835\uDC00" + StringUtil.TRUNCATE_CHAR, StringUtil.truncate(test, 2));
        assertEquals(test, StringUtil.truncate(test, 3));
        assertEquals(test, StringUtil.truncate(test, 100));
    }

    @Test
    public void testNormalizeWidth() {
        String test = "Foo 123 " // ASCII
                + "\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13 " // Full-width
                                                                // alphanumerics
                + "\uFF01\uFF1F\uFF08\uFF09 " // Full-width punctuation
                + "\u3371 " // Squared Latin Abbreviations
                + "\u2100 " // Letter-Like Symbols
                + "\u30AC\u30D1\u30AA " // Katakana
                + "\uD55C\uAD6D\uC5B4 " // Full-width Hangul
                + "\u314E\u314F\u3134"; // Full-width Jamo
        assertEquals("Foo 123 Foo 123 !?() hPa a/c \u30AC\u30D1\u30AA \uD55C\uAD6D\uC5B4 \u314E\u314F\u3134",
                StringUtil.normalizeWidth(test));
        test = "\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13 " // Full-width
                                                             // alphanumerics
                + "Foo 123 !?() " // ASCII
                + "\uFF76\uFF9E\uFF8A\uFF9F\uFF75 " // Half-width Katakana
                + "\uFFBE\uFFC2\uFFA4"; // Half-width Jamo
        assertEquals("Foo 123 Foo 123 !?() \u30AC\u30D1\u30AA \u314E\uFFC2\u3134",
                StringUtil.normalizeWidth(test));
        test = "\uff21\uff22\uff23\uff0e\uff11\uff12\uff13\uff04\uff01";
        assertEquals("ABC.123$!", StringUtil.normalizeWidth(test));
        test = "\u30a2\uff71\u30ac\uff76\u3099\u3000";
        assertEquals("\u30a2\u30a2\u30ac\u30ac ", StringUtil.normalizeWidth(test));
    }

    // U+00A0 NO-BREAK SPACE
    private static final String ALPHA_WITH_NOBREAK_SPACE = "ABC\u00a0";

    @Test
    public void testRstrip() {
        assertEquals("", StringUtil.rstrip(""));
        assertEquals("", StringUtil.rstrip(" "));
        assertEquals("ABC", StringUtil.rstrip("ABC"));
        assertEquals("ABC", StringUtil.rstrip("ABC "));
        assertEquals(" ABC", StringUtil.rstrip(" ABC "));
        assertEquals("ABC", StringUtil.rstrip("ABC       "));
        assertEquals(ALPHA_WITH_NOBREAK_SPACE, StringUtil.rstrip(ALPHA_WITH_NOBREAK_SPACE));
        assertEquals("Test", StringUtil.rstrip("Test\n "));
        assertEquals("Line1 Line2", StringUtil.rstrip("Line1 Line2   "));
        assertEquals("  Trimmed", StringUtil.rstrip("  Trimmed  "));
        assertEquals("MixedWhitespace", StringUtil.rstrip("MixedWhitespace\t\n "));
        try {
            StringUtil.rstrip(null);
            fail();
        } catch (NullPointerException ex) {
            // Should fail when stripping null string.
        }
    }

    @Test
    public void testCaseConversion() {
        // Strings which are not affected by replaceCase despite the backslash
        List<String> tests = new ArrayList<>();
        tests.add("\\foo");
        tests.add("foo\\bar");
        tests.add("\\foo\\bar");
        tests.add("\\foo\\");
        tests.add("\\foo\\x");
        tests.add("\\");

        for (String s : tests) {
            assertEquals(s, StringUtil.replaceCase(s, Locale.ENGLISH));
        }

        // Strings where backslash is used as an escape sequence
        tests.clear();
        tests.add("foo\\\\bar"); // double backslash => simple backslash
        tests.add("foo\\$bar"); // backslash + dollar => dollar
        tests.add("\\foo\\$bar"); // simple backslash, then backslash + dollar

        for (String s : tests) {
            assertEquals(s.replace("\\\\", "\\").replace("\\$", "$"),
                    StringUtil.replaceCase(s, Locale.ENGLISH));
        }

        // Test normal behaviour of replace case sequences
        assertEquals("\\hello This is a test",
                StringUtil.replaceCase("\\hello \\uthis is a test", Locale.ENGLISH));
        assertEquals("This is a test", StringUtil.replaceCase("\\uthis is a test", Locale.ENGLISH));
        assertEquals("tHIS IS A TEST", StringUtil.replaceCase("\\lTHIS IS A TEST", Locale.ENGLISH)); // lc
                                                                                                     // first
        assertEquals("tHIS IS A TEST", StringUtil.replaceCase("\\l\\Uthis is a test", Locale.ENGLISH)); // lc
                                                                                                        // first
                                                                                                        // +
                                                                                                        // uc
        assertEquals("THIS IS A TEST", StringUtil.replaceCase("\\Uthis is a test", Locale.ENGLISH)); // uc
                                                                                                     // all
        assertEquals("THIS IS a test", StringUtil.replaceCase("\\Uthis is\\E a test", Locale.ENGLISH)); // uc
                                                                                                        // until
                                                                                                        // E
        assertEquals("THIS is A TEST", StringUtil.replaceCase("\\Uthis\\E is \\Ua test", Locale.ENGLISH)); // uc
                                                                                                           // until
                                                                                                           // E
        // Test that behavior changes for Turkish
        assertEquals("Istanbul", StringUtil.replaceCase("\\uistanbul", Locale.ENGLISH)); // English
                                                                                         // version
        assertEquals("\u0130stanbul", StringUtil.replaceCase("\\uistanbul", new Locale("tr"))); // Turkish
                                                                                                // version
    }

    @Test
    public void testReplaceCaseBasicFunctionality() {
        Locale locale = Locale.ENGLISH;
        assertEquals("TEst", StringUtil.replaceCase("\\uTEst", locale));
        assertEquals("tEST", StringUtil.replaceCase("\\lTEST", locale));
        assertEquals("TESTING", StringUtil.replaceCase("\\UTestING", locale));
        assertEquals("TESTIing test", StringUtil.replaceCase("\\UTesti\\Eing test", locale));
        assertEquals("tEST ME", StringUtil.replaceCase("\\l\\UTest ME", locale));
    }

    @Test
    public void testReplaceCaseEscapeSequences() {
        Locale locale = Locale.ENGLISH;
        // Test escape characters
        assertEquals("\\Path\\To\\File", StringUtil.replaceCase("\\\\Path\\\\To\\\\File", locale));
        assertEquals("$!", StringUtil.replaceCase("\\$!", locale));
        assertEquals("$var", StringUtil.replaceCase("\\$var", locale));
        assertEquals("D:\\\\FOLDER", StringUtil.replaceCase("\\UD:\\\\Folder", locale));
    }

    @Test
    public void testReplaceCaseEdgeCases() {
        Locale locale = Locale.ENGLISH;
        // Test null and empty strings
        try {
            StringUtil.replaceCase(null, locale);
            fail();
        } catch (NullPointerException ex) {
            // expected
        }
        assertEquals("", StringUtil.replaceCase("", locale));
        // Test with no special sequences
        assertEquals("Hello, World!", StringUtil.replaceCase("Hello, World!", locale));
        assertEquals("HELLO", StringUtil.replaceCase("\\UHello", locale));
    }

    @Test
    public void testConvertToList() {
        assertEquals(Arrays.asList("omegat", "level1", "level2"),
                StringUtil.convertToList("omegat level1 level2"));
        assertEquals(Arrays.asList("omegat", "level1", "level2"),
                StringUtil.convertToList("omegat  level1  level2"));
        assertEquals(Arrays.asList("omegat", "level1", "level2"),
                StringUtil.convertToList("  omegat level1 level2  "));
        assertEquals(Arrays.asList("omegat", "level1", "level2"),
                StringUtil.convertToList("  omegat   level1  level2  "));
    }

    @Test
    public void testNormalizeWidthConversion() {
        assertEquals("ABC123", StringUtil.normalizeWidth("\uFF21\uFF22\uFF23\uFF11\uFF12\uFF13"));
        assertEquals("abc123", StringUtil.normalizeWidth("\uFF41\uFF42\uFF43\uFF11\uFF12\uFF13"));
        assertEquals("Test String", StringUtil
                .normalizeWidth("\uFF34\uFF45\uFF53\uFF54\u3000\uFF33\uFF54\uFF52\uFF49\uFF4E\uFF47"));
    }

    @Test
    public void testNormalizeWidthSpecialCharacters() {
        assertEquals("Hello, World!", StringUtil.normalizeWidth(
                "\uFF28\uFF45\uFF4C\uFF4C\uFF4F\uFF0C\u3000\uFF37\uFF4F\uFF52\uFF4C\uFF44\uFF01"));
        assertEquals("!?(){}", StringUtil.normalizeWidth("\uFF01\uFF1F\uFF08\uFF09\uFF5B\uFF5D"));
        assertEquals(" ", StringUtil.normalizeWidth("\u3000"));
    }

    @Test
    public void testNormalizeWidthEdgeCases() {
        assertEquals("", StringUtil.normalizeWidth(""));
        assertEquals("Already normalized", StringUtil.normalizeWidth("Already normalized"));
        try {
            StringUtil.normalizeWidth(null);
            fail();
        } catch (NullPointerException ignored) {
            // expected
        }
    }

    @Test
    public void testWrapBasicFunctionality() {
        // Test wrapping normal text into multiple lines
        assertEquals("This is\na test", StringUtil.wrap("This is a test", 7));
        assertEquals("Hello\nWorld", StringUtil.wrap("Hello World", 6));

        // Test wrapping text with multiple spaces
        assertEquals("This is a\ndemo", StringUtil.wrap("This is a demo", 10));

        // Test no wrapping with long wrap length
        assertEquals("Test string", StringUtil.wrap("Test string", 20));
    }

    @Test
    public void testWrapEdgeCases() {
        // Test empty string
        assertEquals("", StringUtil.wrap("", 5));

        // Test string shorter than wrap length
        assertEquals("Short", StringUtil.wrap("Short", 10));

        // Test single word longer than wrap length
        assertEquals("Longword", StringUtil.wrap("Longword", 5));

        try {
            StringUtil.wrap(null, 5);
            fail();
        } catch (NullPointerException ignored) {
            // expected
        }
    }

    @Test
    public void testCompareToNullable() {
        assertEquals(0, StringUtil.compareToNullable(null, null));
        assertEquals(0, StringUtil.compareToNullable("a", "a"));
        assertEquals(1, StringUtil.compareToNullable("a", null));
        assertEquals(-1, StringUtil.compareToNullable(null, "a"));
        assertEquals(-1, StringUtil.compareToNullable("a", "b"));
        assertEquals(1, StringUtil.compareToNullable("b", "a"));
        assertEquals(32, StringUtil.compareToNullable("a", "A"));
        assertEquals(-32, StringUtil.compareToNullable("A", "a"));
    }

    @Test
    public void testReplaceSquaredLatinAbbreviations() {
        // Test valid squared Latin abbreviations
        assertEquals("hPa", StringUtil.normalizeWidth("\u3371"));
        assertEquals("gal", StringUtil.normalizeWidth("\u33FF"));
        // Test valid squared Latin abbreviations with multiple replacements
        assertEquals("cm³", StringUtil.normalizeWidth("㎤"));
        // Test valid squared Latin abbreviations in edge case 0x33DF
        assertEquals("a/m", StringUtil.normalizeWidth("㏟"));
    }

    @Test
    public void testProcessKatakana() {
        // Test valid Half-width Katakana
        assertEquals("\u30AB", StringUtil.normalizeWidth("\uFF76"));
        // Half-width "Voicing mark"
        assertEquals("\u3099", StringUtil.normalizeWidth("\uFF9E"));
        // Combine Katakana and symbols
        // Half-width "Ka" + "Voicing mark" -> "Ga"
        assertEquals("\u30AC", StringUtil.normalizeWidth("\uFF76\uFF9E"));
    }

    @Test
    public void testProcessHungle() {
        // Test valid Hangul compatibility characters
        assertEquals("\u3164", StringUtil.normalizeWidth("\uFFA0")); // Ensure it's replaced correctly
        // First valid Hangul char in the range
        assertEquals("\u3161", StringUtil.normalizeWidth("\uFFDA"));
        // Last valid Hangul char in the range
        assertEquals("\u25CB", StringUtil.normalizeWidth("\uFFEE"));
        // Hangul character in range with no replacement
        assertEquals("\uFFDD", StringUtil.normalizeWidth("\uFFDD"));
    }

    @Test
    public void testStripFromEnd() {
        // Test removing a single suffix
        assertEquals("Hello World", StringUtil.stripFromEnd("Hello World!!!", "!!!"));
        assertEquals("Hello", StringUtil.stripFromEnd("Hello..", ".."));

        // Test removing multiple suffixes
        assertEquals("Hello World", StringUtil.stripFromEnd("Hello World!!!", "!!!", "...", "??"));
        assertEquals("Hello", StringUtil.stripFromEnd("Hello..!!", "!!", ".."));

        // Test no match
        assertEquals("Hello World", StringUtil.stripFromEnd("Hello World", "!!!"));
        assertEquals("SampleText", StringUtil.stripFromEnd("SampleText", "Suffix"));

        // Test empty suffix
        assertEquals("TestString", StringUtil.stripFromEnd("TestString", ""));

        // Test null inputs
        assertNull(StringUtil.stripFromEnd(null, "suffix"));
        assertEquals("String", StringUtil.stripFromEnd("String", (String[]) null));

        // Test empty string
        assertEquals("", StringUtil.stripFromEnd("", "suffix"));
    }
}
