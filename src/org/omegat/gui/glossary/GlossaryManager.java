/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009-2010 Alex Buloichik
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.omegat.core.Core;
import org.omegat.core.glossaries.IGlossary;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.DirectoryMonitor;
import org.omegat.util.Language;
import org.omegat.util.Log;

/**
 * Class that loads glossary files and adds glossary entries to strings of the source files.
 * 
 * This class don't need any threads synchronization code, since it only set and clear 'glossaryEntries' var.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class GlossaryManager implements DirectoryMonitor.Callback {
    protected static final String EXT_TSV_DEF = ".tab";
    protected static final String EXT_TSV_UTF8 = ".utf8";
    protected static final String EXT_TSV_TXT = ".txt";
    protected static final String EXT_CSV_UTF8 = ".csv";
    protected static final String EXT_TBX = ".tbx";

    protected DirectoryMonitor monitor;

    private final GlossaryTextArea pane;
    private final Map<String, List<GlossaryEntry>> glossaries = new TreeMap<String, List<GlossaryEntry>>();

    protected final IGlossary[] externalGlossaries;

    public GlossaryManager(final GlossaryTextArea pane) {
        this.pane = pane;

        List<IGlossary> gl = new ArrayList<IGlossary>();
        for (Class<?> glc : PluginUtils.getGlossaryClasses()) {
            try {
                gl.add((IGlossary) glc.newInstance());
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
        externalGlossaries = gl.toArray(new IGlossary[gl.size()]);
    }

    public void start() {
        File dir = new File(Core.getProject().getProjectProperties().getGlossaryRoot());
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
            try {
                List<GlossaryEntry> entries = loadGlossaryFile(file);
                if (entries != null) {
                    synchronized (this) {
                        glossaries.put(file.getName(), entries);
                    }
                }
            } catch (Exception ex) {
                Log.logRB("CT_ERROR_ACCESS_GLOSSARY_DIR");
                Log.log(ex);
            }
        }
        pane.refresh();
    }

    /**
     * Loads one glossary file. It choose and calls required required reader.
     */
    private List<GlossaryEntry> loadGlossaryFile(final File file) throws Exception {
        String fname_lower = file.getName().toLowerCase();
        if (fname_lower.endsWith(EXT_TSV_DEF)) {
            Log.logRB("CT_LOADING_GLOSSARY", new Object[] { file.getName() });
            return GlossaryReaderTSV.read(file);
        } else if (fname_lower.endsWith(EXT_TSV_UTF8) || fname_lower.endsWith(EXT_TSV_TXT)) {
            Log.logRB("CT_LOADING_GLOSSARY", new Object[] { file.getName() });
            return GlossaryReaderTSV.read(file);
        } else if (fname_lower.endsWith(EXT_CSV_UTF8)) {
            Log.logRB("CT_LOADING_GLOSSARY", new Object[] { file.getName() });
            return GlossaryReaderCSV.read(file);
        } else if (fname_lower.endsWith(EXT_TBX)) {
            Log.logRB("CT_LOADING_GLOSSARY", new Object[] { file.getName() });
            return GlossaryReaderTBX.read(file);
        } else {
            return null;
        }
    }

    /**
     * Get glossary entries.
     * 
     * @return all entries
     * @param src
     */
    public List<GlossaryEntry> getGlossaryEntries(String src) {
        List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();
        synchronized (this) {
            for (List<GlossaryEntry> en : glossaries.values()) {
                result.addAll(en);
            }
        }

        addExternalGlossaryEntries(result, src);

        return result;
    }

    private void addExternalGlossaryEntries(List<GlossaryEntry> result, String src) {

        Language source = Core.getProject().getProjectProperties().getSourceLanguage();
        Language target = Core.getProject().getProjectProperties().getTargetLanguage();

        for (IGlossary gl : externalGlossaries) {
            try {
                result.addAll(gl.search(source, target, src));
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
    }
}
