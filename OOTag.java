//-------------------------------------------------------------------------
//  
//  OOTag.java - 
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
//  Build date:  9Jan2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.*;

class OOTag 
{
	public OOTag()
	{
		m_name = new LBuffer(8);
		m_verbatum = new LBuffer(16);
	}

	public void addAttr(OOTagAttr hta)
	{
		if (m_attrList == null)
			m_attrList = new LinkedList();
		m_attrList.add(hta);
	}

	public void finalize()
	{
		// assume structural until proven otherwise
		m_type = 1;
		char c = m_name.getChar(0);
		if (c == 't')
		{
			if (m_name.isEqualIgnoreCase("text:a"))
			{
				m_shortcut = 'a';
				m_type = TAG_FORMAT;
			}
			else if (m_name.isEqualIgnoreCase("text:span"))
			{
				m_shortcut = 'f';
				m_type = TAG_FORMAT;
			}
			else if (m_name.isEqualIgnoreCase("text:s"))
			{
				m_shortcut = 's';
				m_type = TAG_FORMAT;
			}
			else if (m_name.isEqualIgnoreCase("text:s/"))
			{
				m_shortcut = 's';
				m_type = TAG_FORMAT;
			}
			else if (m_name.isEqualIgnoreCase("text:tab-stop"))
			{
				m_shortcut = 't';
				m_type = TAG_FORMAT;
			}
			else if (m_name.isEqualIgnoreCase("text:tab-stop/"))
			{
				m_shortcut = 't';
				m_type = TAG_FORMAT;
			}
		}

	}

	public void nameAppend(char c)
	{
		m_name.append(c);
	}
	
	public void verbatumAppend(char c)
	{
		m_verbatum.append(c);
	}

	// case insensitive compare
	public boolean isEqual(String name)
	{
		return m_name.isEqualIgnoreCase(name);
	}

	public String name()	
	{ 
		if (m_close == true)
			return "/" + m_name.string();	
		else
			return m_name.string();	
	}

	public boolean willPartner(OOTag tag)
	{
		if ((m_partner == true) || (tag.hasPartner() == true))
			return false;
		if (tag.close() == m_close)
			return false;
		if (m_name.isEqual(tag.m_name) == false)
			return false;
		return true;
	}

	public int type()		{ return m_type;	}
	public char shortcut()		{ return m_shortcut;	}
	public int num()		{ return m_num;		}
	public void setNum(int x)	{ m_num = x;		}
	public LBuffer verbatum()	{ return m_verbatum;	}
	public void setClose(boolean x)	{ m_close = x;		}
	public boolean close()		{ return m_close;	}

	public boolean hasPartner()	{ return m_partner;	}
	public void setPartner(boolean x)	{ m_partner = x;	}

	public static final int		TAG_NO_IDENT	= 2;
	public static final int		TAG_STRUCTURAL	= 1;
	public static final int		TAG_FORMAT	= 2;
	
	private boolean		m_close = false;
	private boolean		m_partner = false;
	private char		m_shortcut = 0;
	private int		m_num = 0;
	private int		m_type = TAG_NO_IDENT;
	protected LBuffer	m_name;
	private LBuffer		m_verbatum;
	private LinkedList	m_attrList;
}
