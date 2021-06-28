/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009-2010 Alex Buloichik
               2013 Alex Buloichik
               2015 Didier Briel
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.glossaries.IGlossary;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.DirectoryMonitor;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.Token;

/**
 * Class that loads glossary files and adds glossary entries to strings of the source files.
 *
 * This class don't need any threads synchronization code, since it only set and clear 'glossaryEntries' var.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Didier Briel
 */
public class GlossaryManager implements DirectoryMonitor.Callback {

    /**
     * Create new default writable glossary file.
     * @param file a file to be created.
     * @return true if the file was successfully created
     * @throws IOException when there is a problem to create file.
     */
    public static boolean createNewWritableGlossaryFile(File file) throws IOException {
        if (file.exists()) {
            return false;
        }
        String ext = "." + FilenameUtils.getExtension(file.getPath()).toLowerCase(Locale.ENGLISH);
        switch (ext) {
        case OConsts.EXT_TSV_DEF: // fallthrough
        case OConsts.EXT_TSV_TSV: // fallthrough
        case OConsts.EXT_TSV_TXT: // fallthrough
        case OConsts.EXT_TSV_UTF8:
            return GlossaryReaderTSV.createEmpty(file);
        default:
            // Creation not supported
            return false;
        }
    }

    protected DirectoryMonitor monitor;

    private final GlossaryTextArea pane;
    private final Map<String, List<GlossaryEntry>> glossaries = new TreeMap<String, List<GlossaryEntry>>();

    protected File priorityGlossary;
    protected IGlossary[] externalGlossaries;

    public GlossaryManager(final GlossaryTextArea pane) {
        this.pane = pane;

        List<IGlossary> gl = new ArrayList<IGlossary>();
        for (Class<?> glc : PluginUtils.getGlossaryClasses()) {
            try {
                gl.add((IGlossary) glc.getDeclaredConstructor().newInstance());
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
        externalGlossaries = gl.toArray(new IGlossary[gl.size()]);
        Preferences.addPropertyChangeListener(e -> {
            if (Core.getProject().isProjectLoaded()) {
                switch (e.getPropertyName()) {
                case Preferences.GLOSSARY_TBX_DISPLAY_CONTEXT:
                    forceReloadTBX();
                    break;
                case Preferences.GLOSSARY_NOT_EXACT_MATCH:
                case Preferences.GLOSSARY_STEMMING:
                    forceUpdateGlossary();
                    break;
                }
            }
        });
    }

    public void addGlossaryProvider(IGlossary provider) {
        List<IGlossary> providers = new ArrayList<IGlossary>(Arrays.asList(externalGlossaries));
        providers.add(provider);
        externalGlossaries = providers.toArray(new IGlossary[providers.size()]);
    }

    public void start() {
        File dir = new File(Core.getProject().getProjectProperties().getGlossaryRoot());
        priorityGlossary = new File(Core.getProject().getProjectProperties().getWriteableGlossary());
        monitor = new DirectoryMonitor(dir, this);
        monitor.start();
    }

    public void stop() {
        monitor.fin();
        synchronized (this) {
            glossaries.clear();
        }
    }

    @Override
    public void fileChanged(File file) {
        synchronized (this) {
            glossaries.remove(file.getPath());
        }
        if (file.exists()) {
            try {
                List<GlossaryEntry> entries = loadGlossaryFile(file);
                if (entries != null) {
                    synchronized (this) {
                        Log.logRB("CT_LOADING_GLOSSARY_DETAILS", entries.size(), file.getName());
                        glossaries.put(file.getPath(), entries);
                    }
                }
            } catch (Exception ex) {
                Log.logRB("CT_ERROR_ACCESS_GLOSSARY_DIR");
                Log.log(ex);
            }
        }
        pane.refresh();
    }

    public void forceReloadTBX() {
        Set<File> files = monitor.getExistFiles();
        for (File f : files) {
            if (f.getName().toLowerCase(Locale.ENGLISH).endsWith(OConsts.EXT_TBX)) {
                fileChanged(f);
            }
        }
    }

    public void forceUpdateGlossary() {
        pane.refresh();
    }

    /**
     * Loads one glossary file. It choose and calls required required reader.
     */
    private List<GlossaryEntry> loadGlossaryFile(final File file) throws Exception {
        boolean isPriority = priorityGlossary.equals(file);
        String fnameLower = file.getName().toLowerCase(Locale.ENGLISH);
        if (fnameLower.endsWith(OConsts.EXT_TSV_DEF)) {
            Log.logRB("CT_LOADING_GLOSSARY", file.getName());
            return GlossaryReaderTSV.read(file, isPriority);
        } else if (fnameLower.endsWith(OConsts.EXT_TSV_UTF8) ||
                   fnameLower.endsWith(OConsts.EXT_TSV_TXT) ||
                   fnameLower.endsWith(OConsts.EXT_TSV_TSV)) {
            Log.logRB("CT_LOADING_GLOSSARY", file.getName());
            return GlossaryReaderTSV.read(file, isPriority);
        } else if (fnameLower.endsWith(OConsts.EXT_CSV_UTF8)) {
            Log.logRB("CT_LOADING_GLOSSARY", file.getName());
            return GlossaryReaderCSV.read(file, isPriority);
        } else if (fnameLower.endsWith(OConsts.EXT_TBX)) {
            Log.logRB("CT_LOADING_GLOSSARY", file.getName());
            return GlossaryReaderTBX.read(file, isPriority);
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

    /**
     * Get glossary entries for search operation. Almost the same as getGlossaryEntries(), except search
     * usually executed for every segment in project, i.e. should work enough fast. Then, search should be
     * produced by local files only.
     *
     * @return all entries
     */
    public List<GlossaryEntry> getLocalEntries() {
        List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();
        synchronized (this) {
            for (List<GlossaryEntry> en : glossaries.values()) {
                result.addAll(en);
            }
        }

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

    /**
     * Get all glossary entries with source terms found in the provided string.
     *
     * @return A list of matching glossary entries
     */
    public List<GlossaryEntry> searchSourceMatches(SourceTextEntry ste) {
        ITokenizer tok = Core.getProject().getSourceTokenizer();
        if (tok == null) {
            return Collections.emptyList();
        }

        List<GlossaryEntry> entries = getGlossaryEntries(ste.getSrcText());
        if (entries == null) {
            return Collections.emptyList();
        }

        GlossarySearcher searcher = buildSearcher(tok, Core.getProject().getProjectProperties().getSourceLanguage());

        return searcher.searchSourceMatches(ste, entries);
    }

    /**
     * Get tokens of the source text that match the supplied glossary entry.
     *
     * @param ste
     *            The entry to search
     * @return A list of tokens matching the supplied glossary entry
     */
    public List<Token[]> searchSourceMatchTokens(SourceTextEntry ste, GlossaryEntry entry) {
        ITokenizer tok = Core.getProject().getSourceTokenizer();
        if (tok == null) {
            return Collections.emptyList();
        }
        GlossarySearcher searcher = buildSearcher(tok, Core.getProject().getProjectProperties().getSourceLanguage());

        return searcher.searchSourceMatchTokens(ste, entry);
    }

    /**
     * Get all target terms for the provided glossary entry that can be found in the provided string.
     *
     * @param trg
     *            The text to search
     * @param protectedParts
     *            A list of protected parts from which matches should be disregarded (can be null)
     * @param entry
     *            The glossary entry whose target terms should be searched
     * @return A list of matching target terms
     */
    public List<String> searchTargetMatches(String trg, ProtectedPart[] protectedParts, GlossaryEntry entry) {
        ITokenizer tok = Core.getProject().getTargetTokenizer();
        if (tok == null) {
            return Collections.emptyList();
        }
        GlossarySearcher searcher = buildSearcher(tok, Core.getProject().getProjectProperties().getTargetLanguage());

        return searcher.searchTargetMatches(trg, protectedParts, entry);
    }

    private GlossarySearcher buildSearcher(ITokenizer tokenizer, Language language) {
        boolean merge = Preferences.isPreferenceDefault(Preferences.GLOSSARY_MERGE_ALTERNATE_DEFINITIONS,
                Preferences.GLOSSARY_MERGE_ALTERNATE_DEFINITIONS_DEFAULT);
        return new GlossarySearcher(tokenizer, language, merge);
    }
}
