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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.omegat.util.OConsts;
import org.omegat.filters.xml.XMLFileHandler;
import org.omegat.util.EncodingAwareReader;

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
		compressWhitespace(true);
		breakWhitespace(true);
		
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

	/** compiled pattern to extract the encoding from HTML file, if any */
	private static Pattern pattern_xml_encoding = Pattern.compile(
		"<\\?xml\\s+version\\s*=\\s*\".+?\"\\s+encoding\\s*=\\s*\"(\\S+?)\"\\s*>", 
		Pattern.DOTALL);
	
	/** compiled pattern to extract the encoding from HTML file, if any */
	private static Pattern pattern_meta = Pattern.compile(
		"<meta.*?content\\s*=\\s*[\"']\\s*text/html\\s*;\\s*charset\\s*=\\s*(\\S+?)[\"']\\s*>", 
		Pattern.DOTALL);
	
	/**
	 * Return encoding of XHTML file, if defined.
	 * <p/>
	 * In XHTML, encoding may be defined in XML header, e.g.
	 * <code>&lt;?xml version="1.0" encoding="EUC-JP"?&gt;</code>,
	 * or in a HTML content type meta, e.g. 
	 * <code>&lt;meta http-equiv="content-type" content="text/html; charset=EUC-JP"/&gt;</code>.
	 * <p/>
	 * If both are defined, then the encoding from XML header takes precendence.
	 */
	private String fileEncoding(String filename) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		StringBuffer buffer = new StringBuffer();
		while( reader.ready() ) {
			buffer.append( reader.readLine().toLowerCase() );
			Matcher matcher = pattern_meta.matcher(buffer);
			if( matcher.find() )
				return matcher.group(1);
			if( buffer.indexOf("</HEAD") >= 0 ) // NOI18N
				break;
		}
		reader.close();
		
		return ""; // NOI18N
	}
	
	public Reader createInputStream(String filename) throws IOException
	{
		return new EncodingAwareReader(filename, EncodingAwareReader.ST_XML);
	}

}

