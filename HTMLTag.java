//-------------------------------------------------------------------------
//  
//  HTMLTag.java - 
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
//  Build date:  16Sep2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.*;

class HTMLTag 
{
	public HTMLTag()
	{
		m_name = new LBuffer(8);
		m_verbatum = new LBuffer(16);
	}

	public void addAttr(HTMLTagAttr hta)
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
		switch (c)
		{
			case 'a':
			case 'A':
				m_shortcut = 'a';
				if (m_name.isEqualIgnoreCase("a"))
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("abbr"))
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("acronym"))
					m_type = TAG_FORMAT;
				break;
				
			case 'b':
			case 'B':
				m_shortcut = 'b';
				if (m_name.isEqualIgnoreCase("b"))
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("big"))
					m_type = TAG_FORMAT;
				break;
				
			case 'c':
			case 'C':
				m_shortcut = 'c';
				if (m_name.isEqualIgnoreCase("code"))
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("cite"))
					m_type = TAG_FORMAT;
				break;
				
			case 'e':
			case 'E':
				m_shortcut = 'e';
				if (m_name.isEqualIgnoreCase("em"))
					m_type = TAG_FORMAT;
				break;
				
			case 'f':
			case 'F':
				m_shortcut = 'f';
				if (m_name.isEqualIgnoreCase("font"))
					m_type = TAG_FORMAT;
				break;
				
			case 'i':
			case 'I':
				m_shortcut = 'i';
				if (m_name.isEqualIgnoreCase("i"))
					m_type = TAG_FORMAT;
//				else if (m_name.isEqualIgnoreCase("img"))
//					m_type = TAG_FORMAT;
				break;
				
			case 'k':
			case 'K':
				m_shortcut = 'k';
				if (m_name.isEqualIgnoreCase("kbd"))
					m_type = TAG_FORMAT;
				break;
				
			case 's':
			case 'S':
				m_shortcut = 's';
				if (m_name.isEqualIgnoreCase("samp"))
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("strike"))
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("s"))
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("small"))
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("sub"))
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("sup"))
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("strong"))
					m_type = TAG_FORMAT;
				break;
				
			case 't':
			case 'T':
				m_shortcut = 't';
				if (m_name.isEqualIgnoreCase("tt"))
					m_type = TAG_FORMAT;
				break;
				
			case 'u':
			case 'U':
				m_shortcut = 'u';
				if (m_name.isEqualIgnoreCase("u"))
					m_type = TAG_FORMAT;
				break;
				
			case 'v':
			case 'V':
				m_shortcut = 'v';
				if (m_name.isEqualIgnoreCase("var"))
					m_type = TAG_FORMAT;
				break;
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

	public boolean willPartner(HTMLTag tag)
	{
		if ((m_partner == true) || (tag.hasPartner() == true))
			return false;
		if (tag.close() == m_close)
			return false;
		if (m_name.isEqual(tag.m_name) == false)
			return false;
		return true;
	}

	// TODO - ident tags w/ imbedded translatable text
	public boolean hasTrans()	{ return false;		}

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
