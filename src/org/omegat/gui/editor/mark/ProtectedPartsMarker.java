/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.editor.mark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.gui.Styles;

/**
 * Marker for SourceTextEntry.protectedParts.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProtectedPartsMarker implements IMarker {
    protected static final HighlightPainter PAINTER = new TransparentHighlightPainter(Styles.COLOR_PLACEHOLDER, 0.2F);

    @Override
    public HighlightPainter getPainter() {
        return PAINTER;
    }

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive)
            throws Exception {
        if (ste.getProtectedParts() == null) {
            return Collections.emptyList();
        }

        List<Mark> r = new ArrayList<Mark>();

        for (String tag : ste.getProtectedParts().keySet()) {
            int pos = -1;
            while ((pos = sourceText.indexOf(tag, pos + 1)) >= 0) {
                Mark m = new Mark(Mark.ENTRY_PART.SOURCE, pos, pos + tag.length());
                m.toolTipText = escapeHtml(ste.getProtectedParts().get(tag));
                r.add(m);
            }
            if (translationText != null) {
                pos = -1;
                while ((pos = translationText.indexOf(tag, pos + 1)) >= 0) {
                    Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, pos, pos + tag.length());
                    m.toolTipText = escapeHtml(ste.getProtectedParts().get(tag));
                    r.add(m);
                }
            }
        }

        return r;
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
