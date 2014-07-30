/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik, Didier Briel
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

package org.omegat.core.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                if (!hasICE && !has100PC) {// TMXEntry without x-ids
                    boolean isDefaultTranslation = !isAltTranslation(e);
                    if (!existTranslation.defaultTranslation && isDefaultTranslation) {
                        // Existing translation is alt but the TMX entry is not.
                        continue;
                    }
                    if (!isDefaultTranslation && !altTranslationMatches(e, ste.getKey())) {
                        // TMX entry is an alternative translation that does not match this STE.
                        continue;
                    }
                    if (existTranslation.isTranslated()) { // default translation already exist
                        if (existTranslation.linked == TMXEntry.ExternalLinked.xAUTO
                                && !StringUtil.equalsWithNulls(existTranslation.translation, e.translation) 
                                || isEnforcedTMX) {
                            // translation already from auto and really changed or translation comes 
                            // from the enforce folder
                            setTranslation(ste, e, isDefaultTranslation, TMXEntry.ExternalLinked.xAUTO);
                        }
                    } else {
                        // default translation not exist - use from auto tmx
                        setTranslation(ste, e, isDefaultTranslation, TMXEntry.ExternalLinked.xAUTO);
                    }
                } else {// TMXEntry with x-ids
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
                                && !StringUtil.equalsWithNulls(existTranslation.translation, e.translation)) {
                            setTranslation(ste, e, false, TMXEntry.ExternalLinked.xICE);
                        }
                    } else if (existTranslation.linked == TMXEntry.ExternalLinked.x100PC) {
                        // already contains x-100pc
                        if (has100PC
                                && !StringUtil.equalsWithNulls(existTranslation.translation, e.translation)) {
                            setTranslation(ste, e, false, TMXEntry.ExternalLinked.x100PC);
                        }
                    }
                }
            }
        }
    }
    
    private boolean isAltTranslation(PrepareTMXEntry entry) {
        boolean hasFileProp = false;
        boolean hasOtherProp = false;
        for (TMXProp p : entry.otherProperties) {
            if (p.getType().equals(ProjectTMX.PROP_FILE)) {
                hasFileProp = true;
            } else if (p.getType().equals(ProjectTMX.PROP_ID)
                    || p.getType().equals(ProjectTMX.PROP_NEXT)
                    || p.getType().equals(ProjectTMX.PROP_PATH)
                    || p.getType().equals(ProjectTMX.PROP_PREV)) {
                hasOtherProp = true;
            }
        }
        return EntryKey.IGNORE_FILE_CONTEXT ? hasFileProp && hasOtherProp : hasFileProp;
    }
    
    private boolean altTranslationMatches(PrepareTMXEntry entry, EntryKey key) {
        try {
            for (TMXProp p : entry.otherProperties) {
                if (p.getType().equals(ProjectTMX.PROP_FILE) && EntryKey.IGNORE_FILE_CONTEXT) {
                    continue;
                }
                Field f = EntryKey.class.getField(p.getType());
                Object value = f.get(key);
                if (!value.equals(p.getValue())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
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
