/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.omegat.core.glossary.GlossaryEntry;
import org.omegat.core.matching.NearString;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;


/*
 * String entry represents a unique translatable string
 * (a single string may occur many times in data files, but only
 *  one StringEntry is created for it).
 * Multiple translations can still exist for the single string, however.
 *
 * @author Keith Godfrey
 */
public class StringEntry
{
    public StringEntry(String srcText)
    {
        m_parentList = new LinkedList();
        m_nearList = new TreeSet();
        m_glosList = new LinkedList();
        m_srcText = srcText;
        m_srcTextLow = srcText.toLowerCase();
        m_translation = ""; // NOI18N
    }
    
    /** Returns the source string */
    public String getSrcText()
    { return m_srcText;	}
    /** Retruns source string in lower case */
    public String getSrcTextLow()
    { return m_srcTextLow; }
    
    /** Returns the tokens of this entry's source string */
    public List getSrcTokenList()
    {
        if( srcTokenList==null )
        {
            srcTokenList = new ArrayList();
            StaticUtils.tokenizeText(m_srcText, srcTokenList);
        }
        return srcTokenList;
    }
    
    /** Returns the number of words in this string */
    public int getWordCount()
    { return srcTokenList.size(); }
    
    public LinkedList getParentList()
    { return m_parentList;	}
    public void addParent(SourceTextEntry srcTextEntry)
    {
        m_parentList.add(srcTextEntry);
    }
    
    /**
     * Gets a list with (max) 5 near segments, but only with those that have a
     * translation. This list is recomputed on each call to this method,
     * because some text may become translated after a call to this method.
     *
     * @return list of near segments, that have a translation
     */
    public List getNearListTranslated()
    {
        List res = new ArrayList(OConsts.MAX_NEAR_STRINGS);
        int size;
        Iterator i;
        for(size=0, i=m_nearList.iterator(); i.hasNext() && res.size()<OConsts.MAX_NEAR_STRINGS; )
        {
            NearString next = (NearString) i.next();
            if( next.str.getTranslation().length()!=0 )
                res.add(next);
        }
        return res;
    }
    
    /**
     * Adds near string for this string.
     * Near string links to another existing string entry and has
     * a similarity score and a similarity coloring data.
     * Near string is inserted into the list according to its score,
     * and there cannot be more than MAX_STORED_NEAR_STRINGS (50) near strings.
     *
     * @param strEntry actual near string
     * @param score similarity score
     * @param nearData coloring data
     * @param nearProj the TMX origin of the string, null for project's TMX
     */
    public void addNearString(StringEntry strEntry, int score, byte[] nearData, String nearProj)
    {
        boolean add = true;
        if( m_nearList.size()>=OConsts.MAX_STORED_NEAR_STRINGS )
        {
            NearString last = (NearString)m_nearList.last();
            if( score>last.score )
            {
                m_nearList.remove(last);
            }
            else
            {
                add = false;
            }
        }
        if( add )
        {
            m_nearList.add(new NearString(strEntry, score, nearData, nearProj));
        }
    }
    
    /**
     * Returns a List of Glossary entries, associated with
     * the current String entry
     */
    public List getGlossaryEntries()
    {
        return m_glosList;
    }
    public void addGlossaryEntry(GlossaryEntry glosEntry)
    {
        m_glosList.add(glosEntry);
    }
    
    // these methods aren't sychronized - thought about doing so, but
    //	as the translation is set by user action, any race condition
    //	would be the same as user pressing 'enter' key a few milliseconds
    //	before or after they actually did, making the condition trivial
    // if more processing happens here later, readdress synchronization
    //	issues

    /** Returns the tokens of this entry's translation */
    public List getTransTokenList()
    {
        if( transTokenList==null )
        {
            transTokenList = new ArrayList();
            StaticUtils.tokenizeText(m_translation, transTokenList);
        }
        return transTokenList;
    }
    
    /**
     * Returns the translation of the StringEntry.
     */
    public String getTranslation()
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
        if( trans==null )
            trans = "";                                                         // NOI18N
        boolean was = !"".equals(m_translation);                                // NOI18N
        
        if( !trans.equals(m_translation) )
        {
            // tell the boss things have changed to indicate a save is in order
            // only if translation changed
            CommandThread.core.markAsDirty();
            m_translation = trans;
            
            boolean is = !"".equals(m_translation);                             // NOI18N
            if( was && !is )
                CommandThread.core.decreaseTranslated();
            else if( !was && is )
                CommandThread.core.increaseTranslated();
        }
    }

    /**
     * Returns whether the given string entry is already translated.
     */
    public boolean isTranslated()
    {
        return m_translation!=null && m_translation.length()>0;
    }
    
    // NOTE: references to these lists are returned through the above
    // access calls
    private LinkedList	m_parentList;
    private SortedSet	m_nearList;
    private LinkedList	m_glosList;
    
    private String m_srcText;
    private String m_srcTextLow;
    private String m_translation;
    
    private List srcTokenList;
    private List transTokenList;
}
