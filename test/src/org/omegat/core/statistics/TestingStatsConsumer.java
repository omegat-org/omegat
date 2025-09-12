/*******************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023-2025 Hiroshi Miura
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

import java.util.concurrent.CountDownLatch;

public class TestingStatsConsumer implements IStatsConsumer {
    private String[][] result;
    private final CountDownLatch latch;

    public TestingStatsConsumer(CountDownLatch latch) {
        this.latch = latch;
    }

    public String[][] getTable() {
        return result;
    }

    @Override
    public void appendTextData(final String result) {
        // do nothing
    }

    @Override
    public void appendTable(final String title, final String[] headers, final String[][] data) {
        // do nothing
    }

    @Override
    public void setTextData(final String data) {
        // do nothing
    }

    @Override
    public void setTable(final String[] headers, final String[][] data) {
        result = data;
    }

    @Override
    public void setDataFile(final String path) {
        // do nothing
    }

    @Override
    public void finishData() {
        latch.countDown();
    }

    @Override
    public void showProgress(final int percent) {
        // do nothing
    }
}
