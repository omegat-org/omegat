/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

import static org.junit.Assert.assertEquals;

import java.awt.GraphicsEnvironment;

import org.junit.Assume;
import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.core.search.SearchExpression;
import org.omegat.core.search.SearchMode;

public class SearchWindowTest extends TestCore {

    @Test
    public void testLoadSearchWindow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new SearchWindowController(SearchMode.SEARCH);
    }

    @Test
    public void testLoadSearchAndReplaceWindow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new SearchWindowController(SearchMode.REPLACE);
    }

    /**
     * The search type must follow the selected radio button; a regression
     * once made every search run as an exact search, so a regular expression
     * like "a|b" was quoted and found nothing.
     */
    @Test
    public void testSearchTypeFollowsTheSelectedRadioButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SearchWindowController controller = new SearchWindowController(SearchMode.SEARCH);
        SearchWindowForm form = (SearchWindowForm) controller.getWindow();

        form.m_searchRegexpSearchRB.setSelected(true);
        assertEquals(SearchExpression.SearchExpressionType.REGEXP,
                controller.getSearchExpressionTypeForSearchMode());

        form.m_searchKeywordSearchRB.setSelected(true);
        assertEquals(SearchExpression.SearchExpressionType.KEYWORD,
                controller.getSearchExpressionTypeForSearchMode());

        form.m_searchExactSearchRB.setSelected(true);
        assertEquals(SearchExpression.SearchExpressionType.EXACT,
                controller.getSearchExpressionTypeForSearchMode());
    }

    /**
     * The replace mode has its own radio button group; it must not read the
     * search mode's buttons.
     */
    @Test
    public void testReplaceTypeFollowsTheSelectedRadioButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SearchWindowController controller = new SearchWindowController(SearchMode.REPLACE);
        SearchWindowForm form = (SearchWindowForm) controller.getWindow();

        form.m_replaceRegexpSearchRB.setSelected(true);
        assertEquals(SearchExpression.SearchExpressionType.REGEXP,
                controller.getSearchExpressionTypeForReplaceMode());

        form.m_replaceExactSearchRB.setSelected(true);
        assertEquals(SearchExpression.SearchExpressionType.EXACT,
                controller.getSearchExpressionTypeForReplaceMode());
    }
}
