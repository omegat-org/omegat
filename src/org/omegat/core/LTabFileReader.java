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

package org.omegat.core;

import java.io.*;
import java.util.ArrayList;

/*
 * Assumes input files are ASCII - run native2ascii on any files
 * carrying extended characters
 *
 * @author Keith Godfrey
 */
public class LTabFileReader 
{
	public LTabFileReader()
	{
		m_line = new ArrayList();
	}

    public void load(String file) throws IOException
	{
		String s;
		String z;
		String token;
//		StringTokenizer t;
		int i;
		char c;

		BufferedReader in = new BufferedReader(new FileReader(file));
		ArrayList tokenList = new ArrayList();
		while ((s = in.readLine()) != null)
		{
			// skip lines that start with '#'
			if (s.startsWith("#") == true) // NOI18N
				continue;
			z = ""; // NOI18N
			for (i=0; i<s.length(); i++)
			{
				c = s.charAt(i);
				if ((c == 9) || (c == 10) || (c == 13))
				{
					// close token
					tokenList.add(z);
					z = ""; // NOI18N
				}
				else
					z += c;
			}
			tokenList.add(z);

			// check token list to see if it has a valid string
			for (i=0; i<tokenList.size(); i++)
			{
				// break on non-empty string
				token = (String) tokenList.get(i);
				if (token.length() != 0)
				{
					break;
				}
			}

			if (i < tokenList.size())
			{
				m_line.add(tokenList);
				tokenList = new ArrayList();
			}
			else
			{
				tokenList.clear();
			}
		}
		in.close();
	}

	public String get(int row, int col)
	{
		String retVal = ""; // NOI18N
		if (row < m_line.size())
		{
			ArrayList tokenList = (ArrayList) m_line.get(row);
			if (col < tokenList.size())
			{
				retVal = (String) tokenList.get(col);
			}
		}
		// if element not found, return null, "" or throw exception???
		// for now, return ""
		return retVal;
	}

	public int numRows()
	{
		return m_line.size();
	}

    private ArrayList	m_line;

}
