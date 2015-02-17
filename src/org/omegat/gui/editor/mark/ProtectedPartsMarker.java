/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
               2015 Aaron Madlon-Kay
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
import java.util.regex.Matcher;

import javax.swing.text.AttributeSet;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.commons.lang.StringEscapeUtils;
import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.Document3;
import org.omegat.gui.editor.EditorController;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.gui.Styles;

/**
 * Marker for SourceTextEntry.protectedParts and tags.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class ProtectedPartsMarker implements IMarker {
    protected static final HighlightPainter PAINTERrtl = new TransparentHighlightPainter(Styles.EditorColor.COLOR_PLACEHOLDER.getColor(), 0.2F);
    protected static final AttributeSet ATTRIBUTESltr = Styles.createAttributeSet(Styles.EditorColor.COLOR_PLACEHOLDER.getColor(), null, null,
            null);

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive)
            throws Exception {
        HighlightPainter painter;
        AttributeSet attrs;
        if (((EditorController) Core.getEditor()).getOrientation() == Document3.ORIENTATION.ALL_LTR) {
            attrs = ATTRIBUTESltr;
            painter = null;
        } else {
            attrs = null;
            painter = PAINTERrtl;
        }

        List<Mark> r = new ArrayList<Mark>();

        // find protected parts
        for (ProtectedPart pp : ste.getProtectedParts()) {
            int pos = -1;
            while ((pos = sourceText.indexOf(pp.getTextInSourceSegment(), pos + 1)) >= 0) {
                Mark m = new Mark(Mark.ENTRY_PART.SOURCE, pos, pos + pp.getTextInSourceSegment().length());
                m.painter = painter;
                m.attributes = attrs;
                m.toolTipText = escapeHtml(pp.getDetailsFromSourceFile());
                r.add(m);
            }
            if (translationText != null) {
                pos = -1;
                while ((pos = translationText.indexOf(pp.getTextInSourceSegment(), pos + 1)) >= 0) {
                    Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, pos, pos + pp.getTextInSourceSegment().length());
                    m.painter = painter;
                    m.attributes = attrs;
                    m.toolTipText = escapeHtml(pp.getDetailsFromSourceFile());
                    r.add(m);
                }
            }
        }

        return r;
    }

    private String escapeHtml(String s) {
        boolean doSimplify = Preferences.isPreferenceDefault(Preferences.VIEW_OPTION_PPT_SIMPLIFY, true);
        // See if tooltip is enclosed by tags. If so, either strip the tags (doSimplify == true)
        // or make the enclosed text bold (doSimplify != true).
        Matcher m = PatternConsts.PROTECTED_PARTS_PAIRED_TAG_DECOMPILE.matcher(s);
        if (m.find()) {
            s = s.replace("&", "&amp;");
            // paired tag
            StringBuilder text = new StringBuilder(s.length() * 2);
            if (!doSimplify) {
                text.append(m.group(1).replace("<", "&lt;").replace(">", "&gt;"));
                text.append("<b>");
            }
            text.append(m.group(2).replace("<", "&lt;").replace(">", "&gt;"));
            if (!doSimplify) {
                text.append("</b>");
                text.append(m.group(3).replace("<", "&lt;").replace(">", "&gt;"));
            }
            return text.toString();
        }
        if (doSimplify) {
            // See if the tooltip contains an equiv-text attribute. If so, use just the value.
            m = PatternConsts.EQUIV_TEXT_ATTRIBUTE_DECOMPILE.matcher(s);
            if (m.find()) {
                s = StringEscapeUtils.unescapeHtml(m.group(1));
            }
        }
        // standalone tag
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
