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
    public SearchResultEntry(int num, String preamble, String srcPrefix, String src, String target,
            String note, String properties, SearchMatch[] srcMatch, SearchMatch[] targetMatch,
            SearchMatch[] noteMatch, SearchMatch[] propertiesMatch) {
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
        return (entryNum);
    }

    /** Returns information about where this entry comes from. */
    public String getPreamble() {
        return (preambleText);
    }

    public void setPreamble(String preamble) {
        preambleText = preamble;
    }

    /** Returns the source text of the corresponding entry within a project. */
    public String getSrcText() {
        return (sourceText);
    }

    /** Returns the target text of the corresponding entry within a project. */
    public String getTranslation() {
        return (targetText);
    }

    /** Returns the note text of the corresponding entry within a project. */
    public String getNote() {
        return (note);
    }

    public String getProperties() {
        return propertiesString;
    }

    public String getSrcPrefix() {
        return srcPrefix;
    }

    public SearchMatch[] getSrcMatch() {
        return srcMatch;
    }

    public SearchMatch[] getTargetMatch() {
        return targetMatch;
    }

    public SearchMatch[] getNoteMatch() {
        return noteMatch;
    }

    public SearchMatch[] getPropertiesMatch() {
        return propertiesMatch;
    }

    private final int entryNum;
    private String preambleText;
    private final String srcPrefix;
    private final String sourceText;
    private final String targetText;
    private final String note;
    private final String propertiesString;
    private final SearchMatch[] srcMatch;
    private final SearchMatch[] targetMatch;
    private final SearchMatch[] noteMatch;
    private final SearchMatch[] propertiesMatch;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int entryNum;
        private String preambleText;
        private String srcPrefix;
        private String sourceText;
        private String targetText;
        private String note;
        private String propertiesString;
        private SearchMatch[] srcMatch;
        private SearchMatch[] targetMatch;
        private SearchMatch[] noteMatch;
        private SearchMatch[] propertiesMatch;

        public Builder entryNum(int entryNum) {
            this.entryNum = entryNum;
            return this;
        }

        public Builder preambleText(String preambleText) {
            this.preambleText = preambleText;
            return this;
        }

        public Builder srcPrefix(String srcPrefix) {
            this.srcPrefix = srcPrefix;
            return this;
        }

        public Builder sourceText(String sourceText) {
            this.sourceText = sourceText;
            return this;
        }

        public Builder targetText(String targetText) {
            this.targetText = targetText;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Builder propertiesString(String propertiesString) {
            this.propertiesString = propertiesString;
            return this;
        }

        public Builder srcMatch(SearchMatch[] srcMatch) {
            this.srcMatch = srcMatch;
            return this;
        }

        public Builder targetMatch(SearchMatch[] targetMatch) {
            this.targetMatch = targetMatch;
            return this;
        }

        public Builder noteMatch(SearchMatch[] noteMatch) {
            this.noteMatch = noteMatch;
            return this;
        }

        public Builder propertiesMatch(SearchMatch[] propertiesMatch) {
            this.propertiesMatch = propertiesMatch;
            return this;
        }

        public SearchResultEntry build() {
            return new SearchResultEntry(entryNum, preambleText, srcPrefix, sourceText, targetText,
                    note, propertiesString, srcMatch, targetMatch, noteMatch, propertiesMatch);
        }
    }
}
