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

package org.omegat.filters.text;

import org.omegat.filters.FileHandler;

import java.io.*;
import org.omegat.util.OStrings;

/**
 * Filter to support plain txt files (both latin- and utf8-encoded)
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class TextFileHandler extends FileHandler
{
	/** Text file uses system-default encoding */
	public static final String TYPE_DEFAULT = "Text File";	        // NOI18N
	/** The encoding of the text file is ISO-8859-1 (Latin 1) */
	public static final String TYPE_LATIN1 = "Text File - Latin 1";	// NOI18N
	/** The encoding of the text file is ISO-8859-2 (Latin 2) */
	public static final String TYPE_LATIN2 = "Text File - Latin 2";	// NOI18N
	/** The encoding of the text file is UTF-8 */
	public static final String TYPE_UTF8 = "Text File - UTF-8";		// NOI18N

    public TextFileHandler(String type, String ext)
	{
		super(type, ext);
	}

	public void doLoad() throws IOException
	{ 
		throw new IOException(OStrings.getString("TFH_ERROR_UNSUPPORTED")); 
	}

	/**
	 * Gives the encoding of this text file according to its type.
	 * @return encoding of this text file
	 */
	private String getEncoding()
	{
		if( type().equals(TYPE_LATIN1) )
			return "ISO-8859-1";						// NOI18N
        else if( type().equals(TYPE_LATIN2) )
			return "ISO-8859-2";						// NOI18N
		else if(type().equals(TYPE_UTF8))
			return "UTF8";								// NOI18N
		else
			return null;
        
	}

	/**
	 * Create input stream.
	 */
    protected Reader createInputStream(String infile)
			throws IOException
	{
		FileInputStream fis = new FileInputStream(infile);
        String encoding = getEncoding();
        InputStreamReader isr;
        if( encoding==null )
            isr = new InputStreamReader(fis);
        else
            isr = new InputStreamReader(fis, getEncoding());
		return isr;
	}

	public Writer createOutputStream(String outfile) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(outfile);
        String encoding = getEncoding();
        OutputStreamWriter osw;
        if( encoding==null )
            osw = new OutputStreamWriter(fos);
        else
            osw = new OutputStreamWriter(fos, getEncoding());
		BufferedWriter bw = new BufferedWriter(osw);
		return bw;
	}

	public void load(String file) throws IOException
	{
		String t;
		String srcText;	 // NOI18N
		String s;
		String nonTrans = "";	 // NOI18N
		int ctr = 0;

		m_in = new BufferedReader(createInputStream(file));

		while ((s = getNextLine()) != null)
		{
			ctr++;
			t = s.trim();
			if (t.length() == 0)
			{
				nonTrans += s + "\n";	 // NOI18N
				continue;
			}
			srcText = s;
			
			if (m_outFile != null)
			{
				m_outFile.write(nonTrans);
				nonTrans = "";	 // NOI18N
			}
			processEntry(srcText, file);
			nonTrans += "\n";	 // NOI18N
		}
		if (m_outFile != null && nonTrans.compareTo("") != 0)	 // NOI18N
		{
			m_outFile.write(nonTrans);
		}
	}

}
