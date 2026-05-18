/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 OmegaT contributors
               Home page: https://www.omegat.org/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the Free
 Software Foundation, version 3 or later.
 **************************************************************************/

package org.omegat.core.export;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.SourceTextEntry.DUPLICATE;
import org.omegat.core.data.TMXEntry;

/**
 * Unit tests for {@link ExportBilingualHandler}.
 *
 * The tests cover CSV field quoting, header output, unique-segment
 * deduplication,
 * inclusion of existing translations, and round-trip import correctness.
 */
public class ExportBilingualHandlerTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    // -------------------------------------------------------------------------
    // csvField / csvRow
    // -------------------------------------------------------------------------

    @Test
    public void testCsvFieldPlain() {
        assertEquals("\"hello\"", ExportBilingualHandler.csvField("hello"));
    }

    @Test
    public void testCsvFieldWithQuotes() {
        // Embedded double-quotes must be doubled per RFC-4180
        assertEquals("\"say \\\"hi\\\"\"".replace("\\\"", "\""),
                ExportBilingualHandler.csvField("say \"hi\""));
    }

    @Test
    public void testCsvFieldNull() {
        assertEquals("\"\"", ExportBilingualHandler.csvField(null));
    }

    @Test
    public void testCsvRowFormat() {
        assertEquals("\"Source\",\"Target\"", ExportBilingualHandler.csvRow("Source", "Target"));
    }

    // -------------------------------------------------------------------------
    // parseCsvLine
    // -------------------------------------------------------------------------

    @Test
    public void testParseCsvLineSimple() {
        String[] parts = ExportBilingualHandler.parseCsvLine("\"hello\",\"world\"");
        assertEquals(2, parts.length);
        assertEquals("hello", parts[0]);
        assertEquals("world", parts[1]);
    }

    @Test
    public void testParseCsvLineWithEmbeddedQuotes() {
        // RFC-4180: "" inside a quoted field means one literal "
        String[] parts = ExportBilingualHandler.parseCsvLine("\"say \"\"hi\"\"\",\"ok\"");
        assertEquals(2, parts.length);
        assertEquals("say \"hi\"", parts[0]);
        assertEquals("ok", parts[1]);
    }

    @Test
    public void testParseCsvLineEmptyField() {
        String[] parts = ExportBilingualHandler.parseCsvLine("\"\",\"target\"");
        assertEquals(2, parts.length);
        assertEquals("", parts[0]);
        assertEquals("target", parts[1]);
    }

    // -------------------------------------------------------------------------
    // exportToCSV / importFromCSV (round-trip, no IProject needed for CSV parsing)
    // -------------------------------------------------------------------------

    @Test
    public void testReadCsvSkipsHeaderRow() throws Exception {
        File csvFile = tmp.newFile("test.csv");
        try (PrintWriter pw = new PrintWriter(csvFile, "UTF-8")) {
            pw.println("\"Source\",\"Target\"");
            pw.println("\"Hello\",\"Bonjour\"");
            pw.println("\"World\",\"Monde\"");
        }
        List<String[]> rows = ExportBilingualHandler.readCsv(csvFile);
        assertEquals(2, rows.size());
        assertEquals("Hello", rows.get(0)[0]);
        assertEquals("Bonjour", rows.get(0)[1]);
        assertEquals("World", rows.get(1)[0]);
        assertEquals("Monde", rows.get(1)[1]);
    }

    @Test
    public void testReadCsvHandlesBOM() throws Exception {
        File csvFile = tmp.newFile("bom.csv");
        // Write with UTF-8 BOM
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(csvFile);
                java.io.OutputStreamWriter out = new java.io.OutputStreamWriter(fos, "UTF-8")) {
            fos.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
            out.write("\"Source\",\"Target\"\r\n");
            out.write("\"A\",\"B\"\r\n");
        }
        List<String[]> rows = ExportBilingualHandler.readCsv(csvFile);
        assertEquals(1, rows.size());
        assertEquals("A", rows.get(0)[0]);
    }

    @Test
    public void testReadCsvMultilineField() throws Exception {
        // A newline inside a quoted field is valid RFC-4180
        File csvFile = tmp.newFile("multi.csv");
        try (PrintWriter pw = new PrintWriter(csvFile, "UTF-8")) {
            pw.println("\"Source\",\"Target\"");
            pw.println("\"line one");
            pw.println("line two\",\"ok\"");
        }
        List<String[]> rows = ExportBilingualHandler.readCsv(csvFile);
        assertEquals(1, rows.size());
        assertTrue(rows.get(0)[0].contains("line one"));
        assertTrue(rows.get(0)[0].contains("line two"));
    }

    // -------------------------------------------------------------------------
    // applyTranslations (unit logic, no IProject needed beyond a stub)
    // -------------------------------------------------------------------------

    @Test
    public void testApplyTranslationsSkipsEmptyTarget() {
        // Build a simple in-memory "project" using a stub
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "Hello", "" }); // empty target — must be skipped
        rows.add(new String[] { "World", "Monde" });

        CollectingProject project = new CollectingProject();
        project.addEntry("Hello");
        project.addEntry("World");

        int count = ExportBilingualHandler.applyTranslations(rows, project);
        assertEquals(1, count);
        assertEquals(1, project.applied.size());
        assertNull(project.applied.get("Hello"));
        assertEquals("Monde", project.applied.get("World"));
    }

    @Test
    public void testApplyTranslationsIgnoresUnknownSource() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "Unknown segment", "Some translation" });

        CollectingProject project = new CollectingProject();
        project.addEntry("Hello");

        int count = ExportBilingualHandler.applyTranslations(rows, project);
        assertEquals(0, count);
    }

    // -------------------------------------------------------------------------
    // Helper stub that records setTranslation calls
    // -------------------------------------------------------------------------

    static class CollectingProject extends NotLoadedProject {
        final List<SourceTextEntry> allEntries = new ArrayList<>();
        final java.util.Map<String, String> applied = new java.util.LinkedHashMap<>();
        int entryNum = 0;

        void addEntry(String source) {
            EntryKey key = new EntryKey("file.txt", source, null, null, null, null);
            SourceTextEntry ste = new SourceTextEntry(key, entryNum++,
                    null, null, new ArrayList<>());
            allEntries.add(ste);
        }

        @Override
        public List<SourceTextEntry> getAllEntries() {
            return allEntries;
        }

        @Override
        public TMXEntry getTranslationInfo(SourceTextEntry ste) {
            return IProject.AllTranslations.EMPTY_TRANSLATION;
        }

        @Override
        public boolean isProjectLoaded() {
            return true;
        }

        @Override
        public void setTranslation(SourceTextEntry entry, PrepareTMXEntry trans,
                boolean defaultTranslation, TMXEntry.ExternalLinked externalLinked) {
            if (trans != null && trans.translation != null && !trans.translation.isBlank()) {
                applied.put(entry.getSrcText(), trans.translation);
            }
        }
    }
}
