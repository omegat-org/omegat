//-------------------------------------------------------------------------
//  
//  StringEntry.java - 
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
//  Build date:  8Mar2003
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
import java.security.*;

class GlossaryEntry
{
	public GlossaryEntry(String src, String loc, String com)
	{
		m_src = src;
		m_loc = loc;
		m_com = com;
	}

	public String	getSrcText()	{ return m_src;		}
	public String	getLocText()	{ return m_loc;		}
	public String	getCommentText()	{ return m_com;		}

	protected String	m_src;
	protected String	m_loc;
	protected String	m_com;
}

// a string entry represents a unique translatable string
// (a single string may occur many times in data files, but only
// one StringEntry is created for it)
// multiple translations can still exist the the single string, however
class StringEntry
{
	public StringEntry(String srcText) 
	{
		m_wordCount = 0;
		m_parentList = new LinkedList();
		m_nearList = new LinkedList();
		m_glosList = new LinkedList();
		m_srcText = srcText;
		m_translation = "";
		m_digest = LCheckSum.compute(srcText);
	}

	public long digest()		{ return m_digest;	}
	public String getSrcText()	{ return m_srcText;	}
	public void setWordCount(int n)	{ m_wordCount = n;	}
	public int getWordCount()	{ return m_wordCount;	}

	public LinkedList getParentList()	{ return m_parentList;	}
	public void addParent(SourceTextEntry srcTextEntry)
	{
		m_parentList.add(srcTextEntry);
	}
	
	public LinkedList getNearList()		{ return m_nearList;	}
	public void addNearString(StringEntry strEntry, 
		double score, byte[] parData, byte[] nearData, String nearProj)
	{
		ListIterator it = m_nearList.listIterator();
		int pos = 0;
		NearString ns = null;
		NearString cand;
		cand = new NearString(strEntry, score, parData, 
							nearData, nearProj);
		while (it.hasNext())
		{
			ns = (NearString) it.next();
			if (score > ns.score)
				break;
			pos++;
		}
 		if (m_nearList.size() == 0)
			pos = 0;
		if (pos >= 0)
			m_nearList.add(pos, cand);
		if (m_nearList.size() >= OConsts.MAX_NEAR_STRINGS)
			m_nearList.getLast();
	}
	
	public LinkedList getGlosList()		{ return m_glosList;	}
	public void addGlosString(GlossaryEntry strEntry)
	{
		m_glosList.add(strEntry);
	}

	// these methods aren't sychronized - thought about doing so, but
	//	as the translation is set by user action, any race condition
	//	would be the same as user pressing 'enter' key a few milliseconds
	//	before or after they actually did, making the condition trivial
	// if more processing happens here later, readdress synchronization
	//	issues
	public String getTrans()
	{
		return m_translation;
	}

	public void setTranslation(String trans)
	{
		// tell the boss things have changed to indicate a save is in order
		CommandThread.core.markAsDirty();
		if (trans == null)
			m_translation = "";
		else
			m_translation = trans;
	}

//	// need to share the list for near string processing
//	public LinkedList getTransList()	{ return m_transList;	}
//	
	// NOTE: references to these lists are returned through the above
	// access calls 
	private LinkedList	m_parentList;
	private LinkedList	m_nearList;
	private LinkedList	m_glosList;

	private long	m_digest;
	private String m_srcText;
	private int m_wordCount;
	private String m_translation;
}
