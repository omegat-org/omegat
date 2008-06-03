/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008      Alex Buloichik
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

package org.omegat.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omegat.core.matching.SourceTextEntry;

/**
 * Project context data. It includes all translation, source segments,
 * preferences and other project data which loaded and saved in project dir.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProjectContext {
    public ProjectProperties m_config;

    public boolean m_modifiedFlag;

    /** maps text to strEntry obj */
    public final Map<String, StringEntry> m_strEntryHash;

    /** Unique segments list. Used for save TMX. */
    public final List<StringEntry> m_strEntryList;

    /** List of all segments in project. */
    public final List<SourceTextEntry> m_srcTextEntryArray;

    /** the list of legacy TMX files, each object is the list of string entries */
    public final List<LegacyTM> m_legacyTMs;

    public final List<TransMemory> m_tmList;
    public final List<TransMemory> m_orphanedList;

    public ProjectContext() {
        m_strEntryHash = new HashMap<String, StringEntry>(4096);
        m_strEntryList = new ArrayList<StringEntry>();
        m_srcTextEntryArray = new ArrayList<SourceTextEntry>(4096);
        m_tmList = new ArrayList<TransMemory>();
        m_legacyTMs = new ArrayList<LegacyTM>();
        m_orphanedList = new ArrayList<TransMemory>();
    }
}
