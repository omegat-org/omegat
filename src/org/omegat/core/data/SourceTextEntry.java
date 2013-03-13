/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009-2013 Alex Buloichik
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

import java.util.HashMap;
import java.util.Map;

/**
 * Source text entry represents an individual segment for
 * translation pulled directly from the input files.
 * There can be many SourceTextEntries having identical source
 * language strings
 *
 * @author Keith Godfrey
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SourceTextEntry {
    /** Storage for full entry's identifiers, including source text. */
    private EntryKey key;

    /** Comment in source file. */
    private String comment;
    
    /** Translation from source files. */
    private String sourceTranslation;
    
    /** Translation from source files is fuzzy. */
    private boolean sourceTranslationFuzzy;

    public enum DUPLICATE {
        /** There is no entries with the same source. */
        NONE,
        /** There is entries with the same source, and this is first entry. */
        FIRST,
        /** There is entries with the same source, and this is not first entry. */
        NEXT
    };

    /** If entry with the same source already exist in project. */
    DUPLICATE duplicate;

    /** Holds the number of this entry in a project. */
    private int m_entryNum;

    /**
     * Protected parts(shortcuts) in keys, details of full content in values(for tooltips). It can be null.
     */
    private Map<String, String> protectedParts;

    /**
     * Creates a new source text entry.
     * 
     * @param key
     *            entry key
     * @param entryNum
     *            the number of this entry in a project
     * @param comment
     *            entry comment
     * @param sourceTranslation
     *            translation from source file
     * @param protectedParts
     *            tags shortcuts
     */
    public SourceTextEntry(EntryKey key, int entryNum, String comment, String sourceTranslation,
            Map<String, String> protectedParts) {
        this.key = key;
        m_entryNum = entryNum;
        this.comment = comment;
        this.sourceTranslation = sourceTranslation;
        this.protectedParts = protectedParts;
    }

    public EntryKey getKey() {
        return key;
    }

    /**
     * Returns the source text (shortcut for <code>getStrEntry().getSrcText()</code>).
     */
    public String getSrcText() {
        return key.sourceText;
    }

    /**
     * Returns comment of entry if exist in source document.
     */
    public String getComment() {
        return comment;
    }

    /** Returns the number of this entry in a project. */
    public int entryNum() {
        return m_entryNum;
    }

    /** If entry with the same source already exist in project. */
    public DUPLICATE getDuplicate() {
        return duplicate;
    }
    
    public String getSourceTranslation() {
        return sourceTranslation;
    }
    
    public boolean isSourceTranslationFuzzy() {
        return sourceTranslationFuzzy;
    }
    
    public void setSourceTranslationFuzzy(boolean sourceTranslationFuzzy) {
        this.sourceTranslationFuzzy = sourceTranslationFuzzy;
    }

    public Map<String, String> getProtectedParts() {
        return protectedParts;
    }
}
