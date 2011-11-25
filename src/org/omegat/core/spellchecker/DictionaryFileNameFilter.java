/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.core.spellchecker;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A file name filter for use when searching for dictionary files
 * 
 * @author bartkoz
 */
public class DictionaryFileNameFilter implements FilenameFilter {

    /**
     * the file name extension
     */
    private String extension;

    /** Creates a new instance of DictionaryFileNameFilter */
    public DictionaryFileNameFilter(String extension) {
        this.extension = extension;
    }

    /**
     * What to accept. The file should have the specified extension.
     */
    public boolean accept(File dir, String name) {
        boolean result = true;

        if (extension != null)
            result &= name.endsWith(extension);

        return result;
    }

}
