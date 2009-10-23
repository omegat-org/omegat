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
        m_srcText = srcText;
        m_translation = "";                                                     // NOI18N
    }
    
    /** Returns the source string */
    public String getSrcText()
    { 
        return m_srcText;	
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
     * 
     * @return how translation count changed: +1 - added, -1 - substracted, 0 - not changed
     */
    public int setTranslation(String trans)
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
                return -1;
            else if( !was && is )
                return +1;
        }
        return 0;
    }

    /**
     * Returns whether the given string entry is already translated.
     */
    public boolean isTranslated()
    {
        return m_translation!=null && m_translation.length()>0;
    }
    
    
    private String m_srcText;
    private String m_translation;
}
