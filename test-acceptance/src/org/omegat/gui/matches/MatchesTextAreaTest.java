/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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

import java.util.Locale;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;

import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.LocaleRule;
import org.omegat.util.OStrings;

public class MatchesTextAreaTest extends TestCoreGUI {

    private static final String PROJECT_PATH = "test-acceptance/data/project/";

    @Rule
    public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

    @Test
    public void testFuzzyMatches() throws Exception {
        // load project
        openSampleProject(PROJECT_PATH);
        robot().waitForIdle();
        // check a fuzzy match pane
        window.scrollPane(OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Fuzzy_Matches")).requireVisible();
        window.textBox("matches_pane").requireVisible();
        window.textBox("matches_pane").requireNotEditable();
        Pattern pattern = Pattern.compile("1. Error while reading MT results\\n"
                + "Erreur lors de la lecture des résultats de TA\\n"
                + "<\\d+/\\d+/\\d+%\\s*>");
        window.textBox("matches_pane").requireText(pattern);
        closeProject();
    }
}
