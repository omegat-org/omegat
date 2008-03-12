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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.util.xml;

import java.util.HashMap;

/**
 * Entity filter for XML.
 * <p>
 * Does XML Entity -> Symbol conversion on source file read
 * and Symbol -> XML Entity conversion on translation write.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class DefaultEntityFilter
{
    
    private static HashMap<Character,String>	m_charMap;
    private static HashMap<String,Character>	m_escMap;
    
    static
    {
        m_escMap = new HashMap<String, Character>(512);
        m_charMap = new HashMap<Character, String>(512);
        
        addMapEntry('\'', "apos");		     // NOI18N
        addMapEntry('"', "quot");            // NOI18N
        addMapEntry('&', "amp");             // NOI18N
        addMapEntry((char)60, "lt");		 // NOI18N
        addMapEntry((char)62, "gt");		 // NOI18N
        addMapEntry((char)160, "nbsp");		 // NOI18N
        addMapEntry((char)161, "iexcl");	 // NOI18N
        addMapEntry((char)162, "cent");		 // NOI18N
        addMapEntry((char)163, "pound");	 // NOI18N
        addMapEntry((char)164, "curren");	 // NOI18N
        addMapEntry((char)165, "yen");		 // NOI18N
        addMapEntry((char)166, "brvbar");	 // NOI18N
        addMapEntry((char)167, "sect");		 // NOI18N
        addMapEntry((char)168, "uml");		 // NOI18N
        addMapEntry((char)169, "copy");		 // NOI18N
        addMapEntry((char)170, "ordf");		 // NOI18N
        addMapEntry((char)171, "laquo");	 // NOI18N
        addMapEntry((char)172, "not");		 // NOI18N
        addMapEntry((char)173, "shy");		 // NOI18N
        addMapEntry((char)174, "reg");		 // NOI18N
        addMapEntry((char)175, "macr");		 // NOI18N
        addMapEntry((char)176, "deg");		 // NOI18N
        addMapEntry((char)177, "plusmn");	 // NOI18N
        addMapEntry((char)178, "sup2");		 // NOI18N
        addMapEntry((char)179, "sup3");		 // NOI18N
        addMapEntry((char)180, "acute");	 // NOI18N
        addMapEntry((char)181, "micro");	 // NOI18N
        addMapEntry((char)182, "para");		 // NOI18N
        addMapEntry((char)183, "middot");	 // NOI18N
        addMapEntry((char)184, "cedil");	 // NOI18N
        addMapEntry((char)185, "sup1");		 // NOI18N
        addMapEntry((char)186, "ordm");		 // NOI18N
        addMapEntry((char)187, "raquo");	 // NOI18N
        addMapEntry((char)188, "frac14");	 // NOI18N
        addMapEntry((char)189, "frac12");	 // NOI18N
        addMapEntry((char)190, "frac34");	 // NOI18N
        addMapEntry((char)191, "iquest");	 // NOI18N
        addMapEntry((char)192, "Agrave");	 // NOI18N
        addMapEntry((char)193, "Aacute");	 // NOI18N
        addMapEntry((char)194, "Acirc");	 // NOI18N
        addMapEntry((char)195, "Atilde");	 // NOI18N
        addMapEntry((char)196, "Auml");		 // NOI18N
        addMapEntry((char)197, "Aring");	 // NOI18N
        addMapEntry((char)198, "AElig");	 // NOI18N
        addMapEntry((char)199, "Ccedil");	 // NOI18N
        addMapEntry((char)200, "Egrave");	 // NOI18N
        addMapEntry((char)201, "Eacute");	 // NOI18N
        addMapEntry((char)202, "Ecirc");	 // NOI18N
        addMapEntry((char)203, "Euml");		 // NOI18N
        addMapEntry((char)204, "Igrave");	 // NOI18N
        addMapEntry((char)205, "Iacute");	 // NOI18N
        addMapEntry((char)206, "Icirc");	 // NOI18N
        addMapEntry((char)207, "Iuml");		 // NOI18N
        addMapEntry((char)208, "ETH");		 // NOI18N
        addMapEntry((char)209, "Ntilde");	 // NOI18N
        addMapEntry((char)210, "Ograve");	 // NOI18N
        addMapEntry((char)211, "Oacute");	 // NOI18N
        addMapEntry((char)212, "Ocirc");	 // NOI18N
        addMapEntry((char)213, "Otilde");	 // NOI18N
        addMapEntry((char)214, "Ouml");		 // NOI18N
        addMapEntry((char)215, "times");	 // NOI18N
        addMapEntry((char)216, "Oslash");	 // NOI18N
        addMapEntry((char)217, "Ugrave");	 // NOI18N
        addMapEntry((char)218, "Uacute");	 // NOI18N
        addMapEntry((char)219, "Ucirc");	 // NOI18N
        addMapEntry((char)220, "Uuml");		 // NOI18N
        addMapEntry((char)221, "Yacute");	 // NOI18N
        addMapEntry((char)222, "THORN");	 // NOI18N
        addMapEntry((char)223, "szlig");	 // NOI18N
        addMapEntry((char)224, "agrave");	 // NOI18N
        addMapEntry((char)225, "aacute");	 // NOI18N
        addMapEntry((char)226, "acirc");	 // NOI18N
        addMapEntry((char)227, "atilde");	 // NOI18N
        addMapEntry((char)228, "auml");		 // NOI18N
        addMapEntry((char)229, "aring");	 // NOI18N
        addMapEntry((char)230, "aelig");	 // NOI18N
        addMapEntry((char)231, "ccedil");	 // NOI18N
        addMapEntry((char)232, "egrave");	 // NOI18N
        addMapEntry((char)233, "eacute");	 // NOI18N
        addMapEntry((char)234, "ecirc");	 // NOI18N
        addMapEntry((char)235, "euml");		 // NOI18N
        addMapEntry((char)236, "igrave");	 // NOI18N
        addMapEntry((char)237, "iacute");	 // NOI18N
        addMapEntry((char)238, "icirc");	 // NOI18N
        addMapEntry((char)239, "iuml");		 // NOI18N
        addMapEntry((char)240, "eth");		 // NOI18N
        addMapEntry((char)241, "ntilde");	 // NOI18N
        addMapEntry((char)242, "ograve");	 // NOI18N
        addMapEntry((char)243, "oacute");	 // NOI18N
        addMapEntry((char)244, "ocirc");	 // NOI18N
        addMapEntry((char)245, "otilde");	 // NOI18N
        addMapEntry((char)246, "ouml");		 // NOI18N
        addMapEntry((char)247, "divide");	 // NOI18N
        addMapEntry((char)248, "oslash");	 // NOI18N
        addMapEntry((char)249, "ugrave");	 // NOI18N
        addMapEntry((char)250, "uacute");	 // NOI18N
        addMapEntry((char)251, "ucirc");	 // NOI18N
        addMapEntry((char)252, "uuml");		 // NOI18N
        addMapEntry((char)253, "yacute");	 // NOI18N
        addMapEntry((char)254, "thorn");	 // NOI18N
        addMapEntry((char)255, "yuml");		 // NOI18N
        addMapEntry((char)338, "OElig");	 // NOI18N
        addMapEntry((char)339, "oelig");	 // NOI18N
        addMapEntry((char)352, "Scaron");	 // NOI18N
        addMapEntry((char)353, "scaron");	 // NOI18N
        addMapEntry((char)376, "Yuml");		 // NOI18N
        addMapEntry((char)402, "fnof");		 // NOI18N
        addMapEntry((char)710, "circ");		 // NOI18N
        addMapEntry((char)732, "tilde");	 // NOI18N
        addMapEntry((char)913, "Alpha");	 // NOI18N
        addMapEntry((char)914, "Beta");		 // NOI18N
        addMapEntry((char)915, "Gamma");	 // NOI18N
        addMapEntry((char)916, "Delta");	 // NOI18N
        addMapEntry((char)917, "Epsilon");	 // NOI18N
        addMapEntry((char)918, "Zeta");		 // NOI18N
        addMapEntry((char)919, "Eta");		 // NOI18N
        addMapEntry((char)920, "Theta");	 // NOI18N
        addMapEntry((char)921, "Iota");		 // NOI18N
        addMapEntry((char)922, "Kappa");	 // NOI18N
        addMapEntry((char)923, "Lambda");	 // NOI18N
        addMapEntry((char)924, "Mu");		 // NOI18N
        addMapEntry((char)925, "Nu");		 // NOI18N
        addMapEntry((char)926, "Xi");		 // NOI18N
        addMapEntry((char)927, "Omicron");	 // NOI18N
        addMapEntry((char)928, "Pi");		 // NOI18N
        addMapEntry((char)929, "Rho");		 // NOI18N
        addMapEntry((char)931, "Sigma");	 // NOI18N
        addMapEntry((char)932, "Tau");		 // NOI18N
        addMapEntry((char)933, "Upsilon");	 // NOI18N
        addMapEntry((char)934, "Phi");		 // NOI18N
        addMapEntry((char)935, "Chi");		 // NOI18N
        addMapEntry((char)936, "Psi");		 // NOI18N
        addMapEntry((char)937, "Omega");	 // NOI18N
        addMapEntry((char)945, "alpha");	 // NOI18N
        addMapEntry((char)946, "beta");		 // NOI18N
        addMapEntry((char)947, "gamma");	 // NOI18N
        addMapEntry((char)948, "delta");	 // NOI18N
        addMapEntry((char)949, "epsilon");	 // NOI18N
        addMapEntry((char)950, "zeta");		 // NOI18N
        addMapEntry((char)951, "eta");		 // NOI18N
        addMapEntry((char)952, "theta");	 // NOI18N
        addMapEntry((char)953, "iota");		 // NOI18N
        addMapEntry((char)954, "kappa");	 // NOI18N
        addMapEntry((char)955, "lambda");	 // NOI18N
        addMapEntry((char)956, "mu");		 // NOI18N
        addMapEntry((char)957, "nu");		 // NOI18N
        addMapEntry((char)958, "xi");		 // NOI18N
        addMapEntry((char)959, "omicron");	 // NOI18N
        addMapEntry((char)960, "pi");		 // NOI18N
        addMapEntry((char)961, "rho");		 // NOI18N
        addMapEntry((char)962, "sigmaf");	 // NOI18N
        addMapEntry((char)963, "sigma");	 // NOI18N
        addMapEntry((char)964, "tau");		 // NOI18N
        addMapEntry((char)965, "upsilon");	 // NOI18N
        addMapEntry((char)966, "phi");		 // NOI18N
        addMapEntry((char)967, "chi");		 // NOI18N
        addMapEntry((char)968, "psi");		 // NOI18N
        addMapEntry((char)969, "omega");	 // NOI18N
        addMapEntry((char)977, "thetasym");	 // NOI18N
        addMapEntry((char)978, "upsih");	 // NOI18N
        addMapEntry((char)982, "piv");		 // NOI18N
        addMapEntry((char)8194, "ensp");	 // NOI18N
        addMapEntry((char)8195, "emsp");	 // NOI18N
        addMapEntry((char)8201, "thinsp");	 // NOI18N
        addMapEntry((char)8204, "zwnj");	 // NOI18N
        addMapEntry((char)8205, "zwj");		 // NOI18N
        addMapEntry((char)8206, "lrm");		 // NOI18N
        addMapEntry((char)8207, "rlm");		 // NOI18N
        addMapEntry((char)8211, "ndash");	 // NOI18N
        addMapEntry((char)8212, "mdash");	 // NOI18N
        addMapEntry((char)8216, "lsquo");	 // NOI18N
        addMapEntry((char)8217, "rsquo");	 // NOI18N
        addMapEntry((char)8218, "sbquo");	 // NOI18N
        addMapEntry((char)8220, "ldquo");	 // NOI18N
        addMapEntry((char)8221, "rdquo");	 // NOI18N
        addMapEntry((char)8222, "bdquo");	 // NOI18N
        addMapEntry((char)8224, "dagger");	 // NOI18N
        addMapEntry((char)8225, "Dagger");	 // NOI18N
        addMapEntry((char)8226, "bull");	 // NOI18N
        addMapEntry((char)8230, "hellip");	 // NOI18N
        addMapEntry((char)8240, "permil");	 // NOI18N
        addMapEntry((char)8242, "prime");	 // NOI18N
        addMapEntry((char)8243, "Prime");	 // NOI18N
        addMapEntry((char)8249, "lsaquo");	 // NOI18N
        addMapEntry((char)8250, "rsaquo");	 // NOI18N
        addMapEntry((char)8254, "oline");	 // NOI18N
        addMapEntry((char)8260, "frasl");	 // NOI18N
        addMapEntry((char)8364, "euro");	 // NOI18N
        addMapEntry((char)8465, "image");	 // NOI18N
        addMapEntry((char)8472, "weierp");	 // NOI18N
        addMapEntry((char)8476, "real");	 // NOI18N
        addMapEntry((char)8482, "trade");	 // NOI18N
        addMapEntry((char)8501, "alefsym");	 // NOI18N
        addMapEntry((char)8592, "larr");	 // NOI18N
        addMapEntry((char)8593, "uarr");	 // NOI18N
        addMapEntry((char)8594, "rarr");	 // NOI18N
        addMapEntry((char)8595, "darr");	 // NOI18N
        addMapEntry((char)8596, "harr");	 // NOI18N
        addMapEntry((char)8629, "crarr");	 // NOI18N
        addMapEntry((char)8656, "lArr");	 // NOI18N
        addMapEntry((char)8657, "uArr");	 // NOI18N
        addMapEntry((char)8658, "rArr");	 // NOI18N
        addMapEntry((char)8659, "dArr");	 // NOI18N
        addMapEntry((char)8660, "hArr");	 // NOI18N
        addMapEntry((char)8704, "forall");	 // NOI18N
        addMapEntry((char)8706, "part");	 // NOI18N
        addMapEntry((char)8707, "exist");	 // NOI18N
        addMapEntry((char)8709, "empty");	 // NOI18N
        addMapEntry((char)8711, "nabla");	 // NOI18N
        addMapEntry((char)8712, "isin");	 // NOI18N
        addMapEntry((char)8713, "notin");	 // NOI18N
        addMapEntry((char)8715, "ni");		 // NOI18N
        addMapEntry((char)8719, "prod");	 // NOI18N
        addMapEntry((char)8721, "sum");		 // NOI18N
        addMapEntry((char)8722, "minus");	 // NOI18N
        addMapEntry((char)8727, "lowast");	 // NOI18N
        addMapEntry((char)8730, "radic");	 // NOI18N
        addMapEntry((char)8733, "prop");	 // NOI18N
        addMapEntry((char)8734, "infin");	 // NOI18N
        addMapEntry((char)8736, "ang");		 // NOI18N
        addMapEntry((char)8743, "and");		 // NOI18N
        addMapEntry((char)8744, "or");		 // NOI18N
        addMapEntry((char)8745, "cap");		 // NOI18N
        addMapEntry((char)8746, "cup");		 // NOI18N
        addMapEntry((char)8747, "int");		 // NOI18N
        addMapEntry((char)8756, "there4");	 // NOI18N
        addMapEntry((char)8764, "sim");		 // NOI18N
        addMapEntry((char)8773, "cong");	 // NOI18N
        addMapEntry((char)8776, "asymp");	 // NOI18N
        addMapEntry((char)8800, "ne");		 // NOI18N
        addMapEntry((char)8801, "equiv");	 // NOI18N
        addMapEntry((char)8804, "le");		 // NOI18N
        addMapEntry((char)8805, "ge");		 // NOI18N
        addMapEntry((char)8834, "sub");		 // NOI18N
        addMapEntry((char)8835, "sup");		 // NOI18N
        addMapEntry((char)8836, "nsub");	 // NOI18N
        addMapEntry((char)8838, "sube");	 // NOI18N
        addMapEntry((char)8839, "supe");	 // NOI18N
        addMapEntry((char)8853, "oplus");	 // NOI18N
        addMapEntry((char)8855, "otimes");	 // NOI18N
        addMapEntry((char)8869, "perp");	 // NOI18N
        addMapEntry((char)8901, "sdot");	 // NOI18N
        addMapEntry((char)8968, "lceil");	 // NOI18N
        addMapEntry((char)8969, "rceil");	 // NOI18N
        addMapEntry((char)8970, "lfloor");	 // NOI18N
        addMapEntry((char)8971, "rfloor");	 // NOI18N
        addMapEntry((char)9001, "lang");	 // NOI18N
        addMapEntry((char)9002, "rang");	 // NOI18N
        addMapEntry((char)9674, "loz");		 // NOI18N
        addMapEntry((char)9824, "spades");	 // NOI18N
        addMapEntry((char)9827, "clubs");	 // NOI18N
        addMapEntry((char)9829, "hearts");	 // NOI18N
        addMapEntry((char)9830, "diams");	 // NOI18N
    }
    
    private static void addMapEntry(char val, String name)
    {
        m_escMap.put(name, new Character(val));
        m_charMap.put(new Character(val), name);
    }
    
    
    /**
     * Converts plaintext symbol to XML entity.
     */
	public String convertToEntity(char c)
    {
        String s = m_charMap.get(c);
        if (s != null)
        {
            return "&" + s + ";";	 // NOI18N
        }
        else
            return ""+c;             // NOI18N
    }
    /**
     * Converts XML entity to plaintext character.
     * If the entity cannot be converted, returns 0.
     */
	public char convertToSymbol(String escapeSequence)
    {
        Character c = m_escMap.get(escapeSequence);
        if (c == null)
        {
            try
            {
                Integer i = new Integer(escapeSequence);
                return (char) i.intValue();
            }
            catch( NumberFormatException e )
            {
                // Unconvertable Entity
                return 0;
            }
        }
        else
            return c.charValue();
    }
}
