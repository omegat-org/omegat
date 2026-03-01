/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025-2026 Hiroshi Miura
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
package org.omegat.gui.search;

import org.assertj.swing.fixture.FrameFixture;
import org.jspecify.annotations.NonNull;
import org.junit.Test;
import org.omegat.core.data.TestCoreState;
import org.omegat.core.search.SearchExpression;
import org.omegat.core.search.SearchMode;
import org.omegat.core.search.SearchResultEntry;
import org.omegat.core.search.Searcher;
import org.omegat.gui.main.TestCoreGUI;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SearchWindowTest extends TestCoreGUI {

    private static final Path PROJECT_PATH = Paths.get("test-acceptance/data/project/");
    private SearchWindowController searchWindowController;

    private FrameFixture frameFixture;

    @Test
    public void testSearchWindowShow() throws Exception {
        // load project
        openSampleProject(PROJECT_PATH);
        robot().waitForIdle();
        //
        assertNotNull(window);
        SwingUtilities.invokeLater(() -> {
            searchWindowController = new SearchWindowController(SearchMode.SEARCH);
            SearchExpression s = new SearchExpression();
            searchWindowController.displaySearchResult(new Searcher(TestCoreState.getInstance().getProject(), s) {
                @Override
                public @NonNull List<SearchResultEntry> getSearchResults() {
                    SearchResultEntry entry = new SearchResultEntry(0, null, null, "Error", null,  null, null, null, null, null, null);
                    return List.of(entry);
                }
            });
            assertEquals(SearchMode.SEARCH, searchWindowController.getMode());
            JFrame frame = searchWindowController.getWindow();
            frame.setVisible(true);
            frameFixture = new FrameFixture(robot(), frame);
            searchWindowController.setSearchText("Error");
        });
        robot().waitForIdle();
        //
        frameFixture.requireVisible();
        frameFixture.label("SearchWindowForm.m_searchLabel").requireText("Search for:");
        frameFixture.comboBox("SearchWindowForm.m_searchField").requireNoSelection();
        //
        frameFixture.button("SearchWindowForm.m_searchButton").click();
        robot().waitForIdle();
        //
        Pattern pattern = Pattern.compile("(?s)(?=.*\\bError\\b)(?=.*\\bGlossary\\b).*");
        frameFixture.textBox("SearchWindowForm.m_viewer").requireText(pattern);
        //
        searchWindowController.dispose();
        closeProject();
    }

}
