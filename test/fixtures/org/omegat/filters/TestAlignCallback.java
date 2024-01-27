/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.filters;

import java.util.ArrayList;
import java.util.List;

import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;

public class TestAlignCallback implements IAlignCallback {
    public List<AlignedEntry> entries = new ArrayList<>();

    public void addTranslation(String id, String source, String translation, boolean isFuzzy,
                               String path, IFilter filter) {
        AlignedEntry en = new AlignedEntry();
        en.id = id;
        en.source = source;
        en.translation = translation;
        en.path = path;
        entries.add(en);
    }

    protected static class AlignedEntry {
        public String id;
        public String source;
        public String translation;
        public String path;
    }
}
