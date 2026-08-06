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

package org.omegat.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.data.TestRuntimePreferenceStore;
import org.omegat.util.OStrings;
import org.omegat.util.RuntimePreferences;

import picocli.CommandLine;

/**
 * Tests that the configuration options of the legacy command line syntax
 * are applied.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class LegacyParametersTest {

    @Before
    public void setUp() {
        TestRuntimePreferenceStore.reset();
    }

    @After
    public void tearDown() {
        TestRuntimePreferenceStore.reset();
    }

    @Test
    public void testInitializeAppliesConfigDir() {
        LegacyParameters params = new LegacyParameters();
        new CommandLine(params).parseArgs(LegacyParameters.CONFIG_DIR, "/tmp/omegat-conf");
        assertNull(RuntimePreferences.getConfigDir());
        params.initialize();
        assertEquals("/tmp/omegat-conf", RuntimePreferences.getConfigDir());
    }

    @Test
    public void testInitializeExpandsTilde() {
        LegacyParameters params = new LegacyParameters();
        new CommandLine(params).parseArgs(LegacyParameters.CONFIG_DIR + "=~/omegat-conf");
        params.initialize();
        assertEquals(FileUtils.getUserDirectoryPath() + "/omegat-conf", RuntimePreferences.getConfigDir());
    }

    @Test
    public void testInitializeWithoutConfigDir() {
        LegacyParameters params = new LegacyParameters();
        new CommandLine(params).parseArgs();
        params.initialize();
        assertNull(RuntimePreferences.getConfigDir());
    }

    /**
     * The legacy runtime switches must reach the runtime preferences on every
     * route through initialize(), including the plain GUI start.
     */
    @Test
    public void testInitializeAppliesRuntimeFlags() {
        TestRuntimePreferenceStore.resetPristine();
        LegacyParameters params = new LegacyParameters();
        new CommandLine(params).parseArgs(LegacyParameters.DISABLE_PROJECT_LOCKING,
                LegacyParameters.DISABLE_LOCATION_SAVE, LegacyParameters.NO_TEAM);
        assertTrue(RuntimePreferences.isProjectLockingEnabled());
        assertTrue(RuntimePreferences.isLocationSaveEnabled());
        assertFalse(RuntimePreferences.isNoTeam());
        params.initialize();
        assertFalse(RuntimePreferences.isProjectLockingEnabled());
        assertFalse(RuntimePreferences.isLocationSaveEnabled());
        assertTrue(RuntimePreferences.isNoTeam());
    }

    /**
     * --resource-bundle must replace the strings that OStrings serves, like
     * before the picocli migration.
     */
    @Test
    public void testInitializeLoadsResourceBundle() throws Exception {
        Path bundleFile = Files.createTempFile("omegat-bundle", ".properties");
        try {
            try (InputStream in = OStrings.class.getResourceAsStream("/org/omegat/Bundle.properties")) {
                assertNotNull(in);
                Files.copy(in, bundleFile, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.writeString(bundleFile, "\nTF_MENU_FILE=Bundle from the command line\n",
                    StandardOpenOption.APPEND);
            LegacyParameters params = new LegacyParameters();
            new CommandLine(params).parseArgs(LegacyParameters.RESOURCE_BUNDLE, bundleFile.toString());
            params.initialize();
            assertEquals("Bundle from the command line", OStrings.getString("TF_MENU_FILE"));
            assertEquals(bundleFile.toString(), RuntimePreferences.getResourceBundleFile());
        } finally {
            OStrings.loadBundle(Locale.getDefault());
            Files.deleteIfExists(bundleFile);
        }
    }
}
