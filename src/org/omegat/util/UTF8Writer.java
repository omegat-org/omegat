/**************************************************************************
 * OmegaT - Java based Computer Assisted Translation (CAT) tool
 * Copyright (C) 2002-2004  Keith Godfrey et al
 * keithgodfrey@users.sourceforge.net
 * 907.223.2039
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;


/** 
 * This class acts as an interceptor of output:
 * First it collects all the output inside itself in a string.
 * then:
 * <ul>
 * <li>for HTML - adds a META with UTF-8 charset (or replaces the charset with UTF-8)
 * <li>for XML - adds UTF-8 encoding declaration (or replaces the encoding with UTF-8)
 * </ul>
 * and writes out to the file.
 * <p/>
 * Note: There's no need to wrap it with <code>BufferedWriter</code>,
 * as the class itself uses StringWriter to internally store data.
 * 
 * @author Maxym Mykhalchuk
 */
public class UTF8Writer extends Writer
{
	/** file to write to */
	private String fileName;
	
	/** This is HTML stream */
	public static final int ST_HTML = 1;
	/** This is XML stream */
	public static final int ST_XML = 2;
	
	/** The type of inner data - HTML or XML */
	private int type;
	
	private StringWriter writer;
	
	/**
	 * Creates new UTF8Writer.
	 *
	 * @param fileName - file name to write to
	 * @param type - the type of data (HTML or XML)
	 */
	public UTF8Writer(String fileName, int type)
	{
		writer = new StringWriter();
		this.fileName = fileName;
		this.type = type;
	}

	/**
	 * Does the real write-out of the data, first adding/replacing
	 * encoding statement.
	 */
	public void close() throws IOException
	{
		String UTF8_META = "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">"; // NOI18N
		String XML_UTF8_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; // NOI18N

		StringBuffer buffer = writer.getBuffer();

		String contents;
		switch( type )
		{
			case ST_HTML:
				Matcher matcher_enc = PatternConsts.HTML_ENCODING.matcher(buffer);
				if( matcher_enc.find() )
				{
					contents = matcher_enc.replaceFirst(UTF8_META);
				}
				else
				{
					Matcher matcher_head = PatternConsts.HTML_HEAD.matcher(buffer);
					if( matcher_head.find() )
					{
						contents =  matcher_head.replaceFirst("<head>\n    "+UTF8_META); // NOI18N
					}
					else
					{
						Matcher matcher_html = PatternConsts.HTML_HTML.matcher(buffer);
						if( matcher_html.find() )
						{
							contents = matcher_html.replaceFirst("<html>\n<head>\n    "+UTF8_META+"\n</head>\n"); // NOI18N
						}
						else
						{
							contents = "<html>\n<head>\n    "+UTF8_META+"\n</head>\n"+ // NOI18N
								buffer.toString();
						}
					}
				}
				break;
				
			case ST_XML:
				Matcher matcher_header = PatternConsts.XML_HEADER.matcher(buffer);
				if( matcher_header.find() )
				{
					contents = matcher_header.replaceFirst(XML_UTF8_HEADER);
				}
				else
				{
					throw new IOException("XML file is invalid: It doesn't have <?xml ... ?> header!");
				}
				break;
				
			default:
				throw new IOException("[U8W] Wrong type of stream specified: Either it's HTML, or XML!");
		}
		
		FileOutputStream fos = new FileOutputStream(fileName);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8"); // NOI18N
		BufferedWriter writer = new BufferedWriter(osw);
		writer.write(contents);
		writer.close();
	}

	/**
	 * Makes no effect.
	 */
	public void flush() throws IOException
	{
		writer.flush();		
	}

	/**
	 * Write a portion of an array of characters. 
	 * Simply calls <code>write(char[], int, int)</code> of the internal
	 * <code>StringWriter</code>.
	 *
	 * @param cbuf - Array of characters
	 * @param off - Offset from which to start writing characters
	 * @param len - Number of characters to write
	 * @throws IOException - If an I/O error occurs
	 */
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		writer.write(cbuf, off, len);
	}
}
