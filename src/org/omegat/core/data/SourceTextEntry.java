/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009-2013 Alex Buloichik
               2015 Aaron Madlon-Kay
               2019 Thomas Cordonnier
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Source text entry represents an individual segment for translation pulled
 * directly from the input files. There can be many SourceTextEntries having
 * identical source language strings
 *
 * @author Keith Godfrey
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class SourceTextEntry {

    private static final ProtectedPart[] EMPTY_PROTECTED_PARTS = new ProtectedPart[0];

    /** Storage for full entry's identifiers, including source text. */
    private final EntryKey key;

    /**
     * String properties from source file. Contents are alternating key (even
     * index), value (odd index) strings. Should be of even length.
     */
    private final String[] props;

    /** Translation from source files. */
    private final String sourceTranslation;

    /** Translation from source files is fuzzy. */
    private boolean sourceTranslationFuzzy;

    /**
     * Flag indicating if the segment is located at the start of a paragraph.
     */
    private final boolean paragraphStart;

    public enum DUPLICATE {
        /** There is no entries with the same source. */
        NONE,
        /** There is entries with the same source, and this is first entry. */
        FIRST,
        /**
         * There is entries with the same source, and this is not first entry.
         */
        NEXT
    };

    /**
     * A list of duplicates of this STE. Will be non-null for the FIRST
     * duplicate, null for NONE and NEXT STEs. See {@link #getDuplicate()} for
     * full logic.
     */
    List<SourceTextEntry> duplicates;

    /**
     * The first duplicate of this STE. Will be null for NONE and FIRST STEs,
     * non-null for NEXT STEs. See {@link #getDuplicate()} for full logic.
     */
    SourceTextEntry firstInstance;

    /** Holds the number of this entry in a project. */
    private final int m_entryNum;

    /**
     * Protected parts(shortcuts) and details of full content (for tooltips).
     * Read-only info, cat be accessible from any threads. It can't be null.
     */
    private final ProtectedPart[] protectedParts;

    private boolean finalState;

    /**
     * Creates a new source text entry.
     *
     * @param key
     *            entry key
     * @param entryNum
     *            the number of this entry in a project
     * @param props
     *            optional entry metadata
     * @param sourceTranslation
     *            translation from source file
     * @param protectedParts
     *            protected parts
     * @param paragraphStart
     *            indicate it is a start of paragraph.
     * @param finalState
     *            indicate it is a final state (as in XLIFF).
     */
    public SourceTextEntry(EntryKey key, int entryNum, String[] props, String sourceTranslation,
            List<ProtectedPart> protectedParts, boolean paragraphStart, boolean finalState) {
        this.key = key;
        m_entryNum = entryNum;
        this.props = props;
        this.sourceTranslation = sourceTranslation;
        this.paragraphStart = paragraphStart;
        if (protectedParts.isEmpty()) {
            this.protectedParts = EMPTY_PROTECTED_PARTS;
        } else {
            // remove empty protected parts
            for (int i = 0; i < protectedParts.size(); i++) {
                if (protectedParts.get(i).getTextInSourceSegment().isEmpty()) {
                    protectedParts.remove(i);
                    i--;
                }
            }
            this.protectedParts = protectedParts.toArray(new ProtectedPart[0]);
        }
        this.duplicates = null;
        this.firstInstance = null;
        this.finalState = finalState;
    }

    public SourceTextEntry(EntryKey key, int entryNum, String[] props, String sourceTranslation,
            List<ProtectedPart> protectedParts, boolean paragraphStart) {
        this(key, entryNum, props, sourceTranslation, protectedParts, paragraphStart, false);
    }

    public SourceTextEntry(EntryKey key, int entryNum, String[] props, String sourceTranslation,
            List<ProtectedPart> protectedParts) {
        this(key, entryNum, props, sourceTranslation, protectedParts, true);
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
        if (SegmentProperties.isEmpty(props)) {
            return null;
        }
        return SegmentProperties.joinValues(props);
    }

    public String[] getRawProperties() {
        return SegmentProperties.copy(props);
    }

    /** Returns the number of this entry in a project. */
    public int entryNum() {
        return m_entryNum;
    }

    /** If entry with the same source already exists in a project. */
    public DUPLICATE getDuplicate() {
        if (firstInstance != null) {
            return DUPLICATE.NEXT;
        }
        return duplicates == null ? DUPLICATE.NONE : DUPLICATE.FIRST;
    }

    /**
     * Tell a number of duplications.
     * 
     * @return a number of duplications.
     */
    public int getNumberOfDuplicates() {
        if (firstInstance != null) {
            return firstInstance.getNumberOfDuplicates();
        }
        return duplicates == null ? 0 : duplicates.size();
    }

    /**
     * Give STEs which are duplicated.
     * 
     * @return list of STEs which are duplicated.
     */
    public List<SourceTextEntry> getDuplicates() {
        if (firstInstance != null) {
            List<SourceTextEntry> result = new ArrayList<SourceTextEntry>(firstInstance.getDuplicates());
            result.remove(this);
            result.add(0, firstInstance);
            return Collections.unmodifiableList(result);
        }
        if (duplicates == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(duplicates);
        }
    }

    /**
     * Give source translation.
     * 
     * @return source translation.
     */
    public String getSourceTranslation() {
        return sourceTranslation;
    }

    /**
     * Is source transaltion flagged fuzzy.
     * 
     * @return true when fuzzy, otherwise, false.
     */
    public boolean isSourceTranslationFuzzy() {
        return sourceTranslationFuzzy;
    }

    /**
     * Set translation as fuzzy.
     * 
     * @param sourceTranslationFuzzy
     *            false when reset a status, true indicate fuzzy.
     */
    public void setSourceTranslationFuzzy(boolean sourceTranslationFuzzy) {
        this.sourceTranslationFuzzy = sourceTranslationFuzzy;
    }

    /**
     * Return protected parts.
     * 
     * @return an array of protected parts.
     */
    public ProtectedPart[] getProtectedParts() {
        return protectedParts;
    }

    /**
     * Indicate the STE is the start of paragraph.
     * 
     * @return true when start of paragraph, otherwise, false.
     */
    public boolean isParagraphStart() {
        return paragraphStart;
    }

    /**
     * Indicate the STE is the final state.
     * 
     * @return true when final, otherwise, false.
     */
    public boolean isFinalState() {
        return finalState;
    }
}
