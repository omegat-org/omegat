/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.editor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;

/**
 * Writes segments as a CSV file: header row with the {@link CsvColumn} ids,
 * fields quoted with {@code "} where they contain the separator, a quote, or
 * a line break — or always, when so configured. Quote escaping is doubling
 * per RFC 4180 or a backslash; line breaks in field text optionally turn
 * into literal {@code \n}. Character set and separator come from
 * {@link CsvFormatOptions}; characters the chosen character set cannot
 * encode are replaced.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
final class SegmentCsvExporter {

    /**
     * Segment with its translation state. Callers resolve the translation on
     * the EDT; the project TMX maps are not safe to read concurrently.
     */
    record Row(SourceTextEntry entry, TMXEntry translation) {
    }

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final byte[] UTF16LE_BOM = {(byte) 0xFF, (byte) 0xFE};

    private SegmentCsvExporter() {
    }

    /** Writes the given rows to {@code target}. Runs off the EDT. */
    static void export(List<Row> rows, CsvExportOptions options, Path target) throws IOException {
        List<CsvColumn> columns = options.getSelectedColumns();
        CsvFormatOptions format = options.getFormat();
        try (OutputStream out = Files.newOutputStream(target);
                Writer writer = new BufferedWriter(new OutputStreamWriter(out, encoder(format)))) {
            writeBom(out, format);
            writeHeader(writer, columns, format);
            for (Row row : rows) {
                writeRow(writer, row, columns, format);
            }
        }
    }

    private static CharsetEncoder encoder(CsvFormatOptions format) {
        return format.getCharset().getCharset().newEncoder()
                .onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    private static void writeBom(OutputStream out, CsvFormatOptions format) throws IOException {
        CsvFormatOptions.CsvCharset charset = format.getCharset();
        if (!charset.hasBom()) {
            return;
        }
        out.write(charset == CsvFormatOptions.CsvCharset.UTF_16LE_BOM ? UTF16LE_BOM : UTF8_BOM);
    }

    private static void writeHeader(Writer writer, List<CsvColumn> columns, CsvFormatOptions format)
            throws IOException {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                writer.write(format.getSeparator());
            }
            writer.write(encodeField(columns.get(i).getHeaderId(), format));
        }
        writer.write("\r\n");
    }

    private static void writeRow(Writer writer, Row row, List<CsvColumn> columns, CsvFormatOptions format)
            throws IOException {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                writer.write(format.getSeparator());
            }
            writer.write(encodeField(columns.get(i).extract(row.entry(), row.translation()), format));
        }
        writer.write("\r\n");
    }

    private static String encodeField(String field, CsvFormatOptions format) {
        String value = field;
        if (format.isEscapeNewlines()) {
            value = value.replace("\r\n", "\n").replace('\r', '\n').replace("\n", "\\n");
        }
        char separator = format.getSeparator();
        boolean needsQuote = format.isQuoteAll() || value.indexOf(separator) >= 0
                || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        if (!needsQuote) {
            return value;
        }
        String escaped = format.getQuoteEscape() == CsvFormatOptions.QuoteEscape.BACKSLASH
                ? value.replace("\"", "\\\"")
                : value.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }
}
