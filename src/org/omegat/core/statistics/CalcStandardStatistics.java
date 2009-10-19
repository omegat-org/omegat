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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
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
        String result = buildProjectStats(p.getAllEntries(), p
                .getProjectProperties(), null);
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
            final List<SourceTextEntry> m_srcTextEntryArray,
            final ProjectProperties m_config, final StatisticsInfo hotStat) {

        StatCount total = new StatCount();
        StatCount remaining = new StatCount();
        StatCount unique = new StatCount();
        StatCount remainingUnique = new StatCount();

        Map<String, FileData> counts = new TreeMap<String, FileData>();        
        for (SourceTextEntry ste : m_srcTextEntryArray) {
            String src = ste.getStrEntry().getSrcText();
            
            int words = Statistics.numberOfWords(src);
            String noTags = StaticUtils.stripTags(src);
            int charsNoSpaces = Statistics
                    .numberOfCharactersWithoutSpaces(noTags);
            int chars = noTags.length();
            
            // add to total
            total.segments++;
            total.words += words;
            total.charsWithoutSpaces += charsNoSpaces;
            total.charsWithSpaces += chars;
            
            // add to remaining
            if (!ste.isTranslated()) {
                remaining.segments++;
                remaining.words += words;
                remaining.charsWithoutSpaces += charsNoSpaces;
                remaining.charsWithSpaces += chars;
            }
            
            // add to file's info
            FileData numbers = counts.get(ste.getSrcFile().name);
            if (numbers == null) {
                numbers = new FileData();
                counts.put(ste.getSrcFile().name, numbers);
            }            
            numbers.total.segments++;
            numbers.total.words += words;
            numbers.total.charsWithoutSpaces += charsNoSpaces;
            numbers.total.charsWithSpaces += chars;
            if (!ste.isTranslated()) {
                numbers.remaining.segments++;
                numbers.remaining.words += words;
                numbers.remaining.charsWithoutSpaces += charsNoSpaces;
                numbers.remaining.charsWithSpaces += chars;
            }
        }        
        
        // find unique segments
        Map<String, Integer> uniqueSegment = new HashMap<String, Integer>(
                m_srcTextEntryArray.size() / 2);
        Set<String> translated = new HashSet<String>(
                m_srcTextEntryArray.size() / 2);
        for (SourceTextEntry ste : m_srcTextEntryArray) {
            String src = ste.getStrEntry().getSrcText();
            Integer count = uniqueSegment.get(src);
            if (count == null) {
                uniqueSegment.put(src, 1);
            } else {
                uniqueSegment.put(src, count + 1);
            }
            if (ste.isTranslated()) {
                translated.add(src);
            }
        }
        for (String src : uniqueSegment.keySet()) {
            int words = Statistics.numberOfWords(src);
            String noTags = StaticUtils.stripTags(src);
            int charsNoSpaces = Statistics
                    .numberOfCharactersWithoutSpaces(noTags);

            // add to unique
            unique.segments++;
            unique.words += words;
            unique.charsWithoutSpaces += charsNoSpaces;
            unique.charsWithSpaces += noTags.length();
            // add to unique remaining
            if (!translated.contains(src)) {
                remainingUnique.segments++;
                remainingUnique.words += words;
                remainingUnique.charsWithoutSpaces += charsNoSpaces;
                remainingUnique.charsWithSpaces += noTags.length();
            }
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
        String[][] filesTable = calcFilesTable(m_config, counts);
        result.append(TextUtil.showTextTable(ftHeaders, filesTable, ftAlign));

        if (hotStat != null) {
            hotStat.numberOfSegmentsTotal = total.segments;
            hotStat.numberofTranslatedSegments = translated.size();
            hotStat.numberOfUniqueSegments = unique.segments;
        }
        
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

    protected static String[][] calcFilesTable(final ProjectProperties m_config,
            final Map<String, FileData> counts) {
        String[][] table = new String[counts.size()][7];

        int r = 0;
        for (String filename : counts.keySet()) {
            FileData numbers = counts.get(filename);
            table[r][0] = StaticUtils.makeFilenameRelative(filename, m_config
                    .getSourceRoot());
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
