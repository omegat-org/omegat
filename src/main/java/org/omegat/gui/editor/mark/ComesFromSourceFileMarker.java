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

package org.omegat.gui.editor.mark;

import java.util.Collections;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.gui.Styles;

/**
 * Marker for translations that were pre-filled from the source file itself,
 * i.e. the source document already carried a translation (for example a
 * pre-translated XLIFF or PO file) and the translator has not changed it yet.
 * <p>
 * The detection is a heuristic: it compares the current translation with the
 * translation carried by the source file. A translation typed from scratch
 * that happens to be identical to the pre-filled one is therefore marked as
 * well.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class ComesFromSourceFileMarker implements IMarker {

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText,
            String translationText, boolean isActive) {
        if (!Core.getEditor().getSettings().isMarkAutoPopulated()) {
            return null;
        }
        if (ste == null || translationText == null) {
            return null;
        }
        String fromFile = ste.getSourceTranslation();
        if (fromFile == null || ste.isSourceTranslationFuzzy()) {
            return null;
        }
        TMXEntry e = Core.getProject().getTranslationInfo(ste);
        if (e == null || !e.isTranslated() || e.linked != null) {
            // not translated, or already marked as linked to an external TMX
            return null;
        }
        if (!fromFile.equals(e.translation)) {
            // the translator has replaced the pre-filled translation
            return null;
        }
        Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, 0, translationText.length());
        // create the painter per call so that color preference changes take
        // effect without restarting the application
        m.painter = new TransparentHighlightPainter(
                Styles.EditorColor.COLOR_MARK_COMES_FROM_SOURCE_FILE.getColor(), 0.5F);
        return Collections.singletonList(m);
    }
}
