/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
**************************************************************************/

package org.omegat.util;

import java.io.File;
import java.io.PrintStream;

import org.omegat.filters.TestFilterBase;

import junit.framework.TestCase;

/**
 * @author Aaron Madlon-Kay
 */
public class PreferencesTest extends TestCase {

    /**
     * Test that if an error is encountered when loading the
     * preferences file, the original file is backed up.
     * <p>
     * Note that this test can spuriously fail if run in a situation
     * where the Preferences class has already been initialized, for
     * instance when running the entire suite of tests in Eclipse. It
     * behaves correctly when run individually, or with ant.
     */
    public void testPreferencesBackup() throws Exception {
        File tmpDir = FileUtil.createTempDir();
        try {
            assertTrue(tmpDir.isDirectory());
            
            // Initialize the log first because if we don't then the log
            // file will be put under our temp config dir, and on Windows
            // the log file will be locked, so when we try to delete the
            // temp dir at the end of the test it will fail.
            Log.log("Dummy log line");
            StaticUtils.setConfigDir(tmpDir.getAbsolutePath());
            
            File prefs = new File(tmpDir, Preferences.FILE_PREFERENCES);
            
            // Write anything that is malformed XML, to force a parsing error.
            PrintStream out = new PrintStream(prefs);
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            out.println("<omegat>");
            out.println("<preference version=\"1.0\">");
            out.close();
            
            // Load bad prefs file.
            Preferences.doLoad();
            
            // The actual backup file will have a timestamp in the filename,
            // so we have to loop through looking for it.
            File backup = null;
            for (File f : tmpDir.listFiles()) {
                String name = f.getName();
                if (name.startsWith("omegat.prefs") && name.endsWith(".bak")) {
                    backup = f;
                    break;
                }
            }
            
            assertNotNull(backup);
            assertTrue(backup.isFile());
            
            TestFilterBase.compareBinary(prefs, backup);
        } finally {
            assertTrue(FileUtil.deleteTree(tmpDir));
        }
    }
}
