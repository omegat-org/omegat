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
//  Build date:  17Mar2003
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

class HandlerMaster
{
	public HandlerMaster()
	{
		m_handlerList = new ArrayList();
		setupDefaultHandlers();
	}

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
		addHandler(new TextFileHandler("textfile-latin1", "txt1"));
		addHandler(new TextFileHandler("textfile-latin2", "txt2"));
		addHandler(new TextFileHandler("textfile-utf8", "utf8"));
		addHandler(new HTMLFileHandler());
		addHandler(new HTMLXmlFileHandler());
		addHandler(new OOXmlFileHandler());
		addHandler(new testhandler());
	}

	public ArrayList getHandlerList()	{ return m_handlerList;	}

	ArrayList	m_handlerList;

////////////////////////////////////////////////////////////

}
