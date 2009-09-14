/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.filters2;

/**
 * Callback for processing all entries for filter.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public interface IParseCallback {
    String processEntry(String entry);

    /**
     * Read entry from source file
     * 
     * @param id
     *            ID in source file, or null if ID not suported by format
     * @param source
     *            source entry text
     * @param translation
     *            exist translation text
     * @param isFuzzy
     *            translation fuzzy flag
     * @param comment
     *            comment for entry, if format supports it
     */
    void addEntry(String id, String source, String translation,
            boolean isFuzzy, String comment);

    /**
     * Get translation for output to compiled target.
     * 
     * @param id
     *            ID in source file, or null if ID not suported by format
     * @param source
     *            source entry text
     * @return translation or source if translation not exist
     */
    String getTranslation(String id, String source);
}
