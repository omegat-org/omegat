/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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
import java.util.Collections;
import java.util.List;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.search.SearchMatch;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.filter.ReplaceFilter;
import org.omegat.util.gui.Styles;

/**
 * Marker for replace candidate.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ReplaceMarker implements IMarker {
    protected static final HighlightPainter PAINTER = new TransparentHighlightPainter(Styles.EditorColor.COLOR_REPLACE.getColor(),
            0.4F);

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText,
            boolean isActive) throws Exception {

        IEditorFilter filter = Core.getEditor().getFilter();
        if (filter == null || !(filter instanceof ReplaceFilter)) {
            return Collections.emptyList();
        }

        ReplaceFilter replaceFilter = (ReplaceFilter) filter;
        List<SearchMatch> matches = replaceFilter.getReplacementsForEntry(translationText);
        if (matches == null) {
            return Collections.emptyList();
        }

        List<Mark> r = new ArrayList<Mark>(matches.size());
        for (SearchMatch s : matches) {
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, s.getStart(), s.getEnd());
            m.painter = PAINTER;
            r.add(m);
        }
        return r;
    }
}
