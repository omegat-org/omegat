//-------------------------------------------------------------------------
//  
//  XMLBlock.java - 
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


import java.lang.*;
import java.util.*;

// a block is either a tag (with optional attributes), or a string
class XMLBlock
{
	public XMLBlock()
	{
		reset();
	}

	public void reset()
	{
		m_text = "";
		m_isClose = false;
		m_isEmpty = false;
		m_isComment = false;
		m_isTag = false;
		m_typeChar = 0;
		m_hasText = false;
		m_shortcut = "";

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

	public void setAttribute(XMLAttribute attr)
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
			if ((c != 9) && (c != 10) && (c != 13) && (c != ' '))
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
		if ((m_shortcut != null) && (m_shortcut.equals("") == false))
		{
			if (m_isClose)
				return "/" + m_shortcut;
			else if (m_isComment)
				return OConsts.XB_COMMENT_SHORTCUT;
		}
		return m_shortcut;	
	}
	
	public void setCloseFlag()	{	m_isClose = true; m_isEmpty = false;	}
	public void setEmptyFlag()	{	m_isEmpty = true; m_isClose = false;	}

	public void setComment(String text)
	{
		m_isTag = true;
		setTypeChar('!');
		m_isComment = true;
		m_text = text;
		m_isClose = false;
		m_isEmpty = false;
	}

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

	public void setTag(boolean isTag)
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
	public char getTypeChar()	{ return m_typeChar;	}

	// returns the block as text - either raw text if not a tag,
	//  or the tag and attributes in text form if it is
	public String getText()
	{
		if (m_typeChar == '?')
		{
			// write < + [/ +] tagname + attributes + [/ +] >
			String tag = "<?" + m_text;
			if (m_attrList != null)
			{
				XMLAttribute attr;
				for (int i=0; i<m_attrList.size(); i++)
				{
					// add attribute/value pair
					attr = (XMLAttribute) m_attrList.get(i);
					tag += " " + attr.name + "=\"" + attr.value + "\"";
				}
			}

			tag += "?>";
			return tag;
		}
		else if (m_typeChar == '!')
		{
			String tag = "<!";
			if (m_text.equals("CDATA"))
			{
				tag += "[";
				if (m_attrList != null)
				{
					if (m_attrList.size() > 0)
					{
						tag += ((XMLAttribute) m_attrList.get(0)).name;
					}
				}
			   	tag += "]>";
			}
			else if (m_isComment)
			{
				tag += "-- ";
				tag += m_text;
				tag += " -->";
			}
			else
			{
				tag += m_text + " ";
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
			String tag = "<";
			if (m_isClose)
			{
				tag += '/';
			}
			tag += m_text;
			if (m_attrList != null)
			{
				XMLAttribute attr;
				for (int i=0; i<m_attrList.size(); i++)
				{
					// add attribute/value pair
					attr = (XMLAttribute) m_attrList.get(i);
					tag += " " + attr.name + "=\"" + attr.value + "\"";
				}
			}

			if (m_isEmpty)
				tag += " /";

			tag += '>';

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
			return "";
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
		if ((n < 0) || (isTag() == false) || (m_attrList == null) 
					|| (n > m_attrList.size()))
		{
			return null;
		}
		else
			return (XMLAttribute) m_attrList.get(n);
	}

	public String getAttribute(String name)
	{
		if ((isTag() == false) || (m_attrList == null))
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

	protected String	m_text;	// tagname if tag; text if not
	protected String	m_shortcut;	// user display for tag
	private boolean		m_isClose;
	private boolean		m_isComment;
	private boolean		m_isEmpty;
	private boolean		m_isTag;
	private boolean		m_hasText;
	private char		m_typeChar;
	protected ArrayList	m_attrList = null;

	///////////////////////////////////////////////////////////
	// debugging code

	public static void main(String[] args)
	{
		// test making blocks and writing them out
		XMLBlock blk = new XMLBlock();

		blk.setTagName("body");
		System.out.println(blk.getText());
		blk.reset();

		blk.setTagName("section");
		blk.setAttribute("chapter", "1");
		blk.setAttribute("color", "green");
		System.out.println(blk.getText());
		blk.reset();

		blk.setText("As the world turns");
		System.out.println(blk.getText());
		blk.reset();

		blk.setTagName("paragraph");
		blk.setEmptyFlag();
		blk.setAttribute("color", "green");
		System.out.println(blk.getText());
		blk.reset();

		blk.setText("The days of our lives");
		System.out.println(blk.getText());
		blk.reset();

		blk.setTagName("paragraph");
		blk.setEmptyFlag();
		System.out.println(blk.getText());
		blk.reset();

		blk.setText("Smokey and the Bandit");
		System.out.println(blk.getText());
		blk.reset();

		blk.setTagName("section");
		blk.setCloseFlag();
		System.out.println(blk.getText());
		blk.reset();

		blk.setTagName("body");
		blk.setCloseFlag();
		System.out.println(blk.getText());
		blk.reset();

		blk.setTagName("reader");
		blk.setAttribute("chapter", "1");
		blk.setAttribute("color", "green");
		blk.setAttribute("sport", "golf");
		System.out.println(blk.getText());
		String str = blk.getAttribute("color");
		System.out.println("color = " + str);
		str = blk.getAttribute("smell");
		System.out.println("smell = " + str);
		XMLAttribute att = blk.getAttribute(0);
		System.out.println("attribute 0: " + att.name + " = " + att.value);
		blk.reset();
		
		blk.setTagName("CDATA");
		blk.setTypeChar('!');
		blk.setAttribute("CDATA[ code data <>!& goes here]", "");
		System.out.println(blk.getText());
		blk.reset();

		blk.setComment("comment goes here");
		System.out.println(blk.getText());
		blk.reset();

		blk.setTagName("xml");
		blk.setTypeChar('?');
		blk.setAttribute("version", "1.0");
		blk.setAttribute("encoding", "UTF-8");
		System.out.println(blk.getText());
		blk.reset();
	}
}
