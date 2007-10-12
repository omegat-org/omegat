/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Didier Briel
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

package org.omegat.filters3.xml.opendoc;

import java.awt.Dialog;
import java.io.Serializable;


/**
 * Options for OpenDoc filter.
 * Serializable to allow saving to / reading from configuration file.
 * <p>
 * OpenDoc filter have the following options
 * ([+] means default on).
 * Translatable elements:
 * <ul>[+] Index entries
 * <ul>[-] Bookmarks
 * <ul>[+] Notes
 * <ul>[+] Comments
 * </ul>
 * @author Didier Briel
 */
public class OpenDocOptions implements Serializable
{    
     
    /** Hold value of properties. */
    private boolean translateIndexes = true;
    private boolean translateBookmarks = false;
    private boolean translateNotes = true;
    private boolean translateComments = true;
    
    /**
     * Returns whether Indexes should be translated.
     */
    public boolean getTranslateIndexes()
    {
        return this.translateIndexes;
    }

    /**
     * Sets whether Indexes be translated.
     */
    public void setTranslateIndexes(boolean translateIndexes)
    {
        this.translateIndexes = translateIndexes;
    }
   
    /**
     * Returns whether Bookmarks should be translated.
     */
    public boolean getTranslateBookmarks()
    {
        return this.translateBookmarks;
    }

    /**
     * Sets whether Bookmarks should be translated.
     */
    public void setTranslateBookmarks(boolean translateBookmarks)
    {
        this.translateBookmarks = translateBookmarks;
    }
    
    /**
     * Returns whether Notes should be translated.
     */
    public boolean getTranslateNotes()
    {
        return this.translateNotes;
    }

    /**
     * Sets whether Notes should be translated.
     */
    public void setTranslateNotes(boolean translateNotes)
    {
        this.translateNotes = translateNotes;
    }
    /**
     * Returns whether Comments should be translated.
     */
    
    public boolean getTranslateComments()
    {
        return this.translateNotes;
    }

    /**
     * Sets whether Comments should be translated.
     */
    public void setTranslateComments(boolean translateComments)
    {
        this.translateComments = translateComments;
    }

}
