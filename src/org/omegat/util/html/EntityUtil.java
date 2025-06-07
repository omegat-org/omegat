/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Didier Briel, Martin Fleurke
               2010 Didier Briel
               2011 Didier Briel, Martin Fleurke
               2012 Didier Briel, Martin Fleurke
               2013 Didier Briel, Alex Buloichik
               2017 Aaron Madlon-Kay
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
package org.omegat.util.html;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collection;
import java.util.regex.Pattern;

public class EntityUtil {

    private static final int PARSE_ERROR = -1;

    /** Named HTML Entities and corresponding numeric character references */
    private static final Object[][] ENTITIES = {
            { "quot", 34 },
            { "amp", 38 },
            { "apos", 39 },
            { "lt", 60 },
            { "gt", 62 },

            // Latin Extended-A
            { "OElig", 338 }, // latin capital ligature OE, U+0152 ISOlat2
            { "oelig", 339 }, // latin small ligature oe, U+0153 ISOlat2
            // ligature is a misnomer, this is a separate
            // character in some languages
            { "Scaron", 352 }, // latin capital letter S with caron, U+0160
            // ISOlat2
            { "scaron", 353 }, // latin small letter s with caron, U+0161
            // ISOlat2
            { "Yuml", 376 }, // latin capital letter Y with diaeresis, U+0178
            // ISOlat2

            // Spacing Modifier Letters
            { "circ", 710 }, // modifier letter circumflex accent, U+02C6 ISOpub
            { "tilde", 732 }, // small tilde, U+02DC ISOdia

            // General Punctuation
            { "ensp", 8194 }, // en space, U+2002 ISOpub
            { "emsp", 8195 }, // em space, U+2003 ISOpub
            { "thinsp", 8201 }, // thin space, U+2009 ISOpub
            { "zwnj", 8204 }, // zero width non-joiner, U+200C NEW RFC 2070
            { "zwj", 8205 }, // zero width joiner, U+200D NEW RFC 2070
            { "lrm", 8206 }, // left-to-right mark, U+200E NEW RFC 2070
            { "rlm", 8207 }, // right-to-left mark, U+200F NEW RFC 2070
            { "ndash", 8211 }, // en dash, U+2013 ISOpub
            { "mdash", 8212 }, // em dash, U+2014 ISOpub
            { "lsquo", 8216 }, // left single quotation mark, U+2018 ISOnum
            { "rsquo", 8217 }, // right single quotation mark, U+2019 ISOnum
            { "sbquo", 8218 }, // single low-9 quotation mark, U+201A NEW
            { "ldquo", 8220 }, // left double quotation mark, U+201C ISOnum
            { "rdquo", 8221 }, // right double quotation mark, U+201D ISOnum
            { "bdquo", 8222 }, // double low-9 quotation mark, U+201E NEW
            { "dagger", 8224 }, // dagger, U+2020 ISOpub
            { "Dagger", 8225 }, // double dagger, U+2021 ISOpub
            { "permil", 8240 }, // per mille sign, U+2030 ISOtech
            { "lsaquo", 8249 }, // single left-pointing angle quotation mark,
            // U+2039 ISO
            // proposed: lsaquo is proposed but not yet ISO
            // standardized
            { "rsaquo", 8250 }, // single right-pointing angle quotation mark,
            // U+203A ISO
            // proposed: rsaquo is proposed but not yet ISO
            // standardized
            { "euro", 8364 }, // euro sign, U+20AC NEW

            { "nbsp", 160 }, { "iexcl", 161 }, { "cent", 162 },
            { "pound", 163 }, { "curren", 164 }, { "yen", 165 },
            { "brvbar", 166 }, { "sect", 167 }, { "uml", 168 },
            { "copy", 169 }, { "ordf", 170 }, { "laquo", 171 },
            { "not", 172 }, { "shy", 173 }, { "reg", 174 },
            { "macr", 175 }, { "deg", 176 }, { "plusmn", 177 },
            { "sup2", 178 }, { "sup3", 179 }, { "acute", 180 },
            { "micro", 181 }, { "para", 182 }, { "middot", 183 },
            { "cedil", 184 }, { "sup1", 185 }, { "ordm", 186 },
            { "raquo", 187 }, { "frac14", 188 }, { "frac12", 189 },
            { "frac34", 190 }, { "iquest", 191 }, { "Agrave", 192 },
            { "Aacute", 193 }, { "Acirc", 194 }, { "Atilde", 195 },
            { "Auml", 196 }, { "Aring", 197 }, { "AElig", 198 },
            { "Ccedil", 199 }, { "Egrave", 200 }, { "Eacute", 201 },
            { "Ecirc", 202 }, { "Euml", 203 }, { "Igrave", 204 },
            { "Iacute", 205 }, { "Icirc", 206 }, { "Iuml", 207 },
            { "ETH", 208 }, { "Ntilde", 209 }, { "Ograve", 210 },
            { "Oacute", 211 }, { "Ocirc", 212 }, { "Otilde", 213 },
            { "Ouml", 214 }, { "times", 215 }, { "Oslash", 216 },
            { "Ugrave", 217 }, { "Uacute", 218 }, { "Ucirc", 219 },
            { "Uuml", 220 }, { "Yacute", 221 }, { "THORN", 222 },
            { "szlig", 223 }, { "agrave", 224 }, { "aacute", 225 },
            { "acirc", 226 }, { "atilde", 227 }, { "auml", 228 },
            { "aring", 229 }, { "aelig", 230 }, { "ccedil", 231 },
            { "egrave", 232 }, { "eacute", 233 }, { "ecirc", 234 },
            { "euml", 235 }, { "igrave", 236 }, { "iacute", 237 },
            { "icirc", 238 }, { "iuml", 239 }, { "eth", 240 },
            { "ntilde", 241 }, { "ograve", 242 }, { "oacute", 243 },
            { "ocirc", 244 }, { "otilde", 245 }, { "ouml", 246 },
            { "divide", 247 }, { "oslash", 248 }, { "ugrave", 249 },
            { "uacute", 250 }, { "ucirc", 251 }, { "uuml", 252 },
            { "yacute", 253 }, { "thorn", 254 }, { "yuml", 255 },

            { "fnof", 402 },

            { "Alpha", 913 }, { "Beta", 914 }, { "Gamma", 915 },
            { "Delta", 916 }, { "Epsilon", 917 }, { "Zeta", 918 },
            { "Eta", 919 }, { "Theta", 920 }, { "Iota", 921 },
            { "Kappa", 922 }, { "Lambda", 923 }, { "Mu", 924 },
            { "Nu", 925 }, { "Xi", 926 }, { "Omicron", 927 },
            { "Pi", 928 }, { "Rho", 929 }, { "Sigma", 931 },
            { "Tau", 932 }, { "Upsilon", 933 }, { "Phi", 934 },
            { "Chi", 935 }, { "Psi", 936 }, { "Omega", 937 },
            { "alpha", 945 }, { "beta", 946 }, { "gamma", 947 },
            { "delta", 948 }, { "epsilon", 949 }, { "zeta", 950 },
            { "eta", 951 }, { "theta", 952 }, { "iota", 953 },
            { "kappa", 954 }, { "lambda", 955 }, { "mu", 956 },
            { "nu", 957 }, { "xi", 958 }, { "omicron", 959 },
            { "pi", 960 }, { "rho", 961 }, { "sigmaf", 962 },
            { "sigma", 963 }, { "tau", 964 }, { "upsilon", 965 },
            { "phi", 966 }, { "chi", 967 }, { "psi", 968 },
            { "omega", 969 }, { "thetasym", 977 }, { "upsih", 978 },
            { "piv", 982 },

            { "bull", 8226 }, { "hellip", 8230 }, { "prime", 8242 },
            { "Prime", 8243 }, { "oline", 8254 }, { "frasl", 8260 },

            { "weierp", 8472 }, { "image", 8465 }, { "real", 8476 },
            { "trade", 8482 }, { "alefsym", 8501 },

            { "larr", 8592 }, { "uarr", 8593 }, { "rarr", 8594 },
            { "darr", 8595 }, { "harr", 8596 }, { "crarr", 8629 },
            { "lArr", 8656 }, { "uArr", 8657 }, { "rArr", 8658 },
            { "dArr", 8659 }, { "hArr", 8660 },

            { "forall", 8704 }, { "part", 8706 }, { "exist", 8707 },
            { "empty", 8709 }, { "nabla", 8711 }, { "isin", 8712 },
            { "notin", 8713 }, { "ni", 8715 }, { "prod", 8719 },
            { "sum", 8722 }, { "minus", 8722 }, { "lowast", 8727 },
            { "radic", 8730 }, { "prop", 8733 }, { "infin", 8734 },
            { "ang", 8736 }, { "and", 8869 }, { "or", 8870 },
            { "cap", 8745 }, { "cup", 8746 }, { "int", 8747 },
            { "there4", 8756 }, { "sim", 8764 }, { "cong", 8773 },
            { "asymp", 8773 }, { "ne", 8800 }, { "equiv", 8801 },
            { "le", 8804 }, { "ge", 8805 }, { "sub", 8834 },
            { "sup", 8835 }, { "nsub", 8836 }, { "sube", 8838 },
            { "supe", 8839 }, { "oplus", 8853 }, { "otimes", 8855 },
            { "perp", 8869 }, { "sdot", 8901 },

            { "lceil", 8968 }, { "rceil", 8969 }, { "lfloor", 8970 },
            { "rfloor", 8971 }, { "lang", 9001 }, { "rang", 9002 },

            { "loz", 9674 },

            { "spades", 9824 }, { "clubs", 9827 }, { "hearts", 9829 },
            { "diams", 9830 } };

    private static final String NBSP = "&nbsp;";
    private static final String AMP = "&amp;";
    private static final String GT = "&gt;";
    private static final String LT = "&lt;";

    public String entitiesToChars(String input) {
        int inputLength = input.length();
        StringBuilder result = new StringBuilder(inputLength);
        for (int cp, i = 0; i < inputLength; i += Character.charCount(cp)) {
            cp = input.codePointAt(i);
            if (cp == '&') {
                // if there's one more symbol, reading it,
                // otherwise it's a dangling '&'
                if (input.codePointCount(i, inputLength) < 2) {
                    result.appendCodePoint(cp);
                    continue;
                }
                int nextCodePoint;
                nextCodePoint = input.codePointAt(input.offsetByCodePoints(i, 1));
                if (nextCodePoint == '#') {
                    // numeric entity
                    i = handleNumericEntity(input, i, inputLength, result);
                } else if (isLatinLetter(nextCodePoint)) {
                    i = handleNamedEntity(input, i, inputLength, result);
                } else {
                    // dangling '&'
                    result.appendCodePoint(cp);
                }
            } else {
                result.appendCodePoint(cp);
            }
        }
        return result.toString();
    }

    public String charsToEntities(String input, String encoding, Collection<String> shortcuts) {
        int stringLength = input.length();
        StringBuilder resultBuilder = new StringBuilder(stringLength * 5);

        for (int codePoint, i = 0; i < stringLength; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            switch (codePoint) {
            case '\u00A0':
                resultBuilder.append(NBSP);
                break;
            case '&':
                resultBuilder.append(AMP);
                break;
            case '>':
                if (i > 0 && input.codePointBefore(i) == '?') {
                    resultBuilder.append(">");
                } else {
                    resultBuilder.append(GT);
                }
                break;
            case '<':
                handleLessThan(input, shortcuts, resultBuilder, i);
                break;
            default:
                resultBuilder.appendCodePoint(codePoint);
            }
        }

        String result = resultBuilder.toString();
        if (encoding != null) {
            result = rewriteUnencodableCharacters(result, encoding);
        }
        return result;
    }

    private void handleLessThan(String input, Collection<String> shortcuts, StringBuilder resultBuilder, int startIndex) {
        int questionMarkPos = input.indexOf('?', startIndex);
        if (questionMarkPos == input.offsetByCodePoints(startIndex, 1)) {
            resultBuilder.append("<");
            return;
        }

        int greaterThanPos = input.indexOf('>', startIndex);
        if (greaterThanPos >= 0) {
            String possibleShortcut = input.substring(startIndex, input.offsetByCodePoints(greaterThanPos, 1));
            if (shortcuts.contains(possibleShortcut)) {
                resultBuilder.append(possibleShortcut);
                return;
            }
        }
        resultBuilder.append(LT);
    }

    private String rewriteUnencodableCharacters(String contents, String encoding) {
        CharsetEncoder charsetEncoder = Charset.forName(encoding).newEncoder();
        for (int i = 0; i < contents.length(); ) {
            int codePoint = contents.codePointAt(i);
            String charAsString = new String(Character.toChars(codePoint));
            if (!charsetEncoder.canEncode(charAsString)) {
                String replacement = "&#" + codePoint + ';';
                contents = contents.replaceAll(Pattern.quote(charAsString), replacement);
            }
            i += charAsString.length();
        }
        return contents;
    }

    private int handleNumericEntity(String input, int startIndex, int inputLength, StringBuilder result) {
        int thirdCodePoint = input.codePointAt(input.offsetByCodePoints(startIndex, 2));
        if (thirdCodePoint == 'x' || thirdCodePoint == 'X') {
            return handleHexEntity(input, startIndex, inputLength, result);
        } else {
            return handleDecimalEntity(input, startIndex, inputLength, result);
        }
    }

    private int handleHexEntity(String input, int startIndex, int inputLength, StringBuilder result) {
        int startOffset = input.offsetByCodePoints(startIndex, 3);
        int endOffset = findEndOfDigits(input, startOffset, inputLength, this::isHexDigit);

        String hexValue = input.substring(startOffset, endOffset);
        int parsedValue = parseEntity(hexValue, 16);
        return processParsedEntity(input, startIndex, inputLength, result, endOffset, parsedValue);
    }

    private int handleDecimalEntity(String input, int startIndex, int inputLength, StringBuilder result) {
        int startOffset = input.offsetByCodePoints(startIndex, 2);
        int endOffset = findEndOfDigits(input, startOffset, inputLength, this::isDecimalDigit);

        String decimalValue = input.substring(startOffset, endOffset);
        int parsedValue = parseEntity(decimalValue, 10);
        return processParsedEntity(input, startIndex, inputLength, result, endOffset, parsedValue);
    }

    private int handleNamedEntity(String input, int startIndex, int inputLength, StringBuilder result) {
        int startOffset = input.offsetByCodePoints(startIndex, 1);
        int endOffset = findEndOfDigits(input, startOffset, inputLength, ch -> isLatinLetter(ch) || isDecimalDigit(ch));

        String entityName = input.substring(startOffset, endOffset);
        int parsedValue = lookupEntity(entityName);
        if (parsedValue > 0 && parsedValue <= 65535) {
            result.append((char) parsedValue);
            if (endOffset < inputLength && input.codePointAt(endOffset) == ';') {
                return endOffset;
            }
            return input.offsetByCodePoints(endOffset, -1);
        }

        // Invalid named entity
        result.appendCodePoint(input.codePointAt(startIndex));
        return startIndex;
    }

    private int processParsedEntity(String input, int startIndex, int inputLength, StringBuilder result, int endOffset, int parsedValue) {
        // check if parsedValue is PARSE_ERROR or invalid unicode code point.
        if (parsedValue <= 0 || parsedValue > 0x10FFFF) {
            // invalid char code
            result.appendCodePoint(input.codePointAt(startIndex));
            return startIndex;
        }

        result.appendCodePoint(parsedValue);
        if (endOffset < inputLength && input.codePointAt(endOffset) == ';') {
            return endOffset;
        }
        return input.offsetByCodePoints(endOffset, -1);
    }

    private int findEndOfDigits(String input, int startOffset, int inputLength, java.util.function.IntPredicate isDigit) {
        int currentOffset = startOffset;
        while (currentOffset < inputLength) {
            int currentCodePoint = input.codePointAt(currentOffset);
            if (!isDigit.test(currentCodePoint)) {
                break;
            }
            currentOffset += Character.charCount(currentCodePoint);
        }
        return currentOffset;
    }

    private int parseEntity(String entityValue, int radix) {
        try {
            return Integer.parseInt(entityValue, radix);
        } catch (NumberFormatException e) {
            // return out of char range that is recognized in processParsedEntity method
            // as to ignore parsedValue and return the original character.
            return PARSE_ERROR;
        }
    }

    /** Returns true if a char is a latin letter */
    public boolean isLatinLetter(int ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    /** Returns true if a char is a decimal digit */
    public boolean isDecimalDigit(int ch) {
        return (ch >= '0' && ch <= '9');
    }

    /** Returns true if a char is a hex digit */
    public boolean isHexDigit(int ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }

    /**
     * returns a character for HTML entity, or -1 if the passed string is not an
     * entity
     */
    private int lookupEntity(String entity) {
        for (Object[] onent : ENTITIES) {
            if (entity.equals(onent[0])) {
                return (Integer) onent[1];
            }
        }
        return -1;
    }

}