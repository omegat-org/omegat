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

package org.omegat.filters.html;

import org.omegat.filters.LBuffer;

import java.util.LinkedList;

/**
 * The name explains itself - class to represent a single HTML tag.
 *
 * @author Keith Godfrey
 */
public class HTMLTag
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
			case 'a':	 // NOI18N
			case 'A':	 // NOI18N
				m_shortcut = 'a';	 // NOI18N
				if (m_name.isEqualIgnoreCase("a"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("abbr"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("acronym"))	 // NOI18N
					m_type = TAG_FORMAT;
				break;
				
			case 'b':	 // NOI18N
			case 'B':	 // NOI18N
				m_shortcut = 'b';	 // NOI18N
				if (m_name.isEqualIgnoreCase("b"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("big"))	 // NOI18N
					m_type = TAG_FORMAT;
				break;
				
			case 'c':	 // NOI18N
			case 'C':	 // NOI18N
				m_shortcut = 'c';	 // NOI18N
				if (m_name.isEqualIgnoreCase("code"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("cite"))	 // NOI18N
					m_type = TAG_FORMAT;
				break;
				
			case 'e':	 // NOI18N
			case 'E':	 // NOI18N
				m_shortcut = 'e';	 // NOI18N
				if (m_name.isEqualIgnoreCase("em"))	 // NOI18N
					m_type = TAG_FORMAT;
				break;
				
			case 'f':	 // NOI18N
			case 'F':	 // NOI18N
				m_shortcut = 'f';	 // NOI18N
				if (m_name.isEqualIgnoreCase("font"))	 // NOI18N
					m_type = TAG_FORMAT;
				break;
				
			case 'i':	 // NOI18N
			case 'I':	 // NOI18N
				m_shortcut = 'i';	 // NOI18N
				if (m_name.isEqualIgnoreCase("i"))	 // NOI18N
					m_type = TAG_FORMAT;
				break;
				
			case 'k':	 // NOI18N
			case 'K':	 // NOI18N
				m_shortcut = 'k';	 // NOI18N
				if (m_name.isEqualIgnoreCase("kbd"))	 // NOI18N
					m_type = TAG_FORMAT;
				break;
				
			case 's':	 // NOI18N
			case 'S':	 // NOI18N
				m_shortcut = 's';	 // NOI18N
				if (m_name.isEqualIgnoreCase("samp"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("strike"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("s"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("small"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("sub"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("sup"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("strong"))	 // NOI18N
					m_type = TAG_FORMAT;
				else if (m_name.isEqualIgnoreCase("span"))	 // NOI18N
					m_type = TAG_FORMAT;
				break;
				
			case 't':	 // NOI18N
			case 'T':	 // NOI18N
				m_shortcut = 't';	 // NOI18N
				if (m_name.isEqualIgnoreCase("tt"))	 // NOI18N
					m_type = TAG_FORMAT;
				break;
				
			case 'u':	 // NOI18N
			case 'U':	 // NOI18N
				m_shortcut = 'u';	 // NOI18N
				if (m_name.isEqualIgnoreCase("u"))	 // NOI18N
					m_type = TAG_FORMAT;
				break;
				
			case 'v':	 // NOI18N
			case 'V':	 // NOI18N
				m_shortcut = 'v';	 // NOI18N
				if (m_name.isEqualIgnoreCase("var"))	 // NOI18N
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
			return "/" + m_name.string();		 // NOI18N
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
