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

package org.omegat.filters.xml.xhtml;

import java.io.*;
import org.omegat.util.OConsts;
import org.omegat.filters.xml.XMLFileHandler;
import org.omegat.util.AntiCRReader;
import org.omegat.util.EncodingAwareReader;
import org.omegat.util.UTF8Writer;

/**
 * Filter for handling XHTML files
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class XHTMLFileHandler extends XMLFileHandler
{
	public XHTMLFileHandler()
	{
		super("XML based HTML", OConsts.FH_XML_BASED_HTML);	 // NOI18N
		setStreamFilter(new XHTMLStreamFilter());
		// TODO manually compress white space to help preserve file format
		compressWhitespace();
		breakWhitespace();
        
		defineFormatTag("a", "a");	 // NOI18N
		defineFormatTag("abbr", "abbr");	 // NOI18N
		defineFormatTag("acronym", "acronym");	 // NOI18N
		defineFormatTag("b", "b");	 // NOI18N
		defineFormatTag("big", "big");	 // NOI18N
		defineFormatTag("code", "code");	 // NOI18N
		defineFormatTag("cite", "cite");	 // NOI18N
		defineFormatTag("em", "em");	 // NOI18N
		defineFormatTag("font", "f");	 // NOI18N
		defineFormatTag("i", "i");	 // NOI18N
		defineFormatTag("kbd", "k");	 // NOI18N
		defineFormatTag("samp", "samp");	 // NOI18N
		defineFormatTag("strike", "strike");	 // NOI18N
		defineFormatTag("s", "s");	 // NOI18N
		defineFormatTag("small", "small");	 // NOI18N
		defineFormatTag("span", "span");	 // NOI18N
		defineFormatTag("sub", "sub");	 // NOI18N
		defineFormatTag("sup", "sup");	 // NOI18N
		defineFormatTag("strong", "strong");	 // NOI18N
		defineFormatTag("tt", "tt");	 // NOI18N
		defineFormatTag("u", "u");	 // NOI18N
		defineFormatTag("var", "var");	 // NOI18N
	}

	public Reader createInputStream(String filename) throws IOException
	{
		return new AntiCRReader( new EncodingAwareReader(filename, EncodingAwareReader.ST_XML) );
	}

    protected Writer createOutputStream(String outfile) throws IOException
    {
        return new UTF8Writer(outfile, UTF8Writer.ST_XML);
    }


}

