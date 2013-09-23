/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.omegat.util.OConsts;

/**
 * Reader for comma separated glossaries.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class GlossaryReaderCSV {
    /** Fields separator. Can be dependent of regional options. */
    protected static final char SEPARATOR = ',';

    public static List<GlossaryEntry> read(final File file, boolean priorityGlossary) throws IOException {
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), OConsts.UTF8);

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
                String tokens[] = parseLine(s);
                // check token list to see if it has a valid string
                if (tokens.length < 2 || tokens[0].length() == 0)
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

    private static String[] parseLine(String line) {
        List<String> result = new ArrayList<String>();
        StringBuilder w = new StringBuilder();
        boolean fopened = false; // field opened by "
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            char cn;
            try {
                cn = line.charAt(i + 1);
            } catch (StringIndexOutOfBoundsException ex) {
                cn = 0;
            }
            switch (c) {
            case '"':
                if (w.length() == 0 && !fopened) {
                    // first " in field
                    fopened = true;
                } else if (cn == '"') {
                    // double " - add one
                    w.append(c);
                    i++;
                } else {
                    // last " in field
                    fopened = false;
                }
                break;
            case SEPARATOR:
                if (fopened) {
                    w.append(c);
                } else {
                    result.add(w.toString());
                    w.setLength(0);
                }
                break;
            default:
                w.append(c);
                break;
            }
        }
        result.add(w.toString());
        return result.toArray(new String[result.size()]);
    }
}
