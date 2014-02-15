/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omegat.util.StringUtil;

/**
 * Utility class for import translations from tm/auto/ files.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
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

    void process(ExternalTMX tmx) {
        for (TMXEntry e : tmx.getEntries()) { // iterate by all entries in TMX
            List<SourceTextEntry> list = existEntries.get(e.source);
            if (list == null) {
                continue; // there is no entries for this source
            }
            for (SourceTextEntry ste : list) { // for each TMX entry - get all sources in project
                TMXEntry existTranslation = project.getTranslationInfo(ste);

                if (e.xICE == null && e.x100PC == null) {// TMXEntry without x-ids
                    if (!existTranslation.defaultTranslation) {
                        // alternative exist - skip
                        continue;
                    }
                    if (existTranslation.isTranslated()) { // default translation already exist
                        if (existTranslation.xAUTO
                                && !StringUtil.equalsWithNulls(existTranslation.translation, e.translation)) {
                            // translation already from auto and really changed
                            setDefaultTranslation(project, ste, e);
                        }
                    } else {
                        // default translation not exist - use from auto tmx
                        setDefaultTranslation(project, ste, e);
                    }
                } else {// TMXEntry with x-ids
                    if (!existTranslation.isTranslated() || existTranslation.defaultTranslation) {
                        // need to update if id in xICE or x100PC
                        if (isContainsId(e.xICE, ste.getKey().id) || isContainsId(e.x100PC, ste.getKey().id)) {
                            String xICE = isContainsId(e.xICE, ste.getKey().id) ? ste.getKey().id : null;
                            String x100PC = isContainsId(e.x100PC, ste.getKey().id) ? ste.getKey().id : null;
                            setAlternativeTranslation(project, ste, e, xICE, x100PC);
                        }
                    } else if (existTranslation.xICE != null) {
                        // already contains x-ice
                        if (e.xICE.contains(ste.getKey().id)
                                && !StringUtil.equalsWithNulls(existTranslation.translation, e.translation)) {
                            setAlternativeTranslation(project, ste, e, ste.getKey().id, null);
                        }
                    } else if (existTranslation.x100PC != null) {
                        // already contains x-100pc
                        if ((e.xICE.contains(ste.getKey().id) || e.x100PC.contains(ste.getKey().id))
                                && !StringUtil.equalsWithNulls(existTranslation.translation, e.translation)) {
                            setAlternativeTranslation(project, ste, e, ste.getKey().id, null);
                        }
                    }
                }
            }
        }
    }

    boolean isContainsId(List<String> list, String id) {
        if (list == null) {
            return false;
        }
        return list.contains(id);
    }

    void setDefaultTranslation(RealProject p, SourceTextEntry ste, TMXEntry entryFromTmx) {
        PrepareTMXEntry tr = new PrepareTMXEntry();
        tr.defaultTranslation = true;
        tr.source = entryFromTmx.source;
        tr.translation = entryFromTmx.translation;
        tr.note = entryFromTmx.note;
        p.setTranslation(ste, tr);
    }

    void setAlternativeTranslation(RealProject p, SourceTextEntry ste, TMXEntry entryFromTmx, String xICE,
            String x100PC) {
        PrepareTMXEntry tr = new PrepareTMXEntry();
        tr.defaultTranslation = false;
        tr.source = entryFromTmx.source;
        tr.translation = entryFromTmx.translation;
        tr.note = entryFromTmx.note;
        if (xICE != null) {
            tr.xICE = new ArrayList<String>(1);
            tr.xICE.add(xICE);
        }
        if (x100PC != null) {
            tr.x100PC = new ArrayList<String>(1);
            tr.x100PC.add(x100PC);
        }
        p.setTranslation(ste, tr);
    }
}
