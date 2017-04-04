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

package org.omegat.gui.dialogs;

import static org.junit.Assert.assertTrue;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.nio.file.Files;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.dialogs.ProjectPropertiesDialog.Mode;

public class DialogsTest extends TestCore {

    @Before
    public final void setUp() {
        // Set default locale to English so that uses of old/stale
        // resource keys can't be masked by out-of-date translated
        // resources when runtime locale is not English.
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void testAboutDialog() {
        try {
            new AboutDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testCreateGlossaryEntryDialog() {
        try {
            new CreateGlossaryEntry(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testDictionaryInstallerDialog() {
        try {
            new DictionaryInstallerDialog(null, null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testFileCollisionDialog() {
        try {
            new FileCollisionDialog((Frame) null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testFilenamePatternsEditor() {
        try {
            new FilenamePatternsEditor(null, true);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testGoToSegmentDialog() {
        try {
            new GoToSegmentDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testLastChangesDialog() {
        try {
            new LastChangesDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testLicenseDialog() {
        try {
            new LicenseDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testLogDialog() {
        try {
            new LogDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testNewProjectFileChooser() {
        try {
            new NewProjectFileChooser();
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testNewTeamProject() {
        try {
            new NewTeamProject(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    @Test
    public void testProjectPropertiesDialog() throws Exception {
        File dir = Files.createTempDirectory("omegat").toFile();
        try {
            new ProjectPropertiesDialog(null, new ProjectProperties(dir),
                    "project", Mode.NEW_PROJECT);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        } finally {
            assertTrue(dir.delete());
        }
    }
}
