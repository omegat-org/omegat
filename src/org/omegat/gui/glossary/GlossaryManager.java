/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.glossary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.omegat.core.Core;
import org.omegat.util.DirectoryMonitor;
import org.omegat.util.Log;

/**
 * Class that loads glossary files and adds glossary entries to strings of the
 * source files.
 * 
 * This class don't need any threads synchronization code, since it only set and
 * clear 'glossaryEntries' var.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class GlossaryManager implements DirectoryMonitor.Callback {
    private final String EXT_DEF_ENC = ".tab";
    private final String EXT_UTF8_ENC = ".utf8";

    protected DirectoryMonitor monitor;

    private final GlossaryTextArea pane;
    private final Map<String, List<GlossaryEntry>> glossaries = new TreeMap<String, List<GlossaryEntry>>();

    public GlossaryManager(final GlossaryTextArea pane) {
        this.pane = pane;
    }

    public void start() {
        File dir = new File(Core.getProject().getProjectProperties()
                .getGlossaryRoot());
        monitor = new DirectoryMonitor(dir, this);
        monitor.start();
    }

    public void stop() {
        monitor.fin();
        synchronized (this) {
            glossaries.clear();
        }
    }

    public void fileChanged(File file) {
        synchronized (this) {
            glossaries.remove(file.getName());
        }
        if (file.exists()) {
            String fname_lower = file.getName().toLowerCase();
            if (fname_lower.endsWith(EXT_DEF_ENC)
                    || fname_lower.endsWith(EXT_UTF8_ENC)) {
                Log.logRB("CT_LOADING_GLOSSARY",
                        new Object[] { file.getName() });
                try {
                    List<GlossaryEntry> entries = loadGlossaryFile(file);
                    synchronized (this) {
                        glossaries.put(file.getName(), entries);
                    }
                } catch (Exception ex) {
                    Log.logRB("CT_ERROR_ACCESS_GLOSSARY_DIR");
                    Log.log(ex);
                }
            }
        }
        pane.refresh();
    }

    /**
     * Loads one glossary file. Detects a file format and loads a file in
     * appropriate encoding.
     */
    private List<GlossaryEntry> loadGlossaryFile(final File file)
            throws FileNotFoundException, UnsupportedEncodingException,
            IOException {
        final List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();
        String fname_lower = file.getName().toLowerCase();
        InputStreamReader reader = null;
        if (fname_lower.endsWith(EXT_DEF_ENC)) {
            reader = new InputStreamReader(new FileInputStream(file));
        } else if (fname_lower.endsWith(EXT_UTF8_ENC)) {
            InputStream fis = new FileInputStream(file);
            reader = new InputStreamReader(fis, "UTF-8"); // NOI18N
        }

        BufferedReader in = new BufferedReader(reader);

        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch != 0xFEFF)
            in.reset();

        for (String s = in.readLine(); s != null; s = in.readLine()) {
            // skip lines that start with '#'
            if (s.startsWith("#")) // NOI18N
                continue;

            // divide lines on tabs
            String tokens[] = s.split("\t"); // NOI18N
            // check token list to see if it has a valid string
            if (tokens.length < 2 || tokens[0].length() == 0)
                continue;

            // creating glossary entry and add it to the hash
            // (even if it's already there!)
            String comment = ""; // NOI18N
            if (tokens.length >= 3)
                comment = tokens[2];
            result.add(new GlossaryEntry(tokens[0], tokens[1], comment));
        }
        in.close();

        return result;
    }

    /**
     * Get glossary entries.
     * 
     * @return all entries
     */
    public List<GlossaryEntry> getGlossaryEntries() {
        List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();
        synchronized (this) {
            for (List<GlossaryEntry> en : glossaries.values()) {
                result.addAll(en);
            }
        }
        return result;
    }
}
