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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegat.core.data.IProject;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
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
     * Writes the specified text to a file, along with the current date and time.
     * If the target file's parent directories do not exist, they will be created.
     * Any existing content in the file will be overwritten.
     *
     * @param filename the name and path of the file to which the text will be written
     * @param text the text content to write to the file
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
     * Writes the statistics result to the specified directory in all selected output formats.
     *
     * @param dir the directory where the statistics should be written
     * @param result the statistics result object containing the data to be written
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
     * Writes the statistics result to the specified directory in the given output format.
     * Depending on the format, the data is written as text, XML, or JSON. The file is encoded
     * in UTF-8. If any errors occur during file writing, they are logged.
     *
     * @param dir the directory where the statistics file should be written
     * @param result the statistics result object containing the data to be written
     * @param format the format in which the statistics should be written (TEXT, XML, or JSON)
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

    /**
     * Get unique key for segment for uniqueness calculation.
     */
    public static String getUniquenessKey(SourceTextEntry ste) {
        String src = ste.getSrcText();
        for (ProtectedPart pp : ste.getProtectedParts()) {
            String srcText = pp.getTextInSourceSegment();
            String replacement = pp.getReplacementUniquenessCalculation();
            if (srcText != null && replacement != null) {
                src = src.replace(srcText, replacement);
            }
        }
        return src;
    }

    /**
     * Builds a file with statistic info about the project. The total word &amp;
     * character count of the project, the total number of unique segments, plus
     * the details for each file.
     */
    public static StatsResult buildProjectStats(final IProject project) {
        StatCount total = new StatCount();
        StatCount remaining = new StatCount();
        StatCount unique = new StatCount();
        StatCount remainingUnique = new StatCount();

        Set<String> translated = new HashSet<>();
        Set<String> filesUnique = new HashSet<>();
        Set<String> filesRemainingUnique = new HashSet<>();
        Map<String, Boolean> globalFirstSeenUnique = new HashMap<>();
        List<FileData> counts = new ArrayList<>();

        for (IProject.FileInfo file : project.getProjectFiles()) {
            FileData numbers = new FileData();
            numbers.filename = file.filePath;
            counts.add(numbers);
            int fileTotal = 0;
            int fileRemaining = 0;

            for (SourceTextEntry ste : file.entries) {
                String key = getUniquenessKey(ste);
                StatCount count = new StatCount(ste);
                TMXEntry tr = project.getTranslationInfo(ste);
                boolean isTranslated = tr.isTranslated();

                // Project-wide total and remaining
                total.add(count);
                fileTotal = 1;
                if (!isTranslated) {
                    remaining.add(count);
                    fileRemaining = 1;
                }

                // File-wide info
                numbers.total.add(count);
                if (!isTranslated) {
                    numbers.remaining.add(count);
                }

                // Unique segments (Project-wide and File-wide)
                if (!globalFirstSeenUnique.containsKey(key)) {
                    globalFirstSeenUnique.put(key, isTranslated);
                    unique.add(count);
                    filesUnique.add(ste.getKey().file);
                    numbers.unique.add(count);

                    if (!isTranslated) {
                        remainingUnique.add(count);
                        filesRemainingUnique.add(ste.getKey().file);
                        numbers.remainingUnique.add(count);
                    }
                } else if (isTranslated) {
                    // Update translated status for project-wide translated set
                    globalFirstSeenUnique.put(key, true);
                }
            }
            total.addFiles(fileTotal);
            remaining.addFiles(fileRemaining);
        }

        unique.addFiles(filesUnique.size());
        remainingUnique.addFiles(filesRemainingUnique.size());

        // Fill translated set for StatsResult
        for (Map.Entry<String, Boolean> entry : globalFirstSeenUnique.entrySet()) {
            if (entry.getValue()) {
                translated.add(entry.getKey());
            }
        }

        return new StatsResult(total, remaining, unique, remainingUnique, translated, counts);
    }
}
