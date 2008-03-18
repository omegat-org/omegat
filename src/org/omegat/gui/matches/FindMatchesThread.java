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

import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.core.data.LegacyTM;
import org.omegat.core.data.StringEntry;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.LevenshteinDistance;
import org.omegat.core.matching.NearString;
import org.omegat.util.OConsts;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;

/**
 * Class for find matches and show result in the matches pane.
 * 
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class FindMatchesThread extends Thread {
    private final MatchesTextArea matcherController;

    /**
     * Entry which processed currently.
     * 
     * If entry in controller was changed, it means user was moved to other entry, and there is no sense to continue.
     */
    private final StringEntry processedEntry;

    /** Result list. */
    private List<NearString> result = new ArrayList<NearString>(OConsts.MAX_NEAR_STRINGS + 1);

    /** Tokens for original string. */
    private Token[] strTokens;
    /** Tokens for original string. */
    private Token[] strTokensAll;

    public FindMatchesThread(final MatchesTextArea matcherController, final StringEntry entry) {
        this.matcherController = matcherController;
        this.processedEntry = entry;
    }

    @Override
    public void run() {
        final List<StringEntry> entries = Core.getDataEngine().getAllEntries();
        final List<LegacyTM> memory = Core.getDataEngine().getMemory();
        if (entries == null || memory == null) {
            // project is closed
            return;
        }

        long before = System.currentTimeMillis();

        // get tokens for original string
        strTokens = processedEntry.getSrcTokenList();
        if (strTokens.length == 0) {
            return;
            // HP: maybe also test on strTokensComplete.size(), if strTokensSize is 0
            // HP: perhaps that would result in better number/non-word matching too
        }
        strTokensAll = processedEntry.getSrcTokenListAll();// HP: includes non-word tokens

        // travel by project entries
        for (StringEntry candEntry : entries) {
            if (matcherController.processedEntry != processedEntry) {
                // Processed entry changed, because user moved to other entry.
                // I.e. we don't need to find and display data for old entry.
                return;
            }
            if (StringUtil.isEmpty(candEntry.getTranslation())) {
                continue;
            }
            processEntry(candEntry, null);
        }

        // travel by translation memories
        for (LegacyTM mem : memory) {
            for (StringEntry candEntry : mem.getStrings()) {
                if (matcherController.processedEntry != processedEntry) {
                    // Processed entry changed, because user moved to other entry.
                    // I.e. we don't need to find and display data for old entry.
                    return;
                }
                if (StringUtil.isEmpty(candEntry.getTranslation())) {
                    continue;
                }
                processEntry(candEntry, mem.getName());
            }
        }

        // fill similarity data only for result
        for (NearString near : result) {
            // fix for bug 1586397
            byte[] similarityData = FuzzyMatcher.buildSimilarityData(strTokensAll, near.str.getSrcTokenListAll());
            near.attr = similarityData;
        }

        long after = System.currentTimeMillis();
        System.out.println("Time for find matches: " + (after - before));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (matcherController.processedEntry == processedEntry) {
                    matcherController.setMatches(result);
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
    protected void processEntry(final StringEntry candEntry, final String tmxName) {
        Token[] candTokens = candEntry.getSrcTokenList();
        if (candTokens.length == 0) {
            return;
        }

        int ld = LevenshteinDistance.compute(strTokens, candTokens);
        int similarity = (100 * (Math.max(strTokens.length, candTokens.length) - ld))
                / Math.max(strTokens.length, candTokens.length);

        if (similarity < OConsts.FUZZY_MATCH_THRESHOLD)
            return;

        if (haveChanceToAdd(similarity)) {
            Token[] candTokensAll = candEntry.getSrcTokenListAll();
            int ldAll = LevenshteinDistance.compute(strTokensAll, candTokensAll);
            int simAdjusted = (100 * (Math.max(strTokensAll.length, candTokensAll.length) - ldAll))
                    / Math.max(strTokensAll.length, candTokensAll.length);

            addNearString(candEntry, similarity, simAdjusted, null, tmxName);
        }
    }

    /**
     * Check if entry have a chance to be added to result list. If no, there is no sense to calculate other parameters.
     * 
     * @param similarity
     *                calculate similarity
     * @return true if additional calculation need
     */
    protected boolean haveChanceToAdd(final int similarity) {
        if (result.size() < OConsts.MAX_NEAR_STRINGS) {
            return true;
        }
        NearString st = result.get(result.size() - 1);
        return st.score <= similarity;
    }

    /**
     * Add near string into result list. Near strings sorted by "similarity,simAdjusted"
     */
    protected void addNearString(final StringEntry candEntry, final int similarity, final int simAdjusted,
            final byte[] similarityData, final String tmxName) {
        // find position for new data
        int pos = 0;
        for (int i = 0; i < result.size(); i++) {
            NearString st = result.get(i);
            if (st.score < similarity) {
                break;
            }
            if (st.score == similarity) {
                if (st.adjustedScore < simAdjusted) {
                    break;
                }
            }
            pos = i + 1;
        }

        result.add(pos, new NearString(candEntry, similarity, simAdjusted, similarityData, tmxName));
        if (result.size() > OConsts.MAX_NEAR_STRINGS) {
            result.remove(result.size() - 1);
        }
    }
}
