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

import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.Locale;

import org.omegat.core.TestCore;
import org.omegat.core.data.ProjectProperties;
import org.omegat.gui.dialogs.ProjectPropertiesDialog.Mode;
import org.omegat.util.FileUtil;
import org.omegat.util.Language;

public class DialogsTest extends TestCore {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Set default locale to English so that uses of old/stale
        // resource keys can't be masked by out-of-date translated
        // resources when runtime locale is not English.
        Locale.setDefault(Locale.ENGLISH);
    }

    public void testAboutDialog() {
        try {
            new AboutDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testAutotestAutoCompleterOptionsDialog() {
        try {
            new AutotextAutoCompleterOptionsDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testCharTableAutoCompleterOptionsDialog() {
        try {
            new CharTableAutoCompleterOptionsDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testCreateGlossaryEntryDialog() {
        try {
            new CreateGlossaryEntry(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testCustomColorSelectionDialog() {
        try {
            new CustomColorSelectionDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testDictionaryInstallerDialog() {
        try {
            new DictionaryInstallerDialog(null, null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testExternalTMXMatchesDialog() {
        try {
            new ExternalTMXMatchesDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testFileCollisionDialog() {
        try {
            new FileCollisionDialog((Frame) null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testFilenamePatternsEditor() {
        try {
            new FilenamePatternsEditor(null, true);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testFontSelectionDialog() {
        try {
            new FontSelectionDialog(null, new Font(Font.DIALOG, Font.PLAIN, 12));
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testGlossaryAutoCompleterOptionsDialog() {
        try {
            new GlossaryAutoCompleterOptionsDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testGoToSegmentDialog() {
        try {
            new GoToSegmentDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testLastChangesDialog() {
        try {
            new LastChangesDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testLicenseDialog() {
        try {
            new LicenseDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testLogDialog() {
        try {
            new LogDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testNewProjectFileChooser() {
        try {
            new NewProjectFileChooser();
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testNewTeamProject() {
        try {
            new NewTeamProject(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testProjectPropertiesDialog() throws Exception {
        try {
            new ProjectPropertiesDialog(new ProjectProperties(FileUtil.createTempDir()),
                    "project", Mode.NEW_PROJECT);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testSaveOptionsDialog() {
        try {
            new SaveOptionsDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testSpellcheckerConfigurationDialog() {
        try {
            new SpellcheckerConfigurationDialog(null, new Language(Locale.ENGLISH));
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testTagProcessingOptionsDialog() {
        try {
            new TagProcessingOptionsDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testTeamOptionsDialog() {
        try {
            new TeamOptionsDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testTeamUserPassDialog() {
        try {
            new TeamUserPassDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testUserPassDialog() {
        try {
            new UserPassDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testViewOptionsDialog() {
        try {
            new ViewOptionsDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }

    public void testWorkflowOptionsDialog() {
        try {
            new WorkflowOptionsDialog(null);
        } catch (HeadlessException ignore) {
            // Can't do this test when headless
        }
    }
}
