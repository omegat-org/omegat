/*******************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura
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
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
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
 * Unit tests for the {@code CalcMatchStatistics} class.
 * This class validates the functionality of calculating match statistics for
 * translation memory segments. The tests assert correctness of match categories
 * such as repetitions, exact matches, fuzzy matches, and no matches across
 * multiple similarity thresholds.
 *
 * The following annotations and methods are notable:
 *
 * - {@code @Before}: Used for test-specific setup, ensuring the testing environment
 *   is initialized and isolated.
 * - {@code @BeforeClass}: Utilized for one-time setup before all tests are executed,
 *   including temporary directory creation for testing purposes.
 * - {@code @Test}: Defines a method as a test case, validating different
 *   statistical metrics with assertions.
 *
 * Key functionality tested:
 *
 * - Ensures that statistics calculations such as repetitions, exact matches, and
 *   fuzzy matches (e.g., 95%, 85%, 75%, and 50% similarity thresholds) produce
 *   expected and valid results.
 * - Asserts total metrics for match statistics to verify data aggregation is
 *   correct and comprehensive.
 * - Confirms integration of dependent components like segmenters and testing
 *   consumers used for capturing results.
 */
public class CalcMatchStatisticsTest extends TestCore {

    // On some CI environments, calculating statistics can occasionally be slow
    // due to limited CPU resources and I/O.
    private static Path tmpDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
    }

    private TestingProject project;
    private final Segmenter segmenter = new Segmenter(SRX.getDefault());

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
        String[][] result = allResult.get(0);
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
        // test/data/filters/po/file-POFilter-match-stat-en-ca.po: 108 108 97 97 ....
        assertRowValues(result[0], "108", "108", "97", "97");
    }

    @Test
    public void testPerFileCalcMatchStatistics() {
        TestingStatsConsumer testingStatsConsumer = new TestingStatsConsumer();
        ICalcStatistics calc = new CalcPerFileMatchStatistics(project, segmenter, testingStatsConsumer);
        CancellationToken ctoken = new CancellationToken();
        calc.run(ctoken);
        Completion completion = testingStatsConsumer.completion().join();
        assertFalse(ctoken.isCancelled());
        assertTrue(completion.isSuccess());

        List<String[][]> allResult = testingStatsConsumer.getTable();
        assertEquals(2, allResult.size());
        String[][] result = allResult.get(0);
        assertNotNull(result);
        assertStatistics(result, true);

        result = allResult.get(1);
        assertNotNull(result);
        assertStatistics(result, false);
    }

    @Test
    public void testCalcMatchStatics() {
        TestingProject project = new TestingProject(tmpDir);
        Segmenter segmenter = new Segmenter(SRX.getDefault());
        TestingStatsConsumer testingStatsConsumer = new TestingStatsConsumer();
        CalcMatchStatistics calcMatchStatistics = new CalcMatchStatistics(project, segmenter, testingStatsConsumer);
        CancellationToken ctoken = new CancellationToken();
        calcMatchStatistics.run(ctoken);
        Completion completion = testingStatsConsumer.completion().join();
        assertFalse(ctoken.isCancelled());
        assertTrue(completion.isSuccess());

        List<String[][]> allResult = testingStatsConsumer.getTable();
        assertEquals(2, allResult.size());
        String[][] result = allResult.get(0);
        assertNotNull(result);
        assertEquals(3, result.length);
        // Repetitions: 11 90 509 583
        assertRowValues(result[0], "11", "90", "509", "583");
        assertRowValues(result[1], "0", "0", "0", "0");
        assertRowValues(result[2], "0", "0", "0", "0");
        result = allResult.get(1);
        assertNotNull(result);
        assertStatistics(result, false);
    }

    private void assertStatistics(String[][] result, boolean perFile) {
        int rowIndex = 0;
        // Repetitions: 11 90 509 583
        assertRowValues(result[rowIndex++], "11", "90", "509", "583");
        if (perFile) {
            // Repetition from other files: 0 0 0 0
            assertRowValues(result[rowIndex++], "0", "0", "0", "0");
        }
        // Exact match: 0 0 0 0
        assertRowValues(result[rowIndex++], "0", "0", "0", "0");
        // 95%-100%: 84 712 3606 4225
        assertRowValues(result[rowIndex++], "84", "712", "3606", "4225");
        // 85%-94%: 0 0 0 0
        assertRowValues(result[rowIndex++], "0", "0", "0", "0");
        // 75%-84%: 3 32 234 256
        assertRowValues(result[rowIndex++], "3", "32", "234", "256");
        // 50%-74%: 4 61 304 361
        assertRowValues(result[rowIndex++], "4", "61", "304", "361");
        // No match: 6 43 241 274
        assertRowValues(result[rowIndex++], "6", "43", "241", "274");
        // Total: 108 938 4894 5699
        assertRowValues(result[rowIndex], "108", "938", "4894", "5699");
    }

    private void assertRowValues(String[] row, String v1, String v2, String v3, String v4) {
        assertEquals(v1, row[1]);
        assertEquals(v2, row[2]);
        assertEquals(v3, row[3]);
        assertEquals(v4, row[4]);
    }
}
