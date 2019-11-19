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

package org.omegat.filters2.html2;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collection;
import java.util.regex.Pattern;

public final class HTMLUtils {

    private HTMLUtils() {
    }

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
            { "Scaron", 352 }, // latin capital letter S with caron, U+0160 ISOlat2
            { "scaron", 353 }, // latin small letter s with caron, U+0161 ISOlat2
            { "Yuml", 376 }, // latin capital letter Y with diaeresis, U+0178 ISOlat2

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
            { "lsaquo", 8249 }, // single left-pointing angle quotation mark, U+2039 ISO
                                // proposed: lsaquo is proposed but not yet ISO standardized
            { "rsaquo", 8250 }, // single right-pointing angle quotation mark, U+203A ISO
                                // proposed: rsaquo is proposed but not yet ISO standardized
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

    /** Converts HTML entities to normal characters */
    public static String entitiesToChars(String str) {
        int strlen = str.length();
        StringBuilder res = new StringBuilder(strlen);
        for (int cp, i = 0; i < strlen; i += Character.charCount(cp)) {
            cp = str.codePointAt(i);
            switch (cp) {
            case '&':
                int cp1;
                // if there's one more symbol, reading it,
                // otherwise it's a dangling '&'
                if (str.codePointCount(i, strlen) < 2) {
                    res.appendCodePoint(cp);
                    break;
                } else {
                    cp1 = str.codePointAt(str.offsetByCodePoints(i, 1));
                }
                if (cp1 == '#') {
                    // numeric entity
                    int cp2 = str.codePointAt(str.offsetByCodePoints(i, 2));
                    if (cp2 == 'x' || cp2 == 'X') {
                        // hex numeric entity
                        int hexStart = str.offsetByCodePoints(i, 3);
                        int hexEnd = hexStart;
                        while (hexEnd < strlen) {
                            int hexCp = str.codePointAt(hexEnd);
                            if (!isHexDigit(hexCp)) {
                                break;
                            }
                            hexEnd += Character.charCount(hexCp);
                        }
                        String sEntity = str.substring(hexStart, hexEnd);
                        try {
                            int nEntity = Integer.parseInt(sEntity, 16);
                            if (nEntity > 0 && nEntity <= 0x10FFFF) {
                                res.appendCodePoint(nEntity);
                                if (hexEnd < strlen && str.codePointAt(hexEnd) == ';') {
                                    i = hexEnd;
                                } else {
                                    i = str.offsetByCodePoints(hexEnd, -1);
                                }
                            } else {
                                // too big number
                                // dangling '&'
                                res.appendCodePoint(cp);
                            }
                        } catch (NumberFormatException nfe) {
                            // do nothing
                            // dangling '&'
                            res.appendCodePoint(cp);
                        }
                    } else {
                        // decimal entity
                        int decStart = str.offsetByCodePoints(i, 2);
                        int decEnd = decStart;
                        while (decEnd < strlen) {
                            int decCp = str.codePointAt(decEnd);
                            if (!isDecimalDigit(decCp)) {
                                break;
                            }
                            decEnd += Character.charCount(decCp);
                        }
                        String sEntity = str.substring(decStart, decEnd);
                        try {
                            int nEntity = Integer.parseInt(sEntity, 10);
                            if (nEntity > 0 && nEntity <= 0x10FFFF) {
                                res.appendCodePoint(nEntity);
                                if (decEnd < strlen && str.codePointAt(decEnd) == ';') {
                                    i = decEnd;
                                } else {
                                    i = str.offsetByCodePoints(decEnd, -1);
                                }
                            } else {
                                // too big number
                                // dangling '&'
                                res.appendCodePoint(cp);
                            }
                        } catch (NumberFormatException nfe) {
                            // do nothing
                            // dangling '&'
                            res.appendCodePoint(cp);
                        }
                    }
                } else if (isLatinLetter(cp1)) {
                    // named entity?
                    int entStart = str.offsetByCodePoints(i, 1);
                    int entEnd = entStart;
                    while (entEnd < strlen) {
                        int entCp = str.codePointAt(entEnd);
                        // Some entities contain numbers, e.g. frac12
                        if (!isLatinLetter(entCp) && !isDecimalDigit(entCp)) {
                            break;
                        }
                        entEnd += Character.charCount(entCp);
                    }
                    String sEntity = str.substring(entStart, entEnd);
                    int nEntity = lookupEntity(sEntity);
                    if (nEntity > 0 && nEntity <= 65535) {
                        res.append((char) nEntity);
                        if (entEnd < strlen && str.codePointAt(entEnd) == ';') {
                            i = entEnd;
                        } else {
                            i = str.offsetByCodePoints(entEnd, -1);
                        }
                    } else {
                        // too big number
                        // dangling '&'
                        res.appendCodePoint(cp);
                    }
                } else {
                    // dangling '&'
                    res.appendCodePoint(cp);
                }
                break;
            default:
                res.appendCodePoint(cp);
            }
        }
        return res.toString();
    }

    /** Returns true if a char is a latin letter */
    public static boolean isLatinLetter(int ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    /** Returns true if a char is a decimal digit */
    public static boolean isDecimalDigit(int ch) {
        return (ch >= '0' && ch <= '9');
    }

    /** Returns true if a char is a hex digit */
    public static boolean isHexDigit(int ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }

    /**
     * returns a character for HTML entity, or -1 if the passed string is not an entity
     */
    private static int lookupEntity(String entity) {
        for (int i = 0; i < ENTITIES.length; i++) {
            Object[] onent = ENTITIES[i];
            if (entity.equals(onent[0])) {
                return ((Integer) onent[1]).intValue();
            }
        }
        return -1;
    }

    /**
     * Converts characters that must be converted (&lt; &gt; &amp; '&nbsp;' (nbsp)) into HTML entities.
     *
     * @param str
     *            The string to convert
     * @param encoding
     *            The output encoding. May be null (all characters are assumed to be supported).
     * @param shortcuts
     *            A collection of tag-like strings ({@code <foo>}) that will be "protected", i.e. they will not be
     *            escaped as entities.
     */
    public static String charsToEntities(String str, String encoding, Collection<String> shortcuts) {
        int strlen = str.length();
        StringBuilder res = new StringBuilder(strlen * 5);
        for (int cp, i = 0; i < strlen; i += Character.charCount(cp)) {
            cp = str.codePointAt(i);
            switch (cp) {
            case '\u00A0':
                res.append("&nbsp;");
                break;
            case '&':
                res.append("&amp;");
                break;
            case '>':
                // If it's the end of a processing instruction
                if ((i > 0) && str.codePointBefore(i) == '?') {
                    res.append(">");
                } else {
                    res.append("&gt;");
                }
                break;
            case '<':
                int qMarkPos = str.indexOf('?', i);
                // If it's the beginning of a processing instruction
                if (qMarkPos == str.offsetByCodePoints(i, 1)) {
                    res.append("<");
                    break;
                }
                int gtpos = str.indexOf('>', i);
                if (gtpos >= 0) {
                    String maybeShortcut = str.substring(i, str.offsetByCodePoints(gtpos, 1));
                    boolean foundShortcut = false; // here because it's
                                                   // impossible to step out of
                                                   // two loops at once
                    for (String currShortcut : shortcuts) {
                        if (maybeShortcut.equals(currShortcut)) {
                            // skipping the conversion of < into &lt;
                            // because it's a part of the tag
                            foundShortcut = true;
                            break;
                        }
                    }
                    if (foundShortcut) {
                        res.append(maybeShortcut);
                        i = gtpos;
                        continue;
                    } else {
                        // dangling <
                        res.append("&lt;");
                    }
                } else {
                    // dangling <
                    res.append("&lt;");
                }
                break;
            default:
                res.appendCodePoint(cp);
            }
        }
        String contents = res.toString();
        // Rewrite characters that cannot be encoded to html character strings.
        // Each character in the contents-string is checked. If a character
        // can't be encoded, all its occurrences are replaced with the
        // html-equivalent string.
        // Then, the next character is checked.
        // (The loop over the contents-string is restarted for the modified
        // content, but the starting-position will be the position where the
        // last unencodable character was found)
        // [1802000] HTML filter loses html-encoded characters if not supported
        if (encoding != null) {
            CharsetEncoder charsetEncoder = Charset.forName(encoding).newEncoder();
            int i = 0;
            while (true) {
                String substring;
                for (int cp; i < contents.length(); i += substring.length()) {
                    cp = contents.codePointAt(i);
                    substring = contents.substring(i, i + Character.charCount(cp));
                    if (!charsetEncoder.canEncode(substring)) {
                        String replacement = "&#" + cp + ';';
                        contents = contents.replaceAll(Pattern.quote(substring), replacement);
                        break;
                    }
                }
                if (i == contents.length()) {
                    break;
                }
            }
        }
        return contents;
    }
}
