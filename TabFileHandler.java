//-------------------------------------------------------------------------
//  
//  TabFileHandler.java - 
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

class TabFileHandler extends FileHandler
{
	public TabFileHandler()
	{
		super("tabfile", "tab");
	}

	public void doLoad() throws IOException
	{
		throw new IOException ("unsupported function");
	}

	public void load(String file) throws IOException
	{
		String id;
		int i;
		StringTokenizer t;
		String srcText = "";
		String s;
		String nonTrans = "";
		BufferedReader in = new BufferedReader(new FileReader(file));
		while ((s = in.readLine()) != null)
		{
			t = new StringTokenizer(s, "\t");
			if (t.countTokens() < 2)
			{
				nonTrans += s;
				continue;
			}
			id = t.nextToken();
			nonTrans += id + "\t";
			srcText = t.nextToken();
			
			if (m_outFile != null)
			{
				m_outFile.write(nonTrans);
				nonTrans = "";
			}
			processEntry(srcText, file);

			while (t.hasMoreTokens())
			{
				nonTrans += "\t" + t.nextToken();
			}
		}
		if ((m_outFile != null) && (nonTrans.compareTo("") != 0))
		{
			m_outFile.write(nonTrans);
		}
	}

///////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		TabFileHandler tab = new TabFileHandler();
		CommandThread.core = new CommandThread(null);
		tab.setTestMode(true);
		try 
		{
			tab.load("src/glos1.txt");
		}
		catch (IOException e)
		{
			System.out.println("failed to open test file");
		}
	}
}
