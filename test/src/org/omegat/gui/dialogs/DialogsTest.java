/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

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
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Files;
import java.util.Locale;

import org.junit.Assume;
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
        // All tests in this class should require a GUI; any tests that can be
        // run when headless should be put in a different class.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
    }

    @Test
    public void testAboutDialog() {
        new AboutDialog(null);
    }

    @Test
    public void testCreateGlossaryEntryDialog() {
        new CreateGlossaryEntry(null);
    }

    @Test
    public void testDictionaryInstallerDialog() {
        new DictionaryInstallerDialog(null, null);
    }

    @Test
    public void testFileCollisionDialog() {
        new FileCollisionDialog((Frame) null);
    }

    @Test
    public void testFilenamePatternsEditor() {
        new FilenamePatternsEditor(null, true);
    }

    @Test
    public void testGoToSegmentDialog() {
        new GoToSegmentDialog(null);
    }

    @Test
    public void testLastChangesDialog() {
        new LastChangesDialog(null);
    }

    @Test
    public void testLicenseDialog() {
        new LicenseDialog(null);
    }

    @Test
    public void testLogDialog() {
        new LogDialog(null);
    }

    @Test
    public void testNewProjectFileChooser() {
        new NewProjectFileChooser();
    }

    @Test
    public void testNewTeamProject() {
        new NewTeamProject(null);
    }

    @Test
    public void testProjectPropertiesDialog() throws Exception {
        File dir = Files.createTempDirectory("omegat").toFile();
        try {
            new ProjectPropertiesDialog(null, new ProjectProperties(dir), "project", Mode.NEW_PROJECT);
        } finally {
            assertTrue(dir.delete());
        }
    }
}
