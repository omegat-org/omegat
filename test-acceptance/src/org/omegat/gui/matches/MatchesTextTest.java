/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024-2025 Hiroshi Miura
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

package org.omegat.gui.matches;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.OStrings;

import static org.junit.Assert.assertNotNull;

public class MatchesTextTest extends TestCoreGUI {

    private static final Path PROJECT_PATH = Paths.get("test-acceptance/data/project/");

    @Test
    public void testFuzzyMatches() throws Exception {
        // load project
        openSampleProjectWaitMatches(PROJECT_PATH);
        robot().waitForIdle();
        // check a fuzzy match pane
        assertNotNull(window);
        window.scrollPane(OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Fuzzy_Matches")).requireVisible();
        window.textBox("matches_pane").requireVisible();
        window.textBox("matches_pane").requireNotEditable();
        Pattern pattern = Pattern.compile("1. Error while reading MT results\\n"
                + "Erreur lors de la lecture des résultats de TA\\n"
                + "<\\d+/\\d+/\\d+%\\s*" + OStrings.getString(
                        "MATCHES_VAR_EXPANSION_MATCH_COMES_FROM_MEMORY") + "\\s*>");
        window.textBox("matches_pane").requireText(pattern);
        closeProject();
    }

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        tmpDir = Files.createTempDirectory("omegat-sample-project-").toFile();
        FileUtils.copyDirectory(PROJECT_PATH.toFile(), tmpDir);
        FileUtils.forceDeleteOnExit(tmpDir);
    }
}
