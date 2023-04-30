/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010-2013 Alex Buloichik
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
 * Class for store info about matching position.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SearchMatch implements Comparable<SearchMatch> {
    private int start, end;
    private String replacement;

    public SearchMatch(int start, int end) {
        this.start = start;
        this.end = end;
        this.replacement = null;
    }

    public SearchMatch(int start, int end, String replacement) {
        this.start = start;
        this.end = end;
        this.replacement = replacement;
    }

    public int compareTo(SearchMatch o) {
        int diff = start - o.start;
        if (diff == 0) {
            diff = end - o.end;
        }
        return diff;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return end - start;
    }

    public String getReplacement() {
        return replacement;
    }

    public void move(int offset) {
        start += offset;
        end += offset;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d] => %s", start, end, replacement);
    }
}
