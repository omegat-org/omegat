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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.core.statistics;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
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
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;

/**
 * Thread for calculate match statistics.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class CalcMatchStatistics extends LongProcessThread {

    private String[] header = new String[] { "",
            OStrings.getString("CT_STATS_Segments"),
            OStrings.getString("CT_STATS_Words"),
            OStrings.getString("CT_STATS_Characters_NOSP"),
            OStrings.getString("CT_STATS_Characters") };
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
            int p = Statistics.getMaxSimilarityPercent(ste, distanceCalculator,
                    allEntries, tokensCache, alreadyProcessed);
            int r = getRowByPercent(p);

            result[r].segments++;
            result[r].words += Statistics.numberOfWords(ste.getSrcText());
            String charWithoutTags = StaticUtils.stripTags(ste.getSrcText());
            result[r].charsWithoutSpaces += Statistics
                    .numberOfCharactersWithoutSpaces(charWithoutTags);
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
        final String outText = showTextTable(header, table, align);

        callback.displayData(outText);

        String fn = Core.getProject().getProjectProperties()
                .getProjectInternal()
                + OConsts.STATS_MATCH_FILENAME;
        try {
            OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(fn), OConsts.UTF8);
            try {
                out.write(DateFormat.getInstance().format(new Date()) + "\n");
                out.write(outText);
                out.flush();
            } finally {
                out.close();
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
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
    protected String[][] calcTable(final StatCount[] result) {
        String[][] table = new String[result.length][5];
        // dump result - will be changed for UI
        for (int i = 0; i < result.length; i++) {
            switch (i) {
            case 0:
                table[i][0] = OStrings
                        .getString("CT_STATSMATCH_RowRepetitions");
                break;
            case 1:
                table[i][0] = OStrings.getString("CT_STATSMATCH_RowExactMatch");
                break;
            case 2:
                table[i][0] = OStrings.getString("CT_STATSMATCH_RowMatch95");
                break;
            case 3:
                table[i][0] = OStrings.getString("CT_STATSMATCH_RowMatch85");
                break;
            case 4:
                table[i][0] = OStrings.getString("CT_STATSMATCH_RowMatch75");
                break;
            case 5:
                table[i][0] = OStrings.getString("CT_STATSMATCH_RowMatch50");
                break;
            case 6:
                table[i][0] = OStrings.getString("CT_STATSMATCH_RowNoMatch");
                break;
            }
            table[i][1] = Integer.toString(result[i].segments);
            table[i][2] = Integer.toString(result[i].words);
            table[i][3] = Integer.toString(result[i].charsWithoutSpaces);
            table[i][4] = Integer.toString(result[i].charsWithSpaces);
        }
        return table;
    }

    /**
     * Draw text table with columns align.
     * 
     * @param columnHeaders
     *            column headers
     * @param table
     *            table data
     * @return text
     */
    protected static String showTextTable(String[] columnHeaders,
            String[][] table, boolean[] alignRight) {
        StringBuilder out = new StringBuilder();

        // calculate max column size
        int maxColSize[] = new int[columnHeaders.length];
        for (int c = 0; c < columnHeaders.length; c++) {
            maxColSize[c] = columnHeaders[c].length();
        }
        for (int r = 0; r < table.length; r++) {
            for (int c = 0; c < table[r].length; c++) {
                maxColSize[c] = Math.max(maxColSize[c], table[r][c].length());
            }
        }

        for (int c = 0; c < columnHeaders.length; c++) {
            appendField(out, columnHeaders[c], maxColSize[c], alignRight[c]);
        }
        out.append('\n');
        for (int r = 0; r < table.length; r++) {
            for (int c = 0; c < table[r].length; c++) {
                appendField(out, table[r][c], maxColSize[c], alignRight[c]);
            }
            out.append('\n');
        }
        return out.toString();
    }

    /**
     * Output field with specified length.
     * 
     * @param out
     *            output stream
     * @param data
     *            field data
     * @param colSize
     *            field size
     */
    private static void appendField(StringBuilder out, String data,
            int colSize, boolean alignRight) {
        if (!alignRight) {
            out.append(data);
        }
        for (int i = data.length(); i < colSize; i++) {
            out.append(' ');
        }
        if (alignRight) {
            out.append(data);
        }
        out.append("\t");
    }
}
