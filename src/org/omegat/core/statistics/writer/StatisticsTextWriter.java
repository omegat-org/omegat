/*
 * OmegaT - Computer Assisted Translation (CAT) tool
 *          with fuzzy matching, translation memory, keyword search,
 *          glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
 *                2007 Zoltan Bartko
 *                2009 Didier Briel, Alex Buloichik
 *                2012 Thomas Cordonnier
 *                2026 Hiroshi Miura
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
 *  along with OmegaT.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.core.statistics.writer;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;

import org.omegat.core.statistics.dso.FileData;
import org.omegat.core.statistics.dso.StatCount;
import org.omegat.core.statistics.dso.StatsResult;
import org.omegat.gui.stat.StatisticsPanel;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.TextUtil;

/**
 * Save project statistic into text file.
 *
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
 * @author Zoltan Bartko (bartkozoltan@bartkozoltan.com)
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Thomas Cordonnier
 */
public class StatisticsTextWriter extends AbstractStatisticsWriter {

    private static final String[] HT_HEADERS = { "", OStrings.getString("CT_STATS_Segments"),
            OStrings.getString("CT_STATS_Words"), OStrings.getString("CT_STATS_Characters_NOSP"),
            OStrings.getString("CT_STATS_Characters"), OStrings.getString("CT_STATS_Files"), };

    private static final String[] HT_ROWS = { OStrings.getString("CT_STATS_Total"),
            OStrings.getString("CT_STATS_Remaining"), OStrings.getString("CT_STATS_Unique"),
            OStrings.getString("CT_STATS_Unique_Remaining"), };

    private static final boolean[] HT_ALIGN = new boolean[] { false, true, true, true, true, true };

    private static final String[] FT_HEADERS = { OStrings.getString("CT_STATS_FILE_Name"),
            OStrings.getString("CT_STATS_FILE_Total_Segments"),
            OStrings.getString("CT_STATS_FILE_Remaining_Segments"),
            OStrings.getString("CT_STATS_FILE_Unique_Segments"),
            OStrings.getString("CT_STATS_FILE_Unique_Remaining_Segments"),
            OStrings.getString("CT_STATS_FILE_Total_Words"),
            OStrings.getString("CT_STATS_FILE_Remaining_Words"),
            OStrings.getString("CT_STATS_FILE_Unique_Words"),
            OStrings.getString("CT_STATS_FILE_Unique_Remaining_Words"),
            OStrings.getString("CT_STATS_FILE_Total_Characters_NOSP"),
            OStrings.getString("CT_STATS_FILE_Remaining_Characters_NOSP"),
            OStrings.getString("CT_STATS_FILE_Unique_Characters_NOSP"),
            OStrings.getString("CT_STATS_FILE_Unique_Remaining_Characters_NOSP"),
            OStrings.getString("CT_STATS_FILE_Total_Characters"),
            OStrings.getString("CT_STATS_FILE_Remaining_Characters"),
            OStrings.getString("CT_STATS_FILE_Unique_Characters"),
            OStrings.getString("CT_STATS_FILE_Unique_Remaining_Characters") };

    private static final boolean[] FT_ALIGN = { false, true, true, true, true, true, true, true, true, true,
            true, true, true, true, true, true, true, };

    @Override
    public void write(StatsResult result, Writer out) throws IOException {
        out.write(DateFormat.getInstance().format(new Date()) + "\n");
        out.write(getTextData(result));
    }

    /**
     * Populates the statistics panel with data from the given statistics result object.
     *
     * @param result   the statistics result containing the data to be displayed
     * @param callback the statistics panel that will be updated with the provided data
     */
    public void write(StatsResult result, StatisticsPanel callback) {
        callback.setProjectTableData(HT_HEADERS, getHeaderTable(result));
        callback.setFilesTableData(FT_HEADERS, getFilesTable(result));
        callback.setTextData(getTextData(result));
        callback.finishData();
    }

    /**
     * Return pretty printed statistics data.
     *
     * @return pretty-printed string.
     */
    public String getTextData(StatsResult result) {
        return OStrings.getString("CT_STATS_Project_Statistics") + "\n\n"
                + TextUtil.showTextTable(HT_HEADERS, getHeaderTable(result), HT_ALIGN) + "\n\n" +

                // STATISTICS BY FILE
                OStrings.getString("CT_STATS_FILE_Statistics") + "\n\n"
                + TextUtil.showTextTable(FT_HEADERS, getFilesTable(result), FT_ALIGN);
    }

    // CHECKSTYLE:OFF
    private String[][] getHeaderTable(StatsResult statsResult) {
        StatCount[] result = new StatCount[] { statsResult.getTotal(), statsResult.getRemaining(),
                statsResult.getUnique(), statsResult.getRemainingUnique() };
        String[][] table = new String[result.length][HT_HEADERS.length];

        for (int i = 0; i < result.length; i++) {
            table[i][0] = HT_ROWS[i];
            table[i][1] = Integer.toString(result[i].segments);
            table[i][2] = Integer.toString(result[i].words);
            table[i][3] = Integer.toString(result[i].charsWithoutSpaces);
            table[i][4] = Integer.toString(result[i].charsWithSpaces);
            table[i][5] = Integer.toString(result[i].files);
        }
        return table;
    }

    private String[][] getFilesTable(StatsResult statsResult) {
        String[][] table = new String[statsResult.getCounts().size()][FT_HEADERS.length];

        int r = 0;
        for (FileData numbers : statsResult.getCounts()) {
            table[r][0] = StaticUtils.makeFilenameRelative(numbers.filename,
                    statsResult.getProps().getSourceRoot());
            table[r][1] = Integer.toString(numbers.total.segments);
            table[r][2] = Integer.toString(numbers.remaining.segments);
            table[r][3] = Integer.toString(numbers.unique.segments);
            table[r][4] = Integer.toString(numbers.remainingUnique.segments);
            table[r][5] = Integer.toString(numbers.total.words);
            table[r][6] = Integer.toString(numbers.remaining.words);
            table[r][7] = Integer.toString(numbers.unique.words);
            table[r][8] = Integer.toString(numbers.remainingUnique.words);
            table[r][9] = Integer.toString(numbers.total.charsWithoutSpaces);
            table[r][10] = Integer.toString(numbers.remaining.charsWithoutSpaces);
            table[r][11] = Integer.toString(numbers.unique.charsWithoutSpaces);
            table[r][12] = Integer.toString(numbers.remainingUnique.charsWithoutSpaces);
            table[r][13] = Integer.toString(numbers.total.charsWithSpaces);
            table[r][14] = Integer.toString(numbers.remaining.charsWithSpaces);
            table[r][15] = Integer.toString(numbers.unique.charsWithSpaces);
            table[r][16] = Integer.toString(numbers.remainingUnique.charsWithSpaces);
            r++;
        }
        return table;
    }
    // CHECKSTYLE:ON
}
