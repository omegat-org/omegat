/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/
package org.omegat.core.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXReader2;
import org.omegat.util.TMXWriter2;

/**
 * Class for store data from project_save.tmx.
 * 
 * Orphaned or non-orphaned translation calculated by RealProject.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProjectTMX {
    protected static final String PROP_FILE = "file";
    protected static final String PROP_ID = "id";
    protected static final String PROP_PREV = "prev";
    protected static final String PROP_NEXT = "next";
    protected static final String PROP_PATH = "path";

    /**
     * Storage for default translations for current project.
     * 
     * It must be used with synchronization around ProjectTMX.
     */
    Map<String, TMXEntry> defaults;

    /**
     * Storage for alternative translations for current project.
     * 
     * It must be used with synchronization around ProjectTMX.
     */
    Map<EntryKey, TMXEntry> alternatives;
    
    final CheckOrphanedCallback checkOrphanedCallback;

    public ProjectTMX(Language sourceLanguage, Language targetLanguage, boolean isSentenceSegmentingEnabled, File file, CheckOrphanedCallback callback) throws Exception {
        this.checkOrphanedCallback = callback;
        alternatives = new HashMap<EntryKey, TMXEntry>();
        defaults = new HashMap<String, TMXEntry>();

        if (!file.exists()) {
            // file not exist - new project
            return;
        }

        new TMXReader2().readTMX(
                file,
                sourceLanguage,
                targetLanguage,
                isSentenceSegmentingEnabled,
                false,
                true,
                false,
                new Loader(sourceLanguage, targetLanguage, isSentenceSegmentingEnabled));
    }

    /**
     * Constructor for TMX delta.
     */
    public ProjectTMX() {
        alternatives = new HashMap<EntryKey, TMXEntry>();
        defaults = new HashMap<String, TMXEntry>();
        checkOrphanedCallback = null;
    }

    /**
     * Check TMX for empty.
     */
    public boolean isEmpty() {
        return defaults.isEmpty() && alternatives.isEmpty();
    }

    /**
     * It saves current translation into file.
     */
    public void save(ProjectProperties props, String translationFile, boolean translationUpdatedByUser)
            throws Exception {
        if (!translationUpdatedByUser) {
            Log.logInfoRB("LOG_DATAENGINE_SAVE_NONEED");
            return;
        }

        File newFile = new File(translationFile + OConsts.NEWFILE_EXTENSION);

        // Save data into '*.new' file
        exportTMX(props, newFile, false, false, true);

        File backup = new File(translationFile + OConsts.BACKUP_EXTENSION);
        File orig = new File(translationFile);
        if (backup.exists()) {
            if (!backup.delete()) {
                throw new IOException("Error delete backup file");
            }
        }

        // Rename existing project file in case a fatal error
        // is encountered during the write procedure - that way
        // everything won't be lost
        if (orig.exists()) {
            if (!orig.renameTo(backup)) {
                throw new IOException("Error rename old file to backup");
            }
        }

        // Rename new file into TMX file
        if (!newFile.renameTo(orig)) {
            throw new IOException("Error rename new file to tmx");
        }
    }

    public void exportTMX(ProjectProperties props, File outFile, final boolean forceValidTMX,
            final boolean levelTwo, final boolean useOrphaned) throws Exception {
        TMXWriter2 wr = new TMXWriter2(outFile, props.getSourceLanguage(), props.getTargetLanguage(),
                props.isSentenceSegmentingEnabled(), levelTwo, forceValidTMX);
        try {
            Map<String, TMXEntry> tempDefaults = new TreeMap<String, TMXEntry>();
            Map<EntryKey, TMXEntry> tempAlternatives = new TreeMap<EntryKey, TMXEntry>();

            synchronized (this) {
                if (useOrphaned) {
                    // fast call - just copy
                    tempDefaults.putAll(defaults);
                    tempAlternatives.putAll(alternatives);
                } else {
                    // slow call - copy non-orphaned only
                    for(Map.Entry<String, TMXEntry> en:defaults.entrySet()) {
                        if (checkOrphanedCallback.existSourceInProject(en.getKey())) {
                            tempDefaults.put(en.getKey(), en.getValue());
                        }
                    }
                    for(Map.Entry<EntryKey, TMXEntry> en:alternatives.entrySet()) {
                        if (checkOrphanedCallback.existEntryInProject(en.getKey())) {
                            tempAlternatives.put(en.getKey(), en.getValue());
                        }
                    }
                }
            }

            wr.writeComment(" Default translations ");
            for (Map.Entry<String, TMXEntry> en : new TreeMap<String, TMXEntry>(tempDefaults).entrySet()) {
                wr.writeEntry(en.getKey(), en.getValue().translation, en.getValue(), null);
            }

            wr.writeComment(" Alternative translations ");
            for (Map.Entry<EntryKey, TMXEntry> en : new TreeMap<EntryKey, TMXEntry>(tempAlternatives).entrySet()) {
                EntryKey k = en.getKey();
                wr.writeEntry(en.getKey().sourceText, en.getValue().translation, en.getValue(), new String[] {
                        PROP_FILE, k.file, PROP_ID, k.id, PROP_PREV, k.prev, PROP_NEXT, k.next, PROP_PATH,
                        k.path });
            }
        } finally {
            wr.close();
        }
    }

    /**
     * Get default translation or null if not exist.
     */
    public TMXEntry getDefaultTranslation(String source) {
        synchronized (this) {
            return defaults.get(source);
        }
    }

    /**
     * Get multiple translation or null if not exist.
     */
    public TMXEntry getMultipleTranslation(EntryKey ek) {
        synchronized (this) {
            return alternatives.get(ek);
        }
    }

    /**
     * Set new translation.
     */
    public void setTranslation(SourceTextEntry ste, TMXEntry te, boolean isDefault) {
        synchronized (this) {
            if (te == null) {
                if (isDefault) {
                    defaults.remove(ste.getKey().sourceText);
                } else {
                    alternatives.remove(ste.getKey());
                }
            } else {
                if (isDefault) {
                    defaults.put(ste.getKey().sourceText, te);
                } else {
                    alternatives.put(ste.getKey(), te);
                }
            }
        }
    }

    private class Loader implements TMXReader2.LoadCallback {
        private final Language sourceLang;
        private final Language targetLang;
        private final boolean sentenceSegmentingEnabled;

        public Loader(Language sourceLang, Language targetLang,
                boolean sentenceSegmentingEnabled) {
            this.sourceLang = sourceLang;
            this.targetLang = targetLang;
            this.sentenceSegmentingEnabled = sentenceSegmentingEnabled;
        }

        public boolean onEntry(TMXReader2.ParsedTu tu, TMXReader2.ParsedTuv tuvSource,
                TMXReader2.ParsedTuv tuvTarget, boolean isParagraphSegtype) {
            if (tuvSource == null) {
                // source Tuv not found
                return false;
            }
            String changer = null;
            long dt = 0;
            String translation = null;

            if (tuvTarget != null) {
                changer = StringUtil.nvl(tuvTarget.changeid, tuvTarget.creationid, tu.changeid,
                    tu.creationid);
                dt = StringUtil.nvlLong(tuvTarget.changedate, tuvTarget.creationdate, tu.changedate,
                    tu.creationdate);
                translation = tuvTarget.text;
            }

            List<String> sources = new ArrayList<String>();
            List<String> targets = new ArrayList<String>();
            Segmenter.segmentEntries(sentenceSegmentingEnabled && isParagraphSegtype, sourceLang,
                    tuvSource.text, targetLang, translation, sources, targets);

            synchronized (this) {
                for (int i = 0; i < sources.size(); i++) {
                    String segmentSource = sources.get(i);
                    String segmentTranslation = targets.get(i);
                    EntryKey key = createKeyByProps(segmentSource, tu.props);
                    boolean defaultTranslation = key.file == null;
                    TMXEntry te = new TMXEntry(segmentSource, segmentTranslation, changer, dt, tu.note,
                            defaultTranslation);
                    if (defaultTranslation) {
                        // default translation
                        defaults.put(segmentSource, te);
                    } else {
                        // multiple translation
                        alternatives.put(key, te);
                    }
                }
            }
            return true;
        }
    };

    private EntryKey createKeyByProps(String src, Map<String, String> props) {
        return new EntryKey(props.get(PROP_FILE), src, props.get(PROP_ID), props.get(PROP_PREV),
                props.get(PROP_NEXT), props.get(PROP_PATH));
    }

    /**
     * Returns the collection of TMX entries that have a default translation
     * @return
     */
    public Collection<TMXEntry> getDefaults() {
        return defaults.values();
    }
    /**
     * Returns the collection of TMX entries that have an alternative translation
     * @return
     */
    public Collection<TMXEntry> getAlternatives() {
        return alternatives.values();
    }

    public interface CheckOrphanedCallback {
        boolean existEntryInProject(EntryKey key);

        boolean existSourceInProject(String src);
    }

    /**
     * Calculates delta between base and changed TMX.
     *
     * @return a tmx with all updated/removed/added entries in changedTMX compared to baseTMX.
     */
    public static ProjectTMX calculateDelta(ProjectTMX baseTMX, ProjectTMX changedTMX) {
        ProjectTMX delta = new ProjectTMX();

        // find updated and removed
        for (Map.Entry<String, TMXEntry> en : baseTMX.defaults.entrySet()) {
            TMXEntry newEntry = changedTMX.defaults.get(en.getKey());

            if (!en.getValue().equalsTranslation(newEntry)) {
                delta.defaults.put(en.getKey(), newEntry);
            }
        }
        for (Map.Entry<EntryKey, TMXEntry> en : baseTMX.alternatives.entrySet()) {
            TMXEntry newEntry = changedTMX.alternatives.get(en.getKey());

            if (!en.getValue().equalsTranslation(newEntry)) {
                delta.alternatives.put(en.getKey(), newEntry);
            }
        }

        // find added
        if (changedTMX.defaults != null) {
            for (Map.Entry<String, TMXEntry> en : changedTMX.defaults.entrySet()) {
                if (!baseTMX.defaults.containsKey(en.getKey())) {
                    delta.defaults.put(en.getKey(), en.getValue());
                }
            }
        }
        for (Map.Entry<EntryKey, TMXEntry> en : changedTMX.alternatives.entrySet()) {
            if (!baseTMX.alternatives.containsKey(en.getKey())) {
                delta.alternatives.put(en.getKey(), en.getValue());
            }
        }

        return delta;
    }

    /**
     * Replaces the current translations with those of the given TMX, and applies the delta on the translations
     *
     * @param newTMX the translation memory of which the translations have to be used
     * @param delta the delta that has to be applied on the new TMX.
     */
    void applyTMXandDelta(ProjectTMX newTMX, ProjectTMX delta) {
        defaults = newTMX.defaults;
        alternatives = newTMX.alternatives;

        for (Map.Entry<String, TMXEntry> en : delta.defaults.entrySet()) {
            if (en.getValue() != null) {
                defaults.put(en.getKey(), en.getValue());
            } else {
                defaults.remove(en.getKey());
            }
        }
        for (Map.Entry<EntryKey, TMXEntry> en : delta.alternatives.entrySet()) {
            if (en.getValue() != null) {
                alternatives.put(en.getKey(), en.getValue());
            } else {
                alternatives.remove(en.getKey());
            }
        }
    }
}
