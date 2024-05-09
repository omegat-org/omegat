/**************************************************************************
OmegaT - Computer Assisted Translation (CAT) tool
         with fuzzy matching, translation memory, keyword search,
         glossaries, and translation leveraging into updated projects.

Copyright (C) 2013 Alex Buloichik
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

import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.util.gui.Styles;

/**
 * Marker for be able to add marks when a match is inserted from such a TMX manually. TMX should be in tm/mt/
 * folder.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ComesFromMTMarker implements IMarker {
    private final HighlightPainter highlightPainter;
    private SourceTextEntry markedSte;
    private String markedText;

    public ComesFromMTMarker() {
        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            public void onNewFile(String activeFileName) {
            }

            public void onEntryActivated(SourceTextEntry newEntry) {
                synchronized (ComesFromMTMarker.this) {
                    markedSte = null;
                    markedText = null;
                }
            }
        });
        highlightPainter = new TransparentHighlightPainter(
                Styles.EditorColor.COLOR_MARK_COMES_FROM_TM.getColor(), 0.5F);
    }

    public void setMark(SourceTextEntry ste, String text) {
        synchronized (this) {
            markedSte = ste;
            markedText = text;
        }
    }

    @Override
    public synchronized List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText,
            boolean isActive) {
        synchronized (this) {
            if (!isActive || ste != markedSte || !translationText.equals(markedText)) {
                return null;
            }
        }
        Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, 0, translationText.length());
        m.painter = highlightPainter;
        return Collections.singletonList(m);
    }
}
