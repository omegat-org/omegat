/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

/**
 * Bean for store match count for one file or for full project, i.e. "Repetitions", "Exact match", "95%-100%",
 * "85%-94%", etc.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MatchStatCounts {
    private final StatCount[] counts;
    private final int baseForPercents;

    public MatchStatCounts(boolean isTotal) {
        counts = new StatCount[isTotal ? 8 : 9];
        baseForPercents = isTotal ? 1 : 2;
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
        counts[1].add(count);
    }

    public void addForPercents(int percent, StatCount count) {
        counts[getRowByPercent(percent)].add(count);
    }

    /**
     * Get row index by match percent.
     * 
     * @param percent
     *            match percent
     * @return row index
     */
    private int getRowByPercent(int percent) {
        if (percent == Statistics.PERCENT_REPETITIONS) {
            // repetitions
            return 0;
        } else if (percent == Statistics.PERCENT_EXACT_MATCH) {
            // exact match
            return baseForPercents;
        } else if (percent >= 95) {
            return baseForPercents + 1;
        } else if (percent >= 85) {
            return baseForPercents + 2;
        } else if (percent >= 75) {
            return baseForPercents + 3;
        } else if (percent >= 50) {
            return baseForPercents + 4;
        } else {
            return baseForPercents + 5;
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
        String[][] table = new String[baseForPercents + 1][5];

        // dump result - will be changed for UI
        for (int i = 0; i <= baseForPercents; i++) {
            table[i][0] = rows[i];
            table[i][1] = Integer.toString(counts[i].segments);
            table[i][2] = Integer.toString(counts[i].words);
            table[i][3] = Integer.toString(counts[i].charsWithoutSpaces);
            table[i][4] = Integer.toString(counts[i].charsWithSpaces);
        }
        return table;
    }

    /**
     * Extract result to text table.
     * 
     * @param result
     *            result
     * @return text table
     */
    public String[][] calcTable(String[] rows) {
        // calculate total except repetitions from other files
        counts[counts.length - 1].add(counts[0]);
        for (int i = baseForPercents; i < counts.length - 1; i++) {
            counts[counts.length - 1].add(counts[i]);
        }

        String[][] table = new String[counts.length][5];

        // dump result - will be changed for UI
        for (int i = 0; i < counts.length; i++) {
            table[i][0] = rows[i];
            table[i][1] = Integer.toString(counts[i].segments);
            table[i][2] = Integer.toString(counts[i].words);
            table[i][3] = Integer.toString(counts[i].charsWithoutSpaces);
            table[i][4] = Integer.toString(counts[i].charsWithSpaces);
        }
        return table;
    }
}
