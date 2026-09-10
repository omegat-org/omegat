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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author stephan.pakebusch at zollsoft.de
 */
public class MatchStatisticsResultTest {

    /**
     * The constructor is the single place that enforces the copying, so a
     * caller mutating its arrays or map afterwards must not reach the result.
     */
    @Test
    public void testConstructorCopiesParts() {
        String[] headers = { "", "Segments" };
        String[][] data = { { "Repetitions:", "11" } };
        Map<Integer, Integer> rows = new HashMap<>();
        rows.put(1, 0);
        MatchStatisticsResult result = new MatchStatisticsResult(headers, data, rows, null);
        headers[0] = "changed";
        data[0][1] = "changed";
        rows.put(2, 3);
        assertEquals("", result.headers()[0]);
        assertEquals("11", result.data()[0][1]);
        assertEquals(1, result.entryRowIndexes().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntryRowIndexesImmutable() {
        MatchStatisticsResult result = new MatchStatisticsResult(new String[] { "" },
                new String[][] { { "" } }, Map.of(1, 0), null);
        result.entryRowIndexes().put(2, 3);
    }

    @Test
    public void testBuilderRequiresTableAndMapping() {
        MatchStatisticsResult.Builder builder = new MatchStatisticsResult.Builder();
        assertNull(builder.build());
        builder.setTable(new String[] { "" }, new String[][] { { "" } });
        assertNull(builder.build());
        builder.setEntryRowIndexes(Map.of(1, 0));
        MatchStatisticsResult result = builder.build();
        assertNotNull(result);
        assertNull(result.textData());
        builder.setTextData("text");
        MatchStatisticsResult withText = builder.build();
        assertNotNull(withText);
        assertEquals("text", withText.textData());
    }
}
