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

package org.omegat.filters2.xml;

import org.omegat.util.OConsts;
import java.util.ArrayList;

/*
 * XML Block is either a tag (with optional attributes), or a string
 *
 * @author Keith Godfrey
 */
public class XMLBlock
{
	public XMLBlock()
	{
		reset();
	}

	private void reset()
	{
		m_text = "";	// NOI18N
		m_isClose = false;
		m_isEmpty = false;
		m_isComment = false;
		m_isTag = false;
		m_typeChar = 0;
		m_hasText = false;
		m_shortcut = "";	// NOI18N

		if (m_attrList != null)
			m_attrList.clear();
	}

	//////////////////////////////////////////////////
	// initialization methods
	
	public void setAttribute(String attribute, String value)
	{
		XMLAttribute attr = new XMLAttribute(attribute, value);
		setAttribute(attr);
	}

	private void setAttribute(XMLAttribute attr)
	{
		if (m_attrList == null)
			m_attrList = new ArrayList(8);

		// assume that this attribute doesn't exist already
		m_attrList.add(attr);
	}

	public void setText(String text)
	{
		setTag(false);
		m_text = text;

		// block considered text if it has length=1 and includes non ws
		m_hasText = false;
		if (text.length() == 1)
		{
			char c = text.charAt(0);
			if (c != 9 && c != 10 && c != 13 && c != ' ')
				m_hasText = true;
		}
		else
			m_hasText = true;
	}

	public void setTypeChar(char c)
	{
		m_typeChar = c;
	}

	public void setShortcut(String shortcut)
	{
		m_shortcut = shortcut;
	}
	
	public String getShortcut()	
	{
		if (m_shortcut != null && !m_shortcut.equals(""))	// NOI18N
		{
			if (m_isClose)
				return "/" + m_shortcut;	// NOI18N
			else if (m_isComment)
				return OConsts.XB_COMMENT_SHORTCUT;
		}
		return m_shortcut;	
	}
	
	public void setCloseFlag()	{	m_isClose = true; m_isEmpty = false;	}
	public void setEmptyFlag()	{	m_isEmpty = true; m_isClose = false;	}

    public void setComment()
	{
		m_isTag = true;
		setTypeChar('!');
		m_isComment = true;
		m_isClose = false;
		m_isEmpty = false;
	}
	
	public void setTagName(String name)
	{
		setTag(true);
		m_text = name;
	}

	private void setTag(boolean isTag)
	{
		m_isTag = isTag;
	}

	/////////////////////////////////////////////////
	// data retrieval functions
	
	public boolean hasText()	{ return m_hasText;		}
	public boolean isTag()		{ return m_isTag;		}
	public boolean isEmpty()	{ return m_isEmpty;		}
	public boolean isClose()	{ return m_isClose;		}
	public boolean isComment()	{ return m_isComment;	}

    /**
     * Returns the block as text - either raw text if not a tag,
     * or the tag and attributes in text form if it is
     */
	public String getText()
	{
		if (m_typeChar == '?')
		{
			// write < + [/ +] tagname + attributes + [/ +] >
			String tag = "<?" + m_text;	// NOI18N
			if (m_attrList != null)
			{
				XMLAttribute attr;
				for (int i=0; i<m_attrList.size(); i++)
				{
					// add attribute/value pair
					attr = (XMLAttribute) m_attrList.get(i);
					tag += " " + attr.name + "=\"" + attr.value + "\"";	// NOI18N
				}
			}

			tag += "?>";	// NOI18N
			return tag;
		}
		else if (m_typeChar == '!')
		{
			String tag = "<!";                                                  // NOI18N
			if (m_text.startsWith("CDATA"))                                   // NOI18N
			{
                tag += m_text;
			   	tag += "]>";	                                                // NOI18N
			}
			else if (m_isComment)
			{
				tag += "-- ";	// NOI18N
				tag += m_text;
				tag += " -->";	// NOI18N
			}
			else
			{
				tag += m_text + " ";	// NOI18N
				if (m_attrList != null)
				{
					if (m_attrList.size() > 0)
					{
						tag += ((XMLAttribute) m_attrList.get(0)).name;
					}
				}
				tag += '>';
			}
			return tag;
		}
		else if (isTag())
		{
			// write < + [/ +] tagname + attributes + [/ +] >
			String tag = "<";	// NOI18N
			if (m_isClose)
			{
				tag += '/';	// NOI18N
			}
			tag += m_text;
			if (m_attrList != null)
			{
				XMLAttribute attr;
				for (int i=0; i<m_attrList.size(); i++)
				{
					// add attribute/value pair
					attr = (XMLAttribute) m_attrList.get(i);
					tag += " " + attr.name + "=\"" + attr.value + "\"";	// NOI18N
				}
			}

			if (m_isEmpty)
				tag += " /";	// NOI18N

			tag += '>';	// NOI18N

			return tag;
		}
		else
			return m_text;
	}
	
	public String getTagName()		
	{ 
		if (isTag())
			return m_text;
		else
			return "";	// NOI18N
	}

	public int numAttributes()	
	{ 
		if (m_attrList == null)
			return 0;
		else
			return m_attrList.size();	
	}

	public XMLAttribute getAttribute(int n)
	{
		if (n < 0 || !isTag() || m_attrList == null
					|| n > m_attrList.size())
		{
			return null;
		}
		else
			return (XMLAttribute) m_attrList.get(n);
	}

	public String getAttribute(String name)
	{
		if (!isTag() || m_attrList == null)
			return null;
		XMLAttribute attr = null;
		
		for (int i=0; i<m_attrList.size(); i++)
		{
			attr = (XMLAttribute) m_attrList.get(i);
			if (attr.name.equals(name))
				break;
			else
				attr = null;
		}
		if (attr == null)
			return null;
		else
			return attr.value;
	}

	private String	m_text;	// tagname if tag; text if not
	private String	m_shortcut;	// user display for tag
	private boolean		m_isClose;
	private boolean		m_isComment;
	private boolean		m_isEmpty;
	private boolean		m_isTag;
	private boolean		m_hasText;
	private char		m_typeChar;
	private ArrayList	m_attrList;

}
