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
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.data.ProjectProperties;
import org.omegat.util.OConsts;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.TestPreferencesInitializer;

public class MainTest {

    private static Path tmpDir;
    private static String configDir;

    @Before
    public final void setUp() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
        configDir = Files.createDirectory(tmpDir.resolve(".omegat")).toString();
        TestPreferencesInitializer.init(configDir);
    }

    @After
    public final void tearDown() throws Exception {
        FileUtils.deleteDirectory(tmpDir.toFile());
        assertFalse(tmpDir.toFile().exists());
    }

    @Test
    public void testConsoleTranslate() throws Exception {

        // Create project properties
        ProjectProperties props = new ProjectProperties(tmpDir.toFile());
        // Create project internal directories
        props.autocreateDirectories();
        // Create version-controlled glossary file
        props.getWritableGlossaryFile().getAsFile().createNewFile();
        ProjectFileStorage.writeProjectFile(props);

        String fileName = "foo.txt";
        List<String> fileContent = Arrays.asList("Foo");

        Path srcFile = tmpDir.resolve(OConsts.DEFAULT_SOURCE).resolve(fileName);
        Files.write(srcFile, fileContent);

        Main.main(new String[] {String.format("--config-dir=%s", configDir), "--mode=console-translate",
                tmpDir.toString() });

        Path trgFile = tmpDir.resolve(OConsts.DEFAULT_TARGET).resolve(fileName);
        assertTrue(trgFile.toFile().isFile());
        assertEquals(fileContent, Files.readAllLines(trgFile));
    }
}
