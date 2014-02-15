/**************************************************************************
OmegaT - Computer Assisted Translation (CAT) tool
         with fuzzy matching, translation memory, keyword search,
         glossaries, and translation leveraging into updated projects.

Copyright (C) 2014 Alex Buloichik
              Home page: http://www.omegat.org/
              Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor.mark;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.gui.Styles;

/**
 * Marker for marks entries from auto TMX.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ComesFromAutoTMMarker implements IMarker {
    protected final HighlightPainter PAINTER = new TransparentHighlightPainter(
            Styles.COLOR_MARK_COMES_FROM_TM, 0.5F);

    @Override
    public synchronized List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText,
            String translationText, boolean isActive) {
        TMXEntry e = Core.getProject().getTranslationInfo(ste);
        List<Mark> marks = new ArrayList<Mark>(1);
        if (e.xAUTO || e.x100PC != null || e.xICE != null) {
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, 0, translationText.length());
            m.painter = PAINTER;
            marks.add(m);
        }
        return marks;
    }
}
