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

package org.omegat.gui.scripting;

import java.awt.HeadlessException;
import java.io.File;

import org.omegat.core.TestCore;
import org.omegat.util.Preferences;

public class ScriptingTest extends TestCore {

    /**
     * Test for bug #775: Unresolvable scripting folder setting can cause an empty
     * Scripting window
     * <p>
     * NPE while initializing quick script menu entries when the script folder path
     * member is null (failed to be set because it was invalid).
     * 
     * @see bug https://sourceforge.net/p/omegat/bugs/775/
     */
    public void testLoadScriptingWindow() throws Exception {
        // Set quick script
        Preferences.setPreference(Preferences.SCRIPTS_QUICK_PREFIX + 1, "blah");
        
        // Set bogus scripts folder (a file can't be a folder!)
        File tmp = File.createTempFile("omegat", "tmp");
        try {
            Preferences.setPreference(Preferences.SCRIPTS_DIRECTORY, tmp.getAbsolutePath());
            new ScriptingWindow();
        } catch (HeadlessException ex) {
            // Can't do this test when headless
        } finally {
            assertTrue(tmp.delete());
        }
    }
}
