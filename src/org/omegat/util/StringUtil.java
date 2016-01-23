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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
     *
     * @param  String
     * @return String expresssion such as '(A|\uff21)(B|\uff22)(C|\uff23)'
     **/
    public static String createFullHalfMatchExpression(String text) {
        StringBuilder sb = new StringBuilder(text);
        StringBuilder result = new StringBuilder();

        for (int cp, i = 0; i < sb.length(); i += Character.charCount(cp)) {
            cp = sb.codePointAt(i);
            int cq = 0;
            if ((cp >= 0x0021) && (cp <= 0x007E)) { // ASCII
                cq = cp + 0xFEE0;
            } else if (0xff01 < cp && cp < 0xff5e) { // Full width alphabet
                cq = cp - 0xFEE0;
            } else if ((cp > 0xFFA1) && (cp <= 0xFFBE)) { // Half width Hangul
                cq = cp - 0xCE70;
            } else if ((cp > 0x3131) && (cp <= 0x314E)) { // Full width Hangul
                cq = cp + 0xCE70;
            } else if (fullHalfConvertMap.containsKey(cp)) {
                result.append(fullHalfConvertMap.get(cp));
                continue;
            } else {
                result.append(codePoint2String(cp));
                continue;
            }
            // append regex string '(<cp>|<target>)' for charecter '<cp>'
            if (cq != 0) {
                result.append("(")
                      .append(StaticUtils.escapeNonRegex(codePoint2String(cp), false))
                      .append("|")
                      .append(StaticUtils.escapeNonRegex(codePoint2String(cq), false))
                      .append(")");
            }
        }
        return result.toString();
    }

    public static String codePoint2String(int cp) {
        return new String(new int[]{cp}, 0, 1);
    }

    // Full-width and half-width character map for the character that
    // is nessesary to map one-by-one.
    private static final Map<Integer, String> fullHalfConvertMap = new HashMap<Integer, String>() {
        {
            put(0x0020, "(\u0020|\u3000)");
            // Half width Katakana
            put(0xFF61, "(\uFF61|\u3002)");
            put(0xFF62, "(\uFF62|\u300C)");
            put(0xFF63, "(\uFF63|\u300D)");
            put(0xFF64, "(\uFF64|\u3001)");
            put(0xFF65, "(\uFF65|\u30FB)");
            put(0xFF66, "(\uFF66|\u30F2)");
            put(0xFF67, "(\uFF67|\u30A1)");
            put(0xFF68, "(\uFF68|\u30A3)");
            put(0xFF69, "(\uFF69|\u30A5)");
            put(0xFF6A, "(\uFF6A|\u30A7)");
            put(0xFF6B, "(\uFF6B|\u30A9)");
            put(0xFF6C, "(\uFF6C|\u30E3)");
            put(0xFF6D, "(\uFF6D|\u30E5)");
            put(0xFF6E, "(\uFF6E|\u30E7)");
            put(0xFF6F, "(\uFF6F|\u30C3)");
            put(0xFF70, "(\uFF70|\u30FC)");
            put(0xFF71, "(\uFF71|\u30A2)");
            put(0xFF72, "(\uFF72|\u30A4)");
            put(0xFF73, "(\uFF73|\u30A6)");
            put(0xFF74, "(\uFF74|\u30A8)");
            put(0xFF75, "(\uFF75|\u30AA)");
            put(0xFF76, "(\uFF76|\u30AB)");
            put(0xFF77, "(\uFF77|\u30AD)");
            put(0xFF78, "(\uFF78|\u30AF)");
            put(0xFF79, "(\uFF79|\u30B1)");
            put(0xFF7A, "(\uFF7A|\u30B3)");
            put(0xFF7B, "(\uFF7B|\u30B5)");
            put(0xFF7C, "(\uFF7C|\u30B7)");
            put(0xFF7D, "(\uFF7D|\u30B9)");
            put(0xFF7E, "(\uFF7E|\u30BB)");
            put(0xFF7F, "(\uFF7F|\u30BD)");
            put(0xFF80, "(\uFF80|\u30BF)");
            put(0xFF81, "(\uFF81|\u30C1)");
            put(0xFF82, "(\uFF82|\u30C4)");
            put(0xFF83, "(\uFF83|\u30C6)");
            put(0xFF84, "(\uFF84|\u30C8)");
            put(0xFF85, "(\uFF85|\u30CA)");
            put(0xFF86, "(\uFF86|\u30CB)");
            put(0xFF87, "(\uFF87|\u30CC)");
            put(0xFF88, "(\uFF88|\u30CD)");
            put(0xFF89, "(\uFF89|\u30CE)");
            put(0xFF8A, "(\uFF8A|\u30CF)");
            put(0xFF8B, "(\uFF8B|\u30D2)");
            put(0xFF8C, "(\uFF8C|\u30D5)");
            put(0xFF8D, "(\uFF8D|\u30D8)");
            put(0xFF8E, "(\uFF8E|\u30DB)");
            put(0xFF8F, "(\uFF8F|\u30DE)");
            put(0xFF90, "(\uFF90|\u30DF)");
            put(0xFF91, "(\uFF91|\u30E0)");
            put(0xFF92, "(\uFF92|\u30E1)");
            put(0xFF93, "(\uFF93|\u30E2)");
            put(0xFF94, "(\uFF94|\u30E4)");
            put(0xFF95, "(\uFF95|\u30E6)");
            put(0xFF96, "(\uFF96|\u30E8)");
            put(0xFF97, "(\uFF97|\u30E9)");
            put(0xFF98, "(\uFF98|\u30EA)");
            put(0xFF99, "(\uFF99|\u30EB)");
            put(0xFF9A, "(\uFF9A|\u30EC)");
            put(0xFF9B, "(\uFF9B|\u30ED)");
            put(0xFF9C, "(\uFF9C|\u30EF)");
            put(0xFF9D, "(\uFF9D|\u30F3)");
            put(0xFF9E, "(\uFF9E|\u3099)");
            put(0xFF9F, "(\uFF9F|\u309A)");
            // Half width Hangul
            put(0xFFA0, "(\uFFA0|\u3164)");
            put(0xFFDA, "(\uFFDA|\u3161)");
            put(0xFFDB, "(\uFFDB|\u3162)");
            put(0xFFDC, "(\uFFDC|\u3163)");
            // Others
            put(0xFFE8, "(\uFFE8|\u2502)");
            put(0xFFE9, "(\uFFE9|\u2190)");
            put(0xFFEA, "(\uFFEA|\u2191)");
            put(0xFFEB, "(\uFFEB|\u2192)");
            put(0xFFEC, "(\uFFEC|\u2193)");
            put(0xFFED, "(\uFFED|\u25A0)");
            put(0xFFEE, "(\uFFEE|\u25CB)");
            // Full width space
            put(0x3000, "(\u3000|\u0020)");
            // Katakana to half width
            put(0x3002, "(\u3002|\uFF61)");
            put(0x300C, "(\u300C|\uFF62)");
            put(0x300D, "(\u300D|\uFF63)");
            put(0x3001, "(\u3001|\uFF64)");
            put(0x30FB, "(\u30FB|\uFF65)");
            put(0x30F2, "(\u30F2|\uFF66)");
            put(0x30A1, "(\u30A1|\uFF67)");
            put(0x30A3, "(\u30A3|\uFF68)");
            put(0x30A5, "(\u30A5|\uFF69)");
            put(0x30A7, "(\u30A7|\uFF6A)");
            put(0x30A9, "(\u30A9|\uFF6B)");
            put(0x30E3, "(\u30E3|\uFF6C)");
            put(0x30E5, "(\u30E5|\uFF6D)");
            put(0x30E7, "(\u30E7|\uFF6E)");
            put(0x30C3, "(\u30C3|\uFF6F)");
            put(0x30FC, "(\u30FC|\uFF70)");
            put(0x30A2, "(\u30A2|\uFF71)");
            put(0x30A4, "(\u30A4|\uFF72)");
            put(0x30A6, "(\u30A6|\uFF73)");
            put(0x30A8, "(\u30A8|\uFF74)");
            put(0x30AA, "(\u30AA|\uFF75)");
            put(0x30AB, "(\u30AB|\uFF76)");
            put(0x30AD, "(\u30AD|\uFF77)");
            put(0x30AF, "(\u30AF|\uFF78)");
            put(0x30B1, "(\u30B1|\uFF79)");
            put(0x30B3, "(\u30B3|\uFF7A)");
            put(0x30B5, "(\u30B5|\uFF7B)");
            put(0x30B7, "(\u30B7|\uFF7C)");
            put(0x30B9, "(\u30B9|\uFF7D)");
            put(0x30BB, "(\u30BB|\uFF7E)");
            put(0x30BD, "(\u30BD|\uFF7F)");
            put(0x30BF, "(\u30BF|\uFF80)");
            put(0x30C1, "(\u30C1|\uFF81)");
            put(0x30C4, "(\u30C4|\uFF82)");
            put(0x30C6, "(\u30C6|\uFF83)");
            put(0x30C8, "(\u30C8|\uFF84)");
            put(0x30CA, "(\u30CA|\uFF85)");
            put(0x30CB, "(\u30CB|\uFF86)");
            put(0x30CC, "(\u30CC|\uFF87)");
            put(0x30CD, "(\u30CD|\uFF88)");
            put(0x30CE, "(\u30CE|\uFF89)");
            put(0x30CF, "(\u30CF|\uFF8A)");
            put(0x30D2, "(\u30D2|\uFF8B)");
            put(0x30D5, "(\u30D5|\uFF8C)");
            put(0x30D8, "(\u30D8|\uFF8D)");
            put(0x30DB, "(\u30DB|\uFF8E)");
            put(0x30DE, "(\u30DE|\uFF8F)");
            put(0x30DF, "(\u30DF|\uFF90)");
            put(0x30E0, "(\u30E0|\uFF91)");
            put(0x30E1, "(\u30E1|\uFF92)");
            put(0x30E2, "(\u30E2|\uFF93)");
            put(0x30E4, "(\u30E4|\uFF94)");
            put(0x30E6, "(\u30E6|\uFF95)");
            put(0x30E8, "(\u30E8|\uFF96)");
            put(0x30E9, "(\u30E9|\uFF97)");
            put(0x30EA, "(\u30EA|\uFF98)");
            put(0x30EB, "(\u30EB|\uFF99)");
            put(0x30EC, "(\u30EC|\uFF9A)");
            put(0x30ED, "(\u30ED|\uFF9B)");
            put(0x30EF, "(\u30EF|\uFF9C)");
            put(0x30F3, "(\u30F3|\uFF9D)");
            put(0x3099, "(\u3099|\uFF9E)");
            put(0x309A, "(\u309A|\uFF9F)");
            // Hangul
            put(0x3164, "(\u3164|\uFFA0)");
            put(0x3161, "(\u3161|\uFFDA)");
            put(0x3162, "(\u3162|\uFFDB)");
            put(0x3163, "(\u3163|\uFFDC)");
            // Others
            put(0x2502, "(\u2502|\uFFE8)");
            put(0x2190, "(\u2190|\uFFE9)");
            put(0x2191, "(\u2191|\uFFEA)");
            put(0x2192, "(\u2192|\uFFEB)");
            put(0x2193, "(\u2193|\uFFEC)");
            put(0x25A0, "(\u25A0|\uFFED)");
            put(0x25CB, "(\u25CB|\uFFEE)");
        }
    };

}
