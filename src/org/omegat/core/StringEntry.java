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

package org.omegat.core;

import org.omegat.gui.threads.CommandThread;
import org.omegat.util.OConsts;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/*
 * String entry represents a unique translatable string
 * (a single string may occur many times in data files, but only
 *  one org.omegat.core.StringEntry is created for it).
 * Multiple translations can still exist the the single string, however.
 *
 * @author Keith Godfrey
 */
public class StringEntry
{
	public StringEntry(String srcText) 
	{
		m_wordCount = 0;
		m_parentList = new LinkedList();
		m_nearList = new LinkedList();
		m_glosList = new LinkedList();
		m_srcText = srcText;
		m_translation = ""; // NOI18N
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
	
	/**
	 * Gets a list with (max) 5 near segments, but only with those that have a
	 * translation. This list is recomputed on each call to this method,
	 * because some text may become translated after a call to this method.
	 *
	 * @return list of near segments, that have a translation
	 */
	public LinkedList getNearListTranslated() {
		LinkedList res = new LinkedList();
		int size = 0;
		Iterator i = ((LinkedList)getNearList().clone()).listIterator();
		while( i.hasNext() ) {
			NearString next = (NearString) i.next();
			if( next.str.getTrans().length()!=0 ) {
				res.add(next);
				size++;
				if( size>=OConsts.MAX_NEAR_STRINGS )
					break;
			}
		}
		return res;
	}

	/**
	 * Adds near string for this string.
	 * Near string links to another existing string entry and has 
	 * a similarity score and a similarity coloring data.
	 * Near string is inserted into the list according to its score,
	 * and there cannot be more than MAX_STORED_NEAR_STRINGS (30) near strings.
	 * 
	 * @param strEntry actual near string
	 * @param score similarity score
	 * @param nearData coloring data
	 * @param nearProj (?) the project of the string
	 */
	public void addNearString(StringEntry strEntry, double score, byte[] nearData, String nearProj)
	{
		ListIterator it = m_nearList.listIterator();
		int pos = 0;
		NearString ns = null;
		while (it.hasNext())
		{
			ns = (NearString) it.next();
			if (score >= ns.score)
				break;
			pos++;
		}
		if( pos < OConsts.MAX_STORED_NEAR_STRINGS )
		{
			NearString cand = new NearString(strEntry, score, nearData, nearProj);
			m_nearList.add(pos, cand);
		}

		if( m_nearList.size() > OConsts.MAX_STORED_NEAR_STRINGS )
			m_nearList.removeLast();
	}
	private static int counter = 0;
	
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

	/**
	 * Sets the translation of the StringEntry.
	 * If translation given is null or equal to the source, than 
	 * the empty string is set as a translation to indicate that there's no translation.
	 */
	public void setTranslation(String trans)
	{
		// tell the boss things have changed to indicate a save is in order
		CommandThread.core.markAsDirty();
		if( trans==null )
			m_translation = "";		// NOI18N
		else if( trans.equals(m_srcText) )
			m_translation = "";		// NOI18N
		else
			m_translation = trans;
	}

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
