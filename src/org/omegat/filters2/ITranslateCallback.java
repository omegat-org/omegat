/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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
 * Callback for translate files.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public interface ITranslateCallback {
    /**
     * Set current pass number, i.e. 1 or 2.
     * 
     * Any filter called in one-pass mode by default. But if it require second pass for support prev/next
     * segments, it can set flag 'needSecondPass'.
     */
    void setPass(int pass);

    /**
     * This method called from AbstractFilter if filter require second pass. It links prev/next segments for
     * multiple translations.
     */
    void linkPrevNextSegments();

    /**
     * Get translation for output to compiled target.
     * 
     * @param id
     *            ID in source file, or null if ID not supported by format
     * @param source
     *            source entry text
     * @param path
     *            path of segment
     * @return translation or null if translation not exist
     */
    String getTranslation(String id, String source, String path);

    /**
     * Old call without path, for compatibility
     */
    String getTranslation(String id, String source);

}
