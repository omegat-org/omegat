/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2012 Thomas Cordonnier
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IStopped;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.LevenshteinDistance;
import org.omegat.core.matching.NearString;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.gui.stat.StatisticsWindow;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;
import org.omegat.util.gui.TextUtil;

/**
 * Thread for calculate match statistics, total and per file.
 * 
 * Calculation requires two different tags stripping: one for calculate match percentage, and second for
 * calculate number of words and chars.
 * 
 * Number of words/chars calculation requires to just strip all tags, protected parts, placeholders(see
 * StatCount.java).
 * 
 * Calculation of match percentage requires 2 steps for tags processing: 1) remove only simple XML tags for
 * find 5 nearest matches(but not protected parts' text: from "<m0>Acme</m0>" only tags should be removed, but
 * not "Acme" ), then 2) compute better percentage without any tags removing.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Thomas Cordonnier
 */
public class CalcMatchStatistics extends LongProcessThread {
    private String[] header = new String[] { "", OStrings.getString("CT_STATS_Segments"),
            OStrings.getString("CT_STATS_Words"), OStrings.getString("CT_STATS_Characters_NOSP"),
            OStrings.getString("CT_STATS_Characters") };

    private String[] rowsTotal = new String[] { OStrings.getString("CT_STATSMATCH_RowRepetitions"),
            OStrings.getString("CT_STATSMATCH_RowExactMatch"),
            OStrings.getString("CT_STATSMATCH_RowMatch95"), OStrings.getString("CT_STATSMATCH_RowMatch85"),
            OStrings.getString("CT_STATSMATCH_RowMatch75"), OStrings.getString("CT_STATSMATCH_RowMatch50"),
            OStrings.getString("CT_STATSMATCH_RowNoMatch"), OStrings.getString("CT_STATSMATCH_Total") };
    private String[] rowsPerFile = new String[] {
            OStrings.getString("CT_STATSMATCH_RowRepetitionsWithinThisFile"),
            OStrings.getString("CT_STATSMATCH_RowRepetitionsFromOtherFiles"),
            OStrings.getString("CT_STATSMATCH_RowExactMatch"),
            OStrings.getString("CT_STATSMATCH_RowMatch95"), OStrings.getString("CT_STATSMATCH_RowMatch85"),
            OStrings.getString("CT_STATSMATCH_RowMatch75"), OStrings.getString("CT_STATSMATCH_RowMatch50"),
            OStrings.getString("CT_STATSMATCH_RowNoMatch"), OStrings.getString("CT_STATSMATCH_Total") };
    private boolean[] align = new boolean[] { false, true, true, true, true };

    private final StatisticsWindow callback;
    private final boolean perFile;
    private int entriesToProcess;

    /** Already processed segments. Used for repetitions detect. */
    private Set<String> alreadyProcessedInFile = new HashSet<String>();
    private Set<String> alreadyProcessedInProject = new HashSet<String>();

    private ISimilarityCalculator distanceCalculator;
    private FindMatches finder;

    public CalcMatchStatistics(StatisticsWindow callback, boolean perFile) {
        this.callback = callback;
        this.perFile = perFile;
    }

    public void run() {
        try {
            finder = new FindMatches(Core.getProject().getSourceTokenizer(), OConsts.MAX_NEAR_STRINGS, true,
                    false);
            distanceCalculator = new LevenshteinDistance();
            if (perFile) {
                entriesToProcess = Core.getProject().getAllEntries().size() * 2;
                calcPerFile();
            } else {
                entriesToProcess = Core.getProject().getAllEntries().size();
                calcTotal(true);
            }
        } catch (InterruptedException ex) {
        }
    }

    void calcPerFile() throws InterruptedException {
        for (IProject.FileInfo fi : Core.getProject().getProjectFiles()) {
            MatchStatCounts perFile = forFile(fi);
            checkInterrupted();

            String[][] table = perFile.calcTable(rowsPerFile);
            String outText = TextUtil.showTextTable(header, table, align);
            callback.appendData(StaticUtils.format(OStrings.getString("CT_STATSMATCH_File"), fi.filePath)
                    + "\n");
            callback.appendData(outText + "\n");
        }

        MatchStatCounts total = calcTotal(false);

        callback.appendData(OStrings.getString("CT_STATSMATCH_FileTotal") + "\n");
        String[][] table = total.calcTable(rowsTotal);
        String outText = TextUtil.showTextTable(header, table, align);
        callback.appendData(outText + "\n");

        String text = callback.finishData();
        String fn = Core.getProject().getProjectProperties().getProjectInternal()
                + OConsts.STATS_MATCH_PER_FILE_FILENAME;
        Statistics.writeStat(fn, text);
    }

    MatchStatCounts calcTotal(boolean outData) throws InterruptedException {
        MatchStatCounts result = new MatchStatCounts(true);
        alreadyProcessedInProject.clear();

        final List<SourceTextEntry> untranslatedEntries = new ArrayList<SourceTextEntry>();

        // We should iterate all segments from all files in project.
        for (SourceTextEntry ste : Core.getProject().getAllEntries()) {
            checkInterrupted();
            StatCount count = new StatCount(ste);
            boolean isFirst = alreadyProcessedInProject.add(ste.getSrcText());
            if (Core.getProject().getTranslationInfo(ste).isTranslated()) {
                // segment has translation - should be calculated as "Exact matched"
                result.addExact(count);
                entryProcessed();
            } else if (isFirst) {
                untranslatedEntries.add(ste);
            } else {
                // already processed - repetition
                result.addRepetition(count);
                entryProcessed();
            }
        }

        if (outData) {
            String[][] table = result.calcTableWithoutPercentage(rowsTotal);
            String outText = TextUtil.showTextTable(header, table, align);
            callback.displayData(outText, false);
        }

        calcSimilarity(untranslatedEntries, result);

        if (outData) {
            String[][] table = result.calcTable(rowsTotal);
            String outText = TextUtil.showTextTable(header, table, align);
            callback.displayData(outText, true);
            String fn = Core.getProject().getProjectProperties().getProjectInternal()
                    + OConsts.STATS_MATCH_FILENAME;
            Statistics.writeStat(fn, outText);
        }

        return result;
    }

    MatchStatCounts forFile(IProject.FileInfo fi) throws InterruptedException {
        MatchStatCounts result = new MatchStatCounts(false);
        alreadyProcessedInFile.clear();

        final List<SourceTextEntry> untranslatedEntries = new ArrayList<SourceTextEntry>();

        // We should iterate all segments from file.
        for (SourceTextEntry ste : fi.entries) {
            checkInterrupted();
            StatCount count = new StatCount(ste);
            boolean isFirstInFile = alreadyProcessedInFile.add(ste.getSrcText());
            boolean isFirstInProject = alreadyProcessedInProject.add(ste.getSrcText());
            if (Core.getProject().getTranslationInfo(ste).isTranslated()) {
                // segment has translation - should be calculated as
                // "Exact matched"
                result.addExact(count);
                treated++;
            } else if (isFirstInProject) {
                untranslatedEntries.add(ste);
            } else {
                // isFirstInProject==false
                if (isFirstInFile) {
                    // exist in other file
                    result.addRepetitionFromOtherFiles(count);
                } else {
                    result.addRepetitionWithinThisFile(count);
                }
            }
        }

        calcSimilarity(untranslatedEntries, result);

        return result;
    }

    /**
     * For the match calculation, we iterates by untranslated entries. Each untranslated entry compared with
     * source texts of: 1) default translations, 2) alternative translations, 3) TMs(from
     * project.getTransMemories()).
     * 
     * We need to find best matches, because "adjustedScore" for non-best matches can be better for some worse
     * "score", what is not so good. It happen because some tags can be repeated many times, or since we are
     * using not so good tokens comparison. Best matches find will produce the same similarity like in patches
     * pane.
     * 
     * Similarity calculates between tokens tokenized by ITokenizer.tokenizeAllExactly() (adjustedScore)
     */
    void calcSimilarity(List<SourceTextEntry> untranslatedEntries, MatchStatCounts counts)
            throws InterruptedException {
        for (SourceTextEntry ste : untranslatedEntries) {
            checkInterrupted();
            String srcNoXmlTags = ste.getSrcText();
            for (ProtectedPart pp : ste.getProtectedParts()) {
                srcNoXmlTags = srcNoXmlTags.replace(pp.getTextInSourceSegment(),
                        pp.getReplacementMatchCalculation());
            }

            List<NearString> nears;
            try {
                nears = finder.search(Core.getProject(), srcNoXmlTags, true, false, new IStopped() {
                    public boolean isStopped() {
                        return isInterrupted();
                    }
                });
            } catch (FindMatches.StoppedException ex) {
                return;
            }

            final Token[] strTokensStem = finder.tokenizeAll(ste.getSrcText());
            int maxSimilarity = 0;
            CACHE: for (NearString near : nears) {
                final Token[] candTokens = finder.tokenizeAll(near.source);
                int newSimilarity = FuzzyMatcher
                        .calcSimilarity(distanceCalculator, strTokensStem, candTokens);
                if (newSimilarity > maxSimilarity) {
                    maxSimilarity = newSimilarity;
                    if (newSimilarity >= 95) // enough to say that we are in row 2
                        break CACHE;
                }
            }

            StatCount count = new StatCount(ste);
            counts.addForPercents(maxSimilarity, count);

            entryProcessed();
        }
    }

    int treated, percent;

    void entryProcessed() {
        treated++;
        int newPercent = treated * 100 / entriesToProcess;
        if (percent != newPercent) {
            callback.showProgress(newPercent);
            percent = newPercent;
        }
    }
}
