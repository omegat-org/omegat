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
import org.omegat.gui.threads.CommandThread;

import java.io.*;
import org.omegat.util.OStrings;

public class TextFileHandler extends FileHandler
{
	public TextFileHandler()
	{
		super("textfile", "txt");	 // NOI18N
	}

	public TextFileHandler(String type, String ext)
	{
		super(type, ext);
	}

	public void doLoad() throws IOException
	{ 
		throw new IOException(OStrings.getString("TFH_ERROR_UNSUPPORTED")); 
	}

	// NOTE dengue code change - review at future date
	public String translateFileEncoding()
	{
		String code = "ISO-8859-1";	 // NOI18N
		String type = type();
		if (type.equals("textfile-latin1"))	 // NOI18N
			code = "ISO-8859-1";	 // NOI18N
		else if (type.equals("textfile-latin2"))	 // NOI18N
			code = "ISO-8859-2";	 // NOI18N
		else if (type.equals("textfile-utf8"))	 // NOI18N
			code = "UTF8";	 // NOI18N

		return code;
	}

	// create output stream - allow stream to have access to source file
	//  if necessary
	public BufferedReader createInputStream(String infile)
			throws IOException
	{
		FileInputStream fis = new FileInputStream(infile);
		//InputStreamReader isr = new InputStreamReader(fis);
		String code = translateFileEncoding();
		InputStreamReader isr = new InputStreamReader(fis, code);
		BufferedReader br = new BufferedReader(isr);
		return br;
	}

	public BufferedWriter createOutputStream(String infile, String outfile)
			throws IOException
	{
		FileOutputStream fos = new FileOutputStream(outfile);
		//OutputStreamWriter osw = new OutputStreamWriter(fos);
		String code = translateFileEncoding();
		OutputStreamWriter osw = new OutputStreamWriter(fos, code);
		BufferedWriter bw = new BufferedWriter(osw);
		return bw;
	}

	public void load(String file) throws IOException
	{
		int i;
		String t;
		String srcText = "";	 // NOI18N
		String s;
		String nonTrans = "";	 // NOI18N
		int ctr = 0;

		m_in = createInputStream(file);

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
		if ((m_outFile != null) && (nonTrans.compareTo("") != 0))	 // NOI18N
		{
			m_outFile.write(nonTrans);
		}
	}

}
