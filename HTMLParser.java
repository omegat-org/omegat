//-------------------------------------------------------------------------
//  
//  HTMLParser.java - 
//  
//  Copyright (C) 2002, Keith Godfrey
//  
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//  
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  
//  Build date:  21Dec2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.text.*;
import java.io.*;

class HTMLParser
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
//System.out.println(c + " : " + state);

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
				case '"':
					charType = TYPE_QUOTE;
					break;
				case '>':
					charType = TYPE_CLOSE;
					break;
				case 0:
					// unexpected null
					throw new ParseException(
						"Unexpected NULL", lines);
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
					throw new ParseException(
							"bad tag start", lines);
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
							"bad tag", lines);
				}
				break;

			case STATE_RECORD:
				switch (charType)
				{
				case TYPE_QUOTE:
					state = STATE_RECORD_QUOTE;
					break;
				case TYPE_CLOSE:
					state = STATE_CLOSE;
					break;
				}
				break;

			case STATE_RECORD_QUOTE:
				switch (charType)
				{
				case TYPE_QUOTE:
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
						"unexpected character (1) " +
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
						"unexpected character (2) " +
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
				case TYPE_QUOTE:
					state = STATE_VAL_QUOTE;
					break;
				default:
					throw new ParseException(
						"unexpected character (3) " +
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
						"unexpected character (4) " +
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
				case TYPE_QUOTE:
					state = STATE_VAL_QUOTE;
					break;
				default:
					throw new ParseException(
						"unexpected character (5) " +
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
						"unexpected character (6) " +
						tag.verbatum().string(), lines);
				}
				break;

			case STATE_VAL_QUOTE:
				switch (charType)
				{
				case TYPE_QUOTE:
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
						"unexpected character (7) " +
						tag.verbatum().string(), lines);
				}
				break;

			default:
				throw new ParseException(
						"unknown error", lines);
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
	protected static final int TYPE_QUOTE			= 4;
	protected static final int TYPE_CLOSE			= 5;

	protected static final int STATE_START			= 0;
	protected static final int STATE_TOKEN			= 1;
	protected static final int STATE_WS			= 2;
	protected static final int STATE_ATTR			= 3;
	protected static final int STATE_ATTR_WS		= 5;
	protected static final int STATE_EQUAL			= 6;
	protected static final int STATE_EQUAL_WS		= 7;
	protected static final int STATE_VAL_QUOTE		= 10;
	protected static final int STATE_VAL_QUOTE_CLOSE	= 11;
	protected static final int STATE_VAL			= 20;
	protected static final int STATE_RECORD			= 30;
	protected static final int STATE_RECORD_QUOTE		= 31;
	protected static final int STATE_CLOSE			= 40;

	static String convertAll(LBuffer b)
	{
		LBuffer buf = new LBuffer(b.size() * 2);
		char[] car = b.getBuf();
		String s;
		for (int i=0; i<b.length(); i++)
		{
			s = convert(car[i]);
			if (s == null)
				buf.append(car[i]);
			else
			{
				buf.append("&");
				buf.append(s);
				buf.append(";");
			}
		}
		return buf.string();
	}

	static char convert(String tok)
	{
		String s;
		if (tok.startsWith("&"))
			s = tok.substring(1);
		else
			s = tok;
		char c = 0;
		switch (s.charAt(0))
		{
			case 'A':
				if (s.compareToIgnoreCase("AElig") == 0)
					c = 198;
				else if (s.compareToIgnoreCase("Aacute") == 0)
					c = 193;
				else if (s.compareToIgnoreCase("Acirc") == 0)
					c = 194;
				else if (s.compareToIgnoreCase("Agrave") == 0)
					c = 192;
				else if (s.compareToIgnoreCase("Aring") == 0)
					c = 197;
				else if (s.compareToIgnoreCase("Atilde") == 0)
					c = 195;
				else if (s.compareToIgnoreCase("Auml") == 0)
					c = 196;
				break;

			case 'C':
				if (s.compareToIgnoreCase("Ccedil") == 0)
					c = 199;
				break;

			case 'E':
				if (s.compareToIgnoreCase("ETH") == 0)
					c = 208;
				else if (s.compareToIgnoreCase("Eacute") == 0)
					c = 201;
				else if (s.compareToIgnoreCase("Ecirc") == 0)
					c = 202;
				else if (s.compareToIgnoreCase("Egrave") == 0)
					c = 200;
				else if (s.compareToIgnoreCase("Euml") == 0)
					c = 203;
				break;

			case 'I':
				if (s.compareToIgnoreCase("Iacute") == 0)
					c = 205;
				else if (s.compareToIgnoreCase("Icirc") == 0)
					c = 206;
				else if (s.compareToIgnoreCase("Igrave") == 0)
					c = 204;
				else if (s.compareToIgnoreCase("Iuml") == 0)
					c = 207;
				break;

			case 'N':
				if (s.compareToIgnoreCase("Ntilde") == 0)
					c = 209;
				break;

			case 'O':
				if (s.compareToIgnoreCase("Oacute") == 0)
					c = 211;
				else if (s.compareToIgnoreCase("Ocirc") == 0)
					c = 212;
				else if (s.compareToIgnoreCase("Ograve") == 0)
					c = 210;
				else if (s.compareToIgnoreCase("Oslash") == 0)
					c = 216;
				else if (s.compareToIgnoreCase("Otilde") == 0)
					c = 213;
				else if (s.compareToIgnoreCase("Ouml") == 0)
					c = 214;
				break;

			case 'T':
				if (s.compareToIgnoreCase("THORN") == 0)
					c = 222;
				break;

			case 'U':
				if (s.compareToIgnoreCase("Uacute") == 0)
					c = 218;
				else if (s.compareToIgnoreCase("Ucirc") == 0)
					c = 219;
				else if (s.compareToIgnoreCase("Ugrave") == 0)
					c = 217;
				else if (s.compareToIgnoreCase("Uuml") == 0)
					c = 220;
				break;

			case 'Y':
				if (s.compareToIgnoreCase("Yacute") == 0)
					c = 221;
			case 'a':
				if (s.compareToIgnoreCase("amp") == 0)
					c = '&';
				else if (s.compareToIgnoreCase("aacute") == 0)
					c = 225;
				else if (s.compareToIgnoreCase("acirc") == 0)
					c = 226;
				else if (s.compareToIgnoreCase("acute") == 0)
					c = 180;
				else if (s.compareToIgnoreCase("aelig") == 0)
					c = 230;
				else if (s.compareToIgnoreCase("agrave") == 0)
					c = 224;
				else if (s.compareToIgnoreCase("aring") == 0)
					c = 229;
				else if (s.compareToIgnoreCase("atilde") == 0)
					c = 227;
				else if (s.compareToIgnoreCase("auml") == 0)
					c = 228;
				break;

			case 'b':
				if (s.compareToIgnoreCase("brvbar") == 0)
					c = 166;
				break;

			case 'c':
				if (s.compareToIgnoreCase("ccedil") == 0)
					c = 231;
				else if (s.compareToIgnoreCase("cedil") == 0)
					c = 184;
				else if (s.compareToIgnoreCase("cent") == 0)
					c = 162;
				else if (s.compareToIgnoreCase("copy") == 0)
					c = 169;
				else if (s.compareToIgnoreCase("curren") == 0)
					c = 164;
				break;

			case 'd':
				if (s.compareToIgnoreCase("deg") == 0)
					c = 176;
				else if (s.compareToIgnoreCase("divide") == 0)
					c = 247;
				break;

			case 'e':
				if (s.compareToIgnoreCase("eacute") == 0)
					c = 233;
				else if (s.compareToIgnoreCase("ecirc") == 0)
					c = 234;
				else if (s.compareToIgnoreCase("egrave") == 0)
					c = 232;
				else if (s.compareToIgnoreCase("eth") == 0)
					c = 240;
				else if (s.compareToIgnoreCase("euml") == 0)
					c = 235;
				break;

			case 'f':
				if (s.compareToIgnoreCase("frac12") == 0)
					c = 189;
				else if (s.compareToIgnoreCase("frac14") == 0)
					c = 188;
				else if (s.compareToIgnoreCase("frac34") == 0)
					c = 190;
			case 'g':
				if (s.compareToIgnoreCase("gt") == 0)
					c = '>';
				break;

			case 'i':
				if (s.compareToIgnoreCase("iacute") == 0)
					c = 237;
				else if (s.compareToIgnoreCase("icirc") == 0)
					c = 238;
				else if (s.compareToIgnoreCase("iexcl") == 0)
					c = 161;
				else if (s.compareToIgnoreCase("igrave") == 0)
					c = 236;
				else if (s.compareToIgnoreCase("iquest") == 0)
					c = 191;
				else if (s.compareToIgnoreCase("iuml") == 0)
					c = 239;
			case 'l':
				if (s.compareToIgnoreCase("lt") == 0)
					c = '<';
				else if (s.compareToIgnoreCase("laquo") == 0)
					c = 171;
				break;

			case 'm':
				if (s.compareToIgnoreCase("macr") == 0)
					c = 175;
				else if (s.compareToIgnoreCase("micro") == 0)
					c = 181;
				else if (s.compareToIgnoreCase("middot") == 0)
					c = 183;
				break;

			case 'n':
				if (s.compareToIgnoreCase("nbsp") == 0)
					c = 160;
				else if (s.compareToIgnoreCase("not") == 0)
					c = 172;
				else if (s.compareToIgnoreCase("ntilde") == 0)
					c = 241;
				break;

			case 'o':
				if (s.compareToIgnoreCase("oacute") == 0)
					c = 243;
				else if (s.compareToIgnoreCase("ocirc") == 0)
					c = 244;
				else if (s.compareToIgnoreCase("ograve") == 0)
					c = 242;
				else if (s.compareToIgnoreCase("ordf") == 0)
					c = 170;
				else if (s.compareToIgnoreCase("ordm") == 0)
					c = 186;
				else if (s.compareToIgnoreCase("oslash") == 0)
					c = 248;
				else if (s.compareToIgnoreCase("otilde") == 0)
					c = 245;
				else if (s.compareToIgnoreCase("ouml") == 0)
					c = 246;
				break;

			case 'p':
				if (s.compareToIgnoreCase("para") == 0)
					c = 182;
				else if (s.compareToIgnoreCase("plusmn") == 0)
					c = 177;
				else if (s.compareToIgnoreCase("pound") == 0)
					c = 163;
			case 'q':
				if (s.compareToIgnoreCase("quot") == 0)
					c = '"';
				break;

			case 'r':
				if (s.compareToIgnoreCase("raquo") == 0)
					c = 187;
				else if (s.compareToIgnoreCase("reg") == 0)
					c = 174;
				break;

			case 's':
				if (s.compareToIgnoreCase("sect") == 0)
					c = 167;
				else if (s.compareToIgnoreCase("shy") == 0)
					c = 173;
				else if (s.compareToIgnoreCase("sup1") == 0)
					c = 185;
				else if (s.compareToIgnoreCase("sup2") == 0)
					c = 178;
				else if (s.compareToIgnoreCase("sup3") == 0)
					c = 179;
				else if (s.compareToIgnoreCase("szlig") == 0)
					c = 223;
				break;

			case 't':
				if (s.compareToIgnoreCase("thorn") == 0)
					c = 254;
				else if (s.compareToIgnoreCase("times") == 0)
					c = 215;
				break;

			case 'u':
				if (s.compareToIgnoreCase("uacute") == 0)
					c = 250;
				else if (s.compareToIgnoreCase("ucirc") == 0)
					c = 251;
				else if (s.compareToIgnoreCase("ugrave") == 0)
					c = 249;
				else if (s.compareToIgnoreCase("uml") == 0)
					c = 168;
				else if (s.compareToIgnoreCase("uuml") == 0)
					c = 252;
				break;

			case 'y':
				if (s.compareToIgnoreCase("yacute") == 0)
					c = 253;
				else if (s.compareToIgnoreCase("yen") == 0)
					c = 165;
				else if (s.compareToIgnoreCase("yuml") == 0)
					c = 255;
		}
		return c;
	}


	static String convert(char c)
	{
		String s = null;
		switch(c)
		{
			case '&':
				s = "amp";
				break;
			case '<':
				s = "lt";
				break;
			case '>':
				s = "gt";
				break;
			case '"':
				s = "quot";
				break;
			case 160:
				s = "nbsp";
				break;
			case 161:
				s = "iexcl";
				break;
			case 162:
				s = "cent";
				break;
			case 163:
				s = "pound";
				break;
			case 164:
				s = "curren";
				break;
			case 165:
				s = "yen";
				break;
			case 166:
				s = "brvbar";
				break;
			case 167:
				s = "sect";
				break;
			case 168:
				s = "uml";
				break;
			case 169:
				s = "copy";
				break;
			case 170:
				s = "ordf";
				break;
			case 171:
				s = "laquo";
				break;
			case 172:
				s = "not";
				break;
			case 173:
				s = "shy";
				break;
			case 174:
				s = "reg";
				break;
			case 175:
				s = "macr";
				break;
			case 176:
				s = "deg";
				break;
			case 177:
				s = "plusmn";
				break;
			case 178:
				s = "sup2";
				break;
			case 179:
				s = "sup3";
				break;
			case 180:
				s = "acute";
				break;
			case 181:
				s = "micro";
				break;
			case 182:
				s = "para";
				break;
			case 183:
				s = "middot";
				break;
			case 184:
				s = "cedil";
				break;
			case 185:
				s = "sup1";
				break;
			case 186:
				s = "ordm";
				break;
			case 187:
				s = "raquo";
				break;
			case 188:
				s = "frac14";
				break;
			case 189:
				s = "frac12";
				break;
			case 190:
				s = "frac34";
				break;
			case 191:
				s = "iquest";
				break;
			case 192:
				s = "Agrave";
				break;
			case 193:
				s = "Aacute";
				break;
			case 194:
				s = "Acirc";
				break;
			case 195:
				s = "Atilde";
				break;
			case 196:
				s = "Auml";
				break;
			case 197:
				s = "Aring";
				break;
			case 198:
				s = "AElig";
				break;
			case 199:
				s = "Ccedil";
				break;
			case 200:
				s = "Egrave";
				break;
			case 201:
				s = "Eacute";
				break;
			case 202:
				s = "Ecirc";
				break;
			case 203:
				s = "Euml";
				break;
			case 204:
				s = "Igrave";
				break;
			case 205:
				s = "Iacute";
				break;
			case 206:
				s = "Icirc";
				break;
			case 207:
				s = "Iuml";
				break;
			case 208:
				s = "ETH";
				break;
			case 209:
				s = "Ntilde";
				break;
			case 210:
				s = "Ograve";
				break;
			case 211:
				s = "Oacute";
				break;
			case 212:
				s = "Ocirc";
				break;
			case 213:
				s = "Otilde";
				break;
			case 214:
				s = "Ouml";
				break;
			case 215:
				s = "times";
				break;
			case 216:
				s = "Oslash";
				break;
			case 217:
				s = "Ugrave";
				break;
			case 218:
				s = "Uacute";
				break;
			case 219:
				s = "Ucirc";
				break;
			case 220:
				s = "Uuml";
				break;
			case 221:
				s = "Yacute";
				break;
			case 222:
				s = "THORN";
				break;
			case 223:
				s = "szlig";
				break;
			case 224:
				s = "agrave";
				break;
			case 225:
				s = "aacute";
				break;
			case 226:
				s = "acirc";
				break;
			case 227:
				s = "atilde";
				break;
			case 228:
				s = "auml";
				break;
			case 229:
				s = "aring";
				break;
			case 230:
				s = "aelig";
				break;
			case 231:
				s = "ccedil";
				break;
			case 232:
				s = "egrave";
				break;
			case 233:
				s = "eacute";
				break;
			case 234:
				s = "ecirc";
				break;
			case 235:
				s = "euml";
				break;
			case 236:
				s = "igrave";
				break;
			case 237:
				s = "iacute";
				break;
			case 238:
				s = "icirc";
				break;
			case 239:
				s = "iuml";
				break;
			case 240:
				s = "eth";
				break;
			case 241:
				s = "ntilde";
				break;
			case 242:
				s = "ograve";
				break;
			case 243:
				s = "oacute";
				break;
			case 244:
				s = "ocirc";
				break;
			case 245:
				s = "otilde";
				break;
			case 246:
				s = "ouml";
				break;
			case 247:
				s = "divide";
				break;
			case 248:
				s = "oslash";
				break;
			case 249:
				s = "ugrave";
				break;
			case 250:
				s = "uacute";
				break;
			case 251:
				s = "ucirc";
				break;
			case 252:
				s = "uuml";
				break;
			case 253:
				s = "yacute";
				break;
			case 254:
				s = "thorn";
				break;
			case 255:
				s = "yuml";
				break;
		}
		if ((c > 255) || ((c > 126) && (c <= 160)))
			s = "#" + String.valueOf((int) c);

		return s;
	}
}
