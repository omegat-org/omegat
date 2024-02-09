/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2010 Wildrich Fourie
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

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.OStrings;
import org.omegat.util.gui.Styles;

/**
 * Collection of Markers for whitespace symbols.
 *
 * @author Martin Fleurke
 */
@SuppressWarnings("unused")
public final class WhitespaceMarker implements IMarker {

    public WhitespaceMarker() {
    }

    public static void loadPlusins() {
        Core.registerMarkerClass(WhitespaceMarker.class);
    }

    public static void unloadPlugins() {
    }

    private final SymbolPainter spacePainter =
            new SymbolPainter(Styles.EditorColor.COLOR_WHITESPACE.getColor(),
            "·");
    /**
     * Marker for tab
     */
    private final SymbolPainter tabPainter = new SymbolPainter(Styles.EditorColor.COLOR_WHITESPACE.getColor(),
            "»");
    /**
     * Marker for linefeed.
     *
     * There is a linefeed symbol: U+240A. But it is so small / hard to see,
     * that instead we use U+00B6 as the symbol to show, like other applications do.
     */
    private final SymbolPainter lfPainter = new SymbolPainter(Styles.EditorColor.COLOR_WHITESPACE.getColor(),
            "¶");
    //no need for CR marker. There are no CR's.

    /**
     * Marker for whitespaces.
     *
     * @author Martin Fleurke
     * @author Hiroshi Miura
     */
    @Override
    public List<Mark> getMarksForEntry(final SourceTextEntry ste, final String sourceText, final String translationText, final boolean isActive) throws Exception {
        if (!isEnabled()) {
            return null;
        }
        if (sourceText == null || !isActive) {
            return null;
        }
        List<Mark> marks = new ArrayList<>();
        char spacePatternChar = ' ';
        String tabToolTip = OStrings.getString("MARKER_TAB");
        char tabPatternChar = '\t';
        String lfToolTip = "LF";
        char lfPatternChar = '\n';

        if (isActive || Core.getEditor().getSettings().isDisplaySegmentSources() || translationText == null) {
            int pos = 0;
            while ((pos = sourceText.indexOf(spacePatternChar, pos)) >= 0) {
                int next = sourceText.offsetByCodePoints(pos, 1);
                Mark m = new Mark(Mark.ENTRY_PART.SOURCE, pos, next);
                m.painter = spacePainter;
                marks.add(m);
                pos = next;
            }
            pos = 0;
            while ((pos = sourceText.indexOf(tabPatternChar, pos)) >= 0) {
                int next = sourceText.offsetByCodePoints(pos, 1);
                Mark m = new Mark(Mark.ENTRY_PART.SOURCE, pos, next);
                m.painter = tabPainter;
                m.toolTipText = tabToolTip;
                marks.add(m);
                pos = next;
            }
            pos = 0;
            while ((pos = sourceText.indexOf(lfPatternChar, pos)) >= 0) {
                int next = sourceText.offsetByCodePoints(pos, 1);
                Mark m = new Mark(Mark.ENTRY_PART.SOURCE, pos, next);
                m.painter = lfPainter;
                m.toolTipText = lfToolTip;
                marks.add(m);
                pos = next;
            }
        }
        if (translationText != null) {
            int pos = 0;
            while ((pos = translationText.indexOf(spacePatternChar, pos)) >= 0) {
                int next = translationText.offsetByCodePoints(pos, 1);
                Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, pos, next);
                m.painter = spacePainter;
                marks.add(m);
                pos = next;
            }
            pos = 0;
            while ((pos = translationText.indexOf(tabPatternChar, pos)) >= 0) {
                int next = translationText.offsetByCodePoints(pos, 1);
                Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, pos, next);
                m.painter = tabPainter;
                m.toolTipText = tabToolTip;
                marks.add(m);
                pos = next;
            }
            pos = 0;
            while ((pos = translationText.indexOf(lfPatternChar, pos)) >= 0) {
                int next = translationText.offsetByCodePoints(pos, 1);
                Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, pos, next);
                m.painter = lfPainter;
                m.toolTipText = lfToolTip;
                marks.add(m);
                pos = next;
            }
        }
        return marks;
    }

   public boolean isEnabled() {
        return Core.getEditor().getSettings().isMarkWhitespace();
    }
}
