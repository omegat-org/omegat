/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.statistics;

import java.util.function.IntPredicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Bean for store match count for one file or for full project, i.e. "Repetitions", "Exact match", "95%-100%",
 * "85%-94%", etc.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MatchStatCounts {
    private static final int BASE_FOR_PERCENTS = 2;
    private final StatCount[] counts;

    public MatchStatCounts() {
        counts = new StatCount[8];
        for (int i = 0; i < counts.length; i++) {
            counts[i] = new StatCount();
        }
    }

    public void addRepetition(StatCount count) {
        counts[0].add(count);
    }

    public void addRepetitionWithinThisFile(StatCount count) {
        counts[0].add(count);
    }

    public void addRepetitionFromOtherFiles(StatCount count) {
        counts[1].add(count);
    }

    public void addExact(StatCount count) {
        counts[getRowByPercent(Statistics.PERCENT_EXACT_MATCH)].add(count);
    }

    public void addForPercents(int percent, StatCount count) {
        counts[getRowByPercent(percent)].add(count);
    }

    /**
     * Add all counts contained in another MatchStatCounts instance into this
     * instance.
     *
     * @param other
     *            Counts to add
     * @return This instance
     */
    public MatchStatCounts addCounts(MatchStatCounts other) {
        for (int i = 0; i < counts.length; i++) {
            counts[i].add(other.counts[i]);
        }
        return this;
    }

    /**
     * Get row index by match percent.
     *
     * @param percent
     *            match percent
     * @return row index
     */
    private int getRowByPercent(int percent) {
        if (percent == Statistics.PERCENT_EXACT_MATCH) {
            // exact match
            return BASE_FOR_PERCENTS;
        } else if (percent >= 95) {
            return BASE_FOR_PERCENTS + 1;
        } else if (percent >= 85) {
            return BASE_FOR_PERCENTS + 2;
        } else if (percent >= 75) {
            return BASE_FOR_PERCENTS + 3;
        } else if (percent >= 50) {
            return BASE_FOR_PERCENTS + 4;
        } else {
            return BASE_FOR_PERCENTS + 5;
        }
    }

    /**
     * Extract first two rows result to text table.
     *
     * @param result
     *            result
     * @return text table
     */
    public String[][] calcTableWithoutPercentage(String[] rows) {
        String[][] table = new String[BASE_FOR_PERCENTS + 1][5];

        // dump result - will be changed for UI
        for (int i = 0; i <= BASE_FOR_PERCENTS; i++) {
            table[i][0] = rows[i];
            table[i][1] = Integer.toString(counts[i].segments);
            table[i][2] = Integer.toString(counts[i].words);
            table[i][3] = Integer.toString(counts[i].charsWithoutSpaces);
            table[i][4] = Integer.toString(counts[i].charsWithSpaces);
        }
        return table;
    }

    /**
     * Extract result to text table. Convenience method for
     * {@link #calcTable(String[], IntPredicate)} that accepts all rows.
     *
     * @param rows
     *            An array of row headers
     * @return text table
     */
    public String[][] calcTable(String[] rows) {
        if (rows.length != counts.length + 1) {
            throw new IllegalArgumentException("Must supply headers for " + (counts.length + 1) + " rows");
        }
        return calcTable(rows, i -> true);
    }

    /**
     * Extract result to text table. Rows for which the <code>rowFilter</code>
     * returns <code>false</code> will be skipped.
     *
     * @param rows
     *            An array of row headers
     * @param rowFilter
     *            A filter indicating rows that should be kept
     * @return text table
     */
    public String[][] calcTable(String[] rows, IntPredicate rowFilter) {
        // calculate total
        StatCount total = Stream.of(counts).collect(Collector.of(StatCount::new, StatCount::add, StatCount::add));

        String[][] table = new String[rows.length][5];

        // dump result - will be changed for UI
        for (int i = 0, offset = 0; i <= counts.length; i++) {
            if (!rowFilter.test(i)) {
                // Null row header means skip this row
                offset++;
                continue;
            }
            StatCount c = i == counts.length ? total : counts[i];
            int offsetIndex = i - offset;
            table[offsetIndex][0] = rows[offsetIndex];
            table[offsetIndex][1] = Integer.toString(c.segments);
            table[offsetIndex][2] = Integer.toString(c.words);
            table[offsetIndex][3] = Integer.toString(c.charsWithoutSpaces);
            table[offsetIndex][4] = Integer.toString(c.charsWithSpaces);
        }
        return table;
    }
}
