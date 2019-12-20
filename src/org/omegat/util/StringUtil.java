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
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

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

    public static final char TRUNCATE_CHAR = '\u2026';

    /**
     * Check if string is empty, i.e. null or length==0
     */
    public static boolean isEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Returns true if the input has at least one letter and
     * all letters are lower case.
     */
    public static boolean isLowerCase(final String input) {
        if (input.isEmpty()) {
            return false;
        }
        boolean hasLetters = false;
        for (int i = 0, cp; i < input.length(); i += Character.charCount(cp)) {
            cp = input.codePointAt(i);
            if (Character.isLetter(cp)) {
                hasLetters = true;
                if (!Character.isLowerCase(cp)) {
                    return false;
                }
            }
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
        for (int i = 0, cp; i < input.length(); i += Character.charCount(cp)) {
            cp = input.codePointAt(i);
            if (Character.isLetter(cp)) {
                hasLetters = true;
                if (!Character.isUpperCase(cp)) {
                    return false;
                }
            }
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
        for (int i = 0, cp; i < input.length(); i += Character.charCount(cp)) {
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
        }
        return false;
    }

    /**
     * Returns true if the input is title case, meaning the first character is UpperCase or
     * TitleCase* and the rest of the string (if present) is LowerCase.
     * <p>
     * *There are exotic characters that are neither UpperCase nor LowerCase, but are TitleCase:
     * e.g. LATIN CAPITAL LETTER L WITH SMALL LETTER J (U+01C8)<br>
     * These are handled correctly.
     */
    public static boolean isTitleCase(final String input) {
        if (input.isEmpty()) {
            return false;
        }
        if (input.codePointCount(0, input.length()) > 1) {
            return isTitleCase(input.codePointAt(0)) && isLowerCase(input.substring(input.offsetByCodePoints(0, 1)));
        } else {
            return isTitleCase(input.codePointAt(0));
        }
    }

    public static boolean isTitleCase(int codePoint) {
        // True if is actual title case, or if is upper case and has no separate title case variant.
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
        for (int i = 0, cp; i < input.length(); i += Character.charCount(cp)) {
            cp = input.codePointAt(i);
            if (!isWhiteSpace(cp)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the input is a whitespace character
     * (including non-breaking characters that are false according to
     * {@link Character#isWhitespace(int)}).
     */
    public static boolean isWhiteSpace(int codePoint) {
        return Character.isWhitespace(codePoint)
                || codePoint == '\u00A0'
                || codePoint == '\u2007'
                || codePoint == '\u202F';
    }

    public static boolean isCJK(String input) {
        if (input.isEmpty()) {
            return false;
        }
        for (int i = 0, cp; i < input.length(); i += Character.charCount(cp)) {
            cp = input.codePointAt(i);
            // Anything less than CJK Radicals Supplement is "not CJK". Everything else is.
            // TODO: Make this smarter?
            if (cp < '\u2E80') {
                return false;
            }
        }
        return true;
    }

    public static String capitalizeFirst(String text, Locale locale) {
        if (text.isEmpty()) {
            return text;
        }
        int remainder = text.offsetByCodePoints(0, 1);
        String firstCP = text.substring(0, remainder);
        return StringUtil.toTitleCase(firstCP, locale)
                + text.substring(remainder);
    }

    /**
     * Interpret the case replacement language used in regular expressions:
     * <ul>
     * <li>backslash u = uppercase next letter
     * <li>backslash l = lowercase next letter
     * <li>backslash U = uppercase next letters until backslash E
     * <li>backslash L = lowercase next letters until backslash E
     * <li>backslash u + backslash L = uppercase next letter then lowercase all until backslash E
     * <li>backslash l + backslash U = lowercase next letter then uppercase all until backslash E
     * </ul>
     * Warning: this method works with the string you give to it; if you want to do other substitutions, such as
     * variable conversions, they must be done before the call to replaceCase, else this method will not apply to the
     * non-yet converted parts!
     **/
    public static String replaceCase(String txt, Locale lang) {
        if (!txt.startsWith("\\")) {
            int idx = txt.indexOf("\\");
            if (idx == -1) {
                return txt;
            } else {
                return txt.substring(0, idx) + replaceCase(txt.substring(idx), lang);
            }
        }
        // Double symbols are longer, so they must be treated first
        if (txt.startsWith("\\u\\L")) {
            return txt.substring(4, 5).toUpperCase(lang) + replaceCase("\\L" + txt.substring(5), lang);
        }
        if (txt.startsWith("\\l\\U")) {
            return txt.substring(4, 5).toLowerCase(lang) + replaceCase("\\U" + txt.substring(5), lang);
        }
        // Simple symbols without delimiters
        if (txt.startsWith("\\u")) {
            return txt.substring(2, 3).toUpperCase(lang) + replaceCase(txt.substring(3), lang);
        }
        if (txt.startsWith("\\l")) {
            return txt.substring(2, 3).toLowerCase(lang) + replaceCase(txt.substring(3), lang);
        }
        // Simple symbols with \E as delimiter
        if (txt.startsWith("\\E")) {
            return replaceCase(txt.substring(2), lang);
        }
        if (txt.startsWith("\\U")) { // Upper until \E or \L
            txt = txt.substring(2).replace("\\L", "\\E\\L").replace("\\U", "\\E\\U");
            int idx = txt.indexOf("\\E");
            if (idx == -1) {
                return txt.toUpperCase(lang);
            } else {
                return txt.substring(0, idx).toUpperCase(lang) + replaceCase(txt.substring(idx + 2), lang);
            }
        }
        if (txt.startsWith("\\L")) { // Lower until \E or \U
            txt = txt.substring(2).replace("\\L", "\\E\\L").replace("\\U", "\\E\\U");
            int idx = txt.indexOf("\\E");
            if (idx == -1) {
                return txt.toLowerCase(lang);
            } else {
                return txt.substring(0, idx).toLowerCase(lang) + replaceCase(txt.substring(idx + 2), lang);
            }
        }

        if (txt.startsWith("\\\\")) { // literal backslash
            return "\\" + replaceCase(txt.substring(2), lang);
        }
        if (txt.startsWith("\\$")) { // protected $
            return "$" + replaceCase(txt.substring(2), lang);
        }
        // for all other cases, don't consider \ as an escape character
        return "\\" + replaceCase(txt.substring(1), lang);
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
        // If matching to upper (at least 2 chars; otherwise would have hit isTitleCase()
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
        for (int cp; firstLetterIndex < text.length(); firstLetterIndex += Character.charCount(cp)) {
            cp = text.codePointAt(firstLetterIndex);
            if (Character.isLetter(cp)) {
                break;
            }
        }
        if (firstLetterIndex == text.length()) {
            return text;
        }
        int firstTitleCase = Character.toTitleCase(text.codePointAt(firstLetterIndex));
        int remainderOffset = text.offsetByCodePoints(firstLetterIndex, 1);
        // If the first codepoint has an actual title case variant (rare), use that.
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
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0) {
                return values[i];
            }
        }
        return 0;
    }

    /**
     * Compare two values, which could be null.
     */
    public static <T extends Comparable<T>> int compareToWithNulls(T v1, T v2) {
        if (v1 == v2) {
            return 0;
        } else if (v1 == null) {
            return -1;
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

        for (int cp, i = 0; i < s.length(); i += Character.charCount(cp)) {
            cp = s.codePointAt(i);
            if (Character.isLetter(cp)) {
                return Character.toLowerCase(cp);
            }
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
        for (int c, i = 0; i < str.length(); i += Character.charCount(c)) {
            c = str.codePointAt(i);
            if (!isValidXMLChar(c)) {
                c = ' ';
            }
            sb.appendCodePoint(c);
        }
        return sb.toString();
    }

    public static boolean isValidXMLChar(int codePoint) {
        if (codePoint < 0x20) {
            if (codePoint != 0x09 && codePoint != 0x0A && codePoint != 0x0D) {
                return false;
            }
        } else if (codePoint >= 0x20 && codePoint <= 0xD7FF) {
        } else if (codePoint >= 0xE000 && codePoint <= 0xFFFD) {
        } else if (codePoint >= 0x10000 && codePoint <= 0x10FFFF) {
        } else {
            return false;
        }
        return true;
    }

    /**
     * Converts a stream of plaintext into valid XML. Output stream must convert
     * stream to UTF-8 when saving to disk.
     */
    public static String makeValidXML(String plaintext) {
        StringBuilder out = new StringBuilder();
        String text = removeXMLInvalidChars(plaintext);
        for (int cp, i = 0; i < text.length(); i += Character.charCount(cp)) {
            cp = text.codePointAt(i);
            out.append(escapeXMLChars(cp));
        }
        return out.toString();
    }

    /** Compresses spaces in case of non-preformatting paragraph. */
    public static String compressSpaces(String str) {
        int strlen = str.length();
        StringBuilder res = new StringBuilder(strlen);
        boolean wasspace = true;
        for (int cp, i = 0; i < strlen; i += Character.charCount(cp)) {
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
        }
        return res.toString();
    }

    /**
     * Converts a single code point into valid XML. Output stream must convert stream
     * to UTF-8 when saving to disk.
     */
    public static String escapeXMLChars(int cp) {
        switch (cp) {
        // case '\'':
        // return "&apos;";
        case '&':
            return "&amp;";
        case '>':
            return "&gt;";
        case '<':
            return "&lt;";
        case '"':
            return "&quot;";
        default:
            return String.valueOf(Character.toChars(cp));
        }
    }

    /**
     * Converts XML entities to characters.
     */
    public static String unescapeXMLEntities(String text) {

        if (text.contains("&gt;")) {
            text = text.replaceAll("&gt;", ">");
        }
        if (text.contains("&lt;")) {
            text = text.replaceAll("&lt;", "<");
        }
        if (text.contains("&quot;")) {
            text = text.replaceAll("&quot;", "\"");
        }
       // If makeValidXML converts ' to apos;, the following lines should be uncommented
        /* if (text.indexOf("&apos;") >= 0) {
            text = text.replaceAll("&apos;", "'");
        }*/
        if (text.contains("&amp;")) {
            text = text.replaceAll("&amp;", "&");
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
     *
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
    public static String format(String str, Object... arguments) {
        // MessageFormat.format expects single quotes to be escaped
        // by duplicating them, otherwise the string will not be formatted
        str = str.replaceAll("'", "''");
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
     * @return Normalized-width text
     */
    // CHECKSTYLE:OFF
    public static String normalizeWidth(String text) {
        StringBuilder sb = new StringBuilder(text);

        int ch;
        for ( int i=0; i<sb.length(); i++ ) {
            ch = sb.charAt(i);
            // ASCII
            if (( ch >= 0xFF01 ) && ( ch <= 0xFF5E )) {
                sb.setCharAt(i, (char)(ch-0xFEE0));
                continue;
            }
            if ( ch == 0x3000 ) {
                sb.setCharAt(i, ' ');
            }
            switch ( ch ) {
            // Katakana
            case 0xFF61: sb.setCharAt(i, (char)0x3002); break;
            case 0xFF62: sb.setCharAt(i, (char)0x300C); break;
            case 0xFF63: sb.setCharAt(i, (char)0x300D); break;
            case 0xFF64: sb.setCharAt(i, (char)0x3001); break;
            case 0xFF65: sb.setCharAt(i, (char)0x30FB); break;
            case 0xFF66: sb.setCharAt(i, (char)0x30F2); break;
            case 0xFF67: sb.setCharAt(i, (char)0x30A1); break;
            case 0xFF68: sb.setCharAt(i, (char)0x30A3); break;
            case 0xFF69: sb.setCharAt(i, (char)0x30A5); break;
            case 0xFF6A: sb.setCharAt(i, (char)0x30A7); break;
            case 0xFF6B: sb.setCharAt(i, (char)0x30A9); break;
            case 0xFF6C: sb.setCharAt(i, (char)0x30E3); break;
            case 0xFF6D: sb.setCharAt(i, (char)0x30E5); break;
            case 0xFF6E: sb.setCharAt(i, (char)0x30E7); break;
            case 0xFF6F: sb.setCharAt(i, (char)0x30C3); break;
            case 0xFF70: sb.setCharAt(i, (char)0x30FC); break;
            case 0xFF71: sb.setCharAt(i, (char)0x30A2); break;
            case 0xFF72: sb.setCharAt(i, (char)0x30A4); break;
            case 0xFF73: sb.setCharAt(i, (char)0x30A6); break;
            case 0xFF74: sb.setCharAt(i, (char)0x30A8); break;
            case 0xFF75: sb.setCharAt(i, (char)0x30AA); break;
            case 0xFF76: sb.setCharAt(i, (char)0x30AB); break;
            case 0xFF77: sb.setCharAt(i, (char)0x30AD); break;
            case 0xFF78: sb.setCharAt(i, (char)0x30AF); break;
            case 0xFF79: sb.setCharAt(i, (char)0x30B1); break;
            case 0xFF7A: sb.setCharAt(i, (char)0x30B3); break;
            case 0xFF7B: sb.setCharAt(i, (char)0x30B5); break;
            case 0xFF7C: sb.setCharAt(i, (char)0x30B7); break;
            case 0xFF7D: sb.setCharAt(i, (char)0x30B9); break;
            case 0xFF7E: sb.setCharAt(i, (char)0x30BB); break;
            case 0xFF7F: sb.setCharAt(i, (char)0x30BD); break;
            case 0xFF80: sb.setCharAt(i, (char)0x30BF); break;
            case 0xFF81: sb.setCharAt(i, (char)0x30C1); break;
            case 0xFF82: sb.setCharAt(i, (char)0x30C4); break;
            case 0xFF83: sb.setCharAt(i, (char)0x30C6); break;
            case 0xFF84: sb.setCharAt(i, (char)0x30C8); break;
            case 0xFF85: sb.setCharAt(i, (char)0x30CA); break;
            case 0xFF86: sb.setCharAt(i, (char)0x30CB); break;
            case 0xFF87: sb.setCharAt(i, (char)0x30CC); break;
            case 0xFF88: sb.setCharAt(i, (char)0x30CD); break;
            case 0xFF89: sb.setCharAt(i, (char)0x30CE); break;
            case 0xFF8A: sb.setCharAt(i, (char)0x30CF); break;
            case 0xFF8B: sb.setCharAt(i, (char)0x30D2); break;
            case 0xFF8C: sb.setCharAt(i, (char)0x30D5); break;
            case 0xFF8D: sb.setCharAt(i, (char)0x30D8); break;
            case 0xFF8E: sb.setCharAt(i, (char)0x30DB); break;
            case 0xFF8F: sb.setCharAt(i, (char)0x30DE); break;
            case 0xFF90: sb.setCharAt(i, (char)0x30DF); break;
            case 0xFF91: sb.setCharAt(i, (char)0x30E0); break;
            case 0xFF92: sb.setCharAt(i, (char)0x30E1); break;
            case 0xFF93: sb.setCharAt(i, (char)0x30E2); break;
            case 0xFF94: sb.setCharAt(i, (char)0x30E4); break;
            case 0xFF95: sb.setCharAt(i, (char)0x30E6); break;
            case 0xFF96: sb.setCharAt(i, (char)0x30E8); break;
            case 0xFF97: sb.setCharAt(i, (char)0x30E9); break;
            case 0xFF98: sb.setCharAt(i, (char)0x30EA); break;
            case 0xFF99: sb.setCharAt(i, (char)0x30EB); break;
            case 0xFF9A: sb.setCharAt(i, (char)0x30EC); break;
            case 0xFF9B: sb.setCharAt(i, (char)0x30ED); break;
            case 0xFF9C: sb.setCharAt(i, (char)0x30EF); break;
            case 0xFF9D: sb.setCharAt(i, (char)0x30F3); break;
            case 0xFF9E: sb.setCharAt(i, (char)0x3099); break;
            case 0xFF9F: sb.setCharAt(i, (char)0x309A); break;
            }
            // Hangul
            if (( ch > 0xFFA1 ) && ( ch <= 0xFFBE )) {
                sb.setCharAt(i, (char)(ch-0xCE70));
                continue;
            }
            switch ( ch ) {
            // Hangul
            case 0xFFA0: sb.setCharAt(i, (char)0x3164); break;
            case 0xFFDA: sb.setCharAt(i, (char)0x3161); break;
            case 0xFFDB: sb.setCharAt(i, (char)0x3162); break;
            case 0xFFDC: sb.setCharAt(i, (char)0x3163); break;
            // Others
            case 0xFFE8: sb.setCharAt(i, (char)0x2502); break;
            case 0xFFE9: sb.setCharAt(i, (char)0x2190); break;
            case 0xFFEA: sb.setCharAt(i, (char)0x2191); break;
            case 0xFFEB: sb.setCharAt(i, (char)0x2192); break;
            case 0xFFEC: sb.setCharAt(i, (char)0x2193); break;
            case 0xFFED: sb.setCharAt(i, (char)0x25A0); break;
            case 0xFFEE: sb.setCharAt(i, (char)0x25CB); break;
            }

            // Process letter-like symbols
            switch ( ch ) {
            case 0x2100: sb.setCharAt(i, 'a'); sb.insert(i+1, "/c"); i+=2; break;
            case 0x2101: sb.setCharAt(i, 'a'); sb.insert(i+1, "/s"); i+=2; break;
            case 0x2105: sb.setCharAt(i, 'c'); sb.insert(i+1, "/o"); i+=2; break;
            case 0x2103: sb.setCharAt(i, (char)0x00B0); sb.insert(i+1, "C"); i++; break;
            case 0x2109: sb.setCharAt(i, (char)0x00B0); sb.insert(i+1, "F"); i++; break;
            case 0x2116: sb.setCharAt(i, 'N'); sb.insert(i+1, "o"); i++; break;
            case 0x212A: sb.setCharAt(i, 'K'); break;
            case 0x212B: sb.setCharAt(i, (char)0x00C5); break;
            }

            switch ( ch ) {
            // Squared Latin Abbreviations 1
            case 0x3371: sb.setCharAt(i, 'h'); sb.insert(i+1, "Pa"); i+=2; break;
            case 0x3372: sb.setCharAt(i, 'd'); sb.insert(i+1, "a"); i++; break;
            case 0x3373: sb.setCharAt(i, 'A'); sb.insert(i+1, "U"); i++; break;
            case 0x3374: sb.setCharAt(i, 'b'); sb.insert(i+1, "ar"); i+=2; break;
            case 0x3375: sb.setCharAt(i, 'o'); sb.insert(i+1, "V"); i++; break;
            case 0x3376: sb.setCharAt(i, 'p'); sb.insert(i+1, "c"); i++; break;
            case 0x3377: sb.setCharAt(i, 'd'); sb.insert(i+1, "m"); i++; break;
            case 0x3378: sb.setCharAt(i, 'd'); sb.insert(i+1, "m\u00B2"); i+=2; break;
            case 0x3379: sb.setCharAt(i, 'd'); sb.insert(i+1, "m\u00B3"); i+=2; break;
            case 0x337A: sb.setCharAt(i, 'I'); sb.insert(i+1, "U"); i++; break;
            // Squared Latin Abbreviations 2
            case 0x3380: sb.setCharAt(i, 'p'); sb.insert(i+1, "A"); i++; break;
            case 0x3381: sb.setCharAt(i, 'n'); sb.insert(i+1, "A"); i++; break;
            case 0x3382: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "A"); i++; break;
            case 0x3383: sb.setCharAt(i, 'm'); sb.insert(i+1, "A"); i++; break;
            case 0x3384: sb.setCharAt(i, 'k'); sb.insert(i+1, "A"); i++; break;
            case 0x3385: sb.setCharAt(i, 'K'); sb.insert(i+1, "B"); i++; break;
            case 0x3386: sb.setCharAt(i, 'M'); sb.insert(i+1, "B"); i++; break;
            case 0x3387: sb.setCharAt(i, 'G'); sb.insert(i+1, "B"); i++; break;
            case 0x3388: sb.setCharAt(i, 'c'); sb.insert(i+1, "al"); i+=2; break;
            case 0x3389: sb.setCharAt(i, 'k'); sb.insert(i+1, "cal"); i+=3; break;
            case 0x338A: sb.setCharAt(i, 'p'); sb.insert(i+1, "F"); i++; break;
            case 0x338B: sb.setCharAt(i, 'n'); sb.insert(i+1, "F"); i++; break;
            case 0x338C: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "F"); i++; break;
            case 0x338D: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "g"); i++; break;
            case 0x338E: sb.setCharAt(i, 'm'); sb.insert(i+1, "g"); i++; break;
            case 0x338F: sb.setCharAt(i, 'k'); sb.insert(i+1, "g"); i++; break;
            case 0x3390: sb.setCharAt(i, 'H'); sb.insert(i+1, "z"); i++; break;
            case 0x3391: sb.setCharAt(i, 'k'); sb.insert(i+1, "Hz"); i+=2; break;
            case 0x3392: sb.setCharAt(i, 'M'); sb.insert(i+1, "Hz"); i+=2; break;
            case 0x3393: sb.setCharAt(i, 'G'); sb.insert(i+1, "Hz"); i+=2; break;
            case 0x3394: sb.setCharAt(i, 'T'); sb.insert(i+1, "Hz"); i+=2; break;
            case 0x3395: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "\u2113"); i++; break;
            case 0x3396: sb.setCharAt(i, 'm'); sb.insert(i+1, "\u2113"); i++; break;
            case 0x3397: sb.setCharAt(i, 'd'); sb.insert(i+1, "\u2113"); i++; break;
            case 0x3398: sb.setCharAt(i, 'k'); sb.insert(i+1, "\u2113"); i++; break;
            case 0x3399: sb.setCharAt(i, 'f'); sb.insert(i+1, "m"); i++; break;
            case 0x339A: sb.setCharAt(i, 'n'); sb.insert(i+1, "m"); i++; break;
            case 0x339B: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "m"); i++; break;
            case 0x339C: sb.setCharAt(i, 'm'); sb.insert(i+1, "m"); i++; break;
            case 0x339D: sb.setCharAt(i, 'c'); sb.insert(i+1, "m"); i++; break;
            case 0x339E: sb.setCharAt(i, 'k'); sb.insert(i+1, "m"); i++; break;
            case 0x339F: sb.setCharAt(i, 'm'); sb.insert(i+1, "m\u00B2"); i+=2; break;
            case 0x33A0: sb.setCharAt(i, 'c'); sb.insert(i+1, "m\u00B2"); i+=2; break;
            case 0x33A1: sb.setCharAt(i, 'm'); sb.insert(i+1, "\u00B2"); i++; break;
            case 0x33A2: sb.setCharAt(i, 'k'); sb.insert(i+1, "m\u00B2"); i+=2; break;
            case 0x33A3: sb.setCharAt(i, 'm'); sb.insert(i+1, "m\u00B3"); i+=2; break;
            case 0x33A4: sb.setCharAt(i, 'c'); sb.insert(i+1, "m\u00B3"); i+=2; break;
            case 0x33A5: sb.setCharAt(i, 'm'); sb.insert(i+1, "\u00B3"); i++; break;
            case 0x33A6: sb.setCharAt(i, 'k'); sb.insert(i+1, "m\u00B3"); i+=2; break;
            case 0x33A7: sb.setCharAt(i, 'm'); sb.insert(i+1, "/s"); i+=2; break;
            case 0x33A8: sb.setCharAt(i, 'm'); sb.insert(i+1, "/s\u00B2"); i+=3; break;
            case 0x33A9: sb.setCharAt(i, 'P'); sb.insert(i+1, "a"); i++; break;
            case 0x33AA: sb.setCharAt(i, 'k'); sb.insert(i+1, "Pa"); i+=2; break;
            case 0x33AB: sb.setCharAt(i, 'M'); sb.insert(i+1, "Pa"); i+=2; break;
            case 0x33AC: sb.setCharAt(i, 'G'); sb.insert(i+1, "Pa"); i+=2; break;
            case 0x33AD: sb.setCharAt(i, 'r'); sb.insert(i+1, "ad"); i+=2; break;
            case 0x33AE: sb.setCharAt(i, 'r'); sb.insert(i+1, "ad/s"); i+=4; break;
            case 0x33AF: sb.setCharAt(i, 'r'); sb.insert(i+1, "ad/s\u00B2"); i+=5; break;
            case 0x33B0: sb.setCharAt(i, 'p'); sb.insert(i+1, "s"); i++; break;
            case 0x33B1: sb.setCharAt(i, 'n'); sb.insert(i+1, "s"); i++; break;
            case 0x33B2: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "s"); i++; break;
            case 0x33B3: sb.setCharAt(i, 'm'); sb.insert(i+1, "s"); i++; break;
            case 0x33B4: sb.setCharAt(i, 'p'); sb.insert(i+1, "V"); i++; break;
            case 0x33B5: sb.setCharAt(i, 'n'); sb.insert(i+1, "V"); i++; break;
            case 0x33B6: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "V"); i++; break;
            case 0x33B7: sb.setCharAt(i, 'm'); sb.insert(i+1, "V"); i++; break;
            case 0x33B8: sb.setCharAt(i, 'k'); sb.insert(i+1, "V"); i++; break;
            case 0x33B9: sb.setCharAt(i, 'M'); sb.insert(i+1, "V"); i++; break;
            case 0x33BA: sb.setCharAt(i, 'p'); sb.insert(i+1, "W"); i++; break;
            case 0x33BB: sb.setCharAt(i, 'n'); sb.insert(i+1, "W"); i++; break;
            case 0x33BC: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "W"); i++; break;
            case 0x33BD: sb.setCharAt(i, 'm'); sb.insert(i+1, "W"); i++; break;
            case 0x33BE: sb.setCharAt(i, 'k'); sb.insert(i+1, "W"); i++; break;
            case 0x33BF: sb.setCharAt(i, 'M'); sb.insert(i+1, "W"); i++; break;
            case 0x33C0: sb.setCharAt(i, 'k'); sb.insert(i+1, "\u03A9"); i++; break;
            case 0x33C1: sb.setCharAt(i, 'M'); sb.insert(i+1, "\u03A9"); i++; break;
            case 0x33C2: sb.setCharAt(i, 'a'); sb.insert(i+1, ".m."); i+=3; break;
            case 0x33C3: sb.setCharAt(i, 'B'); sb.insert(i+1, "q"); i++; break;
            case 0x33C4: sb.setCharAt(i, 'c'); sb.insert(i+1, "c"); i++; break;
            case 0x33C5: sb.setCharAt(i, 'c'); sb.insert(i+1, "d"); i++; break;
            case 0x33C6: sb.setCharAt(i, 'C'); sb.insert(i+1, "/kg"); i+=3; break;
            case 0x33C7: sb.setCharAt(i, 'C'); sb.insert(i+1, "o."); i+=2; break;
            case 0x33C8: sb.setCharAt(i, 'd'); sb.insert(i+1, "B"); i++; break;
            case 0x33C9: sb.setCharAt(i, 'G'); sb.insert(i+1, "y"); i++; break;
            case 0x33CA: sb.setCharAt(i, 'h'); sb.insert(i+1, "a"); i++; break;
            case 0x33CB: sb.setCharAt(i, 'H'); sb.insert(i+1, "P"); i++; break;
            case 0x33CC: sb.setCharAt(i, 'i'); sb.insert(i+1, "n"); i++; break;
            case 0x33CD: sb.setCharAt(i, 'K'); sb.insert(i+1, "K"); i++; break;
            case 0x33CE: sb.setCharAt(i, 'K'); sb.insert(i+1, "M"); i++; break;
            case 0x33CF: sb.setCharAt(i, 'K'); sb.insert(i+1, "t"); i++; break;
            case 0x33D0: sb.setCharAt(i, 'l'); sb.insert(i+1, "m"); i++; break;
            case 0x33D1: sb.setCharAt(i, 'l'); sb.insert(i+1, "n"); i++; break;
            case 0x33D2: sb.setCharAt(i, 'l'); sb.insert(i+1, "og"); i+=2; break;
            case 0x33D3: sb.setCharAt(i, 'l'); sb.insert(i+1, "x"); i++; break;
            case 0x33D4: sb.setCharAt(i, 'm'); sb.insert(i+1, "b"); i++; break;
            case 0x33D5: sb.setCharAt(i, 'm'); sb.insert(i+1, "il"); i+=2; break;
            case 0x33D6: sb.setCharAt(i, 'm'); sb.insert(i+1, "ol"); i+=2; break;
            case 0x33D7: sb.setCharAt(i, 'p'); sb.insert(i+1, "H"); i++; break;
            case 0x33D8: sb.setCharAt(i, 'p'); sb.insert(i+1, ".m."); i+=3; break;
            case 0x33D9: sb.setCharAt(i, 'P'); sb.insert(i+1, "PM"); i+=2; break;
            case 0x33DA: sb.setCharAt(i, 'P'); sb.insert(i+1, "R"); i++; break;
            case 0x33DB: sb.setCharAt(i, 's'); sb.insert(i+1, "r"); i++; break;
            case 0x33DC: sb.setCharAt(i, 'S'); sb.insert(i+1, "v"); i++; break;
            case 0x33DD: sb.setCharAt(i, 'W'); sb.insert(i+1, "b"); i++; break;
            case 0x33DE: sb.setCharAt(i, 'v'); sb.insert(i+1, "/m"); i+=2; break;
            case 0x33DF: sb.setCharAt(i, 'a'); sb.insert(i+1, "/m"); i+=2; break;
            // Squared Latin Abbreviations 3
            case 0x33FF: sb.setCharAt(i, 'g'); sb.insert(i+1, "al"); i+=2; break;
            }
        }

        String result = sb.toString();

        if (text.equals(result)) {
            // No characters were changed. Return the original text so that
            // composition of unrelated characters is not affected.
            return text;
        }

        return normalizeUnicode(result);
    }
    // CHECKSTYLE:ON

    /**
     * Strip whitespace from the end of a string. Uses
     * {@link Character#isWhitespace(int)}, so it does not strip the extra
     * non-breaking whitespace included in {@link #isWhiteSpace(int)}.
     *
     * @param text
     * @return text with trailing whitespace removed
     */
    public static String rstrip(String text) {
        for (int cp, i = text.length(); i >= 0; i -= Character.charCount(cp)) {
            if (i == 0) {
                return "";
            }
            cp = text.codePointBefore(i);
            if (!Character.isWhitespace(cp)) {
                return text.substring(0, i);
            }
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
     * Convert a string's <code>charset</code> bytes into a Base64-encoded String.
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
     * Convert a char array's <code>charset</code> bytes into a Base64-encoded String.
     * Useful for handling passwords. Intermediate buffers are cleared after use.
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
     * For a string delimited by some separator, retrieve the last {@code segments} segments.
     *
     * @param str
     *            The string
     * @param separator
     *            The separator delimiting the string's segments
     * @param segments
     *            The number of segments to return, starting at the end
     * @return The trailing segments, or, if {@code segments} is greater than the number of segments contained in
     *         {@code str}, then {@code str} itself.
     */
    public static String getTailSegments(String str, int separator, int segments) {
        int start = str.length();
        for (int i = 0; i < segments; i++) {
            start = str.lastIndexOf(separator, start - Character.charCount(1));
            if (start == -1) {
                return str;
            }
        }
        return str.substring(start + Character.charCount(separator), str.length());
    }
}
