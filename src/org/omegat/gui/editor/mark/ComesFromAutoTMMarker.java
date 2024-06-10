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
    private final HighlightPainter painterXice;
    private final HighlightPainter painterX100Pc;
    private final HighlightPainter painterXauto;
    private final HighlightPainter painterLocked;

    public ComesFromAutoTMMarker() {
        painterXice = new TransparentHighlightPainter(
                Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_XICE.getColor(), 0.5F);
        painterX100Pc = new TransparentHighlightPainter(
                Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_X100PC.getColor(), 0.5F);
        painterXauto = new TransparentHighlightPainter(
                Styles.EditorColor.COLOR_MARK_COMES_FROM_TM_XAUTO.getColor(), 0.5F);
        painterLocked = new TransparentHighlightPainter(
                Styles.EditorColor.COLOR_LOCKED_SEGMENT.getColor(), 0.5F);
    }

    @Override
    public synchronized List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText,
            String translationText, boolean isActive) {
        if (!Core.getEditor().getSettings().isMarkAutoPopulated()) {
            return null;
        }
        boolean isLocked = false;
        TMXEntry e = Core.getProject().getTranslationInfo(ste);
        if (e == null || e.linked == null) {
            return null;
        }
        Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, 0, translationText.length());
        switch (e.linked) {
        case xICE:
            m.painter = painterXice;
            break;
        case x100PC:
            m.painter = painterX100Pc;
            break;
        case xAUTO:
            m.painter = painterXauto;
            break;
        case xENFORCED:
            m.painter = painterLocked;
        }
        return Collections.singletonList(m);
    }
}
