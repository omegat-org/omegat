/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2009 Didier Briel, Alex Buloichik
               2012 Thomas Cordonnier
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

import java.io.File;
import java.text.BreakIterator;

import org.omegat.core.statistics.dso.StatsResult;
import org.omegat.core.statistics.writer.StatisticsJsonWriter;
import org.omegat.core.statistics.writer.StatisticsTextWriter;
import org.omegat.core.statistics.writer.StatisticsXmlWriter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

/**
 * Save project statistic into files.
 *
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
 * @author Zoltan Bartko (bartkozoltan@bartkozoltan.com)
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Thomas Cordonnier
 */
public final class Statistics {
    private Statistics() {
    }

    public static final int PERCENT_EXACT_MATCH = 101;
    public static final int PERCENT_REPETITIONS = 102;

    /**
     * Computes the number of characters excluding spaces in a string. Special
     * char for tag replacement not calculated.
     */
    public static int numberOfCharactersWithoutSpaces(String str) {
        int chars = 0;
        for (int cp, i = 0; i < str.length(); i += Character.charCount(cp)) {
            cp = str.codePointAt(i);
            if (cp != StaticUtils.TAG_REPLACEMENT_CHAR && !Character.isSpaceChar(cp)) {
                chars++;
            }
        }
        return chars;
    }

    /**
     * Computes the number of characters with spaces in a string. Special char
     * for tag replacement not calculated.
     */
    public static int numberOfCharactersWithSpaces(String str) {
        int chars = 0;
        for (int cp, i = 0; i < str.length(); i += Character.charCount(cp)) {
            cp = str.codePointAt(i);
            if (cp != StaticUtils.TAG_REPLACEMENT_CHAR) {
                chars++;
            }
        }
        return chars;
    }

    /** Computes the number of words in a string. */
    public static int numberOfWords(String str) {
        int len = str.length();
        if (len == 0) {
            return 0;
        }
        int nTokens = 0;
        BreakIterator breaker = DefaultTokenizer.getWordBreaker();
        breaker.setText(str);

        String tokenStr;

        int start = breaker.first();
        for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker.next()) {
            tokenStr = str.substring(start, end);
            boolean word = false;
            for (int cp, i = 0; i < tokenStr.length(); i += Character.charCount(cp)) {
                cp = tokenStr.codePointAt(i);
                if (Character.isLetterOrDigit(cp)) {
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
     * Writes the statistics result to the specified directory in all selected
     * output formats.
     *
     * @param dir
     *            the directory where the statistics should be written
     * @param result
     *            the statistics result object containing the data to be written
     */
    public static void writeStat(String dir, StatsResult result) {
        int outputFormats = Preferences.getPreferenceDefault(Preferences.STATS_OUTPUT_FORMAT,
                StatOutputFormat.getDefaultFormats());
        for (StatOutputFormat format : StatOutputFormat.values()) {
            if (format.isSelected(outputFormats)) {
                writeStat(dir, result, format);
            }
        }
    }

    /**
     * Writes the statistics result to the specified directory in the given
     * output format. Depending on the format, the data is written as text, XML,
     * or JSON. The file is encoded in UTF-8. If any errors occur during file
     * writing, they are logged.
     *
     * @param dir
     *            the directory where the statistics file should be written
     * @param result
     *            the statistics result object containing the data to be written
     * @param format
     *            the format in which the statistics should be written (TEXT,
     *            XML, or JSON)
     */
    public static void writeStat(String dir, StatsResult result, StatOutputFormat format) {
        File statFile = new File(dir, OConsts.STATS_FILENAME + format.getFileExtension());
        switch (format) {
        case TEXT:
            new StatisticsTextWriter().writeStat(statFile, result);
            break;
        case XML:
            new StatisticsXmlWriter().writeStat(statFile, result);
            break;
        case JSON:
        default:
            new StatisticsJsonWriter().writeStat(statFile, result);
            break;
        }
    }
}
