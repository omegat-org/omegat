/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The check_numbers project setting is opt-out and lives in the sidecar
 * settings file: an absent key means the default (enabled), so default
 * projects need no settings file at all.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class CheckNumbersStorageTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ProjectProperties config;

    @Before
    public void setUp() throws Exception {
        config = new ProjectProperties(folder.newFolder("project"));
    }

    @Test
    public void testDefaultNeedsNoFile() throws Exception {
        assertNull(ProjectSettingsStorage.loadCheckNumbers(config));
        ProjectSettingsStorage.saveCheckNumbers(config, true);
        assertFalse("the default must not materialise a settings file",
                ProjectSettingsStorage.getFile(config).isFile());
    }

    @Test
    public void testDisableEnableRoundTrip() throws Exception {
        ProjectSettingsStorage.saveCheckNumbers(config, false);
        assertEquals(Boolean.FALSE, ProjectSettingsStorage.loadCheckNumbers(config));
        assertEquals("check_numbers=false\n", Files
                .readString(ProjectSettingsStorage.getFile(config).toPath(), StandardCharsets.UTF_8));

        // re-enabling removes the key instead of writing the default
        ProjectSettingsStorage.saveCheckNumbers(config, true);
        assertNull(ProjectSettingsStorage.loadCheckNumbers(config));
    }

    @Test
    public void testCoexistsWithOtherSettings() throws Exception {
        ProjectSettingsStorage.saveMatchNumbers(config, true);
        ProjectSettingsStorage.saveCheckNumbers(config, false);
        assertEquals(Boolean.TRUE, ProjectSettingsStorage.loadMatchNumbers(config));
        assertEquals(Boolean.FALSE, ProjectSettingsStorage.loadCheckNumbers(config));
        ProjectSettingsStorage.saveCheckNumbers(config, true);
        assertEquals("the match_numbers entry must survive the key removal", Boolean.TRUE,
                ProjectSettingsStorage.loadMatchNumbers(config));
    }
}
