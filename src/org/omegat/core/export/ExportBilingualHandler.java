/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 OmegaT contributors
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

package org.omegat.core.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.omegat.core.data.IProject;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.SourceTextEntry.DUPLICATE;
import org.omegat.core.data.TMXEntry;

/**
 * Handler for exporting unique source segments to bilingual CSV/Excel files
 * and importing translations back into the project.
 *
 * <p>
 * The exported file has two columns:
 * <ul>
 * <li>Source – the source text of the segment</li>
 * <li>Target – the existing translation (may be empty for untranslated
 * segments)</li>
 * </ul>
 *
 * <p>
 * External translators fill in the Target column and import the file back.
 * Each non-empty Target value is stored as a default translation for all
 * occurrences of the corresponding Source text in the project.
 *
 * @author OmegaT contributors
 */
public final class ExportBilingualHandler {

    /** Column header for the source language. */
    public static final String HEADER_SOURCE = "Source";
    /** Column header for the target language. */
    public static final String HEADER_TARGET = "Target";

    private ExportBilingualHandler() {
        // Utility class – not instantiable.
    }

    // -------------------------------------------------------------------------
    // CSV export / import
    // -------------------------------------------------------------------------

    /**
     * Exports unique source segments and their current translations to a CSV file.
     *
     * <p>
     * The file is written in UTF-8 with BOM so that Microsoft Excel opens it
     * correctly without encoding issues. Each field is RFC-4180 quoted.
     *
     * @param outputFile destination CSV file
     * @param project    the currently loaded project
     * @throws IOException if the file cannot be written
     */
    public static void exportToCSV(File outputFile, IProject project) throws IOException {
        Map<String, String> segments = collectUniqueSegments(project);

        try (FileOutputStream fos = new FileOutputStream(outputFile);
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {

            // Write UTF-8 BOM for Excel compatibility
            fos.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });

            // Header
            pw.println(csvRow(HEADER_SOURCE, HEADER_TARGET));

            // Data rows
            for (Map.Entry<String, String> entry : segments.entrySet()) {
                pw.println(csvRow(entry.getKey(), entry.getValue()));
            }
        }
    }

    /**
     * Imports translations from a CSV file into the project.
     *
     * <p>
     * Rows whose Target column is empty or blank are skipped.
     * Each translation is stored as a <em>default</em> translation so that it
     * applies to all duplicate occurrences of the same source text.
     *
     * @param inputFile the CSV file to read
     * @param project   the currently loaded project
     * @return number of translations successfully imported
     * @throws IOException if the file cannot be read
     */
    public static int importFromCSV(File inputFile, IProject project) throws IOException {
        List<String[]> rows = readCsv(inputFile);
        return applyTranslations(rows, project);
    }

    // -------------------------------------------------------------------------
    // Excel export / import (requires Apache POI on the classpath)
    // -------------------------------------------------------------------------

    /**
     * Exports unique source segments and their current translations to an Excel
     * (.xlsx) file using Apache POI.
     *
     * @param outputFile destination .xlsx file
     * @param project    the currently loaded project
     * @throws IOException            if the file cannot be written
     * @throws ClassNotFoundException if Apache POI is not available
     */
    public static void exportToXlsx(File outputFile, IProject project)
            throws IOException, ReflectiveOperationException {
        Map<String, String> segments = collectUniqueSegments(project);
        ExcelHelper.write(outputFile, segments);
    }

    /**
     * Imports translations from an Excel (.xlsx) file into the project.
     *
     * @param inputFile the .xlsx file to read
     * @param project   the currently loaded project
     * @return number of translations successfully imported
     * @throws IOException            if the file cannot be read
     * @throws ClassNotFoundException if Apache POI is not available
     */
    public static int importFromXlsx(File inputFile, IProject project)
            throws IOException, ReflectiveOperationException {
        List<String[]> rows = ExcelHelper.read(inputFile);
        return applyTranslations(rows, project);
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    /**
     * Collects all unique source segments from the project and their current
     * translations (empty string if untranslated).
     *
     * @param project the project to inspect
     * @return an ordered map of source text → translation text
     */
    static Map<String, String> collectUniqueSegments(IProject project) {
        Map<String, String> result = new LinkedHashMap<>();
        for (SourceTextEntry ste : project.getAllEntries()) {
            DUPLICATE dup = ste.getDuplicate();
            // Only include NONE (no duplicates) and FIRST (first occurrence of a group)
            if (dup == DUPLICATE.NEXT) {
                continue;
            }
            String source = ste.getSrcText();
            if (result.containsKey(source)) {
                // Already added – should only happen with DUPLICATE.NONE/FIRST
                continue;
            }
            TMXEntry trans = project.getTranslationInfo(ste);
            String target = (trans != null && trans.isTranslated())
                    ? trans.getTranslationText()
                    : "";
            result.put(source, target);
        }
        return result;
    }

    /**
     * Applies a list of [source, target] pairs to the project as default
     * translations. Rows with an empty target are skipped.
     *
     * @param rows    list of [source, target] string pairs (header row already
     *                removed)
     * @param project the project to update
     * @return number of translations applied
     */
    static int applyTranslations(List<String[]> rows, IProject project) {
        // Build a source-text → STE lookup for first occurrences
        Map<String, SourceTextEntry> lookup = new LinkedHashMap<>();
        for (SourceTextEntry ste : project.getAllEntries()) {
            DUPLICATE dup = ste.getDuplicate();
            if (dup != DUPLICATE.NEXT) {
                lookup.putIfAbsent(ste.getSrcText(), ste);
            }
        }

        int count = 0;
        for (String[] row : rows) {
            if (row.length < 2) {
                continue;
            }
            String source = row[0];
            String target = row[1];
            if (source == null || source.isBlank()) {
                continue;
            }
            if (target == null || target.isBlank()) {
                continue; // Nothing to import
            }
            SourceTextEntry ste = lookup.get(source);
            if (ste == null) {
                continue; // Source text not found in current project
            }
            PrepareTMXEntry entry = new PrepareTMXEntry();
            entry.source = source;
            entry.translation = target;
            project.setTranslation(ste, entry, true, null);
            count++;
        }
        return count;
    }

    // -------------------------------------------------------------------------
    // CSV utilities
    // -------------------------------------------------------------------------

    /** Formats two strings as a single RFC-4180 CSV row. */
    static String csvRow(String col1, String col2) {
        return csvField(col1) + "," + csvField(col2);
    }

    /** Wraps a single value in double quotes, escaping any internal quotes. */
    static String csvField(String value) {
        if (value == null) {
            return "\"\"";
        }
        // Escape embedded double-quotes by doubling them (RFC 4180 §2.7)
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    /**
     * Reads a UTF-8 CSV file (with or without BOM) and returns all data rows
     * after the header.
     *
     * @param file the CSV file to read
     * @return list of [source, target] arrays; header row is excluded
     */
    static List<String[]> readCsv(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;
            StringBuilder pending = null; // accumulate multi-line fields

            while ((line = br.readLine()) != null) {
                // Strip UTF-8 BOM from first line
                if (firstLine && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                    firstLine = false;
                }
                firstLine = false;

                if (pending != null) {
                    pending.append('\n').append(line);
                } else {
                    pending = new StringBuilder(line);
                }

                if (!isInsideQuotedField(pending.toString())) {
                    String[] parsed = parseCsvLine(pending.toString());
                    rows.add(parsed);
                    pending = null;
                }
            }
        }

        // Remove header row if it matches expected headers
        if (!rows.isEmpty()) {
            String[] header = rows.get(0);
            if (header.length >= 1 && HEADER_SOURCE.equalsIgnoreCase(header[0].trim())) {
                rows.remove(0);
            }
        }
        return rows;
    }

    /**
     * Returns true if {@code line} ends inside a quoted field (unbalanced quotes).
     */
    private static boolean isInsideQuotedField(String line) {
        boolean inQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuote && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    i++; // skip escaped quote
                } else {
                    inQuote = !inQuote;
                }
            }
        }
        return inQuote;
    }

    /**
     * Parses a single CSV record (which may contain newlines inside quoted fields).
     * Returns an array of field values with quotes removed and escape sequences
     * resolved.
     */
    static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuote) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++; // consume escaped quote
                    } else {
                        inQuote = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuote = true;
                } else if (c == ',') {
                    fields.add(field.toString());
                    field.setLength(0);
                } else {
                    field.append(c);
                }
            }
        }
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }
}
