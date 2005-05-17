/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
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

package org.omegat.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
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
        reader = createReader(fileName, encoding);
    }
	
	/** 
     * Returns the reader of the underlying file in the correct encoding.
     *
     * <p>
     * We can detect the following:
     * <ul>
     * <li>UTF-16 with BOM (byte order mark)
     * <li>UTF-8 with BOM (byte order mark)
     * <li>Any other encoding with 8-bit Latin symbols (e.g. Windows-1251, UTF-8 etc), 
     *     if it is specified using XML/HTML-style encoding declarations.
     * </ul>
     *
     * <p>
     * Note that we cannot detect UTF-16 encoding, if there's no BOM!
     */
	private Reader createReader(String fileName, String defaultEncoding) throws IOException
	{
        // BOM detection
        BufferedInputStream is = new BufferedInputStream(
                new FileInputStream(fileName));
        
        is.mark(OConsts.READ_AHEAD_LIMIT);
        
        int char1 = is.read();
        int char2 = is.read();
        int char3 = is.read();
        String encoding = null;
        if( char1==0xFE && char2==0xFF )
            encoding = "UTF-16BE";                                                  // NOI18N
        if( char1==0xFF && char2==0xFE )
            encoding = "UTF-16LE";                                                  // NOI18N
        if( char1==0xEF && char2==0xBB && char3==0xBF )
            encoding = "UTF-8";                                                     // NOI18N

        is.reset();
        if( encoding!=null )
        {
            return new InputStreamReader(is, encoding);
        }
        
        is.mark(OConsts.READ_AHEAD_LIMIT);
        byte[] buf = new byte[OConsts.READ_AHEAD_LIMIT];
        int len = is.read(buf);
		String buffer = new String(buf, 0, len);
            
        switch( type )
        {
            case ST_HTML:
                Matcher matcher_html = PatternConsts.HTML_ENCODING.matcher(buffer);
                if( matcher_html.find() )
                    encoding = matcher_html.group(1);
                break;
            case ST_XML:
                Matcher matcher_xml = PatternConsts.XML_ENCODING.matcher(buffer);
                if( matcher_xml.find() )
                    encoding = matcher_xml.group(1);
                break;
            default:
                // generally not reached unless some coding error
                // as we still support JDK 1.4, we can't use enum to eliminate this
                throw new IOException("[EAR] Wrong stream type specified: It must be HTML or XML!"); // NOI18N
        }
        
        is.reset();
        if( encoding!=null )
        {
            return new InputStreamReader(is, encoding);
        }
        
        // default encoding if we couldn't detect it ourselves
        try
        {
            return new InputStreamReader(is, defaultEncoding);
        }
        catch( UnsupportedEncodingException e )
        {
            return new InputStreamReader(is);
        }
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
