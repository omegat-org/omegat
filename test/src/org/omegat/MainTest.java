/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.omegat.core.data.ProjectProperties;
import org.omegat.util.OConsts;
import org.omegat.util.ProjectFileStorage;

import junit.framework.TestCase;

public class MainTest extends TestCase {

    private static Path tmpDir;

    @Override
    protected void setUp() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
    }

    @Override
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(tmpDir.toFile());
        assertFalse(tmpDir.toFile().exists());
    }

    public static void testConsoleTranslate() throws Exception {
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

        Main.main(new String[] { "--mode=console-translate", tmpDir.toString() });

        Path trgFile = tmpDir.resolve(OConsts.DEFAULT_TARGET).resolve(fileName);
        assertTrue(trgFile.toFile().isFile());
        assertEquals(fileContent, Files.readAllLines(trgFile));
    }
}
