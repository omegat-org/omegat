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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omegat.core.events.IProjectEventListener;

/**
 * @author stephan.pakebusch at zollsoft.de
 */
public class MatchStatisticsCacheTest {

    private static final String[] HEADERS = { "", "Segments" };
    private static final String[][] DATA = { { "Repetitions:", "11" } };
    private static final Map<Integer, Integer> ROWS = Collections.singletonMap(1, 0);

    @Before
    @After
    public void clearCache() {
        MatchStatisticsCache.clear();
    }

    private static final String PROJECT_ROOT = "/project/root/";

    @Test
    public void testStoreAndGet() {
        assertFalse(MatchStatisticsCache.get().isPresent());
        MatchStatisticsCache.store(HEADERS, DATA, ROWS, "text", PROJECT_ROOT);
        MatchStatisticsCache.Snapshot snapshot = MatchStatisticsCache.get().orElseThrow(AssertionError::new);
        assertArrayEquals(HEADERS, snapshot.getHeaders());
        assertArrayEquals(DATA, snapshot.getData());
        assertEquals(ROWS, snapshot.getEntryRowIndexes());
        assertEquals("text", snapshot.getTextData());
        assertEquals(PROJECT_ROOT, snapshot.getProjectRoot());
        assertNotNull(snapshot.getLastScan());
    }

    @Test
    public void testClearedOnProjectChange() {
        MatchStatisticsCache.store(HEADERS, DATA, ROWS, null, PROJECT_ROOT);
        MatchStatisticsCache.onProjectChanged(IProjectEventListener.PROJECT_CHANGE_TYPE.SAVE);
        assertTrue(MatchStatisticsCache.get().isPresent());
        MatchStatisticsCache.onProjectChanged(IProjectEventListener.PROJECT_CHANGE_TYPE.MODIFIED);
        assertTrue(MatchStatisticsCache.get().isPresent());
        MatchStatisticsCache.onProjectChanged(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE);
        assertFalse(MatchStatisticsCache.get().isPresent());

        MatchStatisticsCache.store(HEADERS, DATA, ROWS, null, PROJECT_ROOT);
        MatchStatisticsCache.onProjectChanged(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
        assertFalse(MatchStatisticsCache.get().isPresent());
    }
}
