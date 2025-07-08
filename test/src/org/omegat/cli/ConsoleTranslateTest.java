/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import org.omegat.Main;

public class ConsoleTranslateTest extends ConsoleTestsCommon {

    @Test
    public void testConsoleTranslateLegacy() throws Exception {
        String fileName = "old.txt";
        testConsoleTranslatePrep(fileName);

        Main.main(new String[] {String.format("--config-dir=%s", getConfigDir()), "--mode=console-translate",
                getProjectDir().toString() });

        Path trgFile = getTargetDir().resolve(fileName);
        assertTrue(trgFile.toFile().isFile());
        assertEquals(List.of("Foo"), Files.readAllLines(trgFile));
    }

    @Test
    public void testConsoleTranslate() throws Exception {
        String fileName = "foo.txt";
        testConsoleTranslatePrep(fileName);

        Main.main(new String[] {"--config-dir", getConfigDir(), "translate", getProjectDir().toString() });

        Path trgFile = getTargetDir().resolve(fileName);
        assertTrue(trgFile.toFile().isFile());
        assertEquals(List.of("Foo"), Files.readAllLines(trgFile));
    }

    private void testConsoleTranslatePrep(String fileName) throws Exception {
        prep();
        List<String> fileContent = List.of("Foo");
        Path srcFile = getSourceDir().resolve(fileName);
        Files.write(srcFile, fileContent);

    }
}
