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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.TestPreferencesInitializer;

import static org.junit.Assert.assertTrue;

public class CalcMatchStatisticsTest {

    private static Path tmpDir;

    /*
     * Setup test project.
     */
    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        assertTrue(tmpDir.toFile().isDirectory());
    }

    @Test
    public void testCalcMatchStatics() {
        TestingProject project = new TestingProject(tmpDir);
        Segmenter segmenter = new Segmenter(SRX.getDefault());
        CountDownLatch latch = new CountDownLatch(1);
        TestingStatsConsumer testingStatsConsumer = new TestingStatsConsumer(latch);
        CalcMatchStatistics calcMatchStatistics = new CalcMatchStatistics(project, segmenter, testingStatsConsumer, false);
        calcMatchStatistics.start();
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException ignored) {
        }
        try {
            calcMatchStatistics.join();
        } catch (InterruptedException ignored) {
        }
        String[][] result = testingStatsConsumer.getTable();
        Assert.assertNotNull(result);

        // assertions
        // RowRepetitions 11 90 509 583
        Assert.assertEquals("11", result[0][1]);
        Assert.assertEquals("90", result[0][2]);
        Assert.assertEquals("509", result[0][3]);
        Assert.assertEquals("583", result[0][4]);
        // RowExactMatch 0 0 0 0
        Assert.assertEquals("0", result[1][1]);
        Assert.assertEquals("0", result[1][2]);
        Assert.assertEquals("0", result[1][3]);
        Assert.assertEquals("0", result[1][4]);
        // RowMatch95 84 712 3606 4225
        Assert.assertEquals("84", result[2][1]);
        Assert.assertEquals("712", result[2][2]);
        Assert.assertEquals("3606", result[2][3]);
        Assert.assertEquals("4225", result[2][4]);
        // RowMatch85 0 0 0 0
        Assert.assertEquals("0", result[3][1]);
        Assert.assertEquals("0", result[3][2]);
        Assert.assertEquals("0", result[3][3]);
        Assert.assertEquals("0", result[3][4]);
        // RowMatch75 3 32 234 256
        Assert.assertEquals("3", result[4][1]);
        Assert.assertEquals("32", result[4][2]);
        Assert.assertEquals("234", result[4][3]);
        Assert.assertEquals("256", result[4][4]);
        // RowMatch50 4 61 304 361
        Assert.assertEquals("4", result[5][1]);
        Assert.assertEquals("61", result[5][2]);
        Assert.assertEquals("304", result[5][3]);
        Assert.assertEquals("361", result[5][4]);
        // RowNoMatch 6 43 241 274
        Assert.assertEquals("6", result[6][1]);
        Assert.assertEquals("43", result[6][2]);
        Assert.assertEquals("241", result[6][3]);
        Assert.assertEquals("274", result[6][4]);
        // Total 108 938 4894 5699
        Assert.assertEquals("108", result[7][1]);
        Assert.assertEquals("938", result[7][2]);
        Assert.assertEquals("4894", result[7][3]);
        Assert.assertEquals("5699", result[7][4]);
    }

}
