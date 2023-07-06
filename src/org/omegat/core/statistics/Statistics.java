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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.util.Date;

import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

/**
 * Save project statistic into text file.
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

    protected static final int PERCENT_EXACT_MATCH = 101;
    protected static final int PERCENT_REPETITIONS = 102;

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

        String tokenStr = "";

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
     * Write text to file.
     *
     * @param filename
     * @param text
     */
    public static void writeStat(String filename, String text) {
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filename),
                StandardCharsets.UTF_8)) {
            out.write(DateFormat.getInstance().format(new Date()) + "\n");
            out.write(text);
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    /**
     * Write statistics to a file with the format set in the preferences.
     *
     * @param filename
     * @param result
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
     * Write statistics to a file in specified format.
     *
     * @param filename
     * @param result
     * @param format
     */
    public static void writeStat(String dir, StatsResult result, StatOutputFormat format) {
        File statFile = new File(dir, OConsts.STATS_FILENAME + format.getFileExtension());
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(statFile),
                StandardCharsets.UTF_8)) {
            switch (format) {
            case TEXT:
                out.write(DateFormat.getInstance().format(new Date()) + "\n");
                out.write(result.getTextData());
                break;
            case XML:
                out.write(result.getXmlData());
                break;
            case JSON:
            default:
                out.write(result.getJsonData());
                break;
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
    }
}
