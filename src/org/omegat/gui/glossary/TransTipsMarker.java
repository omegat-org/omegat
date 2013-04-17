/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.gui.glossary;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Preferences;

/**
 * Marker for TransTips.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TransTipsMarker implements IMarker {
    protected static final HighlightPainter transTipsUnderliner = new UnderlineFactory.SolidBoldUnderliner(Color.blue);

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive) {
        if (!isActive) {
            return null;
        }
        if (!Preferences.isPreference(Preferences.TRANSTIPS)) {
            return null;
        }
        List<GlossaryEntry> glossaryEntries = Core.getGlossary().getDisplayedEntries();
        if (glossaryEntries == null || glossaryEntries.isEmpty()) {
            return null;
        }

        final List<Mark> marks = new ArrayList<Mark>();
        // Get the index of the current segment in the whole document
        sourceText = sourceText.toLowerCase();

        TransTips.Search callback = new TransTips.Search() {
            public void found(GlossaryEntry ge, int start, int end) {
                Mark m = new Mark(Mark.ENTRY_PART.SOURCE, start, end);
                m.painter = TransTipsMarker.transTipsUnderliner;
                marks.add(m);
            }
        };

        for (GlossaryEntry ent : glossaryEntries) {
            TransTips.search(sourceText, ent, callback);
        }
        return marks;
    }
}
