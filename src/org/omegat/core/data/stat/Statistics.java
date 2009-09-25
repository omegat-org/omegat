/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2009 Didier Briel
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

package org.omegat.core.data.stat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.BreakIterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.text.StyledEditorKit.BoldAction;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringEntry;
import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.core.matching.ISimilarityCalculator;
import org.omegat.core.matching.ITokenizer;
import org.omegat.core.matching.LevenshteinDistance;
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
 */
public class Statistics {
    /**
     * Builds a file with statistic info about the project. The total word &
     * character count of the project, the total number of unique segments, plus
     * the details for each file.
     */
    public static void buildProjectStats(final List<StringEntry> m_strEntryList,
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
            ofp.write(OStrings.getString("CT_STATS_Project_Statistics") + "\n\n"); // NOI18N

            // TOTAL

            ofp.write(OStrings.getString("CT_STATS_Total") + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + m_srcTextEntryArray.size() + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + totalWords + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + totalCharsNoSpaces + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + totalChars + "\n"); // NOI18N

            // REMAINING

            ofp.write(OStrings.getString("CT_STATS_Remaining") + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + remainingSegments + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + remainingWords + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + remainingCharsNoSpaces + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + remainingChars + "\n"); // NOI18N

            // UNIQUE

            ofp.write(OStrings.getString("CT_STATS_Unique") + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + m_strEntryList.size() + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + uniqueWords + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + uniqueCharsNoSpaces + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + uniqueChars + "\n"); // NOI18N

            // UNIQUE REMAINING

            ofp.write(OStrings.getString("CT_STATS_Unique_Remaining") + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Segments") + "\t" + remainingUniqueSegments + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Words") + "\t" + remainingUniqueWords + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters_NOSP") + "\t" + remainingUniqueCharsNoSpaces + "\n"); // NOI18N
            ofp.write("\t" + // NOI18N
                    OStrings.getString("CT_STATS_Characters") + "\t" + remainingUniqueChars + "\n");
            ofp.write("\n"); // NOI18N

            // STATISTICS BY FILE

            ofp.write(OStrings.getString("CT_STATS_FILE_Statistics") + "\n\n"); // NOI18N

            ofp.write(OStrings.getString("CT_STATS_FILE_Name") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Total_Words") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Remaining_Words") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Total_Characters_NOSP") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Remaining_Characters_NOSP") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Total_Characters") + "\t" + // NOI18N
                    OStrings.getString("CT_STATS_FILE_Remaining_Characters") + "\n"); // NOI18N

            for (String filename : counts.keySet()) {
                int[] numbers = counts.get(filename);
                ofp.write(filename + "\t" + numbers[I_WORDS] + "\t" + numbers[I_WORDSLEFT] + // NOI18N
                        "\t" + numbers[I_CHARSNSP] + "\t" + numbers[I_CHARSNSPLEFT] + // NOI18N
                        "\t" + numbers[I_CHARS] + "\t" + numbers[I_CHARSLEFT] + // NOI18N
                        "\n"); // NOI18N
            }

            ofp.close();
        } catch (IOException e) {
        }
        
        // dump matches - will be changed
        //buildMatchesStats(m_srcTextEntryArray);
    }

    /**
     * Collect info about matched segments count.
     */
    public static MatchStatisticsInfo buildMatchesStats(
            final List<SourceTextEntry> m_srcTextEntryArray) {
        ISimilarityCalculator distanceCalculator = new LevenshteinDistance();

        MatchStatisticsInfo result = new MatchStatisticsInfo();

        // We should iterate all segments from all files in project.
        for (SourceTextEntry ste : m_srcTextEntryArray) {
            if (!StringUtil.isEmpty(ste.getTranslation())) {
                // segment has translation - should be calculated as
                // "Exact matched"
                int r = result.getRowForExactMatch();
                result.rows[r].segments++;
                result.rows[r].words += numberOfWords(ste.getSrcText());
            } else {
                int maxSimilarity = 0;
                Token[] strTokensStem = Core.getTokenizer().tokenizeWords(
                        ste.getSrcText(), ITokenizer.StemmingMode.MATCHING);
                for (SourceTextEntry cand : m_srcTextEntryArray) {
                    if (cand == ste) {
                        // source entry
                        continue;
                    }
                    Token[] candTokens = Core.getTokenizer()
                            .tokenizeWords(cand.getSrcText(),
                                    ITokenizer.StemmingMode.MATCHING);
                    int newSimilarity = FuzzyMatcher.calcSimilarity(
                            distanceCalculator, strTokensStem, candTokens);
                    maxSimilarity = Math.max(maxSimilarity, newSimilarity);
                }

                // not matched - 0% yet
                int r = result.getRowByPercent(maxSimilarity);
                result.rows[r].segments++;
                result.rows[r].words += numberOfWords(ste.getSrcText());
            }
        }

        String[] header = new String[] { "", "Segments", "Words" };
        boolean[] align = new boolean[] { false, true, true };
        String[][] table = new String[7][3];
        // dump result - will be changed for UI
        for (int i = 0; i < result.rows.length; i++) {
            switch (i) {
            case 0:
                table[i][0] = "Exact match: ";
                break;
            case 1:
                table[i][0] = "100%: ";
                break;
            case 2:
                table[i][0] = "95% - 99%: ";
                break;
            case 3:
                table[i][0] = "85% - 94%: ";
                break;
            case 4:
                table[i][0] = "75% - 84%: ";
                break;
            case 5:
                table[i][0] = "50% - 74%: ";
                break;
            case 6:
                table[i][0] = "No match: ";
                break;
            }
            table[i][1] = Integer.toString(result.rows[i].segments);
            table[i][2] = Integer.toString(result.rows[i].words);
        }
        System.out.println(showTextTable(header, table, align));
        return result;
    }

    /**
     * Draw text table with columns align.
     * 
     * @param columnHeaders
     *            column headers
     * @param table
     *            table data
     * @return text
     */
    protected static String showTextTable(String[] columnHeaders,
            String[][] table, boolean[] alignRight) {
        StringBuilder out = new StringBuilder();

        // calculate max column size
        int maxColSize[] = new int[columnHeaders.length];
        for (int c = 0; c < columnHeaders.length; c++) {
            maxColSize[c] = columnHeaders[c].length();
        }
        for (int r = 0; r < table.length; r++) {
            for (int c = 0; c < table[r].length; c++) {
                maxColSize[c] = Math.max(maxColSize[c], table[r][c].length());
            }
        }

        for (int c = 0; c < columnHeaders.length; c++) {
            appendField(out, columnHeaders[c], maxColSize[c], alignRight[c]);
        }
        out.append('\n');
        for (int r = 0; r < table.length; r++) {
            for (int c = 0; c < table[r].length; c++) {
                appendField(out, table[r][c], maxColSize[c], alignRight[c]);
            }
            out.append('\n');
        }
        return out.toString();
    }

    /**
     * Output field with specified length.
     * 
     * @param out
     *            output stream
     * @param data
     *            field data
     * @param colSize
     *            field size
     */
    private static void appendField(StringBuilder out, String data,
            int colSize, boolean alignRight) {
        if (!alignRight) {
            out.append(data);
        }
        for (int i = data.length(); i < colSize; i++) {
            out.append(' ');
        }
        if (alignRight) {
            out.append(data);
        }
        out.append("  ");
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
    private static int numberOfWords(String str) {
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
