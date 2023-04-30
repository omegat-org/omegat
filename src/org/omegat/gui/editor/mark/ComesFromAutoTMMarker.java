/**************************************************************************
OmegaT - Computer Assisted Translation (CAT) tool
         with fuzzy matching, translation memory, keyword search,
         glossaries, and translation leveraging into updated projects.

Copyright (C) 2014 Alex Buloichik
              2019 Aaron Madlon-Kay
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

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.gui.Styles;

/**
 * Marker for marks entries from TMX that come automatically from tm/auto/ folder.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ComesFromAutoTMMarker implements IMarker {
    protected static final HighlightPainter PAINTER_XICE = new TransparentHighlightPainter(
            Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_XICE.getColor(), 0.5F);
    protected static final HighlightPainter PAINTER_X100PC = new TransparentHighlightPainter(
            Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_X100PC.getColor(), 0.5F);
    protected static final HighlightPainter PAINTER_XAUTO = new TransparentHighlightPainter(
            Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_XAUTO.getColor(), 0.5F);
    protected static final HighlightPainter PAINTER_XENFORCED = new TransparentHighlightPainter(
            Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_XENFORCED.getColor(), 0.5F);

    @Override
    public synchronized List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText,
            String translationText, boolean isActive) {
        if (!Core.getEditor().getSettings().isMarkAutoPopulated()) {
            return null;
        }
        TMXEntry e = Core.getProject().getTranslationInfo(ste);
        if (e.linked == null) {
            return null;
        }
        Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, 0, translationText.length());
        switch (e.linked) {
        case xICE:
            m.painter = PAINTER_XICE;
            break;
        case x100PC:
            m.painter = PAINTER_X100PC;
            break;
        case xAUTO:
            m.painter = PAINTER_XAUTO;
            break;
        case xENFORCED:
            m.painter = PAINTER_XENFORCED;
        }
        return Collections.singletonList(m);
    }
}
