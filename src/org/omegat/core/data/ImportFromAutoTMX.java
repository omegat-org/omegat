/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik, Didier Briel
               2019 Aaron Madlon-Kay
               2020 Briac Pilpre
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.omegat.util.StringUtil;
import org.omegat.util.TMXProp;

/**
 * Utility class for import translations from tm/auto/ files.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public class ImportFromAutoTMX {
    final RealProject project;
    /** Map of all segments in project by source text. Just for optimize some processes. */
    Map<String, List<SourceTextEntry>> existEntries = new HashMap<String, List<SourceTextEntry>>();

    public ImportFromAutoTMX(RealProject project, List<SourceTextEntry> allProjectEntries) {
        this.project = project;
        for (SourceTextEntry ste : allProjectEntries) {
            List<SourceTextEntry> list = existEntries.get(ste.getSrcText());
            if (list == null) {
                list = new ArrayList<SourceTextEntry>();
                existEntries.put(ste.getSrcText(), list);
            }
            list.add(ste);
        }
    }

    /**
     * Process a TMX from an automatic folder
     * @param tmx The name of the TMX to process
     * @param isEnforcedTMX If true, existing default translations will be overwritten in all cases
     */
    void process(ExternalTMX tmx, boolean isEnforcedTMX) {

        for (PrepareTMXEntry e : tmx.getEntries()) { // iterate by all entries in TMX
            List<SourceTextEntry> list = existEntries.get(e.source);
            if (list == null) {
                continue; // there is no entries for this source
            }
            for (SourceTextEntry ste : list) { // for each TMX entry - get all sources in project
                TMXEntry existTranslation = project.getTranslationInfo(ste);

                String id = ste.getKey().id;
                boolean hasICE = id != null && e.hasPropValue(ProjectTMX.PROP_XICE, id);
                boolean has100PC = id != null && e.hasPropValue(ProjectTMX.PROP_X100PC, id);
                boolean hasAlternateTranslations = altTranslationMatches(e, ste.getKey());

                if (e.hasPropValue(ExternalTMFactory.TMXLoader.PROP_FOREIGN_MATCH, "true")) {
                    // Never automatically include matches from foreign languages.
                    continue;
                }
                if (!hasICE && !has100PC) { // TMXEntry without x-ids
                    boolean isDefaultTranslation = !isAltTranslation(e);
                    if (!existTranslation.defaultTranslation && isDefaultTranslation) {
                        // Existing translation is alt but the TMX entry is not.
                        continue;
                    }
                    if (!isDefaultTranslation && !hasAlternateTranslations) {
                        // TMX entry is an alternative translation that does not match this STE.
                        continue;
                    }
                    if (isEnforcedTMX && (!existTranslation.isTranslated()
                            || existTranslation.linked != TMXEntry.ExternalLinked.xENFORCED
                            || (!isDefaultTranslation && existTranslation.defaultTranslation))) {
                        // If there's
                        // - no translation or
                        // - the existing translation doesn't come from an enforced TM or
                        // - the existing enforced translation was a default translation but this one is not
                        setTranslation(ste, e, isDefaultTranslation, TMXEntry.ExternalLinked.xENFORCED);
                    } else if (!existTranslation.isTranslated()
                            || (!isDefaultTranslation && hasAlternateTranslations)) {
                        // default translation not exist - use from auto tmx
                        setTranslation(ste, e, isDefaultTranslation, TMXEntry.ExternalLinked.xAUTO);
                    }
                } else { // TMXEntry with x-ids
                    if (!existTranslation.isTranslated() || existTranslation.defaultTranslation) {
                        // need to update if id in xICE or x100PC
                        if (hasICE) {
                            setTranslation(ste, e, false, TMXEntry.ExternalLinked.xICE);
                        } else if (has100PC) {
                            setTranslation(ste, e, false, TMXEntry.ExternalLinked.x100PC);
                        }
                    } else if (existTranslation.linked == TMXEntry.ExternalLinked.xICE
                            || existTranslation.linked == TMXEntry.ExternalLinked.x100PC) {
                        // already contains x-ice
                        if (hasICE
                                && !Objects.equals(existTranslation.translation, e.translation)) {
                            setTranslation(ste, e, false, TMXEntry.ExternalLinked.xICE);
                        }
                    } else if (existTranslation.linked == TMXEntry.ExternalLinked.x100PC) {
                        // already contains x-100pc
                        if (has100PC
                                && !Objects.equals(existTranslation.translation, e.translation)) {
                            setTranslation(ste, e, false, TMXEntry.ExternalLinked.x100PC);
                        }
                    }
                }
            }
        }
    }

    private boolean isAltTranslation(PrepareTMXEntry entry) {
        if (entry.otherProperties == null) {
            return false;
        }
        boolean hasFileProp = false;
        boolean hasOtherProp = false;
        for (TMXProp p : entry.otherProperties) {
            if (p.getType().equals(ProjectTMX.PROP_FILE)) {
                hasFileProp = true;
            } else if (p.getType().equals(ProjectTMX.PROP_ID)
                    || p.getType().equals(ProjectTMX.ATTR_TUID)
                    || p.getType().equals(ProjectTMX.PROP_NEXT)
                    || p.getType().equals(ProjectTMX.PROP_PATH)
                    || p.getType().equals(ProjectTMX.PROP_PREV)) {
                hasOtherProp = true;
            }
        }
        return EntryKey.isIgnoreFileContext() ? hasOtherProp : hasFileProp;
    }

    private boolean altTranslationMatches(PrepareTMXEntry entry, EntryKey key) {
        if (entry.otherProperties == null) {
            return false;
        }
        String file = null, id = null, next = null, prev = null, path = null;
        for (TMXProp p : entry.otherProperties) {
            if (ProjectTMX.PROP_FILE.equals(p.getType())) {
                file = p.getValue();
            } else if (ProjectTMX.PROP_ID.equals(p.getType())) {
                id = p.getValue();
            } else if (ProjectTMX.ATTR_TUID.equals(p.getType()) && id == null) {
                // Use @tuid as fallback when id prop is not available
                id = p.getValue();
            } else if (ProjectTMX.PROP_NEXT.equals(p.getType())) {
                next = p.getValue();
            } else if (ProjectTMX.PROP_PREV.equals(p.getType())) {
                prev = p.getValue();
            } else if (ProjectTMX.PROP_PATH.equals(p.getType())) {
                path = p.getValue();
            }
        }
        return key.equals(new EntryKey(file, entry.source, id, prev, next, path));
    }

    private void setTranslation(SourceTextEntry entry, PrepareTMXEntry trans, boolean defaultTranslation,
            TMXEntry.ExternalLinked externalLinked) {
        if (StringUtil.isEmpty(trans.note)) {
            trans.note = null;
        }

        trans.source = entry.getSrcText();

        TMXEntry newTrEntry;

        if (trans.translation == null && trans.note == null) {
            // no translation, no note
            newTrEntry = null;
        } else {
            newTrEntry = new TMXEntry(trans, defaultTranslation, externalLinked);
        }
        project.projectTMX.setTranslation(entry, newTrEntry, defaultTranslation);
    }
}
