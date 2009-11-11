/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
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

package org.omegat.gui.matches;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringEntry;
import org.omegat.core.data.TransEntry;
import org.omegat.core.data.TransMemory;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.ITokenizer;
import org.omegat.core.matching.LevenshteinDistance;
import org.omegat.core.matching.NearString;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Token;

/**
 * Class to find matches and show result in the matches pane.
 * 
 * Since we can use stemmers to prepare tokens, we should use 3-pass comparison
 * of similarity. Similarity will be calculated in 3 steps:
 * 
 * 1. Split original segment into word-only tokens using stemmer (with stop words
 * list), then compare tokens.
 * 
 * 2. Split original segment into word-only tokens without stemmer, then compare
 * tokens.
 * 
 * 3. Split original segment into not-only-words tokens (including numbers and
 * tags) without stemmer, then compare tokens.
 * 
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class FindMatchesThread extends Thread {
    private static final Logger LOGGER = Logger
            .getLogger(FindMatchesThread.class.getName());
    
    private final MatchesTextArea matcherController;

    /**
     * Entry which is processed currently.
     * 
     * If entry in controller was changed, it means user has moved to another 
     * entry, and there is no sense to continue.
     */
    private final StringEntry processedEntry;

    /** Result list. */
    private List<NearString> result = new ArrayList<NearString>(OConsts.MAX_NEAR_STRINGS + 1);
    
    private ISimilarityCalculator distance = new LevenshteinDistance();

    /** Tokens for original string, with and without stems. */
    private Token[] strTokensStem, strTokensNoStem;
    
    /** Tokens for original string, includes numbers and tags. */
    private Token[] strTokensAll;

    public FindMatchesThread(final MatchesTextArea matcherController, final StringEntry entry) {
        this.matcherController = matcherController;
        this.processedEntry = entry;
    }

    @Override
    public void run() {
        final List<SourceTextEntry> entries = Core.getProject().getAllEntries();
        Map<String, TransEntry> translations = Core.getProject().getTranslations();
        Map<String, TransEntry> orphaned = Core.getProject()
                .getOrphanedSegments();
        Map<String, List<TransMemory>> memories = Core.getProject()
                .getTransMemories();
        if (entries == null || memories == null || orphaned == null) {
            // project is closed
            clear();
            return;
        }

        long before = 0;
        if (LOGGER.isLoggable(Level.FINER)) {
            // only if need to be logged
            before = System.currentTimeMillis();
        }

        // get tokens for original string
        strTokensStem = Core.getTokenizer().tokenizeWords(
                processedEntry.getSrcText(), ITokenizer.StemmingMode.MATCHING);
        if (strTokensStem.length == 0) {
            clear();
            return;
            // HP: maybe also test on strTokensComplete.size(), if strTokensSize is 0
            // HP: perhaps that would result in better number/non-word matching too
        }
        strTokensNoStem = Core.getTokenizer().tokenizeWords(
                processedEntry.getSrcText(), ITokenizer.StemmingMode.NONE);
        strTokensAll = Core.getTokenizer().tokenizeAllExactly(processedEntry.getSrcText());// HP: includes non-word tokens

        // travel by project entries
        for (Map.Entry<String, TransEntry> en : translations.entrySet()) {
            if (needStop()) {
                return;
            }
            if (en.getKey().equals(processedEntry.getSrcText())) {
                // skip original==original entry comparison
                continue;
            }
            processEntry(en.getKey(), en.getValue().translation, null);
        }

        // travel by orphaned
        String file = OStrings.getString("CT_ORPHAN_STRINGS");
        for (Map.Entry<String, TransEntry> en : orphaned.entrySet()) {
            if (needStop()) {
                return;
            }
            processEntry(en.getKey(), en.getValue().translation, file);
        }
        
        // travel by translation memories
        for(Map.Entry<String, List<TransMemory>> en:memories.entrySet()) {
            for(TransMemory tmen:en.getValue()) {
                if (needStop()) {
                    return;
                }
                processEntry(tmen.source, tmen.target, en.getKey());
            }
        }

        // fill similarity data only for result
        for (NearString near : result) {
            // fix for bug 1586397
            byte[] similarityData = FuzzyMatcher.buildSimilarityData(
                    strTokensAll, Core.getTokenizer().tokenizeAllExactly(
                            near.source));
            near.attr = similarityData;
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            // only if need to be logged
            long after = System.currentTimeMillis();
            LOGGER.finer("Time for find matches: " + (after - before));
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!needStop()) {
                    matcherController.setMatches(result);
                }
            }
        });
    }
    
    /**
     * Check if processed entry changed. In this case, we don't need to find and
     * display data for old entry.
     * 
     * @return true if need stop to find
     */
    private boolean needStop() {
        return matcherController.processedEntry != processedEntry;
    }
    
    /**
     * Clear result window.
     */
    private void clear() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (matcherController.processedEntry == processedEntry) {
                    matcherController.clear();
                }
            }
        });
    }

    /**
     * Compare one entry with original entry.
     * 
     * @param candEntry
     *                entry to compare
     */
    protected void processEntry(final String source, final String translation,
            final String tmxName) {
        Token[] candTokens = Core.getTokenizer().tokenizeWords(source,
                ITokenizer.StemmingMode.MATCHING);
        if (candTokens.length == 0) {
            return;
        }

        int similarityStem = FuzzyMatcher.calcSimilarity(distance, strTokensStem, candTokens);

        if (similarityStem < OConsts.FUZZY_MATCH_THRESHOLD)
            return;

        Token[] candTokensNoStem = Core.getTokenizer().tokenizeWords(
                source, ITokenizer.StemmingMode.NONE);
        int similarityNoStem = FuzzyMatcher.calcSimilarity(distance, strTokensNoStem, candTokensNoStem);

        if (haveChanceToAdd(similarityStem, similarityNoStem)) {
            Token[] candTokensAll = Core.getTokenizer().tokenizeAllExactly(
                    source);
            int simAdjusted = FuzzyMatcher.calcSimilarity(distance, strTokensAll, candTokensAll);

            addNearString(source, translation, similarityStem,
                    similarityNoStem, simAdjusted, null, tmxName);
        }
    }

    /**
     * Check if entry have a chance to be added to result list. 
     * If no, there is no sense to calculate other parameters.
     * 
     * @param similarity
     *                calculate similarity
     * @return true if additional calculation need
     */
    protected boolean haveChanceToAdd(final int similarity, final int similarityNoStem) {
        if (result.size() < OConsts.MAX_NEAR_STRINGS) {
            return true;
        }
        NearString st = result.get(result.size() - 1);
        if (st.score < similarity) {
            return true;
        } else if (st.score>similarity) {
            return false;
        }else {
            return st.scoreNoStem <= similarityNoStem;
        }
    }

    /**
     * Add near string into result list. 
     * Near strings sorted by "similarity,simAdjusted"
     */
    protected void addNearString(final String source, final String translation,
            final int similarity, final int similarityNoStem,
            final int simAdjusted, final byte[] similarityData,
            final String tmxName) {
        // find position for new data
        int pos = 0;
        for (int i = 0; i < result.size(); i++) {
            NearString st = result.get(i);
            if (tmxName==null && st.proj.length()==0 && source.equals(st.source)) {
                // the same source text already in list - don't need to add
                // only if they are from translations 
                return;
            }
            if (st.score < similarity) {
                break;
            }
            if (st.score == similarity) {
                if (st.scoreNoStem < similarityNoStem) {
                    break;
                }
                if (st.scoreNoStem == similarityNoStem) {
                    if (st.adjustedScore < simAdjusted) {
                        break;
                    }
                }
            }
            pos = i + 1;
        }

        result.add(pos, new NearString(source, translation, similarity,
                similarityNoStem, simAdjusted, similarityData, tmxName));
        if (result.size() > OConsts.MAX_NEAR_STRINGS) {
            result.remove(result.size() - 1);
        }
    }
}
