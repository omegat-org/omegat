/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2012 Thomas Cordonnier
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
 * Thread for calculate match statistics.
 * 
 * Calculation requires two different tags stripping: one for calculate match percentage, and second for
 * calculate number of words and chars.
 * 
 * Number of words/chars calculation requires to just strip all tags, protected parts, placeholders.
 * 
 * Calculation of match percentage requires 2 steps for tags processing: 1) remove only simple XML tags for
 * find 5 nearest matches(but not protected parts' text: from "<m0>IBM</m0>" only tags should be removed, but
 * not "IBM" ), then 2) compute better percentage without any tags removing.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Thomas Cordonnier
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

        final List<SourceTextEntry> untranslatedEntries = new ArrayList<SourceTextEntry>(allEntries.size() / 2);

        // We should iterate all segments from all files in project.
        int percent = 0, treated = 0;
        for (int i = 0; i < allEntries.size(); i++) {
            SourceTextEntry ste = allEntries.get(i);
            String src = ste.getSrcText();

            String srcNoTags = StaticUtils.stripAllTagsFromSource(ste);
            boolean isFirst = alreadyProcessed.add(src);
            if (Core.getProject().getTranslationInfo(ste).isTranslated()) {
                // segment has translation - should be calculated as
                // "Exact matched"
                result[1].segments++;
                /* Number of words and chars calculated without all tags and protected parts. */
                result[1].words += Statistics.numberOfWords(srcNoTags);
                result[1].charsWithoutSpaces += Statistics.numberOfCharactersWithoutSpaces(srcNoTags);
                result[1].charsWithSpaces += Statistics.numberOfCharactersWithSpaces(srcNoTags);
                treated ++;
            }
            else if (!isFirst) {
                // already processed - repetition
                result[0].segments++;
                /* Number of words and chars calculated without all tags and protected parts. */
                result[0].words += Statistics.numberOfWords(srcNoTags);
                result[0].charsWithoutSpaces += Statistics.numberOfCharactersWithoutSpaces(srcNoTags);
                result[0].charsWithSpaces += Statistics.numberOfCharactersWithSpaces(srcNoTags);
                treated ++;
            }
            else {
                untranslatedEntries.add(ste);
            }

            if (isStopped) {
                return;
            }
            int newPercent = treated * 100 / allEntries.size();
            if (percent != newPercent) {
                callback.showProgress(newPercent);
                percent = newPercent;
            }
        }
        
        String[][] table = calcTable(result, 2);
        String outText = TextUtil.showTextTable(header, table, align);

        callback.displayData(outText, false);
        

        /*
         * For the match calculation, we iterates by untranslated entries. Each untranslated entry compared
         * with source texts of: 1) default translations, 2) alternative translations, 3) TMs(from
         * project.getTransMemories()).
         * 
         * We need to find best matches, because "adjustedScore" for non-best matches can be better for some
         * worse "score", what is not so good. It happen because some tags can be repeated many times, or
         * since we are using not so good tokens comparison. Best matches find will produce the same
         * similarity like in patches pane.
         * 
         * Similarity calculates between tokens tokenized by ITokenizer.tokenizeAllExactly() (adjustedScore)
         */
        FindMatches finder = new FindMatches(Core.getProject().getSourceTokenizer(), OConsts.MAX_NEAR_STRINGS, true, false);
        for (SourceTextEntry ste : untranslatedEntries) {
            String srcNoXmlTags = StaticUtils.stripXmlTags(ste.getSrcText());

            List<NearString> nears;
            try {
                nears = finder.search(Core.getProject(), srcNoXmlTags, true, false, new IStopped() {
                    public boolean isStopped() {
                        return isStopped;
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

            String srcNoTags = StaticUtils.stripAllTagsFromSource(ste);
            int row = getRowByPercent(maxSimilarity);
            result[row].segments++;
            /* Number of words and chars calculated without all tags and protected parts. */
            result[row].words += Statistics.numberOfWords(srcNoTags);
            result[row].charsWithoutSpaces += Statistics.numberOfCharactersWithoutSpaces(srcNoTags);
            result[row].charsWithSpaces += Statistics.numberOfCharactersWithSpaces(srcNoTags);
            treated++;

            if (isStopped) {
                return;
            }
            int newPercent = treated * 100 / allEntries.size();
            if (percent != newPercent) {
                callback.showProgress(newPercent);
                percent = newPercent;
            }
        }
        
        
        table = calcTable(result, result.length);
        outText = TextUtil.showTextTable(header, table, align);

        callback.displayData(outText, true);

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
    public String[][] calcTable(final StatCount[] result, final int rowsCount) {
        String[][] table = new String[rowsCount][5];

        // dump result - will be changed for UI
        for (int i = 0; i < rowsCount; i++) {
            table[i][0] = rows[i];
            table[i][1] = Integer.toString(result[i].segments);
            table[i][2] = Integer.toString(result[i].words);
            table[i][3] = Integer.toString(result[i].charsWithoutSpaces);
            table[i][4] = Integer.toString(result[i].charsWithSpaces);
        }
        return table;
    }

}
