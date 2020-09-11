/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Antonio Vilei
               2014 Piotr Kulik
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
            String note, String properties, SearchMatch[] srcMatch, SearchMatch[] targetMatch, SearchMatch[] noteMatch, SearchMatch[] propertiesMatch) {
        m_num = num;
        m_preamble = preamble;
        m_srcPrefix = srcPrefix;
        m_src = src;
        m_target = target;
        m_note = note;
        m_properties = properties;
        m_srcMatch = srcMatch;
        m_targetMatch = targetMatch;
        m_noteMatch = noteMatch;
        m_propertiesMatch = propertiesMatch;
    }

    /**
     * Returns the number of the corresponding entry within a project. The
     * returned value is > 0 if the entry belongs to one of the source files of
     * the project; it is -1 if the entry doesn't belong to any of the source
     * files (the entry is stored in the TM or we are searching in a given
     * directory)
     */
    public int getEntryNum() {
        return (m_num);
    }

    /** Returns information about where this entry comes from. */
    public String getPreamble() {
        return (m_preamble);
    }

    public void setPreamble(String preamble) {
        m_preamble = preamble;
    }

    /** Returns the source text of the corresponding entry within a project. */
    public String getSrcText() {
        return (m_src);
    }

    /** Returns the target text of the corresponding entry within a project. */
    public String getTranslation() {
        return (m_target);
    }

    /** Returns the note text of the corresponding entry within a project. */
    public String getNote() {
        return (m_note);
    }

    public String getProperties() {
        return m_properties;
    }

    public String getSrcPrefix() {
        return m_srcPrefix;
    }

    public SearchMatch[] getSrcMatch() {
        return m_srcMatch;
    }

    public SearchMatch[] getTargetMatch() {
        return m_targetMatch;
    }

    public SearchMatch[] getNoteMatch() {
        return m_noteMatch;
    }

    public SearchMatch[] getPropertiesMatch() {
        return m_propertiesMatch;
    }


    private int m_num;
    private String m_preamble;
    private String m_srcPrefix;
    private String m_src;
    private String m_target;
    private String m_note;
    private String m_properties;
    private SearchMatch[] m_srcMatch;
    private SearchMatch[] m_targetMatch;
    private SearchMatch[] m_noteMatch;
    private SearchMatch[] m_propertiesMatch;
}
