/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Antonio Vilei
               2014 Piotr Kulik
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

package org.omegat.core.search;

import org.jspecify.annotations.Nullable;

/**
 * Storage for a search result entry.
 *
 * @author Antonio Vilei
 */
public class SearchResultEntry {

    /**
     * Creates a new search result entry with properties set to given values.
     *
     * @param num
     *            Number of the corresponding entry within a project
     * @param preamble
     *            Information about where this entry comes from
     * @param src
     *            Source text of the corresponding entry within a project
     * @param target
     *            Target text of the corresponding entry within a project
     * @param note
     *            Note text of the corresponding entry within a project
     */
    public SearchResultEntry(int num, @Nullable String preamble, @Nullable String srcPrefix, String src,
                             @Nullable String target, @Nullable String note, @Nullable String properties,
                             @Nullable SearchMatch[] srcMatch, @Nullable SearchMatch[] targetMatch,
                             @Nullable SearchMatch[] noteMatch, @Nullable SearchMatch[] propertiesMatch) {
        entryNum = num;
        preambleText = preamble;
        this.srcPrefix = srcPrefix;
        sourceText = src;
        targetText = target;
        this.note = note;
        propertiesString = properties;
        this.srcMatch = srcMatch;
        this.targetMatch = targetMatch;
        this.noteMatch = noteMatch;
        this.propertiesMatch = propertiesMatch;
    }

    /**
     * Returns the number of the corresponding entry within a project. The
     * returned value is &gt; 0 if the entry belongs to one of the source files
     * of the project; it is -1 if the entry doesn't belong to any of the source
     * files (the entry is stored in the TM or we are searching in a given
     * directory)
     */
    public int getEntryNum() {
        return entryNum;
    }

    /** Returns information about where this entry comes from. */
    public @Nullable String getPreamble() {
        return preambleText;
    }

    public void setPreamble(String preamble) {
        preambleText = preamble;
    }

    /** Returns the source text of the corresponding entry within a project. */
    public String getSrcText() {
        return sourceText;
    }

    /** Returns the target text of the corresponding entry within a project. */
    public @Nullable String getTranslation() {
        return targetText;
    }

    /** Returns the note text of the corresponding entry within a project. */
    public @Nullable String getNote() {
        return note;
    }

    public @Nullable String getProperties() {
        return propertiesString;
    }

    public @Nullable String getSrcPrefix() {
        return srcPrefix;
    }

    public @Nullable SearchMatch[] getSrcMatch() {
        return srcMatch;
    }

    public @Nullable SearchMatch[] getTargetMatch() {
        return targetMatch;
    }

    public @Nullable SearchMatch[] getNoteMatch() {
        return noteMatch;
    }

    public @Nullable SearchMatch[] getPropertiesMatch() {
        return propertiesMatch;
    }

    private final int entryNum;
    private @Nullable String preambleText;
    private final @Nullable String srcPrefix;
    private final String sourceText;
    private final @Nullable String targetText;
    private final @Nullable String note;
    private final @Nullable String propertiesString;
    private final @Nullable SearchMatch[] srcMatch;
    private final @Nullable SearchMatch[] targetMatch;
    private final @Nullable SearchMatch[] noteMatch;
    private final @Nullable SearchMatch[] propertiesMatch;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int entryNum;
        private @Nullable String preambleText;
        private @Nullable String srcPrefix;
        private String sourceText;
        private @Nullable String targetText;
        private @Nullable String note;
        private @Nullable String propertiesString;
        private SearchMatch @Nullable [] srcMatch;
        private SearchMatch @Nullable [] targetMatch;
        private SearchMatch @Nullable [] noteMatch;
        private SearchMatch @Nullable [] propertiesMatch;

        public Builder entryNum(int newEntryNum) {
            this.entryNum = newEntryNum;
            return this;
        }

        public Builder preambleText(@Nullable String newPreambleText) {
            this.preambleText = newPreambleText;
            return this;
        }

        public Builder srcPrefix(@Nullable String newSrcPrefix) {
            this.srcPrefix = newSrcPrefix;
            return this;
        }

        public Builder sourceText(String newSourceText) {
            this.sourceText = newSourceText;
            return this;
        }

        public Builder targetText(@Nullable String newTargetText) {
            this.targetText = newTargetText;
            return this;
        }

        public Builder note(@Nullable String newNote) {
            this.note = newNote;
            return this;
        }

        public Builder propertiesString(@Nullable String properties) {
            this.propertiesString = properties;
            return this;
        }

        public Builder srcMatch(SearchMatch @Nullable [] match) {
            this.srcMatch = match;
            return this;
        }

        public Builder targetMatch(SearchMatch @Nullable [] match) {
            this.targetMatch = match;
            return this;
        }

        public Builder noteMatch(SearchMatch @Nullable [] match) {
            this.noteMatch = match;
            return this;
        }

        public Builder propertiesMatch(SearchMatch @Nullable [] newPropertiesMatch) {
            this.propertiesMatch = newPropertiesMatch;
            return this;
        }

        public SearchResultEntry build() {
            return new SearchResultEntry(entryNum, preambleText, srcPrefix, sourceText, targetText,
                    note, propertiesString, srcMatch, targetMatch, noteMatch, propertiesMatch);
        }
    }
}
