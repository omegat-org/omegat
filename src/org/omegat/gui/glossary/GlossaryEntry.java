/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Alex Buloichik
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

package org.omegat.gui.glossary;

import org.omegat.util.StringUtil;

/**
 * An entry in the glossary.
 * 
 * @author Keith Godfrey
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class GlossaryEntry {
    public GlossaryEntry(String src, String loc, String com) {
        m_src = src;
        m_loc = loc;
        m_com = com;
    }

    public String getSrcText() {
        return m_src;
    }

    public String getLocText() {
        return m_loc;
    }

    public String getCommentText() {
        return m_com;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ( o == null || o.getClass() != this.getClass() ) return false;
        GlossaryEntry otherGlossaryEntry = (GlossaryEntry)o;
        
        return StringUtil.equalsWithNulls(this.m_src, otherGlossaryEntry.m_src)
                && StringUtil.equalsWithNulls(this.m_loc, otherGlossaryEntry.m_loc)
                && StringUtil.equalsWithNulls(this.m_com, otherGlossaryEntry.m_com);

    }

    @Override
    public int hashCode() {
        int hash = 98;
        hash = hash * 17 + (m_src == null ? 0 : m_src.hashCode());
        hash = hash * 31 + (m_loc == null ? 0 : m_loc.hashCode());
        hash = hash * 13 + (m_com == null ? 0 : m_com.hashCode());
        return hash;
    }

    private String m_src;
    private String m_loc;
    private String m_com;
}
