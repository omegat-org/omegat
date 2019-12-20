/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2014 Aaron Madlon-Kay
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
package org.omegat.core.data;

import java.util.Objects;

import org.omegat.util.StringUtil;

/**
 * Class for store full entry's identifier, including file, id, src, etc.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class EntryKey implements Comparable<EntryKey> {
    public final String file;
    public final String sourceText;
    public final String id;
    public final String prev;
    public final String next;
    public final String path;

    /**
     * When true, ignore the {@link #file} member when comparing EntryKeys.
     */
    private static boolean ignoreFileContext = false;

    public EntryKey(final String file, final String sourceText, final String id, final String prev,
            final String next, final String path) {
        this.file = file;
        this.sourceText = sourceText;
        this.id = id;
        this.prev = prev;
        this.next = next;
        this.path = path;
    }

    @Override
    public int hashCode() {
        int hash = sourceText.hashCode();
        if (!ignoreFileContext && file != null) {
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EntryKey)) {
            return false;
        }
        EntryKey o = (EntryKey) obj;
        return Objects.equals(sourceText, o.sourceText) && // source
                (ignoreFileContext || Objects.equals(file, o.file)) && // file
                Objects.equals(id, o.id) && // id
                Objects.equals(prev, o.prev) && // prev
                Objects.equals(next, o.next) && // next
                Objects.equals(path, o.path); // path
    }

    @Override
    public int compareTo(EntryKey o) {
        int c = ignoreFileContext ? 0 : StringUtil.compareToWithNulls(file, o.file);
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

    @Override
    public String toString() {
        return "[file:" + file + ", id=" + id + ", path=" + path + ", source='" + sourceText + "', prev='"
                + prev + "', next='" + next + "']";
    }

    public static void setIgnoreFileContext(boolean ignore) {
        ignoreFileContext = ignore;
    }

    public static boolean isIgnoreFileContext() {
        return ignoreFileContext;
    }
}
