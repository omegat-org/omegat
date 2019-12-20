/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               2013-2014 Aaron Madlon-Kay, Alex Buloichik
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
package org.omegat.core.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omegat.core.Core;
import org.omegat.util.FileUtil;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXReader2;
import org.omegat.util.TMXWriter2;

/**
 * Class for store data from project_save.tmx.
 *
 * Orphaned or non-orphaned translation calculated by RealProject.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class ProjectTMX {

    protected static final String ATTR_TUID = "tuid";
    protected static final String PROP_FILE = "file";
    protected static final String PROP_ID = TMXWriter2.PROP_ID;
    protected static final String PROP_PREV = "prev";
    protected static final String PROP_NEXT = "next";
    protected static final String PROP_PATH = "path";
    protected static final String PROP_XICE = "x-ice";
    protected static final String PROP_X100PC = "x-100pc";
    protected static final String PROP_XAUTO = "x-auto";

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

    public ProjectTMX(Language sourceLanguage, Language targetLanguage, boolean isSentenceSegmentingEnabled,
            File file, CheckOrphanedCallback callback) throws Exception {
        this.checkOrphanedCallback = callback;
        alternatives = new HashMap<EntryKey, TMXEntry>();
        defaults = new HashMap<String, TMXEntry>();

        if (file == null || !file.exists()) {
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
                Preferences.isPreference(Preferences.EXT_TMX_USE_SLASH),
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
            if (new File(translationFile).exists()) {
                // if there is no file - need to save it
                Log.logInfoRB("LOG_DATAENGINE_SAVE_NONEED");
                return;
            }
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
            FileUtil.rename(orig, backup);
        }

        // Rename new file into TMX file
        FileUtil.rename(newFile, orig);
    }

    public void exportTMX(ProjectProperties props, File outFile, final boolean forceValidTMX,
            final boolean levelTwo, final boolean useOrphaned) throws Exception {
        TMXWriter2 wr = new TMXWriter2(outFile, props.getSourceLanguage(), props.getTargetLanguage(),
                props.isSentenceSegmentingEnabled(), levelTwo, forceValidTMX);
        try {
            Map<String, TMXEntry> tempDefaults = new TreeMap<>();
            Map<EntryKey, TMXEntry> tempAlternatives = new TreeMap<>();

            synchronized (this) {
                if (useOrphaned) {
                    // fast call - just copy
                    tempDefaults.putAll(defaults);
                    tempAlternatives.putAll(alternatives);
                } else {
                    // slow call - copy non-orphaned only
                    for (Map.Entry<String, TMXEntry> en : defaults.entrySet()) {
                        if (checkOrphanedCallback.existSourceInProject(en.getKey())) {
                            tempDefaults.put(en.getKey(), en.getValue());
                        }
                    }
                    for (Map.Entry<EntryKey, TMXEntry> en : alternatives.entrySet()) {
                        if (checkOrphanedCallback.existEntryInProject(en.getKey())) {
                            tempAlternatives.put(en.getKey(), en.getValue());
                        }
                    }
                }
            }

            List<String> p = new ArrayList<>();
            wr.writeComment(" Default translations ");
            for (Map.Entry<String, TMXEntry> en : new TreeMap<>(tempDefaults).entrySet()) {
                p.clear();
                if (Preferences.isPreferenceDefault(Preferences.SAVE_AUTO_STATUS, false)) {
                    if (en.getValue().linked == TMXEntry.ExternalLinked.xAUTO) {
                        p.add(PROP_XAUTO);
                        p.add("auto");
                    }
                }
                wr.writeEntry(en.getKey(), en.getValue().translation, en.getValue(), p);
            }

            wr.writeComment(" Alternative translations ");
            for (Map.Entry<EntryKey, TMXEntry> en : new TreeMap<>(tempAlternatives).entrySet()) {
                EntryKey k = en.getKey();
                p.clear();
                p.add(PROP_FILE);
                p.add(k.file);
                p.add(PROP_ID);
                p.add(k.id);
                p.add(PROP_PREV);
                p.add(k.prev);
                p.add(PROP_NEXT);
                p.add(k.next);
                p.add(PROP_PATH);
                p.add(k.path);
                if (Preferences.isPreferenceDefault(Preferences.SAVE_AUTO_STATUS, false)) {
                    if (en.getValue().linked == TMXEntry.ExternalLinked.xICE) {
                        p.add(PROP_XICE);
                        p.add(k.id);
                    } else if (en.getValue().linked == TMXEntry.ExternalLinked.x100PC) {
                        p.add(PROP_X100PC);
                        p.add(k.id);
                    }
                }
                wr.writeEntry(en.getKey().sourceText, en.getValue().translation, en.getValue(), p);
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
                if (!ste.getSrcText().equals(te.source)) {
                    throw new IllegalArgumentException("Source must be the same as in SourceTextEntry");
                }
                if (isDefault != te.defaultTranslation) {
                    throw new IllegalArgumentException("Default/alternative must be the same");
                }
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

        Loader(Language sourceLang, Language targetLang, boolean sentenceSegmentingEnabled) {
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
            String creator = null;
            long created = 0;
            String changer = null;
            long changed = 0;
            String translation = null;

            if (tuvTarget != null) {
                creator = StringUtil.nvl(tuvTarget.creationid, tu.creationid);
                created = StringUtil.nvlLong(tuvTarget.creationdate, tu.creationdate);
                changer = StringUtil.nvl(tuvTarget.changeid, tuvTarget.creationid, tu.changeid,
                        tu.creationid);
                changed = StringUtil.nvlLong(tuvTarget.changedate, tuvTarget.creationdate, tu.changedate,
                    tu.creationdate);
                translation = tuvTarget.text;
            }

            List<String> sources = new ArrayList<String>();
            List<String> targets = new ArrayList<String>();
            Core.getSegmenter().segmentEntries(sentenceSegmentingEnabled && isParagraphSegtype, sourceLang,
                    tuvSource.text, targetLang, translation, sources, targets);

            synchronized (this) {
                for (int i = 0; i < sources.size(); i++) {
                    String segmentSource = sources.get(i);
                    String segmentTranslation = targets.get(i);

                    PrepareTMXEntry te = new PrepareTMXEntry();
                    te.source = segmentSource;
                    te.translation = segmentTranslation;
                    te.changer = changer;
                    te.changeDate = changed;
                    te.creator = creator;
                    te.creationDate = created;
                    te.note = tu.note;
                    te.otherProperties = tu.props;

                    String id = te.getPropValue(PROP_ID);
                    if (id == null) {
                        // Use TMX @tuid if available and "id" prop was not
                        // present
                        id = te.getPropValue(ATTR_TUID);
                    }

                    EntryKey key = new EntryKey(te.getPropValue(PROP_FILE), te.source,
                            id, te.getPropValue(PROP_PREV), te.getPropValue(PROP_NEXT),
                            te.getPropValue(PROP_PATH));

                    TMXEntry.ExternalLinked externalLinkedMode = calcExternalLinkedMode(te);

                    boolean defaultTranslation = key.file == null;
                    if (te.otherProperties != null && te.otherProperties.isEmpty()) {
                        te.otherProperties = null;
                    }

                    if (defaultTranslation) {
                        // default translation
                        defaults.put(segmentSource, new TMXEntry(te, true, externalLinkedMode));
                    } else {
                        // multiple translation
                        alternatives.put(key, new TMXEntry(te, false, externalLinkedMode));
                    }
                }
            }
            return true;
        }
    };

    private TMXEntry.ExternalLinked calcExternalLinkedMode(PrepareTMXEntry te) {
        String id = te.getPropValue(PROP_ID);
        if (id == null) {
            id = te.getPropValue(ATTR_TUID);
        }
        TMXEntry.ExternalLinked externalLinked = null;
        if (externalLinked == null && te.hasPropValue(PROP_XICE, id)) {
            externalLinked = TMXEntry.ExternalLinked.xICE;
        }
        if (externalLinked == null && te.hasPropValue(PROP_X100PC, id)) {
            externalLinked = TMXEntry.ExternalLinked.x100PC;
        }
        if (externalLinked == null && te.hasPropValue(PROP_XAUTO, null)) {
            externalLinked = TMXEntry.ExternalLinked.xAUTO;
        }
        return externalLinked;
    }

    /**
     * Returns the collection of TMX entries that have a default translation
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

    public void replaceContent(ProjectTMX tmx) {
        synchronized (this) {
            defaults = tmx.defaults;
            alternatives = tmx.alternatives;
        }
    }

    @Override
    public String toString() {
        return "[" + Stream.concat(
                defaults.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                        .map(e -> e.getKey() + ": " + e.getValue().translation),
                alternatives.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().sourceText))
                        .map(e -> e.getKey().sourceText + ": " + e.getValue().translation))
                .collect(Collectors.joining(", ")) + "]";
    }
}
