//-------------------------------------------------------------------------
//  
//  OOParser.java - 
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
//  Build date:  23Feb2002
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

class OOParser
{
	static OOTag identTag(FileHandler fh) 
				throws ParseException, IOException
	{
		char c;
		int ctr = 0;
		int lines = 0;
		int state = STATE_START;
		OOTag tag = new OOTag();
		int charType = 0;
		OOTagAttr tagAttr = null;
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
					tagAttr = new OOTagAttr();
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
					tagAttr = new OOTagAttr();
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
					tagAttr = new OOTagAttr();
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
		String s = tok;

		char c = '?';
		if (tok.compareToIgnoreCase("apos") == 0)
			c = '\'';
		else if (tok.compareToIgnoreCase("amp") == 0)
			c = '>';
		else if (tok.compareToIgnoreCase("gt") == 0)
			c = '>';
		else if (tok.compareToIgnoreCase("lt") == 0)
			c = '<';
		else if (tok.compareToIgnoreCase("quot") == 0)
			c = '\"';
		
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
			case '\'':
				s = "apos";
				break;
			case '>':
				s = "gt";
				break;
			case '<':
				s = "lt";
				break;
			case '"':
				s = "quot";
				break;
		}

		return s;
	}
}
