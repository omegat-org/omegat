//-------------------------------------------------------------------------
//  
//  XHTMLFileHandler.java - 
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
//  Build date:  16Sep2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.io.*;
import java.util.*;
import java.text.*;
import java.util.zip.*;

class HTMLXmlFileHandler extends XmlFileHandler
{
	public HTMLXmlFileHandler()
	{
		super("XML based HTML", OConsts.FH_XML_BASED_HTML);
		setStreamFilter(new XHTMLStreamFilter());
		// TODO manually compress white space to help preserve file format
		compressWhitespace(true);
		breakWhitespace(true);
		
		defineFormatTag("a", "a");
		defineFormatTag("abbr", "abbr");
		defineFormatTag("acronym", "acronym");
		defineFormatTag("b", "b");
		defineFormatTag("big", "big");
		defineFormatTag("code", "code");
		defineFormatTag("cite", "cite");
		defineFormatTag("em", "em");
		defineFormatTag("font", "f");
		defineFormatTag("i", "i");
		defineFormatTag("kbd", "k");
		defineFormatTag("samp", "samp");
		defineFormatTag("strike", "strike");
		defineFormatTag("s", "s");
		defineFormatTag("small", "small");
		defineFormatTag("sub", "sub");
		defineFormatTag("sup", "sup");
		defineFormatTag("strong", "strong");
		defineFormatTag("tt", "tt");
		defineFormatTag("u", "u");
		defineFormatTag("var", "var");
	}
}

class XHTMLStreamFilter extends XMLStreamFilter
{
	public XHTMLStreamFilter()
	{
		HTMLParser.initEscCharLookupTable();
	}

	public String convertToEscape(char c)
	{
		return HTMLParser.convertToEsc(c);
	}
	
	public char convertToChar(String escapeSequence)
	{
		return HTMLParser.convertToChar(escapeSequence);
	}
}
