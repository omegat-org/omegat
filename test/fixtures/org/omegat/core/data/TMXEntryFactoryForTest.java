/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.core.data;

public final class TMXEntryFactoryForTest {
    private final PrepareTMXEntry prep;
    private boolean defaultTranslation = true;
    public TMXEntry.ExternalLinked linked;

    public TMXEntryFactoryForTest() {
        prep = new PrepareTMXEntry();
    }

    public TMXEntryFactoryForTest setSource(String source) {
        prep.source = source;
        return this;
    }

    public TMXEntryFactoryForTest setTranslation(String translation) {
        prep.translation = translation;
        return this;
    }

    public TMXEntryFactoryForTest setCreator(String creator) {
        prep.creator = creator;
        return this;
    }

    public TMXEntryFactoryForTest setCreationDate(long creationDate) {
        prep.creationDate = creationDate;
        return this;
    }

    public TMXEntryFactoryForTest setNote(String note) {
        prep.note = note;
        return this;
    }

    public TMXEntryFactoryForTest setDefaultTranslation(boolean defaultTranslation) {
        this.defaultTranslation = defaultTranslation;
        return this;
    }

    public TMXEntryFactoryForTest setExternalLinked(TMXEntry.ExternalLinked linked) {
        this.linked = linked;
        return this;
    }

    public TMXEntry build() {
        return new TMXEntry(prep, defaultTranslation, linked);
    }

    public static TMXEntry createTMXEntry(String source, String translation, String creator, long createDate, boolean defaultTranslation) {
        return new TMXEntryFactoryForTest()
                .setSource(source).setTranslation(translation).setCreator(creator).setCreationDate(createDate)
                .setDefaultTranslation(defaultTranslation)
                .build();
    }
}
