/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.util.OConsts;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.TestPreferencesInitializer;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class ConsoleTestsCommon {

    private Path tmpDir;
    private String configDir;

    @Before
    public final void setUp() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
        configDir = Files.createDirectory(tmpDir.resolve(".omegat")).toString();
        TestPreferencesInitializer.init(configDir);
        Core.initializeConsole();
    }

    @After
    public final void tearDown() throws Exception {
        FileUtils.forceDeleteOnExit(tmpDir.toFile());
    }

    Path getProjectDir() {
        return tmpDir;
    }

    String getConfigDir() {
        return configDir;
    }

    Path getTargetDir() {
        return tmpDir.resolve(OConsts.DEFAULT_TARGET);
    }

    Path getSourceDir() {
        return tmpDir.resolve(OConsts.DEFAULT_SOURCE);
    }

    void prep() throws Exception {
        // Create project properties
        ProjectProperties props = new ProjectProperties(getProjectDir().toFile());
        // Create project internal directories
        props.autocreateDirectories();
        // Create a version-controlled glossary file
        props.getWritableGlossaryFile().getAsFile().createNewFile();
        ProjectFileStorage.writeProjectFile(props);
    }
}
