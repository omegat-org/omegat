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

import javax.swing.text.Highlighter.HighlightPainter;

import org.jspecify.annotations.Nullable;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.gui.Styles;

/**
 * Marker that highlights segments whose translation is identical to the
 * source text. Enabled via the "Mark Identical Segments" view option
 * (SF feature request #1051).
 * <p>
 * The verdict is taken from the stored translation ({@link TMXEntry}), not
 * from the displayed strings: an untranslated segment may echo the source in
 * the target area, but it is not an identical translation and must not be
 * marked.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class IdenticalSegmentMarker implements IMarker {

    @Override
    public @Nullable List<Mark> getMarksForEntry(SourceTextEntry ste, @Nullable String sourceText,
            @Nullable String translationText, boolean isActive) {
        if (!Core.getEditor().getSettings().isMarkIdentical()) {
            return null;
        }
        if (translationText == null) {
            return null;
        }
        TMXEntry info = Core.getProject().getTranslationInfo(ste);
        if (info == null || !info.isTranslated() || !info.translation.equals(ste.getSrcText())) {
            return null;
        }
        // painter created per call so that color preference changes take
        // effect without restarting the application
        HighlightPainter painter = new TransparentHighlightPainter(
                Styles.EditorColor.COLOR_MARK_IDENTICAL.getColor(), 0.5F);
        Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, 0, translationText.length());
        m.painter = painter;
        return Collections.singletonList(m);
    }
}
