/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Aaron Madlon-Kay
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

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.omegat.util.StringUtil;

/**
 * An entry in the glossary.
 * 
 * @author Keith Godfrey
 * @author Aaron Madlon-Kay
 */
public class GlossaryEntry {
    public GlossaryEntry(String src, String[] loc, String[] com, boolean[] fromPriorityGlossary) {
        m_src = src;
        m_loc = loc;
        m_com = com;
        m_priority = fromPriorityGlossary;
    }

    public GlossaryEntry(String src, String loc, String com, boolean fromPriorityGlossary) {
        this(src, new String[] { loc }, new String[] { com }, new boolean[] { fromPriorityGlossary });
    }

    public String getSrcText() {
        return m_src;
    }

    /**
     * Return the first target-language term string.
     * 
     * Glossary entries can have multiple target strings
     * if they have been combined for display purposes.
     * Access all target strings with {@link GlossaryEntry#getLocTerms(boolean)}.
     * 
     * @return The first target-language term string
     */
    public String getLocText() {
        return m_loc.length > 0 ? m_loc[0] : "";
    }

    /**
     * Return each individual target-language term that
     * corresponds to the source term.
     * 
     * @param uniqueOnly Whether or not to filter duplicates from the list
     * @return All target-language terms
     */
    public String[] getLocTerms(boolean uniqueOnly) {
        if (!uniqueOnly || m_loc.length == 1) return m_loc;
        
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < m_loc.length; i++) {
            if (i > 0 && m_loc[i].equals(m_loc[i - 1])) continue;
            list.add(m_loc[i]);
        }
        return list.toArray(new String[0]);
    }

    /**
     * Return the first comment string.
     * 
     * Glossary entries can have multiple comment strings
     * if they have been combined for display purposes.
     * Access all comment strings with {@link GlossaryEntry#getComments()}.
     * 
     * @return The first comment string
     */
    public String getCommentText() {        
        return m_com.length > 0 ? m_com[0] : "";
    }

    public String[] getComments() {
        return m_com;
    }

    public boolean getPriority() {
        return m_priority.length > 0 ? m_priority[0] : false;
    }

    public boolean[] getPriorities() {
        return m_priority;
    }

    public StyledString toStyledString() {
        StyledString result=new StyledString();

        result.text.append(m_src);
        result.text.append(" = ");
        
        StringBuffer comments = new StringBuffer();
        
        int commentIndex = 0;
        for (int i = 0; i < m_loc.length; i++) {
            if (i > 0 && m_loc[i].equals(m_loc[i - 1])) {
                if (!m_com[i].equals("")) {
                    comments.append("\n");
                    comments.append(commentIndex);
                    comments.append(". ");
                    comments.append(m_com[i]);
                }
                continue;
            }
            if (i > 0) result.text.append(", ");
            if (m_priority[i]) {
                result.markBoldStart();
            }
            result.text.append(bracketEntry(m_loc[i]));
            if (m_priority[i]) {
                result.markBoldEnd();
            }
            commentIndex++;
            if (!m_com[i].equals("")) {
                comments.append("\n");
                comments.append(commentIndex);
                comments.append(". ");
                comments.append(m_com[i]);
            }
        }
        
        result.text.append(comments);
        
        return result;
    }
    
    /**
     * If a combined glossary entry contains ',', it needs to be bracketed by
     * quotes, to prevent confusion when entries are combined. However, if the
     * entry contains ';' or '"', it will automatically be bracketed by quotes.
     * 
     * @param entry
     *            A glossary text entry
     * @return A glossary text entry possibly bracketed by quotes
     */
    private String bracketEntry(String entry) {

        if (entry.contains(",") && !(entry.contains(";") || entry.contains("\"")))
            entry = '"' + entry + '"';
        return entry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ( o == null || o.getClass() != this.getClass() ) return false;
        GlossaryEntry otherGlossaryEntry = (GlossaryEntry)o;

        return StringUtil.equalsWithNulls(this.m_src, otherGlossaryEntry.m_src)
                && Arrays.equals(this.m_loc, otherGlossaryEntry.m_loc)
                && Arrays.equals(this.m_com, otherGlossaryEntry.m_com);
    }

    @Override
    public int hashCode() {
        int hash = 98;
        hash = hash * 17 + (m_src == null ? 0 : m_src.hashCode());
        hash = hash * 31 + (m_loc == null ? 0 : m_loc.hashCode());
        hash = hash * 13 + (m_com == null ? 0 : m_com.hashCode());
        return hash;
    }

    static class StyledString {
        public StringBuilder text = new StringBuilder();
        public List<Integer> boldStarts = new ArrayList<Integer>();
        public List<Integer> boldLengths = new ArrayList<Integer>();

        void markBoldStart() {
            boldStarts.add(text.length());
        }

        void markBoldEnd() {
            int start = boldStarts.get(boldStarts.size() - 1);
            boldLengths.add(text.length() - start);
        }

        public void append(StyledString str) {
            int off = text.length();
            text.append(str.text);
            for (int s : str.boldStarts) {
                boldStarts.add(off + s);
            }
            boldLengths.addAll(str.boldLengths);
        }

        public void append(String str) {
            text.append(str);
        }
    }

    private String m_src;
    private String[] m_loc;
    private String[] m_com;
    private boolean[] m_priority;
}
