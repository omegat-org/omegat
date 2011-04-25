/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

import org.omegat.util.StringUtil;

/**
 * Class for store full entry's identifier, including file, id, src, etc.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class EntryKey implements Comparable<EntryKey> {
    public final String file;
    public final String sourceText;
    public final String id;
    public final String prev;
    public final String next;
    public final String path;

    public EntryKey(final String file, final String sourceText, final String id, final String prev,
            final String next, final String path) {
        this.file = file;
        this.sourceText = sourceText;
        this.id = id;
        this.prev = prev;
        this.next = next;
        this.path = path;
    }

    public int hashCode() {
        int hash = sourceText.hashCode();
        if (file != null) {
            hash += file.hashCode();
        }
        if (id != null) {
            hash += id.hashCode();
        }
        if (prev != null) {
            hash += prev.hashCode();
        }
        if (next != null) {
            hash += next.hashCode();
        }
        if (path != null) {
            hash += path.hashCode();
        }
        return hash;
    }

    public boolean equals(Object obj) {
        EntryKey o = (EntryKey) obj;
        return StringUtil.equalsWithNulls(sourceText, o.sourceText) && // source
                StringUtil.equalsWithNulls(file, o.file) && // file
                StringUtil.equalsWithNulls(id, o.id) && // id
                StringUtil.equalsWithNulls(prev, o.prev) && // prev
                StringUtil.equalsWithNulls(next, o.next) && // next
                StringUtil.equalsWithNulls(path, o.path); // path
    }

    public int compareTo(EntryKey o) {
        int c = StringUtil.compareToWithNulls(file, o.file);
        if (c == 0) {
            c = StringUtil.compareToWithNulls(id, o.id);
        }
        if (c == 0) {
            c = StringUtil.compareToWithNulls(sourceText, o.sourceText);
        }
        if (c == 0) {
            c = StringUtil.compareToWithNulls(prev, o.prev);
        }
        if (c == 0) {
            c = StringUtil.compareToWithNulls(next, o.next);
        }
        if (c == 0) {
            c = StringUtil.compareToWithNulls(path, o.path);
        }
        return c;
    }

    public String toString() {
        return "[file:" + file + ", id=" + id + ", path=" + path + ", source='" + sourceText + "', prev='"
                + prev + "', next='" + next + "']";
    }
}
