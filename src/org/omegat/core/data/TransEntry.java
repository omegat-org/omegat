/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009      Alex Buloichik
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

/**
 * Storage for translation information, like translation, author, date,
 * comments, etc.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 */
public class TransEntry {
    public String translation;
    public String comment;
    /**
     * Specifies the date of the last modification of the element.
     */
    public long changeDate;
    /**
     * Change identifier - Specifies the identifier of the user who modified the element last.
     */
    public String changeId;

    public TransEntry(String translation) {
        this.translation = translation;
    }

    /**
     * Creates a new transentry with the properties set to the given values. 
     * @param translation The translation
     * @param changeId The author of the last modification
     * @param changeDate The date (as unix timestamp) of the last modification.
     */
    public TransEntry(String translation, String changeId, long changeDate) {
        this.translation = translation;
        this.changeId = changeId;
        this.changeDate = changeDate;
    }
}
