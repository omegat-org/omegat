/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.util.OConsts;
import org.omegat.util.StaticUtils;

public class CLIParametersTest {

    private static Path tmpDir;

    @Before
    public final void setUp() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
    }

    @After
    public final void tearDown() throws Exception {
        FileUtils.deleteDirectory(tmpDir.toFile());
        assertFalse(tmpDir.toFile().exists());
    }

    @Test
    public void testCLIParameters() throws Exception {
        {
            Map<String, String> params = CLIParameters.parseArgs("--foo=bar", "--baz",
                    "--" + CLIParameters.CONFIG_FILE + "=bazinga", tmpDir.toString());
            // Key, value present.
            assertEquals("bar", params.get("foo"));
            assertEquals("bazinga", params.get(CLIParameters.CONFIG_FILE));
            // Naked keys have null values, but are present in map.
            assertNull(params.get("baz"));
            assertTrue(params.containsKey("baz"));
            // Not included at all.
            assertNull(params.get(CLIParameters.DISABLE_PROJECT_LOCKING));
            assertFalse(params.containsKey(CLIParameters.DISABLE_PROJECT_LOCKING));
            // Project dir not a valid project, so is not accepted.
            assertNull(params.get(CLIParameters.PROJECT_DIR));
            assertFalse(params.containsKey(CLIParameters.PROJECT_DIR));
        }
        {
            // Create minimum project to fool check into accepting project dir.
            assertTrue(new File(tmpDir.toFile(), OConsts.FILE_PROJECT).createNewFile());
            assertTrue(StaticUtils.isProjectDir(tmpDir.toFile()));
            Map<String, String> params = CLIParameters.parseArgs(tmpDir.toString());
            // This time the project dir is valid, so it is accepted.
            assertEquals(tmpDir.toString(), params.get(CLIParameters.PROJECT_DIR));
        }
        {
            // Test old way of specifying resource-bundle (no leading "--")
            Map<String, String> params = CLIParameters.parseArgs(CLIParameters.RESOURCE_BUNDLE + "=blah");
            assertEquals("blah", params.get(CLIParameters.RESOURCE_BUNDLE));
        }
    }
}
