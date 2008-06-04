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

package org.omegat.core.data;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.omegat.core.Core;
import org.omegat.core.matching.SourceTextEntry;


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
        m_srcText = srcText;
        m_translation = "";                                                     // NOI18N
    }
    
    /** Returns the source string */
    public String getSrcText()
    { 
        return m_srcText;	
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

    // these methods aren't sychronized - thought about doing so, but
    //	as the translation is set by user action, any race condition
    //	would be the same as user pressing 'enter' key a few milliseconds
    //	before or after they actually did, making the condition trivial
    // if more processing happens here later, readdress synchronization
    //	issues
    
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
            m_translation = trans;

            boolean is = !"".equals(m_translation);                             // NOI18N
            if( was && !is )
                Core.getProject().decreaseTranslated();
            else if( !was && is )
                Core.getProject().increaseTranslated();
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