/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.util.xml;

import java.util.HashMap;

/**
 * Entity filter for XML.
 * <p>
 * Does XML Entity -> Symbol conversion on source file read and Symbol -> XML
 * Entity conversion on translation write.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class DefaultEntityFilter {

    private static HashMap<Character, String> m_charMap;
    private static HashMap<String, Character> m_escMap;

    static {
        m_escMap = new HashMap<String, Character>(512);
        m_charMap = new HashMap<Character, String>(512);

        addMapEntry('\'', "apos");
        addMapEntry('"', "quot");
        addMapEntry('&', "amp");
        addMapEntry((char) 60, "lt");
        addMapEntry((char) 62, "gt");
        addMapEntry((char) 160, "nbsp");
        addMapEntry((char) 161, "iexcl");
        addMapEntry((char) 162, "cent");
        addMapEntry((char) 163, "pound");
        addMapEntry((char) 164, "curren");
        addMapEntry((char) 165, "yen");
        addMapEntry((char) 166, "brvbar");
        addMapEntry((char) 167, "sect");
        addMapEntry((char) 168, "uml");
        addMapEntry((char) 169, "copy");
        addMapEntry((char) 170, "ordf");
        addMapEntry((char) 171, "laquo");
        addMapEntry((char) 172, "not");
        addMapEntry((char) 173, "shy");
        addMapEntry((char) 174, "reg");
        addMapEntry((char) 175, "macr");
        addMapEntry((char) 176, "deg");
        addMapEntry((char) 177, "plusmn");
        addMapEntry((char) 178, "sup2");
        addMapEntry((char) 179, "sup3");
        addMapEntry((char) 180, "acute");
        addMapEntry((char) 181, "micro");
        addMapEntry((char) 182, "para");
        addMapEntry((char) 183, "middot");
        addMapEntry((char) 184, "cedil");
        addMapEntry((char) 185, "sup1");
        addMapEntry((char) 186, "ordm");
        addMapEntry((char) 187, "raquo");
        addMapEntry((char) 188, "frac14");
        addMapEntry((char) 189, "frac12");
        addMapEntry((char) 190, "frac34");
        addMapEntry((char) 191, "iquest");
        addMapEntry((char) 192, "Agrave");
        addMapEntry((char) 193, "Aacute");
        addMapEntry((char) 194, "Acirc");
        addMapEntry((char) 195, "Atilde");
        addMapEntry((char) 196, "Auml");
        addMapEntry((char) 197, "Aring");
        addMapEntry((char) 198, "AElig");
        addMapEntry((char) 199, "Ccedil");
        addMapEntry((char) 200, "Egrave");
        addMapEntry((char) 201, "Eacute");
        addMapEntry((char) 202, "Ecirc");
        addMapEntry((char) 203, "Euml");
        addMapEntry((char) 204, "Igrave");
        addMapEntry((char) 205, "Iacute");
        addMapEntry((char) 206, "Icirc");
        addMapEntry((char) 207, "Iuml");
        addMapEntry((char) 208, "ETH");
        addMapEntry((char) 209, "Ntilde");
        addMapEntry((char) 210, "Ograve");
        addMapEntry((char) 211, "Oacute");
        addMapEntry((char) 212, "Ocirc");
        addMapEntry((char) 213, "Otilde");
        addMapEntry((char) 214, "Ouml");
        addMapEntry((char) 215, "times");
        addMapEntry((char) 216, "Oslash");
        addMapEntry((char) 217, "Ugrave");
        addMapEntry((char) 218, "Uacute");
        addMapEntry((char) 219, "Ucirc");
        addMapEntry((char) 220, "Uuml");
        addMapEntry((char) 221, "Yacute");
        addMapEntry((char) 222, "THORN");
        addMapEntry((char) 223, "szlig");
        addMapEntry((char) 224, "agrave");
        addMapEntry((char) 225, "aacute");
        addMapEntry((char) 226, "acirc");
        addMapEntry((char) 227, "atilde");
        addMapEntry((char) 228, "auml");
        addMapEntry((char) 229, "aring");
        addMapEntry((char) 230, "aelig");
        addMapEntry((char) 231, "ccedil");
        addMapEntry((char) 232, "egrave");
        addMapEntry((char) 233, "eacute");
        addMapEntry((char) 234, "ecirc");
        addMapEntry((char) 235, "euml");
        addMapEntry((char) 236, "igrave");
        addMapEntry((char) 237, "iacute");
        addMapEntry((char) 238, "icirc");
        addMapEntry((char) 239, "iuml");
        addMapEntry((char) 240, "eth");
        addMapEntry((char) 241, "ntilde");
        addMapEntry((char) 242, "ograve");
        addMapEntry((char) 243, "oacute");
        addMapEntry((char) 244, "ocirc");
        addMapEntry((char) 245, "otilde");
        addMapEntry((char) 246, "ouml");
        addMapEntry((char) 247, "divide");
        addMapEntry((char) 248, "oslash");
        addMapEntry((char) 249, "ugrave");
        addMapEntry((char) 250, "uacute");
        addMapEntry((char) 251, "ucirc");
        addMapEntry((char) 252, "uuml");
        addMapEntry((char) 253, "yacute");
        addMapEntry((char) 254, "thorn");
        addMapEntry((char) 255, "yuml");
        addMapEntry((char) 338, "OElig");
        addMapEntry((char) 339, "oelig");
        addMapEntry((char) 352, "Scaron");
        addMapEntry((char) 353, "scaron");
        addMapEntry((char) 376, "Yuml");
        addMapEntry((char) 402, "fnof");
        addMapEntry((char) 710, "circ");
        addMapEntry((char) 732, "tilde");
        addMapEntry((char) 913, "Alpha");
        addMapEntry((char) 914, "Beta");
        addMapEntry((char) 915, "Gamma");
        addMapEntry((char) 916, "Delta");
        addMapEntry((char) 917, "Epsilon");
        addMapEntry((char) 918, "Zeta");
        addMapEntry((char) 919, "Eta");
        addMapEntry((char) 920, "Theta");
        addMapEntry((char) 921, "Iota");
        addMapEntry((char) 922, "Kappa");
        addMapEntry((char) 923, "Lambda");
        addMapEntry((char) 924, "Mu");
        addMapEntry((char) 925, "Nu");
        addMapEntry((char) 926, "Xi");
        addMapEntry((char) 927, "Omicron");
        addMapEntry((char) 928, "Pi");
        addMapEntry((char) 929, "Rho");
        addMapEntry((char) 931, "Sigma");
        addMapEntry((char) 932, "Tau");
        addMapEntry((char) 933, "Upsilon");
        addMapEntry((char) 934, "Phi");
        addMapEntry((char) 935, "Chi");
        addMapEntry((char) 936, "Psi");
        addMapEntry((char) 937, "Omega");
        addMapEntry((char) 945, "alpha");
        addMapEntry((char) 946, "beta");
        addMapEntry((char) 947, "gamma");
        addMapEntry((char) 948, "delta");
        addMapEntry((char) 949, "epsilon");
        addMapEntry((char) 950, "zeta");
        addMapEntry((char) 951, "eta");
        addMapEntry((char) 952, "theta");
        addMapEntry((char) 953, "iota");
        addMapEntry((char) 954, "kappa");
        addMapEntry((char) 955, "lambda");
        addMapEntry((char) 956, "mu");
        addMapEntry((char) 957, "nu");
        addMapEntry((char) 958, "xi");
        addMapEntry((char) 959, "omicron");
        addMapEntry((char) 960, "pi");
        addMapEntry((char) 961, "rho");
        addMapEntry((char) 962, "sigmaf");
        addMapEntry((char) 963, "sigma");
        addMapEntry((char) 964, "tau");
        addMapEntry((char) 965, "upsilon");
        addMapEntry((char) 966, "phi");
        addMapEntry((char) 967, "chi");
        addMapEntry((char) 968, "psi");
        addMapEntry((char) 969, "omega");
        addMapEntry((char) 977, "thetasym");
        addMapEntry((char) 978, "upsih");
        addMapEntry((char) 982, "piv");
        addMapEntry((char) 8194, "ensp");
        addMapEntry((char) 8195, "emsp");
        addMapEntry((char) 8201, "thinsp");
        addMapEntry((char) 8204, "zwnj");
        addMapEntry((char) 8205, "zwj");
        addMapEntry((char) 8206, "lrm");
        addMapEntry((char) 8207, "rlm");
        addMapEntry((char) 8211, "ndash");
        addMapEntry((char) 8212, "mdash");
        addMapEntry((char) 8216, "lsquo");
        addMapEntry((char) 8217, "rsquo");
        addMapEntry((char) 8218, "sbquo");
        addMapEntry((char) 8220, "ldquo");
        addMapEntry((char) 8221, "rdquo");
        addMapEntry((char) 8222, "bdquo");
        addMapEntry((char) 8224, "dagger");
        addMapEntry((char) 8225, "Dagger");
        addMapEntry((char) 8226, "bull");
        addMapEntry((char) 8230, "hellip");
        addMapEntry((char) 8240, "permil");
        addMapEntry((char) 8242, "prime");
        addMapEntry((char) 8243, "Prime");
        addMapEntry((char) 8249, "lsaquo");
        addMapEntry((char) 8250, "rsaquo");
        addMapEntry((char) 8254, "oline");
        addMapEntry((char) 8260, "frasl");
        addMapEntry((char) 8364, "euro");
        addMapEntry((char) 8465, "image");
        addMapEntry((char) 8472, "weierp");
        addMapEntry((char) 8476, "real");
        addMapEntry((char) 8482, "trade");
        addMapEntry((char) 8501, "alefsym");
        addMapEntry((char) 8592, "larr");
        addMapEntry((char) 8593, "uarr");
        addMapEntry((char) 8594, "rarr");
        addMapEntry((char) 8595, "darr");
        addMapEntry((char) 8596, "harr");
        addMapEntry((char) 8629, "crarr");
        addMapEntry((char) 8656, "lArr");
        addMapEntry((char) 8657, "uArr");
        addMapEntry((char) 8658, "rArr");
        addMapEntry((char) 8659, "dArr");
        addMapEntry((char) 8660, "hArr");
        addMapEntry((char) 8704, "forall");
        addMapEntry((char) 8706, "part");
        addMapEntry((char) 8707, "exist");
        addMapEntry((char) 8709, "empty");
        addMapEntry((char) 8711, "nabla");
        addMapEntry((char) 8712, "isin");
        addMapEntry((char) 8713, "notin");
        addMapEntry((char) 8715, "ni");
        addMapEntry((char) 8719, "prod");
        addMapEntry((char) 8721, "sum");
        addMapEntry((char) 8722, "minus");
        addMapEntry((char) 8727, "lowast");
        addMapEntry((char) 8730, "radic");
        addMapEntry((char) 8733, "prop");
        addMapEntry((char) 8734, "infin");
        addMapEntry((char) 8736, "ang");
        addMapEntry((char) 8743, "and");
        addMapEntry((char) 8744, "or");
        addMapEntry((char) 8745, "cap");
        addMapEntry((char) 8746, "cup");
        addMapEntry((char) 8747, "int");
        addMapEntry((char) 8756, "there4");
        addMapEntry((char) 8764, "sim");
        addMapEntry((char) 8773, "cong");
        addMapEntry((char) 8776, "asymp");
        addMapEntry((char) 8800, "ne");
        addMapEntry((char) 8801, "equiv");
        addMapEntry((char) 8804, "le");
        addMapEntry((char) 8805, "ge");
        addMapEntry((char) 8834, "sub");
        addMapEntry((char) 8835, "sup");
        addMapEntry((char) 8836, "nsub");
        addMapEntry((char) 8838, "sube");
        addMapEntry((char) 8839, "supe");
        addMapEntry((char) 8853, "oplus");
        addMapEntry((char) 8855, "otimes");
        addMapEntry((char) 8869, "perp");
        addMapEntry((char) 8901, "sdot");
        addMapEntry((char) 8968, "lceil");
        addMapEntry((char) 8969, "rceil");
        addMapEntry((char) 8970, "lfloor");
        addMapEntry((char) 8971, "rfloor");
        addMapEntry((char) 9001, "lang");
        addMapEntry((char) 9002, "rang");
        addMapEntry((char) 9674, "loz");
        addMapEntry((char) 9824, "spades");
        addMapEntry((char) 9827, "clubs");
        addMapEntry((char) 9829, "hearts");
        addMapEntry((char) 9830, "diams");
    }

    private static void addMapEntry(char val, String name) {
        m_escMap.put(name, new Character(val));
        m_charMap.put(new Character(val), name);
    }

    /**
     * Converts plaintext symbol to XML entity.
     */
    public String convertToEntity(char c) {
        String s = m_charMap.get(c);
        if (s != null) {
            return "&" + s + ";";
        } else
            return "" + c;
    }

    /**
     * Converts XML entity to plaintext character. If the entity cannot be
     * converted, returns 0.
     */
    public char convertToSymbol(String escapeSequence) {
        Character c = m_escMap.get(escapeSequence);
        if (c == null) {
            try {
                Integer i = new Integer(escapeSequence);
                return (char) i.intValue();
            } catch (NumberFormatException e) {
                // Unconvertable Entity
                return 0;
            }
        } else
            return c.charValue();
    }
}
