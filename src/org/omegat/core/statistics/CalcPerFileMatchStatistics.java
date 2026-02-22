/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2012 Thomas Cordonnier
               2013 Alex Buloichik
               2015 Aaron Madlon-Kay
               2024-2026 Hiroshi Miura
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

import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.statistics.FindMatches.StoppedException;
import org.omegat.core.threads.CancellationToken;
import org.omegat.core.threads.LongProcessInterruptedException;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Thread for calculate match statistics, per file.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Thomas Cordonnier
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class CalcPerFileMatchStatistics extends CalcMatchStatistics implements ICalcStatistics {

    /** Already processed segments. Used for repetitions detect. */
    private final Set<String> alreadyProcessedInFile = new HashSet<>();

    public CalcPerFileMatchStatistics(IStatsConsumer callback) {
        super(callback);
    }

    public CalcPerFileMatchStatistics(IProject project, Segmenter segmenter, IStatsConsumer callback) {
        super(project, segmenter, callback);
    }

    @Override
    public Void run(CancellationToken token) throws StoppedException, LongProcessInterruptedException {
        cancellationToken = token;
        entriesToProcess = getEntrySize() * 2;
        calcPerFile();
        finishData();
        return null;
    }

    private void calcPerFile() {
        int fileNumber = 0;
        for (IProject.FileInfo fi : project.getProjectFiles()) {
            fileNumber++;

            MatchStatCounts perFileCounts = forFile(fi);
            cancellationToken.throwIfCancelled();
            String title = StringUtil.format(OStrings.getString("CT_STATSMATCH_File"), fileNumber, fi.filePath);
            showTextTable(title, perFileCounts, i -> true, true);
        }

        MatchStatCounts total = calcTotal(false);
        String title = OStrings.getString("CT_STATSMATCH_FileTotal");
        showTextTable(title, total, i -> i != 1, false);
        String fn = project.getProjectProperties().getProjectInternal()
                + OConsts.STATS_MATCH_PER_FILE_FILENAME;
        writeLog(fn);
        callback.setDataFile(fn);
    }

    private MatchStatCounts forFile(IProject.FileInfo fi) {
        MatchStatCounts result = new MatchStatCounts();
        alreadyProcessedInFile.clear();

        final List<SourceTextEntry> untranslatedEntries = new ArrayList<>();

        // We should iterate all segments from file.
        for (SourceTextEntry ste : fi.entries) {
            cancellationToken.throwIfCancelled();
            StatCount count = new StatCount(ste);
            boolean existInFile = isEntryProcessedInFile(ste.getSrcText());
            boolean existInPreviousFiles = isEntryProcessed(ste.getSrcText());
            if (project.getTranslationInfo(ste).isTranslated()) {
                // segment has translation - should be calculated as
                // "Exact matched"
                result.addExact(count);
                entryProcessed();
            } else if (existInPreviousFiles) {
                // exist in other file
                result.addRepetitionFromOtherFiles(count);
                entryProcessed();
            } else if (existInFile) {
                // exist in this file
                result.addRepetitionWithinThisFile(count);
                entryProcessed();
            } else {
                // first time
                untranslatedEntries.add(ste);
                alreadyProcessedInFile.add(ste.getSrcText());
            }
        }
        addEntryProcessed(alreadyProcessedInFile);

        calcSimilarity(untranslatedEntries).ifPresent(result::addCounts);

        return result;
    }

    private boolean isEntryProcessedInFile(String srcText) {
        return alreadyProcessedInFile.contains(srcText);
    }
}
