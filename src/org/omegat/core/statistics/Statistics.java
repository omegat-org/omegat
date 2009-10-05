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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.BreakIterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringEntry;
import org.omegat.core.data.TransMemory;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.Tokenizer;
import org.omegat.core.statistics.CalcStandardStatistics.FileData;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.StaticUtils;
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
    /**
     * Builds a file with statistic info about the project. The total word &
     * character count of the project, the total number of unique segments, plus
     * the details for each file.
     */
    public static String buildProjectStats(final List<StringEntry> m_strEntryList,
            final List<SourceTextEntry> m_srcTextEntryArray, final ProjectProperties m_config,
            final int numberofTranslatedSegments) {
        //int I_WORDS = 0, I_WORDSLEFT = 1, I_CHARSNSP = 2, I_CHARSNSPLEFT = 3, I_CHARS = 4, I_CHARSLEFT = 5;

        StatCount total = new StatCount();
        StatCount remaining = new StatCount();
        StatCount unique = new StatCount();
        StatCount remainingUnique = new StatCount();

        String charWithoutTags;

        for (StringEntry se : m_strEntryList) {
            String src = se.getSrcText();
            int dups = se.getParentList().size();

            int words = numberOfWords(src);
            unique.words += words;
            total.words += words * dups;

            charWithoutTags = StaticUtils.stripTags(src);
            int charsNoSpaces = numberOfCharactersWithoutSpaces(charWithoutTags);
            unique.charsWithoutSpaces += charsNoSpaces;
            total.charsWithoutSpaces += charsNoSpaces * dups;

            int chars = charWithoutTags.length();
            unique.charsWithSpaces += chars;
            total.charsWithSpaces += chars * dups;

            if (!se.isTranslated()) {
                remainingUnique.words += words;
                remainingUnique.charsWithoutSpaces += charsNoSpaces;
                remainingUnique.charsWithSpaces += chars;
                remaining.segments += dups;
            }
        }

        remainingUnique.segments = m_strEntryList.size() - numberofTranslatedSegments;

        Map<String, FileData> counts = new TreeMap<String, FileData>();
        for (SourceTextEntry ste : m_srcTextEntryArray) {
            String fileName = ste.getSrcFile().name;
            fileName = StaticUtils.makeFilenameRelative(fileName, m_config.getSourceRoot());
            
            FileData numbers = counts.get(fileName);
            if (numbers==null) {
                numbers = new FileData();
                counts.put(fileName, numbers);
            }
            
            

            String src = ste.getSrcText();
            charWithoutTags = StaticUtils.stripTags(src);
            int words = numberOfWords(src);
            numbers.total.words += words;
            int charsNoSpaces = numberOfCharactersWithoutSpaces(charWithoutTags);
            numbers.total.charsWithoutSpaces += charsNoSpaces;
            int chars = charWithoutTags.length();
            numbers.total.charsWithSpaces += chars;

            if (!ste.isTranslated()) {
                remaining.words += words;
                numbers.remaining.words += words;
                remaining.charsWithoutSpaces += charsNoSpaces;
                numbers.remaining.charsWithoutSpaces += charsNoSpaces;
                remaining.charsWithSpaces += chars;
                numbers.remaining.charsWithSpaces += chars;
            }
            counts.put(fileName, numbers);
        }


        StringBuilder result=new StringBuilder();
        
        try {
            // removing old stats
            try {
                File oldstats = new File(m_config.getProjectInternal() + "word_counts"); // NOI18N
                if (oldstats.exists())
                    oldstats.delete();
            } catch (Exception e) {
            }
            

            // now dump file based word counts to disk
            String fn = m_config.getProjectInternal() + OConsts.STATS_FILENAME;
            Writer ofp = new OutputStreamWriter(new FileOutputStream(fn), OConsts.UTF8);
            result.append(OStrings.getString("CT_STATS_Project_Statistics") + "\n\n"); // NOI18N

            total.segments=m_srcTextEntryArray.size();
            
            // TOTAL

            result.append(OStrings.getString("CT_STATS_Total") + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + total.segments + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + total.words + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + total.charsWithoutSpaces + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + total.charsWithSpaces + "\n"); // NOI18N

            // REMAINING

            result.append(OStrings.getString("CT_STATS_Remaining") + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + remaining.segments + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + remaining.words + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + remaining.charsWithoutSpaces + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + remaining.charsWithSpaces + "\n"); // NOI18N

            // UNIQUE
            unique.segments=m_strEntryList.size();
            result.append(OStrings.getString("CT_STATS_Unique") + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + unique.segments + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + unique.words + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + unique.charsWithoutSpaces + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + unique.charsWithSpaces + "\n"); // NOI18N

            // UNIQUE REMAINING

            result.append(OStrings.getString("CT_STATS_Unique_Remaining") + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + remainingUnique.segments + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + remainingUnique.words + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + remainingUnique.charsWithoutSpaces + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + remainingUnique.charsWithSpaces + "\n");
            result.append("\n"); // NOI18N

            // STATISTICS BY FILE

            result.append(OStrings.getString("CT_STATS_FILE_Statistics") + "\n\n"); // NOI18N

            result.append(OStrings.getString("CT_STATS_FILE_Name") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Total_Words") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Remaining_Words") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Total_Characters_NOSP") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Remaining_Characters_NOSP") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Total_Characters") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Remaining_Characters") + "\n"); // NOI18N

            for (String filename : counts.keySet()) {
                FileData numbers = counts.get(filename);
                result.append(filename + "\t" + numbers.total.words + "\t" + numbers.remaining.words + // NOI18N
                        "\t" + numbers.total.charsWithoutSpaces + "\t" + numbers.remaining.charsWithoutSpaces + // NOI18N
                        "\t" + numbers.total.charsWithSpaces + "\t" + numbers.remaining.charsWithSpaces + // NOI18N
                        "\n"); // NOI18N
            }

            ofp.write(result.toString());
            ofp.close();
           
        } catch (IOException e) {
        }
        return result.toString();
    }

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
            final Map<String, Token[]> tokensCache,final Set<String> alreadyProcessed) {
        
        boolean isFirst=alreadyProcessed.add(ste.getSrcText());

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
        for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker.next()) {
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
}
