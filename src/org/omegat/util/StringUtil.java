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
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utilities for string processing.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Tiago Saboga
 * @author Zoltan Bartko
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class StringUtil {

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
        return Character.isTitleCase(codePoint) ||
                (Character.isUpperCase(codePoint) && Character.toTitleCase(codePoint) == codePoint);
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
        int remainder = text.offsetByCodePoints(0, 1);
        String firstCP = text.substring(0, remainder);
        return StringUtil.toTitleCase(firstCP, locale)
                + text.substring(remainder);
    }
    
    public static String matchCapitalization(String text, String matchTo, Locale locale) {
        if (StringUtil.isEmpty(matchTo)) {
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
        // If matching to lower when input is upper, turn into lower.
        if (StringUtil.isLowerCase(matchTo) && StringUtil.isUpperCase(text)) {
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
        int firstTitleCase = Character.toTitleCase(text.codePointAt(0));
        int remainderOffset = text.offsetByCodePoints(0, 1);
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
    public static <T> T nvl(T... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return values[i];
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
    public static <T> boolean equalsWithNulls(T v1, T v2) {
        if (v1 == null && v2 == null) {
            return true;
        } else if (v1 != null && v2 != null) {
            return v1.equals(v2);
        } else {
            return false;
        }
    }

    /**
     * Compare two values, which could be null.
     */
    public static <T extends Comparable<T>> int compareToWithNulls(T v1, T v2) {
        if (v1 == null && v2 == null) {
            return 0;
        } else if (v1 == null && v2 != null) {
            return -1;
        } else if (v1 != null && v2 == null) {
            return 1;
        } else {
            return v1.compareTo(v2);
        }
    }

    /**
     * Extracts first N chars from string.
     */
    public static String firstN(String str, int len) {
        if (str.length() < len) {
            return str;
        } else {
            return str.substring(0, len) + "...";
        }
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
        return Normalizer.isNormalized(text, Normalizer.Form.NFC) ? text.toString() :
            Normalizer.normalize(text, Normalizer.Form.NFC);
    }

    /**
     * Replace invalid XML chars by spaces. See supported chars at
     * http://www.w3.org/TR/2006/REC-xml-20060816/#charsets.
     *
     * @param str
     *            input stream
     * @return result stream
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
     *
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public static String format(String str, Object... arguments) {
        // MessageFormat.format expects single quotes to be escaped
        // by duplicating them, otherwise the string will not be formatted
        str = str.replaceAll("'", "''");
        return MessageFormat.format(str, arguments);
    }

    /**
     * create match expression for full/half width character
     *
     * Analyze text whether contains full/half width character
     * that has a codepoint in both full-width and half-width expression in Unicode
     *    ex. ASCII, full-width alphabet, full-width katakana, half-width katakana, etc.
     *
     * @param  String
     * @return String expresssion such as '(A|\uff21)(B|\uff22)(C|\uff23)'
     **/
    public static String createFullHalfMatchExpression(String text) {
        StringBuilder sb = new StringBuilder(text);
        StringBuilder result = new StringBuilder();

        for (int cp, i = 0; i < sb.length(); i += Character.charCount(cp) ) {
            cp = sb.codePointAt(i);
            // ASCII
            if (( cp >= 0x0021 ) && ( cp <= 0x007E )) {
                result.append(genFullHalfMatchRegex(quote(cp), (char)(cp+0xFEE0)));
                continue;
            }
            // Half width Hangul
            if (( cp > 0xFFA1 ) && ( cp <= 0xFFBE )) {
                result.append(genFullHalfMatchRegex((char)cp, (char)(cp-0xCE70)));
                continue;
            }
            // Full width alphabet
            if (0xff01 <  cp &&  cp < 0xff5e) {
                result.append(genFullHalfMatchRegex((char)cp, quote(cp-0xFEE0)));
                continue;
            }
            // Full width Hangul
            if (( cp > 0x3131 ) && ( cp <= 0x314E )) {
                result.append(genFullHalfMatchRegex((char)cp, (char)(cp+0xCE70)));
                continue;
            }
            switch ( cp ) {
                // space
                case 0x0020: result.append(genFullHalfMatchRegex((char)cp, (char)0x3000)); break;
                // Half width Katakana
                case 0xFF61: result.append(genFullHalfMatchRegex((char)cp, (char)0x3002)); break;
                case 0xFF62: result.append(genFullHalfMatchRegex((char)cp, (char)0x300C)); break;
                case 0xFF63: result.append(genFullHalfMatchRegex((char)cp, (char)0x300D)); break;
                case 0xFF64: result.append(genFullHalfMatchRegex((char)cp, (char)0x3001)); break;
                case 0xFF65: result.append(genFullHalfMatchRegex((char)cp, (char)0x30FB)); break;
                case 0xFF66: result.append(genFullHalfMatchRegex((char)cp, (char)0x30F2)); break;
                case 0xFF67: result.append(genFullHalfMatchRegex((char)cp, (char)0x30A1)); break;
                case 0xFF68: result.append(genFullHalfMatchRegex((char)cp, (char)0x30A3)); break;
                case 0xFF69: result.append(genFullHalfMatchRegex((char)cp, (char)0x30A5)); break;
                case 0xFF6A: result.append(genFullHalfMatchRegex((char)cp, (char)0x30A7)); break;
                case 0xFF6B: result.append(genFullHalfMatchRegex((char)cp, (char)0x30A9)); break;
                case 0xFF6C: result.append(genFullHalfMatchRegex((char)cp, (char)0x30E3)); break;
                case 0xFF6D: result.append(genFullHalfMatchRegex((char)cp, (char)0x30E5)); break;
                case 0xFF6E: result.append(genFullHalfMatchRegex((char)cp, (char)0x30E7)); break;
                case 0xFF6F: result.append(genFullHalfMatchRegex((char)cp, (char)0x30C3)); break;
                case 0xFF70: result.append(genFullHalfMatchRegex((char)cp, (char)0x30FC)); break;
                case 0xFF71: result.append(genFullHalfMatchRegex((char)cp, (char)0x30A2)); break;
                case 0xFF72: result.append(genFullHalfMatchRegex((char)cp, (char)0x30A4)); break;
                case 0xFF73: result.append(genFullHalfMatchRegex((char)cp, (char)0x30A6)); break;
                case 0xFF74: result.append(genFullHalfMatchRegex((char)cp, (char)0x30A8)); break;
                case 0xFF75: result.append(genFullHalfMatchRegex((char)cp, (char)0x30AA)); break;
                case 0xFF76: result.append(genFullHalfMatchRegex((char)cp, (char)0x30AB)); break;
                case 0xFF77: result.append(genFullHalfMatchRegex((char)cp, (char)0x30AD)); break;
                case 0xFF78: result.append(genFullHalfMatchRegex((char)cp, (char)0x30AF)); break;
                case 0xFF79: result.append(genFullHalfMatchRegex((char)cp, (char)0x30B1)); break;
                case 0xFF7A: result.append(genFullHalfMatchRegex((char)cp, (char)0x30B3)); break;
                case 0xFF7B: result.append(genFullHalfMatchRegex((char)cp, (char)0x30B5)); break;
                case 0xFF7C: result.append(genFullHalfMatchRegex((char)cp, (char)0x30B7)); break;
                case 0xFF7D: result.append(genFullHalfMatchRegex((char)cp, (char)0x30B9)); break;
                case 0xFF7E: result.append(genFullHalfMatchRegex((char)cp, (char)0x30BB)); break;
                case 0xFF7F: result.append(genFullHalfMatchRegex((char)cp, (char)0x30BD)); break;
                case 0xFF80: result.append(genFullHalfMatchRegex((char)cp, (char)0x30BF)); break;
                case 0xFF81: result.append(genFullHalfMatchRegex((char)cp, (char)0x30C1)); break;
                case 0xFF82: result.append(genFullHalfMatchRegex((char)cp, (char)0x30C4)); break;
                case 0xFF83: result.append(genFullHalfMatchRegex((char)cp, (char)0x30C6)); break;
                case 0xFF84: result.append(genFullHalfMatchRegex((char)cp, (char)0x30C8)); break;
                case 0xFF85: result.append(genFullHalfMatchRegex((char)cp, (char)0x30CA)); break;
                case 0xFF86: result.append(genFullHalfMatchRegex((char)cp, (char)0x30CB)); break;
                case 0xFF87: result.append(genFullHalfMatchRegex((char)cp, (char)0x30CC)); break;
                case 0xFF88: result.append(genFullHalfMatchRegex((char)cp, (char)0x30CD)); break;
                case 0xFF89: result.append(genFullHalfMatchRegex((char)cp, (char)0x30CE)); break;
                case 0xFF8A: result.append(genFullHalfMatchRegex((char)cp, (char)0x30CF)); break;
                case 0xFF8B: result.append(genFullHalfMatchRegex((char)cp, (char)0x30D2)); break;
                case 0xFF8C: result.append(genFullHalfMatchRegex((char)cp, (char)0x30D5)); break;
                case 0xFF8D: result.append(genFullHalfMatchRegex((char)cp, (char)0x30D8)); break;
                case 0xFF8E: result.append(genFullHalfMatchRegex((char)cp, (char)0x30DB)); break;
                case 0xFF8F: result.append(genFullHalfMatchRegex((char)cp, (char)0x30DE)); break;
                case 0xFF90: result.append(genFullHalfMatchRegex((char)cp, (char)0x30DF)); break;
                case 0xFF91: result.append(genFullHalfMatchRegex((char)cp, (char)0x30E0)); break;
                case 0xFF92: result.append(genFullHalfMatchRegex((char)cp, (char)0x30E1)); break;
                case 0xFF93: result.append(genFullHalfMatchRegex((char)cp, (char)0x30E2)); break;
                case 0xFF94: result.append(genFullHalfMatchRegex((char)cp, (char)0x30E4)); break;
                case 0xFF95: result.append(genFullHalfMatchRegex((char)cp, (char)0x30E6)); break;
                case 0xFF96: result.append(genFullHalfMatchRegex((char)cp, (char)0x30E8)); break;
                case 0xFF97: result.append(genFullHalfMatchRegex((char)cp, (char)0x30E9)); break;
                case 0xFF98: result.append(genFullHalfMatchRegex((char)cp, (char)0x30EA)); break;
                case 0xFF99: result.append(genFullHalfMatchRegex((char)cp, (char)0x30EB)); break;
                case 0xFF9A: result.append(genFullHalfMatchRegex((char)cp, (char)0x30EC)); break;
                case 0xFF9B: result.append(genFullHalfMatchRegex((char)cp, (char)0x30ED)); break;
                case 0xFF9C: result.append(genFullHalfMatchRegex((char)cp, (char)0x30EF)); break;
                case 0xFF9D: result.append(genFullHalfMatchRegex((char)cp, (char)0x30F3)); break;
                case 0xFF9E: result.append(genFullHalfMatchRegex((char)cp, (char)0x3099)); break;
                case 0xFF9F: result.append(genFullHalfMatchRegex((char)cp, (char)0x309A)); break;
                // Half width Hangul
                case 0xFFA0: result.append(genFullHalfMatchRegex((char)cp, (char)0x3164)); break;
                case 0xFFDA: result.append(genFullHalfMatchRegex((char)cp, (char)0x3161)); break;
                case 0xFFDB: result.append(genFullHalfMatchRegex((char)cp, (char)0x3162)); break;
                case 0xFFDC: result.append(genFullHalfMatchRegex((char)cp, (char)0x3163)); break;
                // Others
                case 0xFFE8: result.append(genFullHalfMatchRegex((char)cp, (char)0x2502)); break;
                case 0xFFE9: result.append(genFullHalfMatchRegex((char)cp, (char)0x2190)); break;
                case 0xFFEA: result.append(genFullHalfMatchRegex((char)cp, (char)0x2191)); break;
                case 0xFFEB: result.append(genFullHalfMatchRegex((char)cp, (char)0x2192)); break;
                case 0xFFEC: result.append(genFullHalfMatchRegex((char)cp, (char)0x2193)); break;
                case 0xFFED: result.append(genFullHalfMatchRegex((char)cp, (char)0x25A0)); break;
                case 0xFFEE: result.append(genFullHalfMatchRegex((char)cp, (char)0x25CB)); break;
                // Full width space
                case 0x3000: result.append(genFullHalfMatchRegex((char)cp, (char)0x0020)); break;
                // Katakana to half width
                case 0x3002: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF61)); break;
                case 0x300C: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF62)); break;
                case 0x300D: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF63)); break;
                case 0x3001: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF64)); break;
                case 0x30FB: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF65)); break;
                case 0x30F2: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF66)); break;
                case 0x30A1: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF67)); break;
                case 0x30A3: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF68)); break;
                case 0x30A5: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF69)); break;
                case 0x30A7: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF6A)); break;
                case 0x30A9: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF6B)); break;
                case 0x30E3: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF6C)); break;
                case 0x30E5: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF6D)); break;
                case 0x30E7: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF6E)); break;
                case 0x30C3: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF6F)); break;
                case 0x30FC: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF70)); break;
                case 0x30A2: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF71)); break;
                case 0x30A4: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF72)); break;
                case 0x30A6: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF73)); break;
                case 0x30A8: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF74)); break;
                case 0x30AA: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF75)); break;
                case 0x30AB: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF76)); break;
                case 0x30AD: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF77)); break;
                case 0x30AF: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF78)); break;
                case 0x30B1: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF79)); break;
                case 0x30B3: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF7A)); break;
                case 0x30B5: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF7B)); break;
                case 0x30B7: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF7C)); break;
                case 0x30B9: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF7D)); break;
                case 0x30BB: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF7E)); break;
                case 0x30BD: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF7F)); break;
                case 0x30BF: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF80)); break;
                case 0x30C1: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF81)); break;
                case 0x30C4: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF82)); break;
                case 0x30C6: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF83)); break;
                case 0x30C8: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF84)); break;
                case 0x30CA: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF85)); break;
                case 0x30CB: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF86)); break;
                case 0x30CC: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF87)); break;
                case 0x30CD: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF88)); break;
                case 0x30CE: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF89)); break;
                case 0x30CF: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF8A)); break;
                case 0x30D2: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF8B)); break;
                case 0x30D5: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF8C)); break;
                case 0x30D8: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF8D)); break;
                case 0x30DB: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF8E)); break;
                case 0x30DE: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF8F)); break;
                case 0x30DF: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF90)); break;
                case 0x30E0: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF91)); break;
                case 0x30E1: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF92)); break;
                case 0x30E2: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF93)); break;
                case 0x30E4: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF94)); break;
                case 0x30E6: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF95)); break;
                case 0x30E8: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF96)); break;
                case 0x30E9: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF97)); break;
                case 0x30EA: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF98)); break;
                case 0x30EB: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF99)); break;
                case 0x30EC: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF9A)); break;
                case 0x30ED: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF9B)); break;
                case 0x30EF: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF9C)); break;
                case 0x30F3: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF9D)); break;
                case 0x3099: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF9E)); break;
                case 0x309A: result.append(genFullHalfMatchRegex((char)cp, (char)0xFF9F)); break;
                // Hangul
                case 0x3164: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFA0)); break;
                case 0x3161: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFDA)); break;
                case 0x3162: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFDB)); break;
                case 0x3163: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFDC)); break;
                // Others
                case 0x2502: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFE8)); break;
                case 0x2190: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFE9)); break;
                case 0x2191: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFEA)); break;
                case 0x2192: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFEB)); break;
                case 0x2193: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFEC)); break;
                case 0x25A0: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFED)); break;
                case 0x25CB: result.append(genFullHalfMatchRegex((char)cp, (char)0xFFEE)); break;
                // No match, add ch
                default: result.append(codePoint2String(cp)); break;
            }
        }
        return result.toString();
    }

    public static String codePoint2String(int cp){
        return new String(new int[]{cp},0,1);
    }

    /**
     * generate regex expression from two char or String parameter
     *
     * returns "(" + a + "|" + b + ")"
     *
     * @param char|String, char|String
     * @return String  (a|b)
     **/
    private static final String genFullHalfMatchRegex(char a, char b) {
        return new String(new char[]{'(',a,'|',b,')'});
    }
    private static final String genFullHalfMatchRegex(String a, char b) {
        return new StringBuilder("(").append(a).append("|").append(b).append(")").toString();
    }
    private static final String genFullHalfMatchRegex(char a, String b) {
        return new StringBuilder("(").append(a).append("|").append(b).append(")").toString();
    }
    private static final String genFullHalfMatchRegex(String a, String b) {
        return new StringBuilder("(").append(a).append("|").append(b).append(")").toString();
    }

    private static String quote(int cp) {
        return StaticUtils.escapeNonRegex(codePoint2String(cp), false);
    }
}
