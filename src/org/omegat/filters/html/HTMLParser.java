/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

package org.omegat.filters.html;

import org.omegat.filters.FileHandler;
import org.omegat.filters.LBuffer;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import org.omegat.util.OStrings;

public class HTMLParser
{
	static HTMLTag identTag(FileHandler fh)
				throws ParseException, IOException
	{
		char c;
		int ctr = 0;
		int lines = 0;
		int state = STATE_START;
		HTMLTag tag = new HTMLTag();
		int charType = 0;
		HTMLTagAttr tagAttr = null;
		int i;
		int excl = 0;

		while (true)
		{
			ctr++;
			i = fh.getNextChar();
			if (i < 0)
				break;
			c = (char) i;
			if ((c == 10) || (c == 13))
				lines++;

			if ((ctr == 1) && (c == '/'))
			{
				tag.setClose(true);
				continue;
			}

			if ((ctr == 1) && (c == '!'))	// script
			{
				excl = 1;
				tag.verbatumAppend(c);
			}

			if (ctr == 1)
			{
				if (c == '/')
				{
					tag.setClose(true);
					continue;
				}
				else if (c == '!')
				{
					excl = 1;
					continue;
				}
			}
			else if ((ctr == 2) && (excl == 1))
			{
				if (c == '-')
					excl = 2;
			}

			if (excl > 1)
			{
				// loop until '-->' encountered
				if (c == '-')
					excl++;
				else if ((c == '>') && (excl >= 4))
					break;
				else 
					excl = 2;

				tag.verbatumAppend(c);
				continue;
			}

			switch (c)
			{
				case 9:
				case 10:
				case 13:
				case ' ':
					charType = TYPE_WS;
					break;
				case '=':
					charType = TYPE_EQUAL;
					break;
				case '\'':
					charType = TYPE_QUOTE_SINGLE;
					break;
				case '"': 
					charType = TYPE_QUOTE_DOUBLE;
					break;
				case '>':
					charType = TYPE_CLOSE;
					break;
				case 0:
					// unexpected null
					throw new ParseException(OStrings.getString("HP_ERROR_NULL"), lines);
				default:
					charType = TYPE_NON_IDENT;
					break;
			}

			switch (state) 
			{
			case STATE_START:
				switch (charType) 
				{
				case TYPE_NON_IDENT:
					tag.nameAppend(c);
					state = STATE_TOKEN;
					break;
				default:
					throw new ParseException(OStrings.getString("HP_ERROR_BAD_TAG_START"), lines);
				}
				break;

			case STATE_TOKEN:
				switch (charType)
				{
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				case TYPE_WS:
					if (excl > 0)
						state = STATE_RECORD;
					else
						state = STATE_WS;
					break;
				case TYPE_NON_IDENT:
					tag.nameAppend(c);
					break;
				default:
					throw new ParseException(
							OStrings.getString("HP_ERROR_BAD_TAG"), lines);
				}
				break;

			case STATE_RECORD:
				switch (charType)
				{
				case TYPE_QUOTE_SINGLE:
					state = STATE_RECORD_QUOTE_SINGLE;
					break;
				case TYPE_QUOTE_DOUBLE:
					state = STATE_RECORD_QUOTE_DOUBLE;
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				}
				break;

			case STATE_RECORD_QUOTE_SINGLE:
				switch (charType)
				{
				case TYPE_QUOTE_SINGLE:
					state = STATE_RECORD;
					break;
				}
				break;

			case STATE_RECORD_QUOTE_DOUBLE:
				switch (charType)
				{
				case TYPE_QUOTE_DOUBLE:
					state = STATE_RECORD;
					break;
				}
				break;

			case STATE_WS:
				switch (charType)
				{
				case TYPE_WS:
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				case TYPE_NON_IDENT:
					tagAttr = new HTMLTagAttr();
					tagAttr.attrAppend(c);
					tag.addAttr(tagAttr);
					state = STATE_ATTR;
					break;
				default:
					throw new ParseException(
						OStrings.getString("HP_ERROR_UNEXPECTED_CHAR_1") +
						tag.verbatum().string(), lines);
				}
				break;

			case STATE_ATTR_WS:
				// space after an attribute marker
				switch (charType)
				{
				case TYPE_WS:
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				case TYPE_NON_IDENT:
					// another attribute
					tagAttr = new HTMLTagAttr();
					tagAttr.attrAppend(c);
					tag.addAttr(tagAttr);
					state = STATE_ATTR;
					break;
				case TYPE_EQUAL:
					state = STATE_EQUAL;
					break;
				default:
					throw new ParseException(
						OStrings.getString("HP_ERROR_UNEXPECTED_CHAR_2") +
						tag.verbatum().string(), lines);
				}
				break;

			case STATE_EQUAL_WS:
				// space after an equal sign
				switch (charType)
				{
				case TYPE_WS:
					// poor formatting but OK
					break;
				case TYPE_NON_IDENT:
					state = STATE_VAL;
					tagAttr.valAppend(c);
					break;
				case TYPE_QUOTE_SINGLE:
					state = STATE_VAL_QUOTE_SINGLE;
					break;
				case TYPE_QUOTE_DOUBLE:
					state = STATE_VAL_QUOTE_DOUBLE;
					break;
				default:
					throw new ParseException(
						OStrings.getString("HP_ERROR_UNEXPECTED_CHAR_3") +
						tag.verbatum().string(), lines);
				}
				break;

			case STATE_ATTR:
				switch (charType)
				{
				case TYPE_NON_IDENT:
					tagAttr.attrAppend(c);
					break;
				case TYPE_WS:
					state = STATE_ATTR_WS;
					break;
				case TYPE_EQUAL:
					state = STATE_EQUAL;
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				default:
					throw new ParseException(
						OStrings.getString("HP_ERROR_UNEXPECTED_CHAR_4") +
						tag.verbatum().string(), lines);
				}
				break;

			case STATE_EQUAL:
				switch (charType)
				{
				case TYPE_WS:
					state = STATE_EQUAL_WS;
					break;
				case TYPE_NON_IDENT:
					state = STATE_VAL;
					tagAttr.valAppend(c);
					break;
				case TYPE_QUOTE_SINGLE:
					state = STATE_VAL_QUOTE_SINGLE;
					break;
				case TYPE_QUOTE_DOUBLE:
					state = STATE_VAL_QUOTE_DOUBLE;
					break;
				default:
					throw new ParseException(
						OStrings.getString("HP_ERROR_UNEXPECTED_CHAR_5") +
						tag.verbatum().string(), lines);
				}
				break;

			case STATE_VAL:
				switch (charType)
				{
				case TYPE_NON_IDENT:
					tagAttr.valAppend(c);
					break;
				case TYPE_WS:
					state = STATE_WS;
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				default:
					throw new ParseException(
						OStrings.getString("HP_ERROR_UNEXPECTED_CHAR_6") +
						tag.verbatum().string(), lines);
				}
				break;

			case STATE_VAL_QUOTE_SINGLE:
				switch (charType)
				{
				case TYPE_QUOTE_SINGLE:
					state = STATE_VAL_QUOTE_CLOSE;
					break;
				default:
					// anything else is fair game
					tagAttr.valAppend(c);
				}
				break;

			case STATE_VAL_QUOTE_DOUBLE:
				switch (charType)
				{
				case TYPE_QUOTE_DOUBLE:
					state = STATE_VAL_QUOTE_CLOSE;
					break;
				default:
					// anything else is fair game
					tagAttr.valAppend(c);
				}
				break;

			case STATE_VAL_QUOTE_CLOSE:
				switch (charType)
				{
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				case TYPE_WS:
					state = STATE_WS;
					break;
				case TYPE_NON_IDENT:
					// treat as new param
					tagAttr = new HTMLTagAttr();
					tagAttr.attrAppend(c);
					tag.addAttr(tagAttr);
					state = STATE_ATTR;
					break;
				default:
					throw new ParseException(
						OStrings.getString("HP_ERROR_UNEXPECTED_CHAR_7") +
						tag.verbatum().string(), lines);
				}
				break;

			default:
				throw new ParseException(
						OStrings.getString("HP_ERROR_UNKNOWN"), lines);
			}

			if (state == STATE_CLOSE)
				break;
			else
				tag.verbatumAppend(c);

		}	
		tag.finalize();
		return tag;
	}

	protected static final int TYPE_WS			= 1;
	protected static final int TYPE_NON_IDENT		= 2;
	protected static final int TYPE_EQUAL			= 3;
	protected static final int TYPE_CLOSE			= 4;
	protected static final int TYPE_QUOTE_SINGLE	= 5;
	protected static final int TYPE_QUOTE_DOUBLE	= 6;
	
	protected static final int STATE_START			= 0;
	protected static final int STATE_TOKEN			= 1;
	protected static final int STATE_WS				= 2;
	protected static final int STATE_ATTR			= 3;
	protected static final int STATE_ATTR_WS		= 5;
	protected static final int STATE_EQUAL			= 6;
	protected static final int STATE_EQUAL_WS		= 7;
	protected static final int STATE_VAL_QUOTE_SINGLE			= 10;
	protected static final int STATE_VAL_QUOTE_DOUBLE			= 11;
	protected static final int STATE_VAL_QUOTE_CLOSE			= 15;
	protected static final int STATE_VAL			= 20;
	protected static final int STATE_RECORD			= 30;
	protected static final int STATE_RECORD_QUOTE_SINGLE		= 31;
	protected static final int STATE_RECORD_QUOTE_DOUBLE		= 32;
	protected static final int STATE_CLOSE			= 40;

	public static void initEscCharLookupTable()
	{
		// see if table has been initialized already
		if (m_escMap != null)
			return;

		m_escMap = new HashMap(512);
		m_charMap = new HashMap(512);

		addMapEntry((char)34, "quot");		 // NOI18N
		addMapEntry((char)38, "amp");		 // NOI18N
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

	protected static void addMapEntry(char val, String name)
	{
		m_escMap.put(name, new Character(val));
		m_charMap.put(new Character(val), name);
	}
	
	public static char convertToChar(String tok)
	{
		Character c = (Character) m_escMap.get(tok);
		if (c == null)
		{
			Integer i = new Integer(tok);
			if (i != null)
				return (char) (i.intValue());

			return 0;
		}
		else
			return c.charValue();
	}

	public static String convertToEsc(char c)
	{
		String s = (String) m_charMap.get(new Character(c));
		if ((s == null) && ((c > 255) || ((c > 126) && (c <= 160))))
		{
			s = "&#" + String.valueOf((int) c) + ";";	 // NOI18N
		}
		else if (s != null)
		{
			s = "&" + s + ";";	 // NOI18N
		}
		
		return s;
	}
	
	public static String convertAllToEsc(LBuffer b)
	{
		LBuffer buf = new LBuffer(b.size() * 2);
		char[] car = b.getBuf();
		String s;
		for (int i=0; i<b.length(); i++)
		{
			char c = car[i];
			s = (String) m_charMap.get(new Character(c));
			if (s == null)
			{
				if ((c > 255) || ((c > 126) && (c <= 160)))
				{
					s = "&#" + String.valueOf((int) c) + ";";	 // NOI18N
					buf.append(s);
				}
				else
					buf.append(car[i]);
			}
			else 
			{
				s = "&" + s + ";";	 // NOI18N
				buf.append(s);
			}
		}
		return buf.string();
	}

	protected static HashMap	m_charMap = null;
	protected static HashMap	m_escMap = null;
}
