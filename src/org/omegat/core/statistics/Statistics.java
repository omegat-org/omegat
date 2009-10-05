/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2009 Didier Briel, Alex Buloichik
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

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TransMemory;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.Tokenizer;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;

/**
 * Save project statistic into text file.
 * 
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
 * @author Zoltan Bartko (bartkozoltan@bartkozoltan.com)
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class Statistics {

    protected static final int PERCENT_EXACT_MATCH = 101;
    protected static final int PERCENT_REPETITIONS = 102;

    /**
     * Calculate max similarity percent for one entry.
     * 
     * @param ste
     *            source entry
     * @param distanceCalculator
     *            calculator
     * @param allEntries
     *            all entries in project
     * @return max similarity percent
     */
    public static int getMaxSimilarityPercent(final SourceTextEntry ste,
            final ISimilarityCalculator distanceCalculator,
            final List<SourceTextEntry> allEntries,
            final Map<String, Token[]> tokensCache,
            final Set<String> alreadyProcessed) {

        boolean isFirst = alreadyProcessed.add(ste.getSrcText());

        if (!StringUtil.isEmpty(ste.getTranslation())) {
            // segment has translation - should be calculated as
            // "Exact matched"
            return PERCENT_EXACT_MATCH;
        }

        Token[] strTokensStem = tokenizeExactlyWithCache(tokensCache, ste
                .getSrcText());
        int maxSimilarity = 0; // not matched - 0% yet

        /* Travel by project entries. */
        // 'for(int i;;)' much faster than 'for(:)'
        for (int i = 0; i < allEntries.size(); i++) {
            SourceTextEntry cand = allEntries.get(i);
            if (cand == ste) {
                // source entry
                continue;
            }
            if (StringUtil.isEmpty(cand.getTranslation())) {
                // target without translation - skip
                continue;
            }
            Token[] candTokens = tokenizeExactlyWithCache(tokensCache, cand
                    .getSrcText());
            int newSimilarity = FuzzyMatcher.calcSimilarity(distanceCalculator,
                    strTokensStem, candTokens);
            maxSimilarity = Math.max(maxSimilarity, newSimilarity);
        }

        /* Travel by TMs. */
        List<TransMemory> tmList = Core.getProject().getTransMemory();
        // 'for(int i;;)' much faster than 'for(:)'
        for (int i = 0; i < tmList.size(); i++) {
            TransMemory tm = tmList.get(i);
            Token[] candTokens = tokenizeExactlyWithCache(tokensCache,
                    tm.source);
            int newSimilarity = FuzzyMatcher.calcSimilarity(distanceCalculator,
                    strTokensStem, candTokens);
            maxSimilarity = Math.max(maxSimilarity, newSimilarity);
        }

        if (maxSimilarity < 50) {
            // No match. Need to add only first segment. Next segments will
            // be 'repetition'.
            if (!isFirst) {
                maxSimilarity = PERCENT_REPETITIONS;
            }
        }

        return maxSimilarity;
    }

    /**
     * Get tokens from cache or tokenize and cache it.
     * 
     * @param tokensCache
     *            cache
     * @param str
     *            string to tokenize
     * @return tokens
     */
    private static Token[] tokenizeExactlyWithCache(
            final Map<String, Token[]> tokensCache, final String str) {
        Token[] result = tokensCache.get(str);
        if (result == null) {
            result = Core.getTokenizer().tokenizeAllExactly(str);
            tokensCache.put(str, result);
        }
        return result;
    }

    /** Computes the number of characters excluding spaces in a string. */
    public static int numberOfCharactersWithoutSpaces(String str) {
        int chars = 0;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isSpaceChar(str.charAt(i)))
                chars++;
        }
        return chars;
    }

    /** Computes the number of words in a string. */
    public static int numberOfWords(String str) {
        int len = str.length();
        if (len == 0)
            return 0;
        int nTokens = 0;
        BreakIterator breaker = Tokenizer.getWordBreaker();
        breaker.setText(str);

        String tokenStr = new String();

        int start = breaker.first();
        for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker
                .next()) {
            tokenStr = str.substring(start, end);
            boolean word = false;
            for (int i = 0; i < tokenStr.length(); i++) {
                char ch = tokenStr.charAt(i);
                if (Character.isLetterOrDigit(ch)) {
                    word = true;
                    break;
                }
            }
            if (word && !PatternConsts.OMEGAT_TAG.matcher(tokenStr).matches()) {
                nTokens++;
            }
        }
        return nTokens;
    }

    /**
     * Write text to file.
     * 
     * @param filename
     * @param data
     */
    public static void writeStat(String filename, String text) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(filename), OConsts.UTF8);
            try {
                out.write(DateFormat.getInstance().format(new Date()) + "\n");
                out.write(text);
                out.flush();
            } finally {
                out.close();
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
    }
}
