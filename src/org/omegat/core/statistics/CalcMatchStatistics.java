/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.core.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.LevenshteinDistance;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.gui.stat.StatisticsWindow;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;
import org.omegat.util.gui.TextUtil;

/**
 * Thread for calculate match statistics.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class CalcMatchStatistics extends LongProcessThread {

    private String[] header = new String[] { "", OStrings.getString("CT_STATS_Segments"),
            OStrings.getString("CT_STATS_Words"), OStrings.getString("CT_STATS_Characters_NOSP"),
            OStrings.getString("CT_STATS_Characters") };

    private String[] rows = new String[] { OStrings.getString("CT_STATSMATCH_RowRepetitions"),
            OStrings.getString("CT_STATSMATCH_RowExactMatch"),
            OStrings.getString("CT_STATSMATCH_RowMatch95"), OStrings.getString("CT_STATSMATCH_RowMatch85"),
            OStrings.getString("CT_STATSMATCH_RowMatch75"), OStrings.getString("CT_STATSMATCH_RowMatch50"),
            OStrings.getString("CT_STATSMATCH_RowNoMatch") };
    private boolean[] align = new boolean[] { false, true, true, true, true };

    private StatisticsWindow callback;

    /** Hash for exact tokens. Only for statistics calculation. */
    private Map<String, Token[]> tokensCache = new HashMap<String, Token[]>();

    /** Already processed segments. Used for repetitions detect. */
    private Set<String> alreadyProcessed = new HashSet<String>();

    public CalcMatchStatistics(StatisticsWindow callback) {
        this.callback = callback;
    }

    public void run() {
        StatCount[] result = new StatCount[7];
        for (int i = 0; i < result.length; i++) {
            result[i] = new StatCount();
        }
        ISimilarityCalculator distanceCalculator = new LevenshteinDistance();
        List<SourceTextEntry> allEntries = Core.getProject().getAllEntries();

        // We should iterate all segments from all files in project.
        int percent = 0;
        for (int i = 0; i < allEntries.size(); i++) {
            SourceTextEntry ste = allEntries.get(i);
            int p = Statistics.getMaxSimilarityPercent(ste, distanceCalculator, allEntries, tokensCache,
                    alreadyProcessed);
            int r = getRowByPercent(p);

            result[r].segments++;
            result[r].words += Statistics.numberOfWords(ste.getSrcText());
            String charWithoutTags = StaticUtils.stripTags(ste.getSrcText());
            result[r].charsWithoutSpaces += Statistics.numberOfCharactersWithoutSpaces(charWithoutTags);
            result[r].charsWithSpaces += charWithoutTags.length();

            if (isStopped) {
                return;
            }
            int newPercent = i * 100 / allEntries.size();
            if (percent != newPercent) {
                callback.showProgress(newPercent);
                percent = newPercent;
            }
        }

        final String[][] table = calcTable(result);
        final String outText = TextUtil.showTextTable(header, table, align);

        callback.displayData(outText);

        String fn = Core.getProject().getProjectProperties().getProjectInternal()
                + OConsts.STATS_MATCH_FILENAME;
        Statistics.writeStat(fn, outText);
    }

    /**
     * Get row index by match percent.
     * 
     * @param percent
     *            match percent
     * @return row index
     */
    public int getRowByPercent(int percent) {
        if (percent == Statistics.PERCENT_REPETITIONS) {
            // repetitions
            return 0;
        } else if (percent == Statistics.PERCENT_EXACT_MATCH) {
            // exact match
            return 1;
        } else if (percent >= 95) {
            return 2;
        } else if (percent >= 85) {
            return 3;
        } else if (percent >= 75) {
            return 4;
        } else if (percent >= 50) {
            return 5;
        } else {
            return 6;
        }
    }

    /**
     * Extract result to text table.
     * 
     * @param result
     *            result
     * @return text table
     */
    public String[][] calcTable(final StatCount[] result) {
        String[][] table = new String[result.length][5];

        // dump result - will be changed for UI
        for (int i = 0; i < result.length; i++) {
            table[i][0] = rows[i];
            table[i][1] = Integer.toString(result[i].segments);
            table[i][2] = Integer.toString(result[i].words);
            table[i][3] = Integer.toString(result[i].charsWithoutSpaces);
            table[i][4] = Integer.toString(result[i].charsWithSpaces);
        }
        return table;
    }

}
