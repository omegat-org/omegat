/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Martin Fleurke
               2013 Alex Buloichik (alex73mail@gmail.com)
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;

/**
 * Abstract marker class that marks source and target text according to some
 * 'pattern' and paints them according to some 'PAINTER' and 'ATTRIBUTES' (all
 * of them defined by implementing classes)
 *
 * @author Martin Fleurke
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public abstract class AbstractMarker implements IMarker {
    protected HighlightPainter painter;
    protected String toolTip;
    protected AttributeSet attributes;
    /** Regexp for mark. */
    protected Pattern pattern;
    /** char for mark. */
    protected int patternChar;

    public AbstractMarker() throws Exception {
    }

    /**
     * Is the marker enabled?
     *
     * @return true when enabled (markers are painted). false when disabled (no
     *         markers painted)
     */
    protected abstract boolean isEnabled();

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive)
            throws Exception {

        if (!isEnabled()) {
            return null;
        }
        if (pattern == null && patternChar == 0) {
            return null;
        }

        List<Mark> r = new ArrayList<>();
        Matcher match;
        if (isActive || Core.getEditor().getSettings().isDisplaySegmentSources() || translationText == null) { // TODO
            initDrawers(true, isActive);
            if (pattern != null) {
                match = pattern.matcher(sourceText);
                while (match.find()) {
                    Mark m = new Mark(Mark.ENTRY_PART.SOURCE, match.start(), match.end());
                    m.painter = painter;
                    m.toolTipText = toolTip;
                    m.attributes = attributes;
                    r.add(m);
                }
            }
            if (patternChar != 0) {
                int pos = 0;
                while ((pos = sourceText.indexOf(patternChar, pos)) >= 0) {
                    int next = sourceText.offsetByCodePoints(pos, 1);
                    Mark m = new Mark(Mark.ENTRY_PART.SOURCE, pos, next);
                    m.painter = painter;
                    m.toolTipText = toolTip;
                    m.attributes = attributes;
                    r.add(m);
                    pos = next;
                }
            }
        }
        if (translationText != null) {
            initDrawers(false, isActive);
            if (pattern != null) {
                match = pattern.matcher(translationText);
                while (match.find()) {
                    Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, match.start(), match.end());
                    m.painter = painter;
                    m.toolTipText = toolTip;
                    m.attributes = attributes;
                    r.add(m);
                }
            }
            if (patternChar != 0) {
                int pos = 0;
                while ((pos = translationText.indexOf(patternChar, pos)) >= 0) {
                    int next = translationText.offsetByCodePoints(pos, 1);
                    Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, pos, next);
                    m.painter = painter;
                    m.toolTipText = toolTip;
                    m.attributes = attributes;
                    r.add(m);
                    pos = next;
                }
            }
        }

        return r;
    }

    /**
     * Marker can override this method for define different PAINTER, ATTRIBUTES
     * and PATTERN, based on source/translation, active/inactive and other.
     */
    protected void initDrawers(boolean isSource, boolean isActive) {
    }
}
