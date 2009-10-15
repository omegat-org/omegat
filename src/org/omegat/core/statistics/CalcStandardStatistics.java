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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringEntry;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.gui.stat.StatisticsWindow;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.TextUtil;

/**
 * Thread for calculate standard statistics.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class CalcStandardStatistics extends LongProcessThread {

    private static final String[] htHeaders = new String[] { "",
            OStrings.getString("CT_STATS_Segments"),
            OStrings.getString("CT_STATS_Words"),
            OStrings.getString("CT_STATS_Characters_NOSP"),
            OStrings.getString("CT_STATS_Characters") };

    private static final String[] htRows = new String[] {
            OStrings.getString("CT_STATS_Total"),
            OStrings.getString("CT_STATS_Remaining"),
            OStrings.getString("CT_STATS_Unique"),
            OStrings.getString("CT_STATS_Unique_Remaining") };
    private static final boolean[] htAlign = new boolean[] { false, true, true,
            true, true };

    private static final String[] ftHeaders = new String[] {
            OStrings.getString("CT_STATS_FILE_Name"),
            OStrings.getString("CT_STATS_FILE_Total_Words"),
            OStrings.getString("CT_STATS_FILE_Remaining_Words"),
            OStrings.getString("CT_STATS_FILE_Total_Characters_NOSP"),
            OStrings.getString("CT_STATS_FILE_Remaining_Characters_NOSP"),
            OStrings.getString("CT_STATS_FILE_Total_Characters"),
            OStrings.getString("CT_STATS_FILE_Remaining_Characters") };

    private static final boolean[] ftAlign = new boolean[] { false, true, true,
            true, true, true, true, true };

    private StatisticsWindow callback;

    public CalcStandardStatistics(StatisticsWindow callback) {
        this.callback = callback;
    }

    public void run() {
        IProject p = Core.getProject();
        String result = buildProjectStats(p.getUniqueEntries(), p
                .getAllEntries(), p.getProjectProperties(),
                p.getStatistics().numberofTranslatedSegments);
        callback.displayData(result);

        String internalDir = p.getProjectProperties().getProjectInternal();
        // removing old stats
        try {
            File oldstats = new File(internalDir + "word_counts");
            if (oldstats.exists())
                oldstats.delete();
        } catch (Exception e) {
        }

        // now dump file based word counts to disk
        String fn = internalDir + OConsts.STATS_FILENAME;
        Statistics.writeStat(fn, result);
    }

    /**
     * Builds a file with statistic info about the project. The total word &
     * character count of the project, the total number of unique segments, plus
     * the details for each file.
     */
    public static String buildProjectStats(
            final List<StringEntry> m_strEntryList,
            final List<SourceTextEntry> m_srcTextEntryArray,
            final ProjectProperties m_config,
            final int numberofTranslatedSegments) {

        StatCount total = new StatCount();
        StatCount remaining = new StatCount();
        StatCount unique = new StatCount();
        StatCount remainingUnique = new StatCount();

        String charWithoutTags;

        total.segments = m_srcTextEntryArray.size();
        unique.segments = m_strEntryList.size();
        for (StringEntry se : m_strEntryList) {
            String src = se.getSrcText();
            int dups = se.getParentList().size();

            int words = Statistics.numberOfWords(src);
            unique.words += words;
            total.words += words * dups;

            charWithoutTags = StaticUtils.stripTags(src);
            int charsNoSpaces = Statistics
                    .numberOfCharactersWithoutSpaces(charWithoutTags);
            unique.charsWithoutSpaces += charsNoSpaces;
            total.charsWithoutSpaces += charsNoSpaces * dups;

            int chars = charWithoutTags.length();
            unique.charsWithSpaces += chars;
            total.charsWithSpaces += chars * dups;

            if (!se.isTranslated()) {
                remainingUnique.words += words;
                remainingUnique.charsWithoutSpaces += charsNoSpaces;
                remainingUnique.charsWithSpaces += chars;
                remaining.segments += dups;
            }
        }

        remainingUnique.segments = m_strEntryList.size()
                - numberofTranslatedSegments;

        Map<String, FileData> counts = new TreeMap<String, FileData>();
        for (SourceTextEntry ste : m_srcTextEntryArray) {
            String fileName = ste.getSrcFile().name;
            fileName = StaticUtils.makeFilenameRelative(fileName, m_config
                    .getSourceRoot());

            FileData numbers = counts.get(fileName);
            if (numbers == null) {
                numbers = new FileData();
                counts.put(fileName, numbers);
            }

            String src = ste.getSrcText();
            charWithoutTags = StaticUtils.stripTags(src);
            int words = Statistics.numberOfWords(src);
            numbers.total.words += words;
            int charsNoSpaces = Statistics
                    .numberOfCharactersWithoutSpaces(charWithoutTags);
            numbers.total.charsWithoutSpaces += charsNoSpaces;
            int chars = charWithoutTags.length();
            numbers.total.charsWithSpaces += chars;

            if (!ste.isTranslated()) {
                remaining.words += words;
                numbers.remaining.words += words;
                remaining.charsWithoutSpaces += charsNoSpaces;
                numbers.remaining.charsWithoutSpaces += charsNoSpaces;
                remaining.charsWithSpaces += chars;
                numbers.remaining.charsWithSpaces += chars;
            }
            counts.put(fileName, numbers);
        }

        StringBuilder result = new StringBuilder();

        result.append(OStrings.getString("CT_STATS_Project_Statistics")
                + "\n\n");

        String[][] headerTable = calcHeaderTable(new StatCount[] { total,
                remaining, unique, remainingUnique });
        result.append(TextUtil.showTextTable(htHeaders, headerTable, htAlign));
        result.append("\n\n");

        // STATISTICS BY FILE
        result.append(OStrings.getString("CT_STATS_FILE_Statistics") + "\n\n");
        String[][] filesTable = calcFilesTable(counts);
        result.append(TextUtil.showTextTable(ftHeaders, filesTable, ftAlign));

        return result.toString();
    }

    protected static String[][] calcHeaderTable(final StatCount[] result) {
        String[][] table = new String[result.length][5];

        for (int i = 0; i < result.length; i++) {
            table[i][0] = htRows[i];
            table[i][1] = Integer.toString(result[i].segments);
            table[i][2] = Integer.toString(result[i].words);
            table[i][3] = Integer.toString(result[i].charsWithoutSpaces);
            table[i][4] = Integer.toString(result[i].charsWithSpaces);
        }
        return table;
    }

    protected static String[][] calcFilesTable(
            final Map<String, FileData> counts) {
        String[][] table = new String[counts.size()][7];

        int r = 0;
        for (String filename : counts.keySet()) {
            FileData numbers = counts.get(filename);
            table[r][0] = filename;
            table[r][1] = Integer.toString(numbers.total.words);
            table[r][2] = Integer.toString(numbers.remaining.words);
            table[r][3] = Integer.toString(numbers.total.charsWithoutSpaces);
            table[r][4] = Integer
                    .toString(numbers.remaining.charsWithoutSpaces);
            table[r][5] = Integer.toString(numbers.total.charsWithSpaces);
            table[r][6] = Integer.toString(numbers.remaining.charsWithSpaces);
            r++;
        }
        return table;
    }

    public static class FileData {
        public StatCount total, remaining;

        public FileData() {
            total = new StatCount();
            remaining = new StatCount();
        }
    }
}
