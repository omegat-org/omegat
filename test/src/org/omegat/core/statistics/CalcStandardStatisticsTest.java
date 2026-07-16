/*******************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2026 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.omegat.core.statistics;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.core.data.TestCoreState;
import org.omegat.core.threads.CancellationToken;
import org.omegat.core.threads.Completion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@code CalcStandardStatistics} class.
 */
public class CalcStandardStatisticsTest extends TestCore {

    // On some CI environments, calculating statistics can occasionally be slow
    // due to limited CPU resources and I/O.
    private static Path tmpDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
    }

    private TestingProject project;

    @Before
    public void setUp() throws Exception {
        project = new TestingProject(tmpDir);
        TestCoreState.getInstance().setProject(project);
    }

    @Test
    public void testStatistics() {
        TestingStatsConsumer testingStatsConsumer = new TestingStatsConsumer();
        ICalcStatistics calc = new CalcStandardStatistics(project, testingStatsConsumer);
        CancellationToken ctoken = new CancellationToken();
        calc.run(ctoken);
        Completion completion = testingStatsConsumer.completion().join();
        assertFalse(ctoken.isCancelled());
        assertTrue(completion.isSuccess());

        List<String[][]> allResult = testingStatsConsumer.getTable();
        assertEquals(2, allResult.size());
        String[][] result = allResult.getFirst();
        assertNotNull(result);
        // Total: 108 938 4894 5699
        assertRowValues(result[0], "108", "938", "4894", "5699");
        // Remaining: 108 938 4894 5699
        assertRowValues(result[1], "108", "938", "4894", "5699");
        // Unique: 97 848 4385 5116
        assertRowValues(result[2], "97", "848", "4385", "5116");
        // Unique Remaining: 97 848 4385 5116
        assertRowValues(result[3], "97", "848", "4385", "5116");
        result = allResult.get(1);
        assertNotNull(result);
        // test/data/filters/po/file-POFilter-match-stat-en-ca.po: 108 108 97 97
        // ....
        assertRowValues(result[0], "108", "108", "97", "97");
    }

    private void assertRowValues(String[] row, String v1, String v2, String v3, String v4) {
        assertEquals(v1, row[1]);
        assertEquals(v2, row[2]);
        assertEquals(v3, row[3]);
        assertEquals(v4, row[4]);
    }
}
