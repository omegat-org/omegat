/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Briac Pilpre
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
import java.util.Collections;
import java.util.List;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.Styles;

/**
 * Collection of Markers for Bidirectional control characters.
 */
public class BidiMarkers extends AbstractMarker {

    static final int LRM = 0x200e;
    static final int RLM = 0x200f;
    static final int LRE = 0x202a;
    static final int RLE = 0x202b;
    static final int PDF = 0x202c;
    static final int LRO = 0x202d;
    static final int RLO = 0x202e;

    private final HighlightPainter lreBidiPainter;
    private final HighlightPainter rleBidiPainter;
    private final HighlightPainter lrmBidiPainter;
    private final HighlightPainter rlmBidiPainter;
    private final HighlightPainter rloBidiPainter;
    private final HighlightPainter lroBidiPainter;

    public BidiMarkers() throws Exception {
        super();
        lreBidiPainter = new BidiPainter(LRE,
                Styles.EditorColor.COLOR_BIDIMARKERS.getColor());
        rleBidiPainter = new BidiPainter(RLE,
                Styles.EditorColor.COLOR_BIDIMARKERS.getColor());
        lrmBidiPainter = new BidiPainter(LRM,
                Styles.EditorColor.COLOR_BIDIMARKERS.getColor());
        rlmBidiPainter = new BidiPainter(RLM,
                Styles.EditorColor.COLOR_BIDIMARKERS.getColor());
        rloBidiPainter = new BidiPainter(RLO,
                Styles.EditorColor.COLOR_BIDIMARKERS.getColor());
        lroBidiPainter = new BidiPainter(LRO,
                Styles.EditorColor.COLOR_BIDIMARKERS.getColor());
    }

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String text, boolean isActive)
            throws Exception {
        if (!isEnabled()) {
            return null;
        }
        if (!isActive || text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        text = StringUtil.normalizeUnicode(text);
        List<Mark> marks = new ArrayList<>();

        int startPos = -1;
        int markCodePoint = -1;
        for (int i = 0, cp; i < text.length(); i += Character.charCount(cp)) {
            cp = text.codePointAt(i);

            if (!(cp == LRE || cp == RLE || cp == LRM || cp == RLM || cp == PDF || cp == LRO || cp == RLO)) {
                continue;
            }

            if (cp == PDF && startPos != -1) {
                Mark mark = new Mark(Mark.ENTRY_PART.TRANSLATION, startPos, i);
                switch (markCodePoint) {
                case LRE:
                    mark.painter = lreBidiPainter;
                    break;
                case RLE:
                    mark.painter = rleBidiPainter;
                    break;
                case LRO:
                    mark.painter = lroBidiPainter;
                    break;
                case RLO:
                    mark.painter = rloBidiPainter;
                    break;
                }
                marks.add(mark);

                startPos = -1;
                markCodePoint = -1;
            } else if (cp == LRM || cp == RLM) {
                Mark mark = new Mark(Mark.ENTRY_PART.TRANSLATION, i, i + 1);
                mark.painter = cp == LRM ? lrmBidiPainter : rlmBidiPainter;
                marks.add(mark);
            } else {
                markCodePoint = cp;
                startPos = i;
            }
        }

        if (startPos != -1) {
            Mark mark = new Mark(Mark.ENTRY_PART.TRANSLATION, startPos, startPos);
            switch (markCodePoint) {
            case LRE:
                mark.painter = lreBidiPainter;
                break;
            case RLE:
                mark.painter = rleBidiPainter;
                break;
            case LRO:
                mark.painter = lroBidiPainter;
                break;
            case RLO:
                mark.painter = rloBidiPainter;
                break;
            }
            marks.add(mark);
        }

        return marks;
    }

    @Override
    protected boolean isEnabled() {
        return Core.getEditor().getSettings().isMarkBidi();
    }

}
