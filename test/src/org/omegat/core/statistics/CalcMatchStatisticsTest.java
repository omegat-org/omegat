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

import org.junit.BeforeClass;
import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.threads.Completion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

    // On some CI environments, calculating statistics can occasionally be slow
    // due to limited CPU resources and I/O.
    private static Path tmpDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
    }

    @Test
    public void testCalcMatchStatics() {
        TestingProject project = new TestingProject(tmpDir);
        Segmenter segmenter = new Segmenter(SRX.getDefault());
        TestingStatsConsumer testingStatsConsumer = new TestingStatsConsumer();
        CalcMatchStatistics calcMatchStatistics = new CalcMatchStatistics(project, segmenter, testingStatsConsumer, false);

        // execute and complete the task
        calcMatchStatistics.start();
        Completion completion = testingStatsConsumer.completion().join();
        assertTrue(completion.isSuccess());

        String[][] result = testingStatsConsumer.getTable();
        assertNotNull(result);

        // assertions
        // RowRepetitions 11 90 509 583
        assertEquals("11", result[0][1]);
        assertEquals("90", result[0][2]);
        assertEquals("509", result[0][3]);
        assertEquals("583", result[0][4]);
        // RowExactMatch 0 0 0 0
        assertEquals("0", result[1][1]);
        assertEquals("0", result[1][2]);
        assertEquals("0", result[1][3]);
        assertEquals("0", result[1][4]);
        // RowMatch95 84 712 3606 4225
        assertEquals("84", result[2][1]);
        assertEquals("712", result[2][2]);
        assertEquals("3606", result[2][3]);
        assertEquals("4225", result[2][4]);
        // RowMatch85 0 0 0 0
        assertEquals("0", result[3][1]);
        assertEquals("0", result[3][2]);
        assertEquals("0", result[3][3]);
        assertEquals("0", result[3][4]);
        // RowMatch75 3 32 234 256
        assertEquals("3", result[4][1]);
        assertEquals("32", result[4][2]);
        assertEquals("234", result[4][3]);
        assertEquals("256", result[4][4]);
        // RowMatch50 4 61 304 361
        assertEquals("4", result[5][1]);
        assertEquals("61", result[5][2]);
        assertEquals("304", result[5][3]);
        assertEquals("361", result[5][4]);
        // RowNoMatch 6 43 241 274
        assertEquals("6", result[6][1]);
        assertEquals("43", result[6][2]);
        assertEquals("241", result[6][3]);
        assertEquals("274", result[6][4]);
        // Total 108 938 4894 5699
        assertEquals("108", result[7][1]);
        assertEquals("938", result[7][2]);
        assertEquals("4894", result[7][3]);
        assertEquals("5699", result[7][4]);
    }

}
