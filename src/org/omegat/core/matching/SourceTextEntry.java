/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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

package org.omegat.core.matching;

import org.omegat.util.ProjectFileData;
import org.omegat.core.*;

/*
 * Source text entry represents an individual segment for
 * translation pulled directly from the input files.
 * There can be many SourceTextEntries having identical source
 * language strings
 *
 * @author Keith Godfrey
 */
public class SourceTextEntry
{
    /** 
     * Creates a new source text entry.
     *
     * @param str       unique StringEntry that holds source and translation of 
     *                      this entry.
     * @param file      information about the file this entry belongs to.
     * @param entryNum  the number of this entry in a project.
     */
    public SourceTextEntry(StringEntry str, ProjectFileData file, 
            int entryNum)
    {
        m_srcFile = file;
        m_strEntry = str;
        m_entryNum = entryNum;
        m_strEntry.addParent(this);
    }
    
    /** Returns information about the file this entry belongs to. */
    public ProjectFileData getSrcFile()
    { 
        return m_srcFile;	
    }
    /** Returns the number of the first entry in the file this entry belongs to. */
    public int getFirstInFile()
    { 
        return m_srcFile.firstEntry;	
    }
    /** Returns the number of the last entry in the file this entry belongs to. */
    public int getLastInFile()
    { 
        return m_srcFile.lastEntry;	
    }
    
    /** Returns the unique StringEntry that holds source and translation of this entry. */
    public StringEntry getStrEntry()
    { 
        return m_strEntry;	
    }
    
    /** 
     * Returns the source text 
     * (shortcut for <code>getStrEntry().getSrcText()</code>). 
     */
    public String getSrcText()
    {
        return m_strEntry.getSrcText();
    }
    
    /** 
     * Returns the translation 
     * (shortcut for <code>getStrEntry().getTranslation()</code>). 
     */
    public String getTranslation()
    {
        return m_strEntry.getTranslation();
    }
    
    /** 
     * Sets the translation 
     * (shortcut for <code>getStrEntry().setTranslation(t)</code>). 
     */
    public void setTranslation(String t)
    {
        m_strEntry.setTranslation(t);
    }
    
    /** 
     * Returns whether this entry is translated 
     * (shortcut for <code>getStrEntry().isTranslated(t)</code>). 
     */
    public boolean isTranslated()
    {
        return m_strEntry.isTranslated();
    }
    
    /** Returns the number of this entry is a project. */
    public int entryNum()
    { 
        return m_entryNum;
    }
    
    /** Holds information about the file this entry belongs to. */
    private	ProjectFileData m_srcFile;
    /** Holds the unique StringEntry of this segment. */
    private StringEntry m_strEntry;
    /** Holds the number of this entry in a project. */
    private int m_entryNum;
}
