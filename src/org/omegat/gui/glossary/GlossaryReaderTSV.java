/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Alex Buloichik
               2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.omegat.filters2.EncodingDetector;
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
public class GlossaryReaderTSV {
    public static List<GlossaryEntry> read(final File file, boolean priorityGlossary) throws IOException {
        String encoding;
        String fname_lower = file.getName().toLowerCase();
        if (fname_lower.endsWith(OConsts.EXT_TSV_DEF) || fname_lower.endsWith(OConsts.EXT_TSV_TXT)) {
            encoding = EncodingDetector.detectEncodingDefault(file, Charset.defaultCharset().name());
        } else if (fname_lower.endsWith(OConsts.EXT_TSV_UTF8)) {
            encoding = OConsts.UTF8;
        } else {
            return null;
        }
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), encoding);

        List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();
        BufferedReader in = new BufferedReader(reader);
        try {
            // BOM (byte order mark) bugfix
            in.mark(1);
            int ch = in.read();
            if (ch != 0xFEFF)
                in.reset();

            for (String s = in.readLine(); s != null; s = in.readLine()) {
                // skip lines that start with '#'
                if (s.startsWith("#"))
                    continue;

                // divide lines on tabs
                String tokens[] = s.split("\t");
                // check token list to see if it has a valid string
                if (tokens.length < 2 || tokens[0].isEmpty())
                    continue;

                // creating glossary entry and add it to the hash
                // (even if it's already there!)
                String comment = "";
                if (tokens.length >= 3)
                    comment = tokens[2];
                result.add(new GlossaryEntry(tokens[0], tokens[1], comment, priorityGlossary));
            }
        } finally {
            in.close();
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
    public static void append(final File file, GlossaryEntry newEntry) throws IOException {
        String encoding = OConsts.UTF8;
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
            }
            file.createNewFile();
        } else {
            encoding = EncodingDetector.detectEncodingDefault(file, OConsts.UTF8);
        }
        Writer wr = new OutputStreamWriter(new FileOutputStream(file, true), encoding);
        wr.append(newEntry.getSrcText()).append('\t').append(newEntry.getLocText());
        if (!StringUtil.isEmpty(newEntry.getCommentText())) {
            wr.append('\t').append(newEntry.getCommentText());
        }
        wr.append(System.getProperty("line.separator"));
        wr.close();
    }
}
