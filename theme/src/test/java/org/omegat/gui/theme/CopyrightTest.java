/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.gui.theme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import org.omegat.util.CommonVerifications;

/**
 * Test for copyright notes exists in source files.
 *
 * @author Alex Buloichik
 */
public class CopyrightTest extends CommonVerifications {

    private static final Path MAIN_DIR = Paths.get("src/main/java");
    private static final Path TEST_DIR = Paths.get("src/test/java");

    @Test
    public void testCopyright() throws Exception {
        List<File> sourceFiles = new ArrayList<>();
        list(MAIN_DIR.toFile(), sourceFiles);
        list(TEST_DIR.toFile(), sourceFiles);
        ByteArrayOutputStream fdata = new ByteArrayOutputStream();
        for (File f : sourceFiles) {
            FileUtils.copyFile(f, fdata);
            String data = fdata.toString(StandardCharsets.UTF_8);
            checkNote(f, data);
            fdata.reset();
        }
    }
}
