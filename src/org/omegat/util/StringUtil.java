/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel and Tiago Saboga
               2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2008 Andrzej Sawula
               2010-2013 Alex Buloichik
               2015 Zoltan Bartko, Aaron Madlon-Kay
               2016 Aaron Madlon-Kay
               2018 Thomas Cordonnier
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

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

/**
 * Utilities for string processing.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Tiago Saboga
 * @author Zoltan Bartko
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Thomas Cordonnier
 */
public final class StringUtil {

    private StringUtil() {
    }

    public static final char TRUNCATE_CHAR = '…';

    /**
     * Check if string is empty, i.e. null or length==0
     */
    public static boolean isEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Returns true if the input has at least one letter and all letters are
     * lower case.
     */
    public static boolean isLowerCase(final String input) {
        if (input.isEmpty()) {
            return false;
        }
        boolean hasLetters = false;
        int i = 0;
        int cp;
        while (i < input.length()) {
            cp = input.codePointAt(i);
            if (Character.isLetter(cp)) {
                hasLetters = true;
                if (!Character.isLowerCase(cp)) {
                    return false;
                }
            }
            i += Character.charCount(cp);
        }
        return hasLetters;
    }

    /**
     * Returns true if the input is upper case.
     */
    public static boolean isUpperCase(final String input) {
        if (input.isEmpty()) {
            return false;
        }
        boolean hasLetters = false;
        int i = 0;
        int cp;
        while (i < input.length()) {
            cp = input.codePointAt(i);
            if (Character.isLetter(cp)) {
                hasLetters = true;
                if (!Character.isUpperCase(cp)) {
                    return false;
                }
            }
            i += Character.charCount(cp);
        }
        return hasLetters;
    }

    /**
     * Returns true if the input has both upper case and lower case letters, but
     * is not title case.
     */
    public static boolean isMixedCase(final String input) {
        if (input.isEmpty() || input.codePointCount(0, input.length()) < 2) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        int i = 0;
        int cp;
        while (i < input.length()) {
            cp = input.codePointAt(i);
            if (Character.isLetter(cp)) {
                // Don't count the first cp as upper to allow for title case
                if (Character.isUpperCase(cp) && i > 0) {
                    hasUpper = true;
                } else if (Character.isLowerCase(cp)) {
                    hasLower = true;
                }
                if (hasUpper && hasLower) {
                    return true;
                }
            }
            i += Character.charCount(cp);
        }
        return false;
    }

    /**
     * Returns true if the input is title case, meaning the first character is
     * UpperCase or TitleCase* and the rest of the string (if present) is
     * LowerCase.
     * <p>
     * *There are exotic characters that are neither UpperCase nor LowerCase,
     * but are TitleCase: e.g. LATIN CAPITAL LETTER L WITH SMALL LETTER J
     * (U+01C8)<br>
     * These are handled correctly.
     */
    public static boolean isTitleCase(final String input) {
        if (input.isEmpty()) {
            return false;
        }
        if (input.codePointCount(0, input.length()) > 1) {
            return isTitleCase(input.codePointAt(0))
                    && isLowerCase(input.substring(input.offsetByCodePoints(0, 1)));
        } else {
            return isTitleCase(input.codePointAt(0));
        }
    }

    public static boolean isTitleCase(int codePoint) {
        // True if is actual title case, or if is upper case and has no separate
        // title case variant.
        return Character.isTitleCase(codePoint)
                || (Character.isUpperCase(codePoint) && Character.toTitleCase(codePoint) == codePoint);
    }

    /**
     * Returns true if the input consists only of whitespace characters
     * (including non-breaking characters that are false according to
     * {@link Character#isWhitespace(int)}).
     */
    public static boolean isWhiteSpace(final String input) {
        if (input.isEmpty()) {
            return false;
        }
        int i = 0;
        int cp;
        while (i < input.length()) {
            cp = input.codePointAt(i);
            if (!isWhiteSpace(cp)) {
                return false;
            }
            i += Character.charCount(cp);
        }
        return true;
    }

    /**
     * Returns true if the input is a whitespace character (including
     * non-breaking characters that are false according to
     * {@link Character#isWhitespace(int)}).
     */
    public static boolean isWhiteSpace(int codePoint) {
        return Character.isWhitespace(codePoint) || codePoint == '\u00A0' || codePoint == '\u2007'
                || codePoint == '\u202F';
    }

    public static boolean isCJK(String input) {
        if (input.isEmpty()) {
            return false;
        }
        int i = 0;
        int cp;
        while (i < input.length()) {
            cp = input.codePointAt(i);
            // Anything less than CJK Radicals Supplement is "not CJK".
            // Everything else is.
            if (cp < '⺀') {
                return false;
            }
            i += Character.charCount(cp);
        }
        return true;
    }

    public static String capitalizeFirst(String text, Locale locale) {
        if (text.isEmpty()) {
            return text;
        }
        int remainder = text.offsetByCodePoints(0, 1);
        String firstCP = text.substring(0, remainder);
        return StringUtil.toTitleCase(firstCP, locale) + text.substring(remainder);
    }

    /**
     * Interpret the case replacement language used in regular expressions:
     * <ul>
     * <li>backslash u = uppercase next letter
     * <li>backslash l = lowercase next letter
     * <li>backslash U = uppercase next letters until backslash E or end
     * <li>backslash L = lowercase next letters until backslash E or end
     * <li>backslash u + backslash L = uppercase next letter then lowercase all
     * until backslash E
     * <li>backslash l + backslash U = lowercase next letter then uppercase all
     * until backslash E
     * </ul>
     * <p>
     * Warning: This method works directly with the input string. Perform other
     * substitutions (e.g., variable conversion) before calling this method;
     * otherwise, unconverted substitutions will not receive proper case
     * handling.
     */
    public static String replaceCase(@NotNull String input, Locale locale) {
        if (!input.startsWith("\\")) {
            int firstBackslashIndex = input.indexOf("\\");
            if (firstBackslashIndex == -1) {
                return input; // No special formatting required
            }
            // Handle prefix before the backslash and process the rest
            // recursively
            return input.substring(0, firstBackslashIndex)
                    + replaceCase(input.substring(firstBackslashIndex), locale);
        }
        // Double symbols are longer, so they must be treated first
        if (input.startsWith("\\u\\L")) {
            return input.substring(4, 5).toUpperCase(locale)
                    + replaceCase("\\L" + input.substring(5), locale);
        }
        if (input.startsWith("\\l\\U")) {
            return input.substring(4, 5).toLowerCase(locale)
                    + replaceCase("\\U" + input.substring(5), locale);
        }
        // Handle specific escape sequences and transformations
        if (input.startsWith("\\\\")) { // Escaped backslash
            return "\\" + replaceCase(input.substring(2), locale);
        }
        if (input.startsWith("\\$")) { // Escaped dollar sign
            return "$" + replaceCase(input.substring(2), locale);
        }

        // Handle case transformations
        if (input.startsWith("\\U") || input.startsWith("\\L")) {
            return extractTransformedText(input, locale);
        }
        if (input.startsWith("\\u") || input.startsWith("\\l")) {
            return handleCapitalizationReplacement(input, locale);
        }

        // For unrecognized escape sequences, preserve the slash
        return "\\" + replaceCase(input.substring(1), locale);
    }

    /**
     * Handles single-letter capitalization transformations: \\u and \l.
     */
    private static String handleCapitalizationReplacement(String input, Locale locale) {
        String firstLetter = input.substring(2, 3);
        String remainingText = input.substring(3);

        if (input.startsWith("\\u")) {
            return firstLetter.toUpperCase(locale) + replaceCase(remainingText, locale);
        }
        if (input.startsWith("\\l")) {
            return firstLetter.toLowerCase(locale) + replaceCase(remainingText, locale);
        }
        return input;
    }

    /**
     * Handles multi-character transformations up to delimiters: \U...\E and
     * \L...\E.
     */
    private static String extractTransformedText(String input, Locale locale) {
        boolean toUpperCase = input.startsWith("\\U");
        String transformedText;
        if (input.contains("\\E")) {
            transformedText = input.substring(2);
        } else {
            transformedText = input.substring(2).replace("\\L", "\\E\\L").replace("\\U", "\\E\\U");
        }

        int delimiterIndex = transformedText.indexOf("\\E");
        if (delimiterIndex == -1) {
            return toUpperCase ? transformedText.toUpperCase(locale) : transformedText.toLowerCase(locale);
        }

        String prefix = transformedText.substring(0, delimiterIndex);
        String suffix = transformedText.substring(delimiterIndex + 2);
        return (toUpperCase ? prefix.toUpperCase(locale) : prefix.toLowerCase(locale))
                + replaceCase(suffix, locale);
    }

    public static String matchCapitalization(String text, String matchTo, Locale locale) {
        if (StringUtil.isEmpty(matchTo) || StringUtil.isEmpty(text)) {
            return text;
        }
        // If input matches term exactly, don't change anything
        if (text.startsWith(matchTo)) {
            return text;
        }

        // If matching to title case (or 1 upper char), capitalize first letter.
        // Don't turn into title case because the text may be e.g. a phrase
        // with intentional mixed casing.
        if (StringUtil.isTitleCase(matchTo)) {
            return capitalizeFirst(text, locale);
        }
        // If matching to lower, turn into lower.
        if (StringUtil.isLowerCase(matchTo)) {
            return text.toLowerCase(locale);
        }
        // If matching to upper (at least 2 chars; otherwise would have hit
        // isTitleCase()
        // above), turn into upper.
        if (StringUtil.isUpperCase(matchTo)) {
            return text.toUpperCase(locale);
        }
        return text;
    }

    /**
     * Convert text to title case according to the supplied locale.
     */
    public static String toTitleCase(String text, Locale locale) {
        if (text.isEmpty()) {
            return text;
        }
        int firstLetterIndex = 0;
        int cp;
        while (firstLetterIndex < text.length()) {
            cp = text.codePointAt(firstLetterIndex);
            if (Character.isLetter(cp)) {
                break;
            }
            firstLetterIndex += Character.charCount(cp);
        }
        if (firstLetterIndex == text.length()) {
            return text;
        }
        int firstTitleCase = Character.toTitleCase(text.codePointAt(firstLetterIndex));
        int remainderOffset = text.offsetByCodePoints(firstLetterIndex, 1);
        // If the first codepoint has an actual title case variant (rare), use
        // that.
        // Otherwise convert first codepoint to upper case according to locale.
        String first = Character.isTitleCase(firstTitleCase)
                ? String.valueOf(Character.toChars(firstTitleCase))
                : text.substring(0, remainderOffset).toUpperCase(locale);
        return first + text.substring(remainderOffset).toLowerCase(locale);
    }

    /**
     * Returns first not null object from list, or null if all values is null.
     */
    @SafeVarargs
    public static <T> T nvl(T... values) {
        for (T val : values) {
            if (val != null) {
                return val;
            }
        }
        return null;
    }

    /**
     * Returns first non-zero object from list, or zero if all values is null.
     */
    public static long nvlLong(long... values) {
        for (long value : values) {
            if (value != 0) {
                return value;
            }
        }
        return 0;
    }

    /**
     * Compares two objects of type T that may be null.
     *
     * @param <T> the type of objects to be compared, which must implement Comparable
     * @param v1 the first object to compare, which may be null
     * @param v2 the second object to compare, which may be null
     * @return a negative integer, zero, or a positive integer as the first argument
     *         is less than, equal to, or greater than the second
     * @deprecated
     */
    @Deprecated
    public static <T extends Comparable<T>> int compareToWithNulls(T v1, T v2) {
        return compareToNullable(v1, v2);
    }

    /**
     * Compares two objects of type T that may be null.
     * <p>
     * Compares two nullable values of a type that extends {@code Comparable}.
     * Handles {@code null} values by considering {@code null} as less than any non-null value.
     * If both values are {@code null}, they are considered equal.
     *
     * @param <T> The type of the values being compared, which must implement {@link Comparable}.
     * @param v1 The first value to compare. May be {@code null}.
     * @param v2 The second value to compare. May be {@code null}.
     * @return A negative integer, zero, or a positive integer if {@code v1} is less than,
     *         equal to, or greater than {@code v2}, respectively.
     */
    public static <T extends Comparable<T>> int compareToNullable(T v1, T v2) {
        if (v1 == null) {
            return v2 == null ? 0 : -1;
        } else if (v2 == null) {
            return 1;
        } else {
            return v1.compareTo(v2);
        }
    }

    /**
     * Extracts first N codepoints from string.
     */
    public static String firstN(String str, int len) {
        if (str.codePointCount(0, str.length()) <= len) {
            return str;
        } else {
            return str.substring(0, str.offsetByCodePoints(0, len));
        }
    }

    /**
     * Truncate the supplied text to a maximum of len codepoints. If truncated,
     * the result will be the first (len - 1) codepoints plus a trailing
     * ellipsis.
     *
     * @param text
     *            The text to truncate
     * @param len
     *            The desired length (in codepoints) of the result
     * @return The truncated string
     */
    public static String truncate(String text, int len) {
        if (text.codePointCount(0, text.length()) <= len) {
            return text;
        }
        return firstN(text, len - 1) + TRUNCATE_CHAR;
    }

    /**
     * Returns first letter in lowercase. Usually used for create tag shortcuts.
     */
    public static int getFirstLetterLowercase(String s) {
        if (s == null) {
            return 0;
        }

        int cp;
        int i = 0;
        while (i < s.length()) {
            cp = s.codePointAt(i);
            if (Character.isLetter(cp)) {
                return Character.toLowerCase(cp);
            }
            i += Character.charCount(cp);
        }

        return 0;
    }

    /**
     * Checks if text contains substring after specified position.
     */
    public static boolean isSubstringAfter(String text, int pos, String substring) {
        if (pos + substring.length() > text.length()) {
            return false;
        }
        return substring.equals(text.substring(pos, pos + substring.length()));
    }

    /**
     * Checks if text contains substring before specified position.
     */
    public static boolean isSubstringBefore(String text, int pos, String substring) {
        if (pos - substring.length() < 0) {
            return false;
        }
        return substring.equals(text.substring(pos - substring.length(), pos));
    }

    public static String stripFromEnd(String string, String... toStrip) {
        if (string == null) {
            return null;
        }
        if (toStrip == null) {
            return string;
        }
        for (String s : toStrip) {
            if (string.endsWith(s)) {
                string = string.substring(0, string.length() - s.length());
            }
        }
        return string;
    }

    /**
     * Apply Unicode NFC normalization to a string.
     */
    public static String normalizeUnicode(CharSequence text) {
        return Normalizer.isNormalized(text, Normalizer.Form.NFC) ? text.toString()
                : Normalizer.normalize(text, Normalizer.Form.NFC);
    }

    /**
     * Replace invalid XML chars by spaces.
     *
     * @param str
     *            input stream
     * @return result stream
     * @see <a href="http://www.w3.org/TR/2006/REC-xml-20060816/#charsets">
     *      Supported chars</a>
     */
    public static String removeXMLInvalidChars(String str) {
        StringBuilder sb = new StringBuilder(str.length());
        int c;
        int i = 0;
        while (i < str.length()) {
            c = str.codePointAt(i);
            if (!isValidXMLChar(c)) {
                c = ' ';
            }
            sb.appendCodePoint(c);
            i += Character.charCount(c);
        }
        return sb.toString();
    }

    private static final int MAX_BASIC_CHAR = 0xD7FF;
    private static final int MIN_SUPPLEMENTARY_CHAR = 0xE000;
    private static final int MAX_SUPPLEMENTARY_CHAR = 0xFFFD;
    private static final int MIN_NONBMP_CHAR = 0x10000;
    private static final int MAX_NONBMP_CHAR = 0x10FFFF;

    /**
     * Determines whether the provided code point is a valid XML character.
     *
     * @param codePoint
     *            the code point to validate
     * @return true if the code point is a valid XML character, otherwise false
     */
    public static boolean isValidXMLChar(int codePoint) {
        if (codePoint < 0x20) {
            return codePoint == 0x09 || codePoint == 0x0A || codePoint == 0x0D;
        }
        return (codePoint <= MAX_BASIC_CHAR)
                || (codePoint >= MIN_SUPPLEMENTARY_CHAR && codePoint <= MAX_SUPPLEMENTARY_CHAR)
                || (codePoint >= MIN_NONBMP_CHAR && codePoint <= MAX_NONBMP_CHAR);
    }

    /**
     * Converts a stream of plaintext into valid XML. Output stream must convert
     * stream to UTF-8 when saving to disk.
     */
    public static String makeValidXML(String plaintext) {
        StringBuilder out = new StringBuilder();
        String text = removeXMLInvalidChars(plaintext);
        int cp;
        int i = 0;
        while (i < text.length()) {
            cp = text.codePointAt(i);
            out.append(escapeXMLChars(cp));
            i += Character.charCount(cp);
        }
        return out.toString();
    }

    /** Compresses spaces in case of non-preformatting paragraph. */
    public static String compressSpaces(String str) {
        int strlen = str.length();
        StringBuilder res = new StringBuilder(strlen);
        boolean wasspace = true;
        int cp;
        int i = 0;
        while (i < strlen) {
            cp = str.codePointAt(i);
            if (Character.isWhitespace(cp)) {
                if (!wasspace) {
                    wasspace = true;
                }
            } else {
                if (wasspace && res.length() > 0) {
                    res.append(' ');
                }
                res.appendCodePoint(cp);
                wasspace = false;
            }
            i += Character.charCount(cp);
        }
        return res.toString();
    }

    /**
     * Converts a single code point into valid XML. Output stream must convert
     * stream to UTF-8 when saving to disk.
     */
    private static final String AMPERSAND_ESCAPED = "&amp;";
    private static final String GREATER_THAN_ESCAPED = "&gt;";
    private static final String LESS_THAN_ESCAPED = "&lt;";
    private static final String QUOTE_ESCAPED = "&quot;";
    private static final String LITER_SYMBOL = "ℓ";
    private static final String CUBIC_METER_SYMBOL = "m³";
    private static final String SQUARE_METER_SYMBOL = "m²";

    public static String escapeXMLChars(int cp) {
        switch (cp) {
        case '&':
            return AMPERSAND_ESCAPED;
        case '>':
            return GREATER_THAN_ESCAPED;
        case '<':
            return LESS_THAN_ESCAPED;
        case '"':
            return QUOTE_ESCAPED;
        default:
            return String.valueOf(Character.toChars(cp));
        }
    }

    /**
     * Converts XML entities to characters.
     */
    public static String unescapeXMLEntities(String text) {

        String[][] entities = { { GREATER_THAN_ESCAPED, ">" }, { LESS_THAN_ESCAPED, "<" },
                { QUOTE_ESCAPED, "\"" }, { AMPERSAND_ESCAPED, "&" } };

        for (String[] entity : entities) {
            if (text.contains(entity[0])) {
                text = text.replaceAll(entity[0], entity[1]);
            }
        }
        return text;
    }

    /**
     * Compares two strings for equality. Handles nulls: if both strings are
     * nulls they are considered equal.
     */
    public static boolean equal(String one, String two) {
        return (one == null && two == null) || (one != null && one.equals(two));
    }

    /**
     * Formats UI strings.
     * <p>
     * Note: This is only a first attempt at putting right what goes wrong in
     * MessageFormat. Currently it only duplicates single quotes, but it doesn't
     * even test if the string contains parameters (numbers in curly braces),
     * and it doesn't allow for string containg already escaped quotes.
     *
     * @param str
     *            The string to format
     * @param arguments
     *            Arguments to use in formatting the string
     *
     * @return The formatted string
     */
    public static String format(@NotNull String str, Object... arguments) {
        // MessageFormat.format expects single quotes to be escaped
        // by duplicating them, otherwise the string will not be formatted
        str = str.replace("'", "''");
        return MessageFormat.format(str, arguments);
    }

    /**
     * Normalize the
     * <a href="https://en.wikipedia.org/wiki/Halfwidth_and_fullwidth_forms">
     * width</a> of characters in the supplied text. Specifically:
     * <ul>
     * <li>ASCII characters will become halfwidth
     * <li>Katakana characters will become fullwidth
     * <li>Hangul will become fullwidth
     * <li>Letter-like symbols and squared Latin abbreviations will be
     * decomposed to ASCII
     * </ul>
     * This method was adapted from <a href=
     * "https://bitbucket.org/okapiframework/okapi/src/52143104fcfc7eda204d04dfbbc273189f3a7f0f/okapi/steps/fullwidthconversion/src/main/java/net/sf/okapi/steps/fullwidthconversion/FullWidthConversionStep.java">
     * FullWidthConversionStep.java</a> in the Okapi Framework under GPLv2+.
     *
     * @param text
     *            source text to convert
     * @return Normalized-width text
     */
    public static String normalizeWidth(String text) {
        StringBuilder sb = new StringBuilder(text);

        int ch;
        int i = 0;
        while (i < sb.length()) {
            ch = sb.charAt(i);
            // ASCII
            if ((ch >= 0xFF01) && (ch <= 0xFF5E)) {
                sb.setCharAt(i, (char) (ch - 0xFEE0));
                i++;
                continue;
            }
            if (ch == 0x00A0 ||  ch == 0x2007 || ch == 0x2008 || ch == 0x2009 || ch == 0x202F || ch == 0x3000 ) {
                sb.setCharAt(i, ' ');
            }
            processKatakana(ch, sb, i);
            // Hangul
            if ((ch > 0xFFA1) && (ch <= 0xFFBE)) {
                sb.setCharAt(i, (char) (ch - 0xCE70));
                i++;
                continue;
            }
            processHangle(ch, sb, i);
            i += processLetterLikeSymbol(ch, sb, i);
            i += replaceSquaredLatinAbbreviations(ch, sb, i);
            i++;
        }

        String result = sb.toString();

        if (text.equals(result)) {
            // No characters were changed. Return the original text so that
            // composition of unrelated characters is not affected.
            return text;
        }

        return normalizeUnicode(result);
    }

    private static final int[] KATAKANA_DATA = { /* 0xFF61 */ 0x3002, 0x300C, 0x300D, 0x3001, 0x30FB, 0x30F2,
            0x30A1, /* 0xFF68 */ 0x30A3, 0x30A5, 0x30A7, 0x30A9, 0x30E3, 0x30E5, 0x30E7, 0x30C3,
            /* 0xFF70 */ 0x30FC, 0x30A2, 0x30A4, 0x30A6, 0x30A8, 0x30AA, 0x30AB, 0x30AD,
            /* 0xFF78 */ 0x30AF, 0x30B1, 0x30B3, 0x30B5, 0x30B7, 0x30B9, 0x30BB, 0x30BD,
            /* 0xFF80 */ 0x30BF, 0x30C1, 0x30C4, 0x30C6, 0x30C8, 0x30CA, 0x30CB, 0x30CC,
            /* 0xFF88 */ 0x30CD, 0x30CE, 0x30CF, 0x30D2, 0x30D5, 0x30D8, 0x30DB, 0x30DE,
            /* 0xFF90 */ 0x30DF, 0x30E0, 0x30E1, 0x30E2, 0x30E4, 0x30E6, 0x30E8, 0x30E9,
            /* 0xFF98 */ 0x30EA, 0x30EB, 0x30EC, 0x30ED, 0x30EF, 0x30F3, 0x3099, 0x309A };

    static void processKatakana(int ch, StringBuilder sb, int i) {
        if (ch >= 0xFF61 && ch <= 0xFF9F) {
            sb.setCharAt(i, (char) KATAKANA_DATA[ch - 0xFF61]);
        }
    }

    private static final int[] HUNGLE_DATA = { /* Hungle from 0xFFDA */ 0x3161, 0x3162, 0x3163,
            /* no conversion from 0xFFDD to 0xFFE7 */ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            /* Others from 0xFFE8 */ 0x2502, 0x2190, 0x2191, 0x2192, 0x2193, 0x25A0, 0x25CB };

    private static void processHangle(int ch, StringBuilder sb, int i) {
        if (ch == 0xFFA0) {
            sb.setCharAt(i, (char) 0x3164);
        } else if (ch >= 0xFFDA && ch <= 0xFFEE && HUNGLE_DATA[ch - 0xFFDA] != 0) {
            sb.setCharAt(i, (char) (HUNGLE_DATA[ch - 0xFFDA]));
        }
    }

    private static final Map<Integer, String> LETTER_LIKE_SYMBOL_DATA = new HashMap<>();

    private static void initLetterLikeSymbolData() {
        LETTER_LIKE_SYMBOL_DATA.put(0x2100, "a/c");
        LETTER_LIKE_SYMBOL_DATA.put(0x2101, "a/s");
        LETTER_LIKE_SYMBOL_DATA.put(0x2105, "c/o");
        LETTER_LIKE_SYMBOL_DATA.put(0x2103, "°C");
        LETTER_LIKE_SYMBOL_DATA.put(0x2109, "°F");
        LETTER_LIKE_SYMBOL_DATA.put(0x2116, "No");
        LETTER_LIKE_SYMBOL_DATA.put(0x212A, "K");
        LETTER_LIKE_SYMBOL_DATA.put(0x212B, "Å");
    }

    private static int processLetterLikeSymbol(int ch, StringBuilder sb, int i) {
        if (LETTER_LIKE_SYMBOL_DATA.isEmpty()) {
            initLetterLikeSymbolData();
        }
        int increment = 0;
        if (LETTER_LIKE_SYMBOL_DATA.containsKey(ch)) {
            sb.setCharAt(i, LETTER_LIKE_SYMBOL_DATA.get(ch).charAt(0));
            for (int j = 1; j < LETTER_LIKE_SYMBOL_DATA.get(ch).length(); j++) {
                sb.insert(i + j, LETTER_LIKE_SYMBOL_DATA.get(ch).charAt(j));
                increment++;
            }
        }
        return increment;
    }

    private static final int MIN_SQUARED_LATIN_ABBREVIATIONS_CHAR = 0x3371;
    private static final int MAX_SQUARED_LATIN_ABBREVIATIONS2_CHAR = 0x33DF;
    private static final int SPECIAL_SQUARED_LATIN_ABBREVIATION_CHAR = 0x33FF;

    private static final List<String> SQUARED_LATIN_ABBREVIATION_DATA = Collections.unmodifiableList(Arrays.asList(
            /* 0x3371 */ "hPa", "da", "AU", "bar", "oV", "pc", "dm", "d" + SQUARE_METER_SYMBOL,
            /* 0x3378 */ "d" + CUBIC_METER_SYMBOL, "IU", null, null, null, null, null,
            /* 0x3380 */ "pA", "nA", "μA", "mA", "kA", "KB", "MB", "GB",
            /* 0x3388 */ "cal", "kcal", "pF", "nF", "μF", "μg", "mg", "kg",
            /* 0x3390 */ "Hz", "kHz", "MHz", "GHz", "THz", "μ" + LITER_SYMBOL, "m" + LITER_SYMBOL,
            "d" + LITER_SYMBOL, /* 0x3398 */ "k" + LITER_SYMBOL, "fm", "nm", "μm", "mm", "cm", "km",
            "m" + SQUARE_METER_SYMBOL, /* 0x33A0 */ "c" + SQUARE_METER_SYMBOL, "m²",
            "k" + SQUARE_METER_SYMBOL, "m" + CUBIC_METER_SYMBOL, "c" + CUBIC_METER_SYMBOL, "m³",
            "k" + CUBIC_METER_SYMBOL, "m/s", /* 0x33A8 */ "m/s²", "Pa", "kPa", "MPa", "GPa", "rad",
            "rad/s", "rad/s²", /* 0x33B0 */ "Ps", "ns", "μs", "ms", "pV", "nV", "μV", "mV",
            /* 0x33B8 */ "kV", "MV", "pW", "nW", "μW", "mW", "kW", "MW",
            /* 0x33C0 */ "kΩ", "MΩ", "a.m.", "Bq", "cc", "cd", "C/kg", "Co.",
            /* 0x33C8 */ "dB", "Gy", "ha", "HP", "in", "KK", "KM", "Kt",
            /* 0x33D0 */ "lm", "ln", "log", "lx", "mb", "mil", "mol", "pH",
            /* 0x33D8 */ "p.m.", "PPM", "PR", "sr", "Sv", "Wb", "v/m", "a/m"
    ));

    private static int replaceSquaredLatinAbbreviations(int ch, StringBuilder sb, int i) {
        // Squared Latin Abbreviations 1 and 2
        if (ch >= MIN_SQUARED_LATIN_ABBREVIATIONS_CHAR && ch <= MAX_SQUARED_LATIN_ABBREVIATIONS2_CHAR) {
            return processLatinAbbreviationRange(ch, sb, i);
        }
        // Squared Latin Abbreviations 3
        if (ch == SPECIAL_SQUARED_LATIN_ABBREVIATION_CHAR) {
            sb.setCharAt(i, 'g');
            sb.insert(i + 1, "al");
            return 2;
        }
        return 0;
    }

    private static int processLatinAbbreviationRange(int ch, StringBuilder sb, int i) {
        String replacement = SQUARED_LATIN_ABBREVIATION_DATA.get(ch - MIN_SQUARED_LATIN_ABBREVIATIONS_CHAR);
        if (replacement == null) {
            return 0;
        }

        sb.setCharAt(i, replacement.charAt(0));
        for (int j = 1; j < replacement.length(); j++) {
            sb.insert(i + j, replacement.charAt(j));
        }
        return replacement.length() - 1;
    }

    /**
     * Strip whitespace from the end of a string. Uses
     * {@link Character#isWhitespace(int)}, so it does not strip the extra
     * non-breaking whitespace included in {@link #isWhiteSpace(int)}.
     *
     * @param text
     * @return text with trailing whitespace removed
     */
    public static String rstrip(@NotNull String text) {
        int cp;
        int i = text.length();
        while (i >= 0) {
            if (i == 0) {
                return "";
            }
            cp = text.codePointBefore(i);
            if (!Character.isWhitespace(cp)) {
                return text.substring(0, i);
            }
            i -= Character.charCount(cp);
        }
        return text;
    }

    /**
     * Convert a byte array into a Base64-encoded String. Convenience method for
     * {@link DatatypeConverter#printBase64Binary(byte[])} (available since Java
     * 1.6) because it's so well hidden.
     *
     * @param bytes
     *            Data bytes
     * @return Base64-encoded String
     */
    private static String encodeBase64(byte[] bytes) {
        return DatatypeConverter.printBase64Binary(bytes);
    }

    /**
     * Convert a string's <code>charset</code> bytes into a Base64-encoded
     * String.
     *
     * @param string
     *            a string
     * @param charset
     *            the charset with which to obtain the bytes
     * @return Base64-encoded String
     */
    public static String encodeBase64(String string, Charset charset) {
        return encodeBase64(string.getBytes(charset));
    }

    /**
     * Convert a char array's <code>charset</code> bytes into a Base64-encoded
     * String. Useful for handling passwords. Intermediate buffers are cleared
     * after use.
     *
     * @param chars
     *            a char array
     * @param charset
     *            the charset with which to obtain the bytes
     * @return Base64-encoded String
     */
    public static String encodeBase64(char[] chars, Charset charset) {
        CharBuffer charBuf = CharBuffer.wrap(chars);
        ByteBuffer byteBuf = charset.encode(charBuf);
        String result = encodeBase64(byteBuf.array());
        Arrays.fill(charBuf.array(), '\0');
        Arrays.fill(byteBuf.array(), (byte) 0);
        return result;
    }

    /**
     * Convert a Base64-encoded String into an array of bytes. Convenience
     * method for {@link DatatypeConverter#parseBase64Binary(String)} (available
     * since Java 1.6) because it's so well hidden.
     *
     * @param b64data
     *            Base64-encoded String
     * @return Data bytes
     */
    private static byte[] decodeBase64(String b64data) {
        return DatatypeConverter.parseBase64Binary(b64data);
    }

    /**
     * Decode the Base64-encoded <code>charset</code> bytes back to a String.
     *
     * @param b64data
     *            Base64-encoded String
     * @param charset
     *            charset of decoded bytes
     * @return String
     */
    public static String decodeBase64(String b64data, Charset charset) {
        return new String(decodeBase64(b64data), charset);
    }

    /**
     * For a string delimited by some separator, retrieve the last
     * {@code segments} segments.
     *
     * @param str
     *            The string
     * @param separator
     *            The separator delimiting the string's segments
     * @param segments
     *            The number of segments to return, starting at the end
     * @return The trailing segments, or, if {@code segments} is greater than
     *         the number of segments contained in {@code str}, then {@code str}
     *         itself.
     */
    public static String getTailSegments(@NotNull String str, int separator, int segments) {
        int start = str.length();
        for (int i = 0; i < segments; i++) {
            start = str.lastIndexOf(separator, start - Character.charCount(1));
            if (start == -1) {
                return str;
            }
        }
        return str.substring(start + Character.charCount(separator), str.length());
    }

    /**
     * For a string containing a space-separated list of items, convert that
     * string into an ArrayList
     *
     * @param str
     *            The string, with items separated by whitespace
     * @return An ArrayList of the items in the original space-separated list
     */
    public static List<String> convertToList(String str) {
        return new ArrayList<>(Arrays.asList(str.trim().split("\\s+")));
    }

    /**
     * Wrap line by length.
     * 
     * @param text
     *            string to process.
     * @param length
     *            wrap length.
     * @return string wrapped.
     */
    public static String wrap(@NotNull String text, int length) {
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\\n")) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            for (String token : line.split("\\s")) {
                if ((sb.length() != 0 || token.length() <= length)
                        && sb.length() + token.length() - sb.lastIndexOf("\n") > length) {
                    sb.append('\n');
                }
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                    sb.append(' ');
                }
                sb.append(token);
            }
        }
        return sb.toString();
    }

}
