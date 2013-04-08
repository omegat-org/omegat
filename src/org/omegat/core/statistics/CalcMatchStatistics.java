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

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.LevenshteinDistance;
import org.omegat.core.matching.FuzzyMatcher;
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
 * @author Thomas Cordonnier
 */
public class CalcMatchStatistics extends LongProcessThread {
    protected static final boolean EXCLUDE_PROTECTED_PARTS = true;

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
        
        final Map<String, Token[]> externalCache = Statistics.buildExternalSourceTexts(tokensCache);
        final List<String> untranslatedEntries = new ArrayList<String>(allEntries.size() / 2);

        // We should iterate all segments from all files in project.
        int percent = 0, treated = 0;
        for (int i = 0; i < allEntries.size(); i++) {
            SourceTextEntry ste = allEntries.get(i);
            String src = ste.getSrcText();
            if (EXCLUDE_PROTECTED_PARTS) {
                src = StaticUtils.stripProtectedParts(src, ste);
            }

            boolean isFirst = alreadyProcessed.add(src);
            if (Core.getProject().getTranslationInfo(ste).isTranslated()) {
                // segment has translation - should be calculated as
                // "Exact matched"
                result[1].segments++;
                result[1].words += Statistics.numberOfWords(src);
                result[1].charsWithoutSpaces += Statistics.numberOfCharactersWithoutSpaces(src);
                result[1].charsWithSpaces += Statistics.numberOfCharactersWithSpaces(src);
                treated ++;
            }
            else if (!isFirst) {
                // already processed - repetition
                result[0].segments++;
                result[0].words += Statistics.numberOfWords(ste.getSrcText());
                result[0].charsWithoutSpaces += Statistics.numberOfCharactersWithoutSpaces(src);
                result[0].charsWithSpaces += Statistics.numberOfCharactersWithSpaces(src);
                treated ++;
            }
            else {
                untranslatedEntries.add(src);
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
        

        for (int i = 0; i < untranslatedEntries.size(); i++) {
            String source = untranslatedEntries.get(i);
            final Token[] strTokensStem = Statistics.tokenizeExactlyWithCache(tokensCache, source);
                        
            int maxSimilarity = 0;
        CACHE:
            for (Token[] candTokens: externalCache.values()) {
                int newSimilarity = FuzzyMatcher.calcSimilarity(distanceCalculator, strTokensStem, candTokens);
                if (newSimilarity > maxSimilarity) {
                    maxSimilarity = newSimilarity;
                    if (newSimilarity >= 95) // enough to say that we are in row 2
                        break CACHE;
                }
            }

            int row = getRowByPercent(maxSimilarity);
            result[row].segments++;
            result[row].words += Statistics.numberOfWords(source);
            result[row].charsWithoutSpaces += Statistics.numberOfCharactersWithoutSpaces(source);
            result[row].charsWithSpaces += Statistics.numberOfCharactersWithSpaces(source);
            treated ++;

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
