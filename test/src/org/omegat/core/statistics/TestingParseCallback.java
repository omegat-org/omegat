/*******************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023-2025 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.omegat.core.statistics;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;

import java.util.ArrayList;
import java.util.List;

public class TestingParseCallback implements IParseCallback {

    private final List<SourceTextEntry> steList;

    public TestingParseCallback(final List<SourceTextEntry> ste) {
        this.steList = ste;
    }

    @Override
    public void addEntryWithProperties(String id, String source, String translation, boolean isFuzzy,
                                       String[] props, String path, IFilter filter, List<ProtectedPart> protectedParts) {
        SourceTextEntry ste = new SourceTextEntry(new EntryKey("source.po", source, id, "", "", path), 1,
                props, translation, protectedParts);
        ste.setSourceTranslationFuzzy(isFuzzy);
        steList.add(ste);
    }

    @Override
    public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                         String path, IFilter filter, List<ProtectedPart> protectedParts) {
        List<String> propList = new ArrayList<>(2);
        if (comment != null) {
            propList.add("comment");
            propList.add(comment);
        }
        String[] props = propList.toArray(new String[0]);
        addEntryWithProperties(id, source, translation, isFuzzy, props, path, filter, protectedParts);
    }

    @Override
    public void linkPrevNextSegments() {
        // do nothing
    }
}
