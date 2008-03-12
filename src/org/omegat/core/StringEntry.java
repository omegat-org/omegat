/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import java.util.Comparator;
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
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;


/*
 * String entry represents a unique translatable string
 * (a single string may occur many times in data files, but only
 *  one StringEntry is created for it).
 * Multiple translations can still exist for the single string, however.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
public class StringEntry
{
    /** Creates a new string entry for a unique translatable string. */
    public StringEntry(String srcText)
    {
        m_parentList = new TreeSet<SourceTextEntry>(new STEComparator());
        m_nearList = new TreeSet<NearString>();
        m_glosList = new LinkedList<GlossaryEntry>();
        m_srcText = srcText;
        m_translation = "";                                                     // NOI18N
    }
    
    /** Returns the source string */
    public String getSrcText()
    { 
        return m_srcText;	
    }
    
    /** Returns the tokens of this entry's source string */
    public List<Token> getSrcTokenList()
    {
        return StaticUtils.tokenizeText(m_srcText); // HP: using cache in StaticUtils now
    }
    
    /**
      * Returns all tokens of this entry's source string,
      * including numbers, tags, and other non-word tokens.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public List<Token> getSrcTokenListAll() {
        return StaticUtils.tokenizeText(m_srcText, true);
    }

    /** List of SourceTextEntry-es this string entry belongs to. */
    public SortedSet<SourceTextEntry> getParentList()
    { 
        return m_parentList;	
    }
    /** Add SourceTextEntry this string entry belongs to. */
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
    public List<NearString> getNearListTranslated()
    {
        List<NearString> res = new ArrayList<NearString>(OConsts.MAX_NEAR_STRINGS);
        Iterator<NearString> i;
        // FIX:
        // HP: this code can sometimes cause a ConcurrentModificationException 
        // HP: because m_nearList is not thread-safe
        // HP: please make it thread-safe, but test for performance issues
        for(i=m_nearList.iterator(); i.hasNext() && res.size()<OConsts.MAX_NEAR_STRINGS; )
        {
            NearString next = i.next();
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
     * @param adjustedScore similarity score adjusted for full string, including non-word tokens
     * @param nearData coloring data
     * @param nearProj the TMX origin of the string, null for project's TMX
     */
    //public void addNearString(StringEntry strEntry, int score, byte[] nearData, String nearProj)
    public void addNearString(StringEntry strEntry, int score, int adjustedScore, byte[] nearData, String nearProj)
    {
        // if list is full, remove last entry if its score is lower than the new entry's score
        if( m_nearList.size()>=OConsts.MAX_STORED_NEAR_STRINGS )
        {
            NearString last = m_nearList.last();
            if((score > last.score) || ((score == last.score) && (adjustedScore > last.adjustedScore)))
                m_nearList.remove(last);
            else
                return;
        }
        m_nearList.add(new NearString(strEntry, score, adjustedScore, nearData, nearProj));
    }
    
    /**
     * Returns a List of Glossary entries, associated with
     * the current String entry
     */
    public List<GlossaryEntry> getGlossaryEntries()
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
    public List<Token> getTransTokenList()
    {
        return StaticUtils.tokenizeText(m_translation);
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
    /** Sorted set of parent source text entries. */
    private SortedSet<SourceTextEntry>	m_parentList;
    /** Sorted set of near matched strings. */
    private SortedSet<NearString>	m_nearList;
    /** List of glossary terms for this string. */
    private List<GlossaryEntry>	m_glosList;
    
    private String m_srcText;
    private String m_translation;
}

/**
 * A comparator for SourceTextEntry classes,
 * which sorts them according to their entryNum() descending.
 */
class STEComparator implements Comparator<SourceTextEntry>
{
    public int compare(SourceTextEntry first, SourceTextEntry second)
    {
        if (first == second)
            return 0;
        if (first.entryNum() == second.entryNum())
            throw new RuntimeException("Should not happen!");                   // NOI18N
        
        if (first.entryNum() < second.entryNum())
            return 1;
        else
            return -1;
    }
}