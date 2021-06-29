/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Alex Buloichik
               2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.glossary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.omegat.util.EncodingDetector;
import org.omegat.util.Log;
import org.omegat.util.MagicComment;
import org.omegat.util.OConsts;
import org.omegat.util.StringUtil;

/**
 * Reader for tab separated glossaries.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Aaron Madlon-Kay
 */
public final class GlossaryReaderTSV {

    private GlossaryReaderTSV() {
    }

    /**
     * Create a new empty TSV glossary file with a leading comment
     * @param file
     * @return true if the file was created successfully
     * @throws IOException
     */
    public static boolean createEmpty(File file) throws IOException {
        if (file.exists()) {
            return false;
        }
        file.getParentFile().mkdirs();
        if (file.createNewFile()) {
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                writer.write("# Glossary in tab-separated format -*- coding: utf-8 -*-");
                writer.write(System.lineSeparator());
            }
            return true;
        }
        return false;
    }

    public static String getFileEncoding(final File file) throws IOException {
        return getFileEncoding(file, Charset.defaultCharset().name());
    }

    public static String getFileEncoding(final File file, String defaultEncoding) throws IOException {
        String fnameLower = file.getName().toLowerCase(Locale.ENGLISH);
        if (fnameLower.endsWith(OConsts.EXT_TSV_UTF8)) {
            return StandardCharsets.UTF_8.name();
        } else {
            return detectEncodingDefault(file, defaultEncoding);
        }
    }

    private static String detectEncodingDefault(final File inFile, final String defaultEncoding) {
        try {
            Map<String, String> magic = MagicComment.parse(inFile);
            String detected = magic.get("coding");
            if (detected != null) {
                return detected;
            }
        } catch (IOException e) {
            Log.log(e);
        }
        return EncodingDetector.detectEncodingDefault(inFile, defaultEncoding);
    }

    public static List<GlossaryEntry> read(final File file, boolean priorityGlossary) throws IOException {
        String encoding = getFileEncoding(file);
        List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            // BOM (byte order mark) bugfix
            in.mark(1);
            int ch = in.read();
            if (ch != 0xFEFF) {
                in.reset();
            }
            for (String s = in.readLine(); s != null; s = in.readLine()) {
                // skip lines that start with '#'
                if (s.startsWith("#")) {
                    continue;
                }
                // divide lines on tabs
                String[] tokens = s.split("\t");
                for (int i = 0; i < tokens.length; i++) {
                    tokens[i] = tokens[i].trim();
                }
                // check token list to see if it has a valid string
                if (tokens.length < 2 || tokens[0].isEmpty()) {
                    continue;
                }
                // creating glossary entry and add it to the hash
                // (even if it's already there!)
                String comment = "";
                if (tokens.length >= 3) {
                    comment = tokens[2];
                }
                result.add(new GlossaryEntry(tokens[0], tokens[1], comment, priorityGlossary, file.getPath()));
            }
        }

        return result;
    }

    /**
     * Appends entry to glossary file. If file does not exist yet, it will be created.
     *
     * @param file The file to (create and) append to
     * @param newEntry the entry to append.
     * @throws IOException
     */
    public static synchronized void append(final File file, GlossaryEntry newEntry) throws IOException {
        Charset encoding;
        if (!file.exists()) {
            createEmpty(file);
            encoding = StandardCharsets.UTF_8;
        } else {
            encoding = Charset.forName(getFileEncoding(file, StandardCharsets.UTF_8.name()));
        }
        // UTF-8 is a superset of ASCII, so always prefer UTF-8
        if (encoding.equals(StandardCharsets.US_ASCII)) {
            encoding = StandardCharsets.UTF_8;
        }
        try (BufferedWriter wr = Files.newBufferedWriter(file.toPath(), encoding, StandardOpenOption.APPEND)) {
            wr.append(newEntry.getSrcText()).append('\t').append(newEntry.getLocText());
            if (!StringUtil.isEmpty(newEntry.getCommentText())) {
                wr.append('\t').append(newEntry.getCommentText());
            }
            wr.append(System.lineSeparator());
        }
    }
}
