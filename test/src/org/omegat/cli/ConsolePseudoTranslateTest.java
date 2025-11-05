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

import org.junit.Test;
import org.omegat.Main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ConsolePseudoTranslateTest extends ConsoleTestsCommon {

    private static final String SOURCEFILE = "foo.txt";
    private static final String SOURCE = "Connect to custom server {0} instead of apertium.org";

    @Test
    public void testPseudoTranslateLegacy() throws Exception {
        testConsoleTranslatePrep(SOURCEFILE);

        String pseudoTranslateFile = getTargetDir().resolve("pseudo.tmx").toString();

        Main.main(new String[] { String.format("--config-dir=%s", getConfigDir()), "--mode=console-createpseudotranslatetmx",
                "--pseudotranslatetmx=" + pseudoTranslateFile,  "--pseudotranslatetype=equal", getProjectDir().toString() });

        Path trgFile = Paths.get(pseudoTranslateFile);
        assertTrue(trgFile.toFile().isFile());
    }

    @Test
    public void testPseudoTranslate() throws Exception {
        testConsoleTranslatePrep(SOURCEFILE);

        String pseudoTranslateFile = getTargetDir().resolve("pseudo.tmx").toString();

        Main.main(new String[] {"--config-dir", getConfigDir(), "pseudo", "--type=equal",
                "--output-file=" + pseudoTranslateFile, getProjectDir().toString() });

        Path trgFile = Paths.get(pseudoTranslateFile);
        assertTrue(trgFile.toFile().isFile());
    }

    private void testConsoleTranslatePrep(String fileName) throws Exception {
        prepOmegaTProjectAndDirectries();
        Path testTmx = Paths.get("test/data/tmx/project_save.tmx");
        Files.copy(testTmx, getProjectDir().resolve("omegat/project_save.tmx"));
        List<String> fileContent = List.of(SOURCE);
        Path srcFile = getSourceDir().resolve(fileName);
        Files.write(srcFile, fileContent);
    }
}
