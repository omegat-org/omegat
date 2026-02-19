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
        calc.run(new CancellationToken());
        Completion completion = testingStatsConsumer.completion().join();
        assertTrue(completion.isSuccess());

        List<String[][]> allResult = testingStatsConsumer.getTable();
        assertEquals(2, allResult.size());
    }

    @Test
    public void testPerFileCalcMatchStatistics() {
        TestingStatsConsumer testingStatsConsumer = new TestingStatsConsumer();
        ICalcStatistics calc = new CalcPerFileMatchStatistics(project, segmenter, testingStatsConsumer);
        calc.run(new CancellationToken());
        Completion completion = testingStatsConsumer.completion().join();
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
    public void testCalcMatchStatistics() {
        TestingStatsConsumer testingStatsConsumer = new TestingStatsConsumer();
        ICalcStatistics calc = new CalcMatchStatistics(project, segmenter, testingStatsConsumer);
        calc.run(new CancellationToken());
        Completion completion = testingStatsConsumer.completion().join();
        assertTrue(completion.isSuccess());

        List<String[][]> allResult = testingStatsConsumer.getTable();
        assertEquals(2, allResult.size());
        String[][] result = allResult.get(1);
        assertNotNull(result);
        assertStatistics(result, false);
    }

    private void assertStatistics(String[][] result, boolean perFile) {
        // assertion
        int n = 0;
        // Repetitions: 11 90 509 583
        assertEquals("11", result[n][1]);
        assertEquals("90", result[n][2]);
        assertEquals("509", result[n][3]);
        assertEquals("583", result[n][4]);
        if (perFile) {
            n++;
            // Repetition from other files: 0 0 0 0
            assertEquals("0", result[n][1]);
            assertEquals("0", result[n][2]);
            assertEquals("0", result[n][3]);
            assertEquals("0", result[n][4]);
        }
        n++;
        // Exact match: 0 0 0 0
        assertEquals("0", result[n][1]);
        assertEquals("0", result[n][2]);
        assertEquals("0", result[n][3]);
        assertEquals("0", result[n][4]);
        n++;
        // 95%-100%: 84 712 3606 4225
        assertEquals("84", result[n][1]);
        assertEquals("712", result[n][2]);
        assertEquals("3606", result[n][3]);
        assertEquals("4225", result[n][4]);
        n++;
        // 85%-94%: 0 0 0 0
        assertEquals("0", result[n][1]);
        assertEquals("0", result[n][2]);
        assertEquals("0", result[n][3]);
        assertEquals("0", result[n][4]);
        n++;
        // 75%-84%: 3 32 234 256
        assertEquals("3", result[n][1]);
        assertEquals("32", result[n][2]);
        assertEquals("234", result[n][3]);
        assertEquals("256", result[n][4]);
        // 50%-74%: 4 61 304 361
        n++;
        assertEquals("4", result[n][1]);
        assertEquals("61", result[n][2]);
        assertEquals("304", result[n][3]);
        assertEquals("361", result[n][4]);
        n++;
        // No match: 6 43 241 274
        assertEquals("6", result[n][1]);
        assertEquals("43", result[n][2]);
        assertEquals("241", result[n][3]);
        assertEquals("274", result[n][4]);
        n++;
        // Total: 108 938 4894 5699
        assertEquals("108", result[n][1]);
        assertEquals("938", result[n][2]);
        assertEquals("4894", result[n][3]);
        assertEquals("5699", result[n][4]);
    }

}
