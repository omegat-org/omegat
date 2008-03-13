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

package org.omegat.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Files processing utilities.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class FileUtil {
    private static final int MAX_BACKUPS = 10;

    /**
     * Removes old backups so that only 10 last are there.
     * 
     * TODO: should be changed for new file saving behavior, i.e. steps for save
     * some data in file(*.xml, for example) should be:
     * 
     * 1. Save data into '*.new' file
     * 
     * 2. Rename '*.xml' into '*.xml.bak'
     * 
     * 3. Rename '*.new' into '*.xml'
     * 
     * It will allow to do not break exist files if some error will be produced
     * in the save process.
     */
    public static void removeOldBackups(final File originalFile) {
        try {
            File[] bakFiles = originalFile.getParentFile().listFiles(new FileFilter() {
                public boolean accept(File f) {
                    return !f.isDirectory() && f.getName().startsWith(originalFile.getName())
                            && f.getName().endsWith(".bak");
                }
            });

            if (bakFiles != null && bakFiles.length > MAX_BACKUPS) {
                Arrays.sort(bakFiles, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return f2.getName().compareTo(f1.getName());
                    }
                });
            }

            for (int i = MAX_BACKUPS; i < bakFiles.length; i++) {
                bakFiles[i].delete();
            }
        } catch (Exception e) {
            // we don't care
        }
    }
}
