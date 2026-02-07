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

import org.omegat.core.statistics.spi.IStatsConsumer;

import java.util.concurrent.CompletableFuture;

/**
 * A consumer class implementing the {@code IStatsConsumer} interface for testing purposes.
 * This class is designed primarily to assist in unit tests by recording table data and notifying
 * the completion of data processing.
 *
 * It does not perform any actual data manipulation or display tasks, and most
 * interface methods are effectively no-ops except for a few methods as described below.
 *
 * Responsibilities:
 * - Captures and stores table data provided via the {@code setTable} method.
 * - Signals the completion of data processing using a {@code CompletableFuture}.
 *
 * Thread Safety:
 * - This class is not explicitly thread-safe. Concurrent access to its methods
 *   may lead to undefined behavior.
 *
 * Methods Overview:
 * - {@code setTable(String[] headers, String[][] data)}: Stores the provided table data.
 * - {@code finishData()}: Completes the provided {@code CompletableFuture}, notifying listeners of completion.
 * - Other methods from {@code IStatsConsumer} do not perform any actions.
 */
public class TestingStatsConsumer implements IStatsConsumer {
    private volatile String[][] result;
    private final CompletableFuture<Void> future;

    public TestingStatsConsumer(CompletableFuture<Void> future) {
        this.future = future;
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
        future.complete(null);
    }

    @Override
    public void showProgress(final int percent) {
        // do nothing
    }
}
