/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2010 Arno Peters
               2013-2014 Alex Buloichik
               2015 Aaron Madlon-Kay
               2020 Vladimir Bychkov
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
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.gui.stat.StatisticsPanel;
import org.omegat.util.OConsts;

/**
 * Thread for calculate standard statistics.
 *
 * Calculation requires two different tags stripping: one for calculate unique
 * and remaining, and second for calculate number of words and chars.
 *
 * Number of words/chars calculation requires to just strip all tags, protected
 * parts, placeholders(see StatCount.java).
 *
 * Calculation of unique and remaining also requires to just strip all tags,
 * protected parts, placeholders for standard calculation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Arno Peters
 * @author Aaron Madlon-Kay
 */
public class CalcStandardStatistics extends LongProcessThread {

    private final StatisticsPanel callback;

    public CalcStandardStatistics(StatisticsPanel callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        IProject p = Core.getProject();
        StatsResult result = buildProjectStats(p);
        callback.setProjectTableData(StatsResult.HT_HEADERS, result.getHeaderTable());
        callback.setFilesTableData(StatsResult.FT_HEADERS, result.getFilesTable());
        callback.setTextData(result.getTextData());
        callback.finishData();

        String internalDir = p.getProjectProperties().getProjectInternal();
        // removing old stats
        try {
            File oldstats = new File(internalDir + "word_counts");
            if (oldstats.exists()) {
                oldstats.delete();
            }
        } catch (Exception e) {
        }

        // now dump file based word counts to disk
        String fn = internalDir + OConsts.STATS_FILENAME;
        Statistics.writeStat(internalDir, result);
        callback.setDataFile(fn);
    }

    /**
     * Builds a file with statistic info about the project. The total word &mp;
     * character count of the project, the total number of unique segments, plus
     * the details for each file.
     */
    public static StatsResult buildProjectStats(final IProject project) {

        StatCount total = new StatCount();
        StatCount remaining = new StatCount();
        StatCount unique = new StatCount();
        StatCount remainingUnique = new StatCount();

        // find unique segments
        Map<String, SourceTextEntry> uniqueSegment = new HashMap<String, SourceTextEntry>();
        Set<String> translated = new HashSet<String>();
        for (SourceTextEntry ste : project.getAllEntries()) {
            String src = ste.getSrcText();
            for (ProtectedPart pp : ste.getProtectedParts()) {
                src = src.replace(pp.getTextInSourceSegment(), pp.getReplacementUniquenessCalculation());
            }
            if (!uniqueSegment.containsKey(src)) {
                uniqueSegment.put(src, ste);
            }
            TMXEntry tr = project.getTranslationInfo(ste);
            if (tr.isTranslated()) {
                translated.add(src);
            }
        }
        Set<String> filesUnique = new HashSet<String>();
        Set<String> filesRemainingUnique = new HashSet<String>();
        for (Map.Entry<String, SourceTextEntry> en : uniqueSegment.entrySet()) {
            /*
             * Number of words and chars calculated without all tags and
             * protected parts.
             */
            StatCount count = new StatCount(en.getValue());

            // add to unique
            unique.add(count);
            filesUnique.add(en.getValue().getKey().file);
            // add to unique remaining
            if (!translated.contains(en.getKey())) {
                remainingUnique.add(count);
                filesRemainingUnique.add(en.getValue().getKey().file);
            }
        }
        unique.addFiles(filesUnique.size());
        remainingUnique.addFiles(filesRemainingUnique.size());

        List<FileData> counts = new ArrayList<FileData>();
        Map<String, Boolean> firstSeenUniqueSegment = new HashMap<String, Boolean>();
        for (FileInfo file : project.getProjectFiles()) {
            FileData numbers = new FileData();
            numbers.filename = file.filePath;
            counts.add(numbers);
            int fileTotal = 0;
            int fileRemaining = 0;
            for (SourceTextEntry ste : file.entries) {
                String src = ste.getSrcText();
                for (ProtectedPart pp : ste.getProtectedParts()) {
                    src = src.replace(pp.getTextInSourceSegment(), pp.getReplacementUniquenessCalculation());
                }

                /*
                 * Number of words and chars calculated without all tags and
                 * protected parts.
                 */
                StatCount count = new StatCount(ste);

                // add to total
                total.add(count);
                fileTotal = 1;

                // add to remaining
                TMXEntry tr = project.getTranslationInfo(ste);
                if (!tr.isTranslated()) {
                    remaining.add(count);
                    fileRemaining = 1;
                }

                // add to file's info
                numbers.total.add(count);

                Boolean firstSeen = firstSeenUniqueSegment.get(src);
                if (firstSeen == null) {
                    firstSeenUniqueSegment.put(src, false);
                    numbers.unique.add(count);

                    if (!tr.isTranslated()) {
                        numbers.remainingUnique.add(count);
                    }
                }

                if (!tr.isTranslated()) {
                    numbers.remaining.add(count);
                }
            }
            total.addFiles(fileTotal);
            remaining.addFiles(fileRemaining);
        }

        return new StatsResult(total, remaining, unique, remainingUnique, translated, counts);
    }
}
