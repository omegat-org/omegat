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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.util.Date;

import org.omegat.core.statistics.dso.FileData;
import org.omegat.core.statistics.dso.StatCount;
import org.omegat.core.statistics.dso.StatsResult;
import org.omegat.core.statistics.spi.StatOutputFormat;
import org.omegat.gui.stat.StatisticsPanel;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.TextUtil;

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
    private static final String[] HT_HEADERS = { "", OStrings.getString("CT_STATS_Segments"),
            OStrings.getString("CT_STATS_Words"), OStrings.getString("CT_STATS_Characters_NOSP"),
            OStrings.getString("CT_STATS_Characters"), OStrings.getString("CT_STATS_Files"), };

    private static final String[] HT_ROWS = { OStrings.getString("CT_STATS_Total"),
            OStrings.getString("CT_STATS_Remaining"), OStrings.getString("CT_STATS_Unique"),
            OStrings.getString("CT_STATS_Unique_Remaining"), };

    private static final boolean[] HT_ALIGN = new boolean[] { false, true, true, true, true, true };

    private static final String[] FT_HEADERS = { OStrings.getString("CT_STATS_FILE_Name"),
            OStrings.getString("CT_STATS_FILE_Total_Segments"),
            OStrings.getString("CT_STATS_FILE_Remaining_Segments"),
            OStrings.getString("CT_STATS_FILE_Unique_Segments"),
            OStrings.getString("CT_STATS_FILE_Unique_Remaining_Segments"),
            OStrings.getString("CT_STATS_FILE_Total_Words"),
            OStrings.getString("CT_STATS_FILE_Remaining_Words"),
            OStrings.getString("CT_STATS_FILE_Unique_Words"),
            OStrings.getString("CT_STATS_FILE_Unique_Remaining_Words"),
            OStrings.getString("CT_STATS_FILE_Total_Characters_NOSP"),
            OStrings.getString("CT_STATS_FILE_Remaining_Characters_NOSP"),
            OStrings.getString("CT_STATS_FILE_Unique_Characters_NOSP"),
            OStrings.getString("CT_STATS_FILE_Unique_Remaining_Characters_NOSP"),
            OStrings.getString("CT_STATS_FILE_Total_Characters"),
            OStrings.getString("CT_STATS_FILE_Remaining_Characters"),
            OStrings.getString("CT_STATS_FILE_Unique_Characters"),
            OStrings.getString("CT_STATS_FILE_Unique_Remaining_Characters") };

    private static final boolean[] FT_ALIGN = { false, true, true, true, true, true, true, true, true, true,
            true, true, true, true, true, true, true, };

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
     * Writes the specified text to a file, along with the current date and
     * time. If the target file's parent directories do not exist, they will be
     * created. Any existing content in the file will be overwritten.
     *
     * @param filename
     *            the name and path of the file to which the text will be
     *            written
     * @param text
     *            the text content to write to the file
     */
    public static void writeStat(String filename, String text) {
        Path path = Paths.get(filename);
        // Create parent directories if they don't exist
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                Log.log(e);
                return;
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(DateFormat.getInstance().format(new Date()) + "\n");
            writer.write(text);
        } catch (Exception ex) {
            Log.log(ex);
        }
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
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(statFile),
                StandardCharsets.UTF_8)) {
            switch (format) {
            case TEXT:
                out.write(DateFormat.getInstance().format(new Date()) + "\n");
                out.write(getTextData(result));
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

    public static void writeToPanel(StatsResult result, StatisticsPanel callback) {
        callback.setProjectTableData(HT_HEADERS, getHeaderTable(result));
        callback.setFilesTableData(FT_HEADERS, getFilesTable(result));
        callback.setTextData(getTextData(result));
        callback.finishData();
    }

    /**
     * Return pretty printed statistics data.
     *
     * @return pretty-printed string.
     */
    public static String getTextData(StatsResult result) {
        return OStrings.getString("CT_STATS_Project_Statistics") + "\n\n"
                + TextUtil.showTextTable(HT_HEADERS, getHeaderTable(result), HT_ALIGN) + "\n\n" +

                // STATISTICS BY FILE
                OStrings.getString("CT_STATS_FILE_Statistics") + "\n\n"
                + TextUtil.showTextTable(FT_HEADERS, getFilesTable(result), FT_ALIGN);
    }

    // CHECKSTYLE:OFF
    public static String[][] getHeaderTable(StatsResult statsResult) {
        StatCount[] result = new StatCount[] { statsResult.getTotal(), statsResult.getRemaining(),
                statsResult.getUnique(), statsResult.getRemainingUnique() };
        String[][] table = new String[result.length][6];

        for (int i = 0; i < result.length; i++) {
            table[i][0] = HT_ROWS[i];
            table[i][1] = Integer.toString(result[i].segments);
            table[i][2] = Integer.toString(result[i].words);
            table[i][3] = Integer.toString(result[i].charsWithoutSpaces);
            table[i][4] = Integer.toString(result[i].charsWithSpaces);
            table[i][5] = Integer.toString(result[i].files);
        }
        return table;
    }

    public static String[][] getFilesTable(StatsResult statsResult) {
        String[][] table = new String[statsResult.getCounts().size()][17];

        int r = 0;
        for (FileData numbers : statsResult.getCounts()) {
            table[r][0] = StaticUtils.makeFilenameRelative(numbers.filename,
                    statsResult.getProps().getSourceRoot());
            table[r][1] = Integer.toString(numbers.total.segments);
            table[r][2] = Integer.toString(numbers.remaining.segments);
            table[r][3] = Integer.toString(numbers.unique.segments);
            table[r][4] = Integer.toString(numbers.remainingUnique.segments);
            table[r][5] = Integer.toString(numbers.total.words);
            table[r][6] = Integer.toString(numbers.remaining.words);
            table[r][7] = Integer.toString(numbers.unique.words);
            table[r][8] = Integer.toString(numbers.remainingUnique.words);
            table[r][9] = Integer.toString(numbers.total.charsWithoutSpaces);
            table[r][10] = Integer.toString(numbers.remaining.charsWithoutSpaces);
            table[r][11] = Integer.toString(numbers.unique.charsWithoutSpaces);
            table[r][12] = Integer.toString(numbers.remainingUnique.charsWithoutSpaces);
            table[r][13] = Integer.toString(numbers.total.charsWithSpaces);
            table[r][14] = Integer.toString(numbers.remaining.charsWithSpaces);
            table[r][15] = Integer.toString(numbers.unique.charsWithSpaces);
            table[r][16] = Integer.toString(numbers.remainingUnique.charsWithSpaces);
            r++;
        }
        return table;
    }
    // CHECKSTYLE:ON
}
