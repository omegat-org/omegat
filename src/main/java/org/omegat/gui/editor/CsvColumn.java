/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.editor;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.OStrings;

/**
 * Columns of a segment CSV. The header ids are the contract for the planned
 * CSV import: they stay stable, and {@code seg_num} respectively
 * {@code file} + {@code seg_id} serve as the row key.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
enum CsvColumn {

    SEG_NUM("seg_num", (ste, trans) -> Integer.toString(ste.entryNum())),
    SEG_ID("seg_id", (ste, trans) -> orEmpty(ste.getKey().id)),
    FILE("file", (ste, trans) -> orEmpty(ste.getKey().file)),
    SOURCE("source", (ste, trans) -> orEmpty(ste.getSrcText())),
    TARGET("target", (ste, trans) -> trans.isTranslated() ? orEmpty(trans.translation) : ""),
    TRANSLATED("translated", (ste, trans) -> Boolean.toString(trans.isTranslated())),
    NOTE("note", (ste, trans) -> orEmpty(trans.note)),
    COMMENT("comment", (ste, trans) -> orEmpty(ste.getComment())),
    CREATOR("creator", (ste, trans) -> orEmpty(trans.creator)),
    CREATED("created", (ste, trans) -> isoDate(trans.creationDate)),
    CHANGER("changer", (ste, trans) -> orEmpty(trans.changer)),
    CHANGED("changed", (ste, trans) -> isoDate(trans.changeDate)),
    ORIGIN("origin", (ste, trans) -> orEmpty(trans.origin)),
    EXTERNAL_LINK("external_link", (ste, trans) -> trans.linked == null ? "" : trans.linked.name());

    private final String headerId;
    private final BiFunction<SourceTextEntry, TMXEntry, String> extractor;

    CsvColumn(String headerId, BiFunction<SourceTextEntry, TMXEntry, String> extractor) {
        this.headerId = headerId;
        this.extractor = extractor;
    }

    String getHeaderId() {
        return headerId;
    }

    /** The label shown on the column checkbox in the export options. */
    String getDisplayName() {
        return OStrings.getString("GUI_EDITORWINDOW_EXPORT_CSV_COL_" + name());
    }

    String extract(SourceTextEntry ste, TMXEntry trans) {
        return extractor.apply(ste, trans);
    }

    private static String orEmpty(@Nullable String value) {
        return value == null ? "" : value;
    }

    private static String isoDate(long epochMillis) {
        return epochMillis == 0 ? "" : DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(epochMillis));
    }
}
