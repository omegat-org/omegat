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
import java.util.TreeMap;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringEntry;
import org.omegat.core.data.TransMemory;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.Tokenizer;
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
        int I_WORDS = 0, I_WORDSLEFT = 1, I_CHARSNSP = 2, I_CHARSNSPLEFT = 3, I_CHARS = 4, I_CHARSLEFT = 5;

        int totalWords = 0, uniqueWords = 0, totalCharsNoSpaces = 0, uniqueCharsNoSpaces = 0, totalChars = 0, uniqueChars = 0, remainingUniqueWords = 0, remainingUniqueCharsNoSpaces = 0, remainingUniqueChars = 0, remainingSegments = 0;

        String charWithoutTags;

        for (StringEntry se : m_strEntryList) {
            String src = se.getSrcText();
            int dups = se.getParentList().size();

            int words = numberOfWords(src);
            uniqueWords += words;
            totalWords += words * dups;

            charWithoutTags = StaticUtils.stripTags(src);
            int charsNoSpaces = numberOfCharactersWithoutSpaces(charWithoutTags);
            uniqueCharsNoSpaces += charsNoSpaces;
            totalCharsNoSpaces += charsNoSpaces * dups;

            int chars = charWithoutTags.length();
            uniqueChars += chars;
            totalChars += chars * dups;

            if (!se.isTranslated()) {
                remainingUniqueWords += words;
                remainingUniqueCharsNoSpaces += charsNoSpaces;
                remainingUniqueChars += chars;
                remainingSegments += dups;
            }
        }

        int remainingUniqueSegments = m_strEntryList.size() - numberofTranslatedSegments;

        int remainingWords = 0;
        int remainingCharsNoSpaces = 0;
        int remainingChars = 0;
        Map<String, int[]> counts = new TreeMap<String, int[]>();
        for (SourceTextEntry ste : m_srcTextEntryArray) {
            String fileName = ste.getSrcFile().name;
            fileName = StaticUtils.makeFilenameRelative(fileName, m_config.getSourceRoot());
            int[] numbers; // [0] - words, [1] - left words
            if (counts.containsKey(fileName))
                numbers = counts.get(fileName);
            else
                numbers = new int[] { 0, 0, 0, 0, 0, 0 };

            String src = ste.getSrcText();
            charWithoutTags = StaticUtils.stripTags(src);
            int words = numberOfWords(src);
            numbers[I_WORDS] += words;
            int charsNoSpaces = numberOfCharactersWithoutSpaces(charWithoutTags);
            numbers[I_CHARSNSP] += charsNoSpaces;
            int chars = charWithoutTags.length();
            numbers[I_CHARS] += chars;

            if (!ste.isTranslated()) {
                remainingWords += words;
                numbers[I_WORDSLEFT] += words;
                remainingCharsNoSpaces += charsNoSpaces;
                numbers[I_CHARSNSPLEFT] += charsNoSpaces;
                remainingChars += chars;
                numbers[I_CHARSLEFT] += chars;
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

            // TOTAL

            result.append(OStrings.getString("CT_STATS_Total") + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + m_srcTextEntryArray.size() + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + totalWords + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + totalCharsNoSpaces + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + totalChars + "\n"); // NOI18N

            // REMAINING

            result.append(OStrings.getString("CT_STATS_Remaining") + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + remainingSegments + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + remainingWords + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + remainingCharsNoSpaces + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + remainingChars + "\n"); // NOI18N

            // UNIQUE

            result.append(OStrings.getString("CT_STATS_Unique") + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + m_strEntryList.size() + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + uniqueWords + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + uniqueCharsNoSpaces + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + uniqueChars + "\n"); // NOI18N

            // UNIQUE REMAINING

            result.append(OStrings.getString("CT_STATS_Unique_Remaining") + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + remainingUniqueSegments + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + remainingUniqueWords + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + remainingUniqueCharsNoSpaces + "\n"); // NOI18N
            result.append("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + remainingUniqueChars + "\n");
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
                int[] numbers = counts.get(filename);
                result.append(filename + "\t" + numbers[I_WORDS] + "\t" + numbers[I_WORDSLEFT] + // NOI18N
                        "\t" + numbers[I_CHARSNSP] + "\t" + numbers[I_CHARSNSPLEFT] + // NOI18N
                        "\t" + numbers[I_CHARS] + "\t" + numbers[I_CHARSLEFT] + // NOI18N
                        "\n"); // NOI18N
            }

            ofp.write(result.toString());
            ofp.close();
           
        } catch (IOException e) {
        }
        return result.toString();
    }

    /**
     * Calculate max similarity percent for one entry.
     * 
     * @param ste source entry
     * @param distanceCalculator calculator
     * @param allEntries all entries in project
     * @return max similarity percent
     */
    public static int getMaxSimilarityPercent(final SourceTextEntry ste,
            final ISimilarityCalculator distanceCalculator,
            final List<SourceTextEntry> allEntries) {
        if (!StringUtil.isEmpty(ste.getTranslation())) {
            // segment has translation - should be calculated as
            // "Exact matched"
            return Integer.MAX_VALUE;
        }

        Token[] strTokensStem = Core.getTokenizer().tokenizeAllExactly(
                ste.getSrcText());
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
            Token[] candTokens = Core.getTokenizer().tokenizeAllExactly(
                    cand.getSrcText());
            int newSimilarity = FuzzyMatcher.calcSimilarity(distanceCalculator,
                    strTokensStem, candTokens);
            maxSimilarity = Math.max(maxSimilarity, newSimilarity);
        }

        /* Travel by TMs. */
        List<TransMemory> tmList = Core.getProject().getTransMemory();
        // 'for(int i;;)' much faster than 'for(:)'
        for (int i = 0; i < tmList.size(); i++) {
            TransMemory tm = tmList.get(i);
            Token[] candTokens = Core.getTokenizer().tokenizeAllExactly(
                    tm.source);
            int newSimilarity = FuzzyMatcher.calcSimilarity(distanceCalculator,
                    strTokensStem, candTokens);
            maxSimilarity = Math.max(maxSimilarity, newSimilarity);
        }
        return maxSimilarity;
    }

    /** Computes the number of characters excluding spaces in a string. */
    private static int numberOfCharactersWithoutSpaces(String str) {
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
