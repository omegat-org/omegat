//-------------------------------------------------------------------------
//  
//  LTabFileReader.java - 
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
//  Build date:  16Apr2003
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

// assumes input files are ASCII - run native2ascii on any files
// carrying extended characters
class LTabFileReader 
{
	public LTabFileReader()
	{
		m_line = new ArrayList();
	}

	public void write(String file) throws IOException
	{
		int i;
		String str;
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for (i=0; i<m_line.size(); i++)
		{
			str = (String) m_line.get(i);
			out.write(str, 0, str.length());
			out.newLine();
		}
		out.close();
	}

	public void reset()
	{
		m_line.clear();
	}

	public void addLine(String str)
	{
		m_line.add(str);
	}

	public void load(File file) throws IOException
	{
		load(file.getAbsolutePath());
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
			if (s.startsWith("#") == true)
				continue;
			z = "";
			for (i=0; i<s.length(); i++)
			{
				c = s.charAt(i);
				if ((c == 9) || (c == 10) || (c == 13))
				{
					// close token
					tokenList.add(z);
					z = "";
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
		String retVal = "";
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

	public int numCols(int row)
	{
		if (row < m_line.size())
		{
			ArrayList tokenList = (ArrayList) m_line.get(row);
			return tokenList.size();
		}
		else 
			return 0;
	}

	private ArrayList	m_line;

/////////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		LTabFileReader rdr = new LTabFileReader();
		try 
		{
			PrintWriter out = new PrintWriter(
				new FileOutputStream(".tfr.tmp"));

			out.println("abc");
			out.println("def\tghi");
			out.println("jkl");
			out.println("");
			out.println("mno\tpqr");
			out.println("stu\tvwx\tyz-");
			out.println("ABC\tDEF");
			out.println("GHI");
			out.println("");
			out.println("JKL");
			out.println("MNO\tPQR");
			out.println("STU\tVWX\tYZ_");
			out.close();
			
			rdr.load(".tfr.tmp");
			int r = rdr.numRows();
			String s;
			int c;
			for (int i=0; i<r; i++)
			{
				c = rdr.numCols(i);
				for (int j=0; j<c; j++)
				{
					if (j > 0)
						System.out.print("\t");
					s = rdr.get(i, j);
					System.out.print(s);
				}
				System.out.println("");
			}
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}
}
