/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.util.xml;

import java.util.HashMap;

/**
 * Entity filter for XML.
 * <p>
 * Does XML Entity -&gt; Symbol conversion on source file read and Symbol -&gt;
 * XML Entity conversion on translation write.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
@Deprecated(forRemoval = true, since = "5.8")
public class DefaultEntityFilter {

    private static final HashMap<Integer, String> CHAR_MAP;
    private static final HashMap<String, Integer> ESC_MAP;

    static {
        ESC_MAP = new HashMap<String, Integer>(512);
        CHAR_MAP = new HashMap<Integer, String>(512);

        // CHECKSTYLE:OFF
        addMapEntry('\'', "apos");
        addMapEntry('"', "quot");
        addMapEntry('&', "amp");
        addMapEntry(60, "lt");
        addMapEntry(62, "gt");
        addMapEntry(160, "nbsp");
        addMapEntry(161, "iexcl");
        addMapEntry(162, "cent");
        addMapEntry(163, "pound");
        addMapEntry(164, "curren");
        addMapEntry(165, "yen");
        addMapEntry(166, "brvbar");
        addMapEntry(167, "sect");
        addMapEntry(168, "uml");
        addMapEntry(169, "copy");
        addMapEntry(170, "ordf");
        addMapEntry(171, "laquo");
        addMapEntry(172, "not");
        addMapEntry(173, "shy");
        addMapEntry(174, "reg");
        addMapEntry(175, "macr");
        addMapEntry(176, "deg");
        addMapEntry(177, "plusmn");
        addMapEntry(178, "sup2");
        addMapEntry(179, "sup3");
        addMapEntry(180, "acute");
        addMapEntry(181, "micro");
        addMapEntry(182, "para");
        addMapEntry(183, "middot");
        addMapEntry(184, "cedil");
        addMapEntry(185, "sup1");
        addMapEntry(186, "ordm");
        addMapEntry(187, "raquo");
        addMapEntry(188, "frac14");
        addMapEntry(189, "frac12");
        addMapEntry(190, "frac34");
        addMapEntry(191, "iquest");
        addMapEntry(192, "Agrave");
        addMapEntry(193, "Aacute");
        addMapEntry(194, "Acirc");
        addMapEntry(195, "Atilde");
        addMapEntry(196, "Auml");
        addMapEntry(197, "Aring");
        addMapEntry(198, "AElig");
        addMapEntry(199, "Ccedil");
        addMapEntry(200, "Egrave");
        addMapEntry(201, "Eacute");
        addMapEntry(202, "Ecirc");
        addMapEntry(203, "Euml");
        addMapEntry(204, "Igrave");
        addMapEntry(205, "Iacute");
        addMapEntry(206, "Icirc");
        addMapEntry(207, "Iuml");
        addMapEntry(208, "ETH");
        addMapEntry(209, "Ntilde");
        addMapEntry(210, "Ograve");
        addMapEntry(211, "Oacute");
        addMapEntry(212, "Ocirc");
        addMapEntry(213, "Otilde");
        addMapEntry(214, "Ouml");
        addMapEntry(215, "times");
        addMapEntry(216, "Oslash");
        addMapEntry(217, "Ugrave");
        addMapEntry(218, "Uacute");
        addMapEntry(219, "Ucirc");
        addMapEntry(220, "Uuml");
        addMapEntry(221, "Yacute");
        addMapEntry(222, "THORN");
        addMapEntry(223, "szlig");
        addMapEntry(224, "agrave");
        addMapEntry(225, "aacute");
        addMapEntry(226, "acirc");
        addMapEntry(227, "atilde");
        addMapEntry(228, "auml");
        addMapEntry(229, "aring");
        addMapEntry(230, "aelig");
        addMapEntry(231, "ccedil");
        addMapEntry(232, "egrave");
        addMapEntry(233, "eacute");
        addMapEntry(234, "ecirc");
        addMapEntry(235, "euml");
        addMapEntry(236, "igrave");
        addMapEntry(237, "iacute");
        addMapEntry(238, "icirc");
        addMapEntry(239, "iuml");
        addMapEntry(240, "eth");
        addMapEntry(241, "ntilde");
        addMapEntry(242, "ograve");
        addMapEntry(243, "oacute");
        addMapEntry(244, "ocirc");
        addMapEntry(245, "otilde");
        addMapEntry(246, "ouml");
        addMapEntry(247, "divide");
        addMapEntry(248, "oslash");
        addMapEntry(249, "ugrave");
        addMapEntry(250, "uacute");
        addMapEntry(251, "ucirc");
        addMapEntry(252, "uuml");
        addMapEntry(253, "yacute");
        addMapEntry(254, "thorn");
        addMapEntry(255, "yuml");
        addMapEntry(338, "OElig");
        addMapEntry(339, "oelig");
        addMapEntry(352, "Scaron");
        addMapEntry(353, "scaron");
        addMapEntry(376, "Yuml");
        addMapEntry(402, "fnof");
        addMapEntry(710, "circ");
        addMapEntry(732, "tilde");
        addMapEntry(913, "Alpha");
        addMapEntry(914, "Beta");
        addMapEntry(915, "Gamma");
        addMapEntry(916, "Delta");
        addMapEntry(917, "Epsilon");
        addMapEntry(918, "Zeta");
        addMapEntry(919, "Eta");
        addMapEntry(920, "Theta");
        addMapEntry(921, "Iota");
        addMapEntry(922, "Kappa");
        addMapEntry(923, "Lambda");
        addMapEntry(924, "Mu");
        addMapEntry(925, "Nu");
        addMapEntry(926, "Xi");
        addMapEntry(927, "Omicron");
        addMapEntry(928, "Pi");
        addMapEntry(929, "Rho");
        addMapEntry(931, "Sigma");
        addMapEntry(932, "Tau");
        addMapEntry(933, "Upsilon");
        addMapEntry(934, "Phi");
        addMapEntry(935, "Chi");
        addMapEntry(936, "Psi");
        addMapEntry(937, "Omega");
        addMapEntry(945, "alpha");
        addMapEntry(946, "beta");
        addMapEntry(947, "gamma");
        addMapEntry(948, "delta");
        addMapEntry(949, "epsilon");
        addMapEntry(950, "zeta");
        addMapEntry(951, "eta");
        addMapEntry(952, "theta");
        addMapEntry(953, "iota");
        addMapEntry(954, "kappa");
        addMapEntry(955, "lambda");
        addMapEntry(956, "mu");
        addMapEntry(957, "nu");
        addMapEntry(958, "xi");
        addMapEntry(959, "omicron");
        addMapEntry(960, "pi");
        addMapEntry(961, "rho");
        addMapEntry(962, "sigmaf");
        addMapEntry(963, "sigma");
        addMapEntry(964, "tau");
        addMapEntry(965, "upsilon");
        addMapEntry(966, "phi");
        addMapEntry(967, "chi");
        addMapEntry(968, "psi");
        addMapEntry(969, "omega");
        addMapEntry(977, "thetasym");
        addMapEntry(978, "upsih");
        addMapEntry(982, "piv");
        addMapEntry(8194, "ensp");
        addMapEntry(8195, "emsp");
        addMapEntry(8201, "thinsp");
        addMapEntry(8204, "zwnj");
        addMapEntry(8205, "zwj");
        addMapEntry(8206, "lrm");
        addMapEntry(8207, "rlm");
        addMapEntry(8211, "ndash");
        addMapEntry(8212, "mdash");
        addMapEntry(8216, "lsquo");
        addMapEntry(8217, "rsquo");
        addMapEntry(8218, "sbquo");
        addMapEntry(8220, "ldquo");
        addMapEntry(8221, "rdquo");
        addMapEntry(8222, "bdquo");
        addMapEntry(8224, "dagger");
        addMapEntry(8225, "Dagger");
        addMapEntry(8226, "bull");
        addMapEntry(8230, "hellip");
        addMapEntry(8240, "permil");
        addMapEntry(8242, "prime");
        addMapEntry(8243, "Prime");
        addMapEntry(8249, "lsaquo");
        addMapEntry(8250, "rsaquo");
        addMapEntry(8254, "oline");
        addMapEntry(8260, "frasl");
        addMapEntry(8364, "euro");
        addMapEntry(8465, "image");
        addMapEntry(8472, "weierp");
        addMapEntry(8476, "real");
        addMapEntry(8482, "trade");
        addMapEntry(8501, "alefsym");
        addMapEntry(8592, "larr");
        addMapEntry(8593, "uarr");
        addMapEntry(8594, "rarr");
        addMapEntry(8595, "darr");
        addMapEntry(8596, "harr");
        addMapEntry(8629, "crarr");
        addMapEntry(8656, "lArr");
        addMapEntry(8657, "uArr");
        addMapEntry(8658, "rArr");
        addMapEntry(8659, "dArr");
        addMapEntry(8660, "hArr");
        addMapEntry(8704, "forall");
        addMapEntry(8706, "part");
        addMapEntry(8707, "exist");
        addMapEntry(8709, "empty");
        addMapEntry(8711, "nabla");
        addMapEntry(8712, "isin");
        addMapEntry(8713, "notin");
        addMapEntry(8715, "ni");
        addMapEntry(8719, "prod");
        addMapEntry(8721, "sum");
        addMapEntry(8722, "minus");
        addMapEntry(8727, "lowast");
        addMapEntry(8730, "radic");
        addMapEntry(8733, "prop");
        addMapEntry(8734, "infin");
        addMapEntry(8736, "ang");
        addMapEntry(8743, "and");
        addMapEntry(8744, "or");
        addMapEntry(8745, "cap");
        addMapEntry(8746, "cup");
        addMapEntry(8747, "int");
        addMapEntry(8756, "there4");
        addMapEntry(8764, "sim");
        addMapEntry(8773, "cong");
        addMapEntry(8776, "asymp");
        addMapEntry(8800, "ne");
        addMapEntry(8801, "equiv");
        addMapEntry(8804, "le");
        addMapEntry(8805, "ge");
        addMapEntry(8834, "sub");
        addMapEntry(8835, "sup");
        addMapEntry(8836, "nsub");
        addMapEntry(8838, "sube");
        addMapEntry(8839, "supe");
        addMapEntry(8853, "oplus");
        addMapEntry(8855, "otimes");
        addMapEntry(8869, "perp");
        addMapEntry(8901, "sdot");
        addMapEntry(8968, "lceil");
        addMapEntry(8969, "rceil");
        addMapEntry(8970, "lfloor");
        addMapEntry(8971, "rfloor");
        addMapEntry(9001, "lang");
        addMapEntry(9002, "rang");
        addMapEntry(9674, "loz");
        addMapEntry(9824, "spades");
        addMapEntry(9827, "clubs");
        addMapEntry(9829, "hearts");
        addMapEntry(9830, "diams");
        // CHECKSTYLE:ON
    }

    private static void addMapEntry(int val, String name) {
        ESC_MAP.put(name, val);
        CHAR_MAP.put(val, name);
    }

    /**
     * Converts plaintext symbol to XML entity.
     */
    public String convertToEntity(int cp) {
        String s = CHAR_MAP.get(cp);
        if (s != null) {
            return "&" + s + ";";
        } else {
            return String.valueOf(Character.toChars(cp));
        }
    }

    /**
     * Converts XML entity to plaintext character. If the entity cannot be
     * converted, returns 0.
     */
    public int convertToSymbol(String escapeSequence) {
        Integer cp = ESC_MAP.get(escapeSequence);
        if (cp != null) {
            return cp;
        }
        try {
            return Integer.parseInt(escapeSequence);
        } catch (NumberFormatException e) {
            // Unconvertable Entity
            return 0;
        }
    }
}
