/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Martin Fleurke
               2013 Alex Buloichik (alex73mail@gmail.com)
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;

/**
 * Abstract marker class that marks source and target text according to some
 * 'pattern' and paints them accoriding to some 'PAINTER' and 'ATTRIBUTES' (all
 * of they defined by implementing classes)
 * 
 * @author Martin Fleurke
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public abstract class AbstractMarker implements IMarker {
    protected HighlightPainter PAINTER;
    protected String toolTip;
    protected AttributeSet ATTRIBUTES;
    /** Regexp for mark. */
    protected Pattern pattern;
    /** char for mark. */
    protected char patternChar;

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

        List<Mark> r = new ArrayList<Mark>();
        Matcher match;
        if (isActive || Core.getEditor().getSettings().isDisplaySegmentSources() || translationText == null) {// TODO
            initDrawers(true, isActive);
            if (pattern != null) {
                match = pattern.matcher(sourceText);
                while (match.find()) {
                    Mark m = new Mark(Mark.ENTRY_PART.SOURCE, match.start(), match.end());
                    m.painter = PAINTER;
                    m.toolTipText = toolTip;
                    m.attributes = ATTRIBUTES;
                    r.add(m);
                }
            }
            if (patternChar != 0) {
                int pos = -1;
                while ((pos = sourceText.indexOf(patternChar, pos + 1)) >= 0) {
                    Mark m = new Mark(Mark.ENTRY_PART.SOURCE, pos, pos + 1);
                    m.painter = PAINTER;
                    m.toolTipText = toolTip;
                    m.attributes = ATTRIBUTES;
                    r.add(m);
                }
            }
        }
        if (translationText != null) {
            initDrawers(false, isActive);
            if (pattern != null) {
                match = pattern.matcher(translationText);
                while (match.find()) {
                    Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, match.start(), match.end());
                    m.painter = PAINTER;
                    m.toolTipText = toolTip;
                    m.attributes = ATTRIBUTES;
                    r.add(m);
                }
            }
            if (patternChar != 0) {
                int pos = -1;
                while ((pos = translationText.indexOf(patternChar, pos + 1)) >= 0) {
                    Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, pos, pos + 1);
                    m.painter = PAINTER;
                    m.toolTipText = toolTip;
                    m.attributes = ATTRIBUTES;
                    r.add(m);
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
