//-------------------------------------------------------------------------
//  
//  HandlerMaster.java - 
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
//  Build date:  4Dec2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.io.*;
import java.util.*;

class HandlerMaster
{
	public HandlerMaster()
	{
		m_handlerList = new ArrayList();
		setupDefaultHandlers();
	}

//	public void buildHandlerList(String srcRoot, String projRoot,
//					String projName) throws IOException
//	{
//		File src = new File(srcRoot);
//		File proj = new File(projRoot);
//		boolean err = false;
//		String errString = "";
//		if (!src.isDirectory())
//		{
//			// complain
//			err = true;
//			errString = "Specified source location '" + 
//				src.getName() + "' is not a directory";
//		}
//
//		if (!proj.isDirectory())
//		{
//			errString = CommandThread.core.langManager().getString(
//					OStrings.HM_MISSING_DIR);
//			err = true;
//		}
//
//		if (err == false)
//		{
//			FileWriter ofp = null;
//			try
//			{
//				String fn = projRoot + File.separator + 
//					projName + OConsts.HANDLER_LIST_EXT;
//				ofp = new FileWriter(fn);
//				ofp.write(src.getAbsolutePath() + "\n");
//				outputFile("", src, ofp);
//				ofp.close();
//			}
//			catch (IOException e)
//			{
//				try { ofp.close();	}
//				catch(IOException d) { ; }
//				err = true;
//				errString = "Generic error writing '" + 
//					projName + OConsts.HANDLER_LIST_EXT + 
//					"' file";
//			}
//		}
//		if (err == true)
//		{
//			throw new IOException(errString);
//		}
//	}
//
//	protected void outputFile(String path, File cur, 
//		FileWriter ofp) throws IOException
//	{
//		File[] files = cur.listFiles();
//		String pathFile = "";
//		String outstr;
//		String hndlr = null;
//		String s;
//		StringTokenizer tok;
//		FileHandler fh;
//		
//		String handler;
//		if (files == null)
//			throw new NullPointerException();
//		
//		int i;
//		Arrays.sort(files);
//		for (i=0; i<files.length; i++)
//		{
//			if (!files[i].isFile())
//				continue;
//
//			if (path.compareTo("") == 0)
//				pathFile = files[i].getName();
//			else
//				pathFile = path + File.separator +
//					files[i].getName();
//
//			// find default handler
//			tok = new StringTokenizer(files[i].getName(),
//				".");
//			s = "";
//			tok.nextToken();
//			while (tok.hasMoreTokens())
//			{
//				s = tok.nextToken();
//			}
//			fh = findPreferredHandler(s);
//			if (fh != null)
//				hndlr = fh.type();
//			else
//				hndlr = OConsts.HANDLER_IGNORE;
//
//			// output filename
//			outstr = pathFile + "\t" + hndlr + "\n";
//			ofp.write(outstr);
//		}
//		for (i=0; i<files.length; i++)
//		{
//			if (!files[i].isDirectory())
//				continue;
//
//			if (path.compareTo("") == 0)
//				pathFile = files[i].getName();
//			else
//				pathFile = path + File.separator +
//					files[i].getName();
//
//			outputFile(pathFile, files[i], ofp);
//		}
//	}
//
	public void addHandler(FileHandler hand)
	{
		m_handlerList.add(hand);
	}
	
	public FileHandler findHandler(String type)
	{
		FileHandler fh = null;
		int i;
		for (i=0; i<m_handlerList.size(); i++)
		{
			fh = (FileHandler) m_handlerList.get(i);
			if (type.compareToIgnoreCase(fh.type()) == 0)
			{
				break;
			}
		}
		if (i >= m_handlerList.size())
		{
			fh = null;
		}
		return fh;
	}

	public FileHandler findPreferredHandler(String ext)
	{
		FileHandler fh = null;
		int i;
		if (ext != null)
		{
			for (i=0; i<m_handlerList.size(); i++)
			{
				fh = (FileHandler) m_handlerList.get(i);
				if (ext.compareToIgnoreCase(
						fh.preferredExtension()) == 0)
				{
					break;
				}
			}
			if (i >= m_handlerList.size())
			{
				fh = null;
			}
		}
		return fh;
	}

	protected void setupDefaultHandlers()
	{
		addHandler(new TabFileHandler());
		addHandler(new TextFileHandler());
		addHandler(new HTMLFileHandler());
		addHandler(new OOFileHandler());
	}

	public ArrayList getHandlerList()	{ return m_handlerList;	}

	ArrayList	m_handlerList;

////////////////////////////////////////////////////////////

//	public static void main(String[] args)
//	{
//		try 
//		{
//			HandlerMaster mast = new HandlerMaster();
//			mast.buildHandlerList(".", ".", "omegat");
//		}
//		catch(IOException e)
//		{
//			System.out.println("Error encountered:");
//			System.out.println("   " + e);
//		}
//	}
}
