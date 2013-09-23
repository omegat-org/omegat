/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2010 Arno Peters
               2013 Alex Buloichik
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.gui.stat.StatisticsWindow;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.TextUtil;

/**
 * Thread for calculate standard statistics.
 * 
 * Calculation requires two different tags stripping: one for calculate unique and remaining, and second for
 * calculate number of words and chars.
 * 
 * Number of words/chars calculation requires to just strip all tags, protected parts, placeholders.
 * 
 * Calculation of unique and remaining also requires to just strip all tags, protected parts, placeholders for
 * standard calculation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Arno Peters
 */
public class CalcStandardStatistics extends LongProcessThread {
    private static final String[] htHeaders = new String[] { "", OStrings.getString("CT_STATS_Segments"),
            OStrings.getString("CT_STATS_Words"), OStrings.getString("CT_STATS_Characters_NOSP"),
            OStrings.getString("CT_STATS_Characters") };

    private static final String[] htRows = new String[] { OStrings.getString("CT_STATS_Total"),
            OStrings.getString("CT_STATS_Remaining"), OStrings.getString("CT_STATS_Unique"),
            OStrings.getString("CT_STATS_Unique_Remaining") };
    private static final boolean[] htAlign = new boolean[] { false, true, true, true, true };

    private static final String[] ftHeaders = new String[] { OStrings.getString("CT_STATS_FILE_Name"),
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
            OStrings.getString("CT_STATS_FILE_Unique_Remaining_Characters"), };

    private static final boolean[] ftAlign = new boolean[] { false, true, true, true, true, true, true, true,
            true, true, true, true, true, true, true, true, true, };

    private StatisticsWindow callback;

    public CalcStandardStatistics(StatisticsWindow callback) {
        this.callback = callback;
    }

    public void run() {
        IProject p = Core.getProject();
        String result = buildProjectStats(p, null);
        callback.displayData(result, true);

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
    public static String buildProjectStats(final IProject project, final StatisticsInfo hotStat) {

        StatCount total = new StatCount();
        StatCount remaining = new StatCount();
        StatCount unique = new StatCount();
        StatCount remainingUnique = new StatCount();

        // find unique segments
        Map<String, Integer> uniqueSegment = new HashMap<String, Integer>();
        Set<String> translated = new HashSet<String>();
        for (SourceTextEntry ste : project.getAllEntries()) {
            String src = StaticUtils.stripAllTagsFromSource(ste);
            Integer count = uniqueSegment.get(src);
            if (count == null) {
                uniqueSegment.put(src, 1);
            } else {
                uniqueSegment.put(src, count + 1);
            }
            TMXEntry tr = project.getTranslationInfo(ste);
            if (tr.isTranslated()) {
                translated.add(src);
            }
        }
        for (String noTags : uniqueSegment.keySet()) {
            /* Number of words and chars calculated without all tags and protected parts. */
            int words = Statistics.numberOfWords(noTags);
            int charsNoSpaces = Statistics.numberOfCharactersWithoutSpaces(noTags);
            int charsWithSpaces = Statistics.numberOfCharactersWithSpaces(noTags);

            // add to unique
            unique.segments++;
            unique.words += words;
            unique.charsWithoutSpaces += charsNoSpaces;
            unique.charsWithSpaces += charsWithSpaces;
            // add to unique remaining
            if (!translated.contains(noTags)) {
                remainingUnique.segments++;
                remainingUnique.words += words;
                remainingUnique.charsWithoutSpaces += charsNoSpaces;
                remainingUnique.charsWithSpaces += charsWithSpaces;
            }
        }

        List<FileData> counts = new ArrayList<FileData>();
        Map<String, Boolean> firstSeenUniqueSegment = new HashMap<String, Boolean>();
        for (FileInfo file : project.getProjectFiles()) {
            FileData numbers = new FileData();
            numbers.filename = file.filePath;
            counts.add(numbers);
            for (SourceTextEntry ste : file.entries) {
                String srcNoTags = StaticUtils.stripAllTagsFromSource(ste);

                /* Number of words and chars calculated without all tags and protected parts. */
                int words = Statistics.numberOfWords(srcNoTags);
                int charsNoSpaces = Statistics.numberOfCharactersWithoutSpaces(srcNoTags);
                int chars = Statistics.numberOfCharactersWithSpaces(srcNoTags);

                // add to total
                total.segments++;
                total.words += words;
                total.charsWithoutSpaces += charsNoSpaces;
                total.charsWithSpaces += chars;

                // add to remaining
                TMXEntry tr = project.getTranslationInfo(ste);
                if (!tr.isTranslated()) {
                    remaining.segments++;
                    remaining.words += words;
                    remaining.charsWithoutSpaces += charsNoSpaces;
                    remaining.charsWithSpaces += chars;
                }

                // add to file's info
                numbers.total.segments++;
                numbers.total.words += words;
                numbers.total.charsWithoutSpaces += charsNoSpaces;
                numbers.total.charsWithSpaces += chars;

                Integer uniqueCount = uniqueSegment.get(srcNoTags);
                Boolean firstSeen = firstSeenUniqueSegment.get(srcNoTags);
                if (firstSeen == null) {
                    firstSeenUniqueSegment.put(srcNoTags, false);
                    numbers.unique.segments++;
                    numbers.unique.words += words;
                    numbers.unique.charsWithoutSpaces += charsNoSpaces;
                    numbers.unique.charsWithSpaces += chars;

                    if (!tr.isTranslated()) {
                        numbers.remainingUnique.segments++;
                        numbers.remainingUnique.words += words;
                        numbers.remainingUnique.charsWithoutSpaces += charsNoSpaces;
                        numbers.remainingUnique.charsWithSpaces += chars;
                    }
                }

                if (!tr.isTranslated()) {
                    numbers.remaining.segments++;
                    numbers.remaining.words += words;
                    numbers.remaining.charsWithoutSpaces += charsNoSpaces;
                    numbers.remaining.charsWithSpaces += chars;
                }
            }
        }

        StringBuilder result = new StringBuilder();

        result.append(OStrings.getString("CT_STATS_Project_Statistics") + "\n\n");

        String[][] headerTable = calcHeaderTable(new StatCount[] { total, remaining, unique, remainingUnique });
        result.append(TextUtil.showTextTable(htHeaders, headerTable, htAlign));
        result.append("\n\n");

        // STATISTICS BY FILE
        result.append(OStrings.getString("CT_STATS_FILE_Statistics") + "\n\n");
        String[][] filesTable = calcFilesTable(project.getProjectProperties(), counts);
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

    protected static String[][] calcFilesTable(final ProjectProperties m_config, final List<FileData> counts) {
        String[][] table = new String[counts.size()][17];

        int r = 0;
        for (FileData numbers : counts) {
            table[r][0] = StaticUtils.makeFilenameRelative(numbers.filename, m_config.getSourceRoot());
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

    public static class FileData {
        public String filename;
        public StatCount total, unique, remaining, remainingUnique;

        public FileData() {
            total = new StatCount();
            unique = new StatCount();
            remaining = new StatCount();
            remainingUnique = new StatCount();
        }
    }
}
