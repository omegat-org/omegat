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

package org.omegat.gui.editor.mark;

import java.util.List;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.SegmentBuilder;

/**
 * Information about entry which processed, for be able to check if this entry
 * changed in UI.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class EntryMarks {
    public SegmentBuilder builder;
    public final int markerIndex;
    private final long entryVersion;
    public List<Mark> result;
    public final SourceTextEntry ste;
    /** May be null if source not displayed */
    public final String sourceText;
    /** May be null if not translated */
    public final String translationText;
    public final boolean isActive;

    public EntryMarks(SegmentBuilder builder, long entryVersion, int markerIndex) {
        this.builder = builder;
        this.entryVersion = entryVersion;
        this.markerIndex = markerIndex;
        this.isActive = builder.isActive();
        this.ste = builder.getSourceTextEntry();
        this.sourceText = builder.getSourceText();
        this.translationText = builder.getTranslationText();
    }

    /**
     * Check if entry changed.
     */
    public boolean isSegmentChanged() {
        return builder.getDisplayVersion() != entryVersion;
    }
}
