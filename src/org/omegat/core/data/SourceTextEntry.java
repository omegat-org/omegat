/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009-2013 Alex Buloichik
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

import org.omegat.filters2.Shortcuts;

/**
 * Source text entry represents an individual segment for translation pulled
 * directly from the input files. There can be many SourceTextEntries having
 * identical source language strings
 * 
 * @author Keith Godfrey
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SourceTextEntry {

    private static ProtectedParts EMPTY_PROTECTED_PARTS;
    static {
        EMPTY_PROTECTED_PARTS = new ProtectedParts();
        EMPTY_PROTECTED_PARTS.parts = new String[0];
        EMPTY_PROTECTED_PARTS.details = new String[0];
        EMPTY_PROTECTED_PARTS.protect = new boolean[0];
    }

    /** Storage for full entry's identifiers, including source text. */
    private final EntryKey key;

    /** Comment in source file. */
    private final String comment;

    /** Translation from source files. */
    private final String sourceTranslation;

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
    private final int m_entryNum;

    /**
     * Protected parts(shortcuts) and details of full content (for tooltips).
     * Read-only info, cat be accessible from any threads. It can't be null.
     */
    private final ProtectedParts protectedParts;

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
     * @param shortcuts
     *            tags shortcuts
     */
    public SourceTextEntry(EntryKey key, int entryNum, String comment, String sourceTranslation, Shortcuts shortcuts) {
        this.key = key;
        m_entryNum = entryNum;
        this.comment = comment;
        this.sourceTranslation = sourceTranslation;
        if (shortcuts == null || shortcuts.isEmpty()) {
            this.protectedParts = EMPTY_PROTECTED_PARTS;
        } else {
            if (shortcuts.shortcuts.size() != shortcuts.shortcutDetails.size()) {
                throw new RuntimeException("Wrong shortcuts info");
            }
            this.protectedParts = new ProtectedParts();
            this.protectedParts.parts = shortcuts.shortcuts.toArray(new String[shortcuts.shortcuts.size()]);
            this.protectedParts.details = shortcuts.shortcutDetails
                    .toArray(new String[shortcuts.shortcutDetails.size()]);
            this.protectedParts.protect = new boolean[shortcuts.shortcutProtected.size()];
            for (int i = 0; i < this.protectedParts.protect.length; i++) {
                this.protectedParts.protect[i] = shortcuts.shortcutProtected.get(i);
            }
        }
    }

    public EntryKey getKey() {
        return key;
    }

    /**
     * Returns the source text (shortcut for
     * <code>getStrEntry().getSrcText()</code>).
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

    public ProtectedParts getProtectedParts() {
        return protectedParts;
    }

    /**
     * Class for store protected parts info.
     */
    public static class ProtectedParts {
        protected String[] parts;
        protected String[] details;
        protected boolean[] protect;

        public String[] getParts() {
            return parts;
        }

        public boolean contains(String s) {
            for (int i = 0; i < parts.length; i++) {
                if (s.equals(parts[i])) {
                    return true;
                }
            }
            return false;
        }

        public String getDetail(String part) {
            for (int i = 0; i < parts.length; i++) {
                if (part.equals(parts[i])) {
                    return details[i];
                }
            }
            return null;
        }

        public boolean isProtected(String part) {
            for (int i = 0; i < parts.length; i++) {
                if (part.equals(parts[i])) {
                    return protect[i];
                }
            }
            return false;
        }
    }
}
