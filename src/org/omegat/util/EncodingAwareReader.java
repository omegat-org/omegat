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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;


/**
 * This class automatically detects encoding of an inner file
 * and constructs a Reader with appropriate encoding.
 * <p/>
 * Detecting of encoding is done:
 * <ul>
 * <li>for HTML - by reading a possible 
 *     <code>&lt;META http-equiv="content-type" content="text/html; charset=..."&gt;</code>
 * <li>for XML -  by reading a value from XML header
 *     <code>&lt;?xml version="1.0" encoding="..."?&gt;</code>
 * </ul>
 * If encoding isn't specified, or it is not supported by Java platform,
 * the file is opened in default system encoding (ISO-8859-2 in USA, Windows-1251 on my OS).
 *
 * @author Maxym Mykhalchuk
 */
public class EncodingAwareReader extends Reader
{
	/** Inner reader */
	private Reader reader;
	
	/** This is HTML stream */
	public static final int ST_HTML = 1;
	/** This is XML stream */
	public static final int ST_XML = 2;
	
	/** The type of inner data - HTML or XML */
	private int type;

	/**
	 * Creates a new instance of EncodingAwareReader.
     * If encoding cannot be detected,
     * falls back to default encoding of Operating System.
	 *
	 * @param fileName - the file to read
	 * @param type - the type of data (HTML or XML)
	 */
	public EncodingAwareReader(String fileName, int type) throws IOException
	{
        constructor(fileName, type, null);
	}
	
	/**
	 * Creates a new instance of EncodingAwareReader.
     * If encoding cannot be detected, falls back to supplied <code>encoding</code>,
     * or (if supplied null, or supplied encoding is not supported by JVM)
     * falls back to default encoding of Operating System.
	 *
	 * @param fileName   The file to read.
	 * @param type       The type of data (HTML or XML).
     * @param encoding   The encoding to use if we can't autodetect.
	 */
	public EncodingAwareReader(String fileName, int type, String encoding)
            throws IOException
	{
        constructor(fileName, type, encoding);
	}
    
    private void constructor(String fileName, int type, String encoding)
            throws IOException
    {
		this.type = type;
		FileInputStream fis = new FileInputStream(fileName);
		try
		{
			String detectencoding = fileEncoding(fileName);
            if( detectencoding==null )
                detectencoding="<WRONG, WRONG ENCODING, REALLY WRONG!!!>";      // NOI18N
            reader = new InputStreamReader(fis, detectencoding);
		}
		catch( UnsupportedEncodingException uee )
		{
            try
            {
                if( encoding==null )
                    encoding="<WRONG, WRONG ENCODING, REALLY WRONG!!!>";      // NOI18N
                reader = new InputStreamReader(fis, encoding);
            }
            catch( UnsupportedEncodingException uee2 )
            {
                reader = new InputStreamReader(fis);
            }
		}
    }
	
	/** 
     * Return encoding of the file, if we can detect it.
     *
     * <p>
     * We can detect the following:
     * <ul>
     * <li>UTF-16 with BOM (byte order mark)
     * <li>UTF-8 with BOM (byte order mark)
     * <li>Any other 8-bit-Latin encoding, specified with XML/HTML-style encoding declarations.
     * </ul>
     *
     * <p>
     * Note that we cannot detect UTF-16 encoding, if there's no BOM!
     */
	private String fileEncoding(String fileName) throws IOException
	{
        // BOM detection
        Reader reader = new InputStreamReader(new FileInputStream(fileName), "ISO-8859-1");
        int char1 = reader.read();
        int char2 = reader.read();
        int char3 = reader.read();
        if( char1==0xFE && char2==0xFF )
            return "UTF-16BE";
        if( char1==0xFF && char2==0xFE )
            return "UTF-16LE";
        if( char1==0xEF && char2==0xBB && char3==0xBF )
            return "UTF-8";
        reader.close();
        
        // Otherwise we look at file contents
		BufferedReader ereader = new BufferedReader(new InputStreamReader(
                                new FileInputStream(fileName), "ISO-8859-1"));
		StringBuffer buffer = new StringBuffer();
		while( ereader.ready() ) {
			buffer.append( ereader.readLine().toLowerCase() );
			switch( type )
			{
				case ST_HTML:
					Matcher matcher_html = PatternConsts.HTML_ENCODING.matcher(buffer);
					if( matcher_html.find() )
						return matcher_html.group(1);
					if( buffer.indexOf("</head>") >= 0 ) // NOI18N
						return "";
					break;
				case ST_XML:
					Matcher matcher_xml = PatternConsts.XML_ENCODING.matcher(buffer);
					if( matcher_xml.find() )
						return matcher_xml.group(1);
					Matcher matcher_xml2 = PatternConsts.XML_HEADER.matcher(buffer);
					if( matcher_xml2.find() )
						return "";
					break;
				default:
					throw new IOException("[EAR] Wrong type of stream specified: Either it's HTML, or XML!");
			}
		}
		ereader.close();
		return ""; // NOI18N
	}
    
	public void close() throws IOException
	{
		reader.close();
	}

	public int read(char[] cbuf, int off, int len) throws IOException
	{
		return reader.read(cbuf, off, len);
	}
	
}
