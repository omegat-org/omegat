/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.util.Log;

/**
 * Class for store and manage all marks.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
class Marker {
    private Map<Mark, Highlighter.Highlight> activeEntryMarks = new HashMap<Mark, Highlight>();

    private final Highlighter highlighter;

    Marker(JTextComponent comp) {
        this.highlighter = comp.getHighlighter();
    }

    void clearAllMarks() {
        highlighter.removeAllHighlights();
        activeEntryMarks.clear();
    }

    void clearActiveEntryMarks() {
        for (Highlighter.Highlight h : activeEntryMarks.values()) {
            highlighter.removeHighlight(h);
        }
        activeEntryMarks.clear();
    }

    void addActiveEntryMarks(SegmentBuilder sb, List<Mark> marks,
            HighlightPainter painter) {
        int startOffset = sb.getStartPosition() + 1;// skip direction char
        for (Mark m : marks) {
            try {
                Highlighter.Highlight h = (Highlighter.Highlight) highlighter
                        .addHighlight(startOffset + m.startOffset, startOffset
                                + m.endOffset, painter);
                activeEntryMarks.put(m, h);
            } catch (BadLocationException ex) {
                Log.log(ex);
            }
        }
    }
}
