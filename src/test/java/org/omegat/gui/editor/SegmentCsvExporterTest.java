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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.data.TestTMXEntries;
import org.omegat.gui.editor.CsvExportOptions.Scope;
import org.omegat.gui.editor.CsvFormatOptions.CsvCharset;
import org.omegat.gui.editor.CsvFormatOptions.QuoteEscape;
import org.omegat.gui.editor.CsvFormatOptions.SeparatorChoice;
import org.omegat.gui.editor.SegmentCsvExporter.Row;

/**
 * Tests for the segment CSV writer.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SegmentCsvExporterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private int exportCount;

    private static TMXEntry translated() {
        PrepareTMXEntry prepared = new PrepareTMXEntry();
        prepared.source = "Sentence one.";
        prepared.translation = "Satz eins.";
        prepared.note = "Check the wording.";
        prepared.creator = "alice";
        prepared.creationDate = 1580000000000L;
        prepared.changer = "bob";
        prepared.changeDate = 1590000000000L;
        return TestTMXEntries.create(prepared, true, null);
    }

    private static SourceTextEntry entry(int num, String id, String source) {
        EntryKey key = new EntryKey("chapter.html", source, id, null, null, null);
        return new SourceTextEntry(key, num, null, null, List.of());
    }

    private static List<Row> rows(SourceTextEntry... entries) {
        return Arrays.stream(entries).map(ste -> new Row(ste, translated())).collect(Collectors.toList());
    }

    private static CsvFormatOptions format(CsvCharset charset, SeparatorChoice separator, char custom) {
        return new CsvFormatOptions(charset, separator, custom, false, false, QuoteEscape.DOUBLED);
    }

    private static CsvExportOptions options(CsvFormatOptions format, CsvColumn... columns) {
        Map<CsvColumn, Boolean> order = new LinkedHashMap<>();
        for (CsvColumn column : columns) {
            order.put(column, true);
        }
        return new CsvExportOptions(Scope.PROJECT, false, false, order, format);
    }

    private Path export(List<Row> rows, CsvExportOptions options) throws IOException {
        Path target = folder.newFile("export" + exportCount++ + ".csv").toPath();
        SegmentCsvExporter.export(rows, options, target);
        return target;
    }

    private List<String> exportedLines(List<Row> rows, CsvExportOptions options) throws IOException {
        return lines(Files.readString(export(rows, options), StandardCharsets.UTF_8));
    }

    private static List<String> lines(String content) {
        return Arrays.asList(content.split("\r\n", -1));
    }

    private static CsvFormatOptions utf8Comma() {
        return format(CsvCharset.UTF_8, SeparatorChoice.COMMA, ',');
    }

    @Test
    public void testWritesHeaderAndValues() throws Exception {
        List<String> lines = exportedLines(rows(entry(7, "para-1", "Sentence one.")),
                options(utf8Comma(), CsvColumn.SEG_NUM, CsvColumn.SEG_ID, CsvColumn.FILE, CsvColumn.SOURCE,
                        CsvColumn.TARGET, CsvColumn.TRANSLATED, CsvColumn.NOTE, CsvColumn.CREATOR,
                        CsvColumn.CREATED, CsvColumn.CHANGER, CsvColumn.CHANGED));
        assertEquals("seg_num,seg_id,file,source,target,translated,note,creator,created,changer,changed",
                lines.get(0));
        assertEquals("7,para-1,chapter.html,Sentence one.,Satz eins.,true,Check the wording.,"
                + "alice,2020-01-26T00:53:20Z,bob,2020-05-20T18:40:00Z", lines.get(1));
        // The file ends with a line break after the last row.
        assertEquals(List.of(""), lines.subList(2, lines.size()));
    }

    @Test
    public void testColumnOrderIsRespected() throws Exception {
        List<String> lines = exportedLines(rows(entry(7, "para-1", "Sentence one.")),
                options(utf8Comma(), CsvColumn.TARGET, CsvColumn.SEG_NUM, CsvColumn.SOURCE));
        assertEquals("target,seg_num,source", lines.get(0));
        assertEquals("Satz eins.,7,Sentence one.", lines.get(1));
    }

    @Test
    public void testUntranslatedSegment() throws Exception {
        List<Row> rows = List.of(new Row(entry(1, null, "Sentence one."),
                TestTMXEntries.create(new PrepareTMXEntry(), true, null)));
        List<String> lines = exportedLines(rows,
                options(utf8Comma(), CsvColumn.TARGET, CsvColumn.TRANSLATED, CsvColumn.CREATED));
        assertEquals("target,translated,created", lines.get(0));
        assertEquals(",false,", lines.get(1));
    }

    @Test
    public void testQuotesSeparatorQuoteAndLineBreak() throws Exception {
        List<String> lines = exportedLines(rows(entry(1, null, "One, two \"three\"\nfour.")),
                options(utf8Comma(), CsvColumn.SEG_NUM, CsvColumn.SOURCE));
        assertEquals("seg_num,source", lines.get(0));
        // Rows are separated by CRLF; the bare LF stays inside the quoted field.
        assertEquals("1,\"One, two \"\"three\"\"\nfour.\"", lines.get(1));
    }

    @Test
    public void testQuoteAll() throws Exception {
        CsvFormatOptions format = new CsvFormatOptions(CsvCharset.UTF_8, SeparatorChoice.COMMA, ',', true,
                false, QuoteEscape.DOUBLED);
        List<String> lines = exportedLines(rows(entry(1, null, "Plain text.")),
                options(format, CsvColumn.SEG_NUM, CsvColumn.SOURCE));
        assertEquals("\"seg_num\",\"source\"", lines.get(0));
        assertEquals("\"1\",\"Plain text.\"", lines.get(1));
    }

    @Test
    public void testEscapeNewlines() throws Exception {
        CsvFormatOptions format = new CsvFormatOptions(CsvCharset.UTF_8, SeparatorChoice.COMMA, ',', false,
                true, QuoteEscape.DOUBLED);
        List<String> lines = exportedLines(rows(entry(1, null, "line one\nline two\r\nline three")),
                options(format, CsvColumn.SOURCE));
        // Escaped line breaks leave nothing to quote.
        assertEquals("line one\\nline two\\nline three", lines.get(1));
        assertEquals(3, lines.size());
    }

    @Test
    public void testBackslashQuoteEscape() throws Exception {
        CsvFormatOptions format = new CsvFormatOptions(CsvCharset.UTF_8, SeparatorChoice.COMMA, ',', false,
                false, QuoteEscape.BACKSLASH);
        List<String> lines = exportedLines(rows(entry(1, null, "He said \"hi\".")),
                options(format, CsvColumn.SOURCE));
        assertEquals("\"He said \\\"hi\\\".\"", lines.get(1));
    }

    @Test
    public void testSeparatorVariants() throws Exception {
        Path semicolon = export(rows(entry(1, null, "a,b;c")),
                options(format(CsvCharset.UTF_8, SeparatorChoice.SEMICOLON, ','), CsvColumn.SEG_NUM,
                        CsvColumn.SOURCE));
        assertEquals("seg_num;source", lines(Files.readString(semicolon, StandardCharsets.UTF_8)).get(0));
        assertEquals("1;\"a,b;c\"", lines(Files.readString(semicolon, StandardCharsets.UTF_8)).get(1));

        Path tab = export(rows(entry(1, null, "a,b;c")),
                options(format(CsvCharset.UTF_8, SeparatorChoice.TAB, ','), CsvColumn.SEG_NUM,
                        CsvColumn.SOURCE));
        assertEquals("1\ta,b;c", lines(Files.readString(tab, StandardCharsets.UTF_8)).get(1));

        Path custom = export(rows(entry(1, null, "a,b;c")),
                options(format(CsvCharset.UTF_8, SeparatorChoice.OTHER, '|'), CsvColumn.SEG_NUM,
                        CsvColumn.SOURCE));
        assertEquals("1|a,b;c", lines(Files.readString(custom, StandardCharsets.UTF_8)).get(1));
    }

    @Test
    public void testCharsetsAndBoms() throws Exception {
        Path utf8Bom = export(rows(entry(1, null, "Café corner.")),
                options(format(CsvCharset.UTF_8_BOM, SeparatorChoice.COMMA, ','), CsvColumn.SOURCE));
        byte[] bytes = Files.readAllBytes(utf8Bom);
        assertArrayEquals(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                Arrays.copyOfRange(bytes, 0, 3));

        Path utf16 = export(rows(entry(1, null, "Café corner.")),
                options(format(CsvCharset.UTF_16LE_BOM, SeparatorChoice.COMMA, ','), CsvColumn.SOURCE));
        bytes = Files.readAllBytes(utf16);
        assertArrayEquals(new byte[] {(byte) 0xFF, (byte) 0xFE}, Arrays.copyOfRange(bytes, 0, 2));
        assertEquals("source\r\nCafé corner.\r\n",
                new String(Arrays.copyOfRange(bytes, 2, bytes.length), StandardCharsets.UTF_16LE));

        Path ascii = export(rows(entry(1, null, "Café corner.")),
                options(format(CsvCharset.US_ASCII, SeparatorChoice.COMMA, ','), CsvColumn.SOURCE));
        assertEquals("Caf? corner.", lines(Files.readString(ascii, StandardCharsets.US_ASCII)).get(1));
    }
}
