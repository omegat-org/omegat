/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.tagvalidation.TagValidation;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.util.NumeralValueParser;
import org.omegat.util.PatternConsts;
import org.omegat.util.TagUtil;
import org.omegat.util.TagUtil.Tag;
import org.omegat.util.gui.Styles;

/**
 * Marker underlining the numerals the numeral check watches over, in the
 * manner of the glossary match underline: every numeral of any writing
 * system, in the source and in the translation, outside the real tags. The
 * marker has no view option of its own - it follows the per-project numeral
 * check - and it ships without a color, so nothing is painted until the
 * user picks a color under Options &gt; Preferences &gt; Colours.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumeralMarker implements IMarker {

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText,
            boolean isActive) {
        // read per call so that color preference changes take effect
        // without restarting the application
        Color color = Styles.EditorColor.COLOR_NUMERALS.getColor();
        if (color == null || !TagValidation.isNumeralCheckEnabled()) {
            return null;
        }
        HighlightPainter underliner = new UnderlineFactory.SolidBoldUnderliner(color);

        List<Mark> result = new ArrayList<>();
        if (sourceText != null
                && (isActive || Core.getEditor().getSettings().isDisplaySegmentSources()
                        || translationText == null)) {
            addMarks(result, Mark.ENTRY_PART.SOURCE, sourceText, ste, underliner);
        }
        if (translationText != null) {
            addMarks(result, Mark.ENTRY_PART.TRANSLATION, translationText, ste, underliner);
        }
        return result;
    }

    private void addMarks(List<Mark> result, Mark.ENTRY_PART part, String text, SourceTextEntry ste,
            HighlightPainter underliner) {
        List<Tag> tags = tagSpans(text, ste);
        Matcher m = PatternConsts.NUMERALS_WITH_SEPARATORS.matcher(text);
        while (m.find()) {
            if (overlapsTag(m.start(), m.end(), tags)) {
                continue;
            }
            Mark mark = new Mark(part, m.start(), m.end());
            mark.painter = underliner;
            mark.toolTipText = toolTip(m.group());
            result.add(mark);
        }
    }

    /**
     * The real tags of the text; numerals inside them belong to the tag
     * checks, not to this marker. A protected part that is itself a numeral
     * the parser reads - typically a number turned into a custom tag by the
     * default custom tag expression - is no tag to this marker: the numeral
     * check compares such a part by value, exactly like a plain numeral, so
     * the marker underlines it too.
     */
    private List<Tag> tagSpans(String text, SourceTextEntry ste) {
        List<Tag> tags = new ArrayList<>();
        if (ste != null) {
            for (Tag tag : TagUtil.buildTagList(text, ste.getProtectedParts())) {
                if (NumeralValueParser.parseTokenValue(tag.tag, false).isEmpty()) {
                    tags.add(tag);
                }
            }
        }
        Matcher m = PatternConsts.OMEGAT_TAG.matcher(text);
        while (m.find()) {
            tags.add(new Tag(m.start(), m.group()));
        }
        return tags;
    }

    private boolean overlapsTag(int start, int end, List<Tag> tags) {
        for (Tag tag : tags) {
            if (start < tag.pos + tag.tag.length() && tag.pos < end) {
                return true;
            }
        }
        return false;
    }

    /**
     * The plain-digit value, for a numeral whose own spelling reads
     * differently; a numeral already written in plain digits needs none,
     * and an ambiguous separated spelling gets none either.
     */
    private String toolTip(String numeral) {
        List<NumeralValueParser.Rational> values = NumeralValueParser.parseSeparatedValues(numeral, false);
        if (values.size() != 1) {
            return null;
        }
        String value = values.get(0).toString();
        return value.equals(numeral) ? null : "= " + value;
    }
}
