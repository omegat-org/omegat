/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.Language;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXReader2;
import org.omegat.util.TMXWriter2;

/**
 * Class for store data from project_save.tmx.
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
     * Storage for translation for current project. Will be null if default translation disabled.
     * 
     * It must be used with synchronization around ProjectTMX.
     */
    final Map<String, TMXEntry> translationDefault;

    /**
     * Storage for alternative translations for current project.
     * 
     * It must be used with synchronization around ProjectTMX.
     */
    final Map<EntryKey, TMXEntry> translationMultiple;

    /**
     * Storage for orphaned segments.
     * 
     * It must be used with synchronization around ProjectTMX.
     */
    final Map<String, TMXEntry> orphanedDefault;

    /**
     * Storage for orphaned alternative translations for current project.
     * 
     * It must be used with synchronization around ProjectTMX.
     */
    final Map<EntryKey, TMXEntry> orphanedMultiple;

    public ProjectTMX(ProjectProperties props, File file, CheckOrphanedCallback callback,
            Map<EntryKey, TMXEntry> sourceTranslations) throws Exception {
        translationMultiple = new HashMap<EntryKey, TMXEntry>();
        translationMultiple.putAll(sourceTranslations);
        orphanedMultiple = new HashMap<EntryKey, TMXEntry>();
        orphanedDefault = new HashMap<String, TMXEntry>();
        if (props.isSupportDefaultTranslations()) {
            translationDefault = new HashMap<String, TMXEntry>();
        } else {
            // Do not even create default storage if not required. It will
            // allow to see errors.
            translationDefault = null;
        }

        if (!file.exists()) {
            // file not exist - new project
            return;
        }

        new TMXReader2().readTMX(
                file,
                props.getSourceLanguage(),
                props.getTargetLanguage(),
                props.isSentenceSegmentingEnabled(),
                false,
                true,
                false,
                new Loader(callback, props.getSourceLanguage(), props.getTargetLanguage(), props
                        .isSentenceSegmentingEnabled()));
    }

    public void save(ProjectProperties props, File outFile, final boolean forceValidTMX,
            final boolean levelTwo, final boolean useOrphaned) throws Exception {
        TMXWriter2 wr = new TMXWriter2(outFile, props.getSourceLanguage(), props.getTargetLanguage(),
                props.isSentenceSegmentingEnabled(), levelTwo, forceValidTMX);
        try {
            Map<String, TMXEntry> defaults = new TreeMap<String, TMXEntry>();
            Map<EntryKey, TMXEntry> alternatives = new TreeMap<EntryKey, TMXEntry>();

            synchronized (this) {
                if (translationDefault != null) {
                    defaults.putAll(translationDefault);
                }
                if (useOrphaned) {
                    defaults.putAll(orphanedDefault);
                }

                alternatives.putAll(translationMultiple);
                if (useOrphaned) {
                    alternatives.putAll(orphanedMultiple);
                }
            }

            wr.writeComment(" Default translations ");
            for (Map.Entry<String, TMXEntry> en : new TreeMap<String, TMXEntry>(defaults).entrySet()) {
                wr.writeEntry(en.getKey(), en.getValue().translation, en.getValue(), null);
            }

            wr.writeComment(" Alternative translations ");
            for (Map.Entry<EntryKey, TMXEntry> en : new TreeMap<EntryKey, TMXEntry>(alternatives).entrySet()) {
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
            return translationDefault != null ? translationDefault.get(source) : null;
        }
    }

    /**
     * Get multiple translation or null if not exist.
     */
    public TMXEntry getMultipleTranslation(EntryKey ek) {
        synchronized (this) {
            return translationMultiple.get(ek);
        }
    }

    /**
     * Set new translation.
     */
    public void setTranslation(SourceTextEntry ste, TMXEntry te, boolean isDefault) {
        synchronized (this) {
            if (te == null) {
                if (isDefault) {
                    translationDefault.remove(ste.getKey().sourceText);
                } else {
                    translationMultiple.remove(ste.getKey());
                }
            } else {
                if (isDefault) {
                    translationDefault.put(ste.getKey().sourceText, te);
                } else {
                    translationMultiple.put(ste.getKey(), te);
                }
            }
        }
    }

    private class Loader implements TMXReader2.LoadCallback {
        private final CheckOrphanedCallback callback;
        private final Language sourceLang;
        private final Language targetLang;
        private final boolean sentenceSegmentingEnabled;

        public Loader(CheckOrphanedCallback callback, Language sourceLang, Language targetLang,
                boolean sentenceSegmentingEnabled) {
            this.callback = callback;
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
                        if (translationDefault != null && callback.existSourceInProject(segmentSource)) {
                            translationDefault.put(segmentSource, te);
                        } else {
                            orphanedDefault.put(segmentSource, te);
                        }
                    } else {
                        // multiple translation
                        if (callback.existEntryInProject(key)) {
                            translationMultiple.put(key, te);
                        } else {
                            orphanedMultiple.put(key, te);
                        }
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

    public interface CheckOrphanedCallback {
        boolean existEntryInProject(EntryKey key);

        boolean existSourceInProject(String src);
    }
}
