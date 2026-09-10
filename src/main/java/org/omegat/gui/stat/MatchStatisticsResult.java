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

import java.util.Arrays;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Result of one total match statistics scan: the final table, the mapping
 * from segment entry number to category row index (see
 * {@link org.omegat.core.statistics.dso.MatchStatCounts}), and the plain
 * text rendering. The constructor copies the arrays and the map, so a result
 * never shares mutable state with whoever produced the parts. The array
 * components keep the default identity-based equals/hashCode; results are
 * carriers, not map keys.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public record MatchStatisticsResult(String[] headers, String[][] data,
        Map<Integer, Integer> entryRowIndexes, @Nullable String textData) {

    public MatchStatisticsResult {
        headers = headers.clone();
        data = Arrays.stream(data).map(String[]::clone).toArray(String[][]::new);
        entryRowIndexes = Map.copyOf(entryRowIndexes);
    }

    /**
     * Accumulator for the parts of a result, which arrive through separate
     * {@link org.omegat.core.statistics.IStatsConsumer} callbacks. One builder
     * holds at most one scan: the consumer replaces it when a calculation
     * starts, so parts of different scans can never combine into one result.
     */
    static final class Builder {

        private volatile String @Nullable [] headers;
        private volatile String @Nullable [][] data;
        private volatile @Nullable Map<Integer, Integer> entryRowIndexes;
        private volatile @Nullable String textData;

        void setTable(String[] headers, String[][] data) {
            this.headers = headers;
            this.data = data;
        }

        void setEntryRowIndexes(Map<Integer, Integer> entryRowIndexes) {
            this.entryRowIndexes = entryRowIndexes;
        }

        void setTextData(String textData) {
            this.textData = textData;
        }

        /**
         * Mapping from entry number to category row of the scan accumulating
         * here, null while it has not arrived. It arrives before the final
         * table of the same scan.
         */
        @Nullable
        Map<Integer, Integer> entryRowIndexes() {
            return entryRowIndexes;
        }

        /**
         * Build the result once all mandatory parts arrived.
         *
         * @return the copied result, or null while the table or the entry
         *         mapping is missing
         */
        @Nullable
        MatchStatisticsResult build() {
            String[] builtHeaders = headers;
            String[][] builtData = data;
            Map<Integer, Integer> builtRows = entryRowIndexes;
            if (builtHeaders == null || builtData == null || builtRows == null) {
                return null;
            }
            return new MatchStatisticsResult(builtHeaders, builtData, builtRows, textData);
        }
    }
}
