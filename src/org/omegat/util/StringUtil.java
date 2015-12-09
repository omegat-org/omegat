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
            // ASCII
            if ((cp >= 0x0021) && (cp <= 0x007E)) {
                result.append(genFullHalfMatchRegex(quote(cp), (char) (cp + 0xFEE0)));
                continue;
            }
            // Half width Hangul
            if ((cp > 0xFFA1) && (cp <= 0xFFBE)) {
                result.append(genFullHalfMatchRegex((char) cp, (char) (cp - 0xCE70)));
                continue;
            }
            // Full width alphabet
            if (0xff01 < cp && cp < 0xff5e) {
                result.append(genFullHalfMatchRegex((char) cp, quote(cp - 0xFEE0)));
                continue;
            }
            // Full width Hangul
            if ((cp > 0x3131) && (cp <= 0x314E)) {
                result.append(genFullHalfMatchRegex((char) cp, (char) (cp + 0xCE70)));
                continue;
            }
            if (fullHalfConvertMap.containsKey(Integer.valueOf(cp))) {
                result.append(genFullHalfMatchRegex((char)cp, (char)fullHalfConvertMap.get(Integer.valueOf(cp)).intValue()));
            } else {
                result.append(codePoint2String(cp));
            }
        }
        return result.toString();
    }

    public static String codePoint2String(int cp) {
        return new String(new int[]{cp}, 0, 1);
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

    private static final Map<Integer, Integer> fullHalfConvertMap = new HashMap<Integer, Integer>() {
        {
            put(0x0020, 0x3000);
            // Half width Katakana
            put(0xFF61, 0x3002);
            put(0xFF62, 0x300C);
            put(0xFF63, 0x300D);
            put(0xFF64, 0x3001);
            put(0xFF65, 0x30FB);
            put(0xFF66, 0x30F2);
            put(0xFF67, 0x30A1);
            put(0xFF68, 0x30A3);
            put(0xFF69, 0x30A5);
            put(0xFF6A, 0x30A7);
            put(0xFF6B, 0x30A9);
            put(0xFF6C, 0x30E3);
            put(0xFF6D, 0x30E5);
            put(0xFF6E, 0x30E7);
            put(0xFF6F, 0x30C3);
            put(0xFF70, 0x30FC);
            put(0xFF71, 0x30A2);
            put(0xFF72, 0x30A4);
            put(0xFF73, 0x30A6);
            put(0xFF74, 0x30A8);
            put(0xFF75, 0x30AA);
            put(0xFF76, 0x30AB);
            put(0xFF77, 0x30AD);
            put(0xFF78, 0x30AF);
            put(0xFF79, 0x30B1);
            put(0xFF7A, 0x30B3);
            put(0xFF7B, 0x30B5);
            put(0xFF7C, 0x30B7);
            put(0xFF7D, 0x30B9);
            put(0xFF7E, 0x30BB);
            put(0xFF7F, 0x30BD);
            put(0xFF80, 0x30BF);
            put(0xFF81, 0x30C1);
            put(0xFF82, 0x30C4);
            put(0xFF83, 0x30C6);
            put(0xFF84, 0x30C8);
            put(0xFF85, 0x30CA);
            put(0xFF86, 0x30CB);
            put(0xFF87, 0x30CC);
            put(0xFF88, 0x30CD);
            put(0xFF89, 0x30CE);
            put(0xFF8A, 0x30CF);
            put(0xFF8B, 0x30D2);
            put(0xFF8C, 0x30D5);
            put(0xFF8D, 0x30D8);
            put(0xFF8E, 0x30DB);
            put(0xFF8F, 0x30DE);
            put(0xFF90, 0x30DF);
            put(0xFF91, 0x30E0);
            put(0xFF92, 0x30E1);
            put(0xFF93, 0x30E2);
            put(0xFF94, 0x30E4);
            put(0xFF95, 0x30E6);
            put(0xFF96, 0x30E8);
            put(0xFF97, 0x30E9);
            put(0xFF98, 0x30EA);
            put(0xFF99, 0x30EB);
            put(0xFF9A, 0x30EC);
            put(0xFF9B, 0x30ED);
            put(0xFF9C, 0x30EF);
            put(0xFF9D, 0x30F3);
            put(0xFF9E, 0x3099);
            put(0xFF9F, 0x309A);
            // Half width Hangul
            put(0xFFA0, 0x3164);
            put(0xFFDA, 0x3161);
            put(0xFFDB, 0x3162);
            put(0xFFDC, 0x3163);
            // Others
            put(0xFFE8, 0x2502);
            put(0xFFE9, 0x2190);
            put(0xFFEA, 0x2191);
            put(0xFFEB, 0x2192);
            put(0xFFEC, 0x2193);
            put(0xFFED, 0x25A0);
            put(0xFFEE, 0x25CB);
            // Full width space
            put(0x3000, 0x0020);
            // Katakana to half width
            put(0x3002, 0xFF61);
            put(0x300C, 0xFF62);
            put(0x300D, 0xFF63);
            put(0x3001, 0xFF64);
            put(0x30FB, 0xFF65);
            put(0x30F2, 0xFF66);
            put(0x30A1, 0xFF67);
            put(0x30A3, 0xFF68);
            put(0x30A5, 0xFF69);
            put(0x30A7, 0xFF6A);
            put(0x30A9, 0xFF6B);
            put(0x30E3, 0xFF6C);
            put(0x30E5, 0xFF6D);
            put(0x30E7, 0xFF6E);
            put(0x30C3, 0xFF6F);
            put(0x30FC, 0xFF70);
            put(0x30A2, 0xFF71);
            put(0x30A4, 0xFF72);
            put(0x30A6, 0xFF73);
            put(0x30A8, 0xFF74);
            put(0x30AA, 0xFF75);
            put(0x30AB, 0xFF76);
            put(0x30AD, 0xFF77);
            put(0x30AF, 0xFF78);
            put(0x30B1, 0xFF79);
            put(0x30B3, 0xFF7A);
            put(0x30B5, 0xFF7B);
            put(0x30B7, 0xFF7C);
            put(0x30B9, 0xFF7D);
            put(0x30BB, 0xFF7E);
            put(0x30BD, 0xFF7F);
            put(0x30BF, 0xFF80);
            put(0x30C1, 0xFF81);
            put(0x30C4, 0xFF82);
            put(0x30C6, 0xFF83);
            put(0x30C8, 0xFF84);
            put(0x30CA, 0xFF85);
            put(0x30CB, 0xFF86);
            put(0x30CC, 0xFF87);
            put(0x30CD, 0xFF88);
            put(0x30CE, 0xFF89);
            put(0x30CF, 0xFF8A);
            put(0x30D2, 0xFF8B);
            put(0x30D5, 0xFF8C);
            put(0x30D8, 0xFF8D);
            put(0x30DB, 0xFF8E);
            put(0x30DE, 0xFF8F);
            put(0x30DF, 0xFF90);
            put(0x30E0, 0xFF91);
            put(0x30E1, 0xFF92);
            put(0x30E2, 0xFF93);
            put(0x30E4, 0xFF94);
            put(0x30E6, 0xFF95);
            put(0x30E8, 0xFF96);
            put(0x30E9, 0xFF97);
            put(0x30EA, 0xFF98);
            put(0x30EB, 0xFF99);
            put(0x30EC, 0xFF9A);
            put(0x30ED, 0xFF9B);
            put(0x30EF, 0xFF9C);
            put(0x30F3, 0xFF9D);
            put(0x3099, 0xFF9E);
            put(0x309A, 0xFF9F);
            // Hangul
            put(0x3164, 0xFFA0);
            put(0x3161, 0xFFDA);
            put(0x3162, 0xFFDB);
            put(0x3163, 0xFFDC);
            // Others
            put(0x2502, 0xFFE8);
            put(0x2190, 0xFFE9);
            put(0x2191, 0xFFEA);
            put(0x2192, 0xFFEB);
            put(0x2193, 0xFFEC);
            put(0x25A0, 0xFFED);
            put(0x25CB, 0xFFEE);
        }
    };

}
