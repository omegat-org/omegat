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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import org.omegat.util.OStrings;

public class TabFileHandler extends FileHandler
{
	public TabFileHandler()
	{
		super("tabfile", "tab");	 // NOI18N
	}

	public void doLoad() throws IOException
	{
		throw new IOException (OStrings.getString("TFH_ERROR_UNSUPPORTED"));
	}

	public void load(String file) throws IOException
	{
		String id;
		int i;
		StringTokenizer t;
		String srcText = "";	 // NOI18N
		String s;
		String nonTrans = "";	 // NOI18N
		BufferedReader in = new BufferedReader(new FileReader(file));
		while ((s = in.readLine()) != null)
		{
			t = new StringTokenizer(s, "\t");	 // NOI18N
			if (t.countTokens() < 2)
			{
				nonTrans += s;
				continue;
			}
			id = t.nextToken();
			nonTrans += id + "\t";	 // NOI18N
			srcText = t.nextToken();
			
			if (m_outFile != null)
			{
				m_outFile.write(nonTrans);
				nonTrans = "";	 // NOI18N
			}
			processEntry(srcText, file);

			while (t.hasMoreTokens())
			{
				nonTrans += "\t" + t.nextToken();	 // NOI18N
			}
		}
		if ((m_outFile != null) && (nonTrans.compareTo("") != 0))	 // NOI18N
		{
			m_outFile.write(nonTrans);
		}
	}

}
