/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

/**
 * Tracks usage and frequency of words and word pairs
 * 
 * @author Keith Godfrey
 */
public class StringData {

    private StringData(int c, String s, byte a, long dl, long dh, long d) {
        m_cnt = c;
        m_orig = s;
        m_attr = a;
        m_digestLow = dl;
        m_digestHigh = dh;
        m_digest = d;
    }

    public Object clone() {
        return new StringData(m_cnt, m_orig, m_attr, m_digestLow, m_digestHigh, m_digest);
    }

    //
    // uniq flag set indicates that a given token doesn't occur
    // elsewhere, flag clear indicates it has a (at least one) partner
    // near flag means that a given word has different neighbors
    // than in its compared-to string (this a constant used elsewhere)
    public static final byte UNIQ = 0x01;
    public static final byte PAIR = 0x02;

    private String m_orig;
    private byte m_attr;
    private int m_cnt;

    private long m_digestHigh;
    private long m_digestLow;
    private long m_digest;
}
