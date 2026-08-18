/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.stat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.omegat.core.statistics.Statistics;
import org.omegat.core.statistics.dso.MatchStatCounts;

/**
 * @author stephan.pakebusch at zollsoft.de
 */
public class MatchStatisticsPanelTest {

    /**
     * The displayed total table skips the repetitions-from-other-files
     * category, so displayed rows and category rows diverge after row 0.
     */
    @Test
    public void testCategoryRowForDisplayRow() {
        // Repetitions
        assertEquals(MatchStatCounts.ROW_REPETITIONS, MatchStatisticsPanel.categoryRowForDisplayRow(0));
        // Exact match
        assertEquals(MatchStatCounts.getRowByPercent(Statistics.PERCENT_EXACT_MATCH),
                MatchStatisticsPanel.categoryRowForDisplayRow(1));
        // Fuzzy bands and no match
        assertEquals(MatchStatCounts.getRowByPercent(95), MatchStatisticsPanel.categoryRowForDisplayRow(2));
        assertEquals(MatchStatCounts.getRowByPercent(85), MatchStatisticsPanel.categoryRowForDisplayRow(3));
        assertEquals(MatchStatCounts.getRowByPercent(75), MatchStatisticsPanel.categoryRowForDisplayRow(4));
        assertEquals(MatchStatCounts.getRowByPercent(50), MatchStatisticsPanel.categoryRowForDisplayRow(5));
        assertEquals(MatchStatCounts.getRowByPercent(0), MatchStatisticsPanel.categoryRowForDisplayRow(6));
    }
}
