/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;

/**
 * Marker for TransTips.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TransTipsMarker implements IMarker {
    protected static final HighlightPainter TRANSTIPS_UNDERLINER = new UnderlineFactory.SolidBoldUnderliner(
            Styles.EditorColor.COLOR_TRANSTIPS.getColor());

    private final DefaultGlossaryRenderer renderer = new DefaultGlossaryRenderer();

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText,
            boolean isActive) {
        if (!isActive || sourceText == null) {
            return null;
        }
        if (!Core.getEditor().getSettings().isMarkGlossaryMatches()) {
            return null;
        }
        List<GlossaryEntry> glossaryEntries = Core.getGlossary().getDisplayedEntries();
        if (glossaryEntries == null || glossaryEntries.isEmpty()) {
            return null;
        }

        List<Mark> marks = new ArrayList<Mark>();

        for (GlossaryEntry ent : glossaryEntries) {
            String tooltip = renderer.renderToHtml(ent);
            List<Token[]> tokens = Core.getGlossaryManager().searchSourceMatchTokens(ste, ent);
            marks.addAll(getMarksForTokens(tokens, ste.getSrcText(), tooltip));
        }
        return marks;
    }

    private static List<Mark> getMarksForTokens(List<Token[]> tokens, String srcText, String tooltip) {
        if (tokens.isEmpty() || srcText.isEmpty()) {
            return Collections.emptyList();
        }
        List<Mark> result = new ArrayList<>(tokens.size());
        tokens.sort(Comparator.comparing(toks -> toks[0].getOffset()));
        for (Token[] toks : tokens) {
            if (toks.length > 1) {
                Arrays.sort(toks, Comparator.comparingInt(Token::getOffset));
            }
            for (Token tok : toks) {
                Mark prev = result.isEmpty() ? null : result.get(result.size() - 1);
                int currStart = tok.getOffset();
                int currEnd = currStart + tok.getLength();
                Mark newMark;
                // If two tokens (representing the same glossary hit) are separated only by whitespace,
                // combine them into a single mark
                if (prev != null && canCloseSpan(srcText, prev.endOffset, currStart)) {
                    newMark = new Mark(Mark.ENTRY_PART.SOURCE, prev.startOffset, currEnd);
                    result.set(result.size() - 1, newMark);
                } else {
                    newMark = new Mark(Mark.ENTRY_PART.SOURCE, currStart, currEnd);
                    result.add(newMark);
                }
                newMark.painter = TRANSTIPS_UNDERLINER;
                newMark.toolTipText = tooltip;
            }
        }
        return result;
    }

    private static boolean canCloseSpan(String text, int start, int end) {
        if (start < 0 || end > text.length()) {
            throw new IndexOutOfBoundsException();
        }
        for (int cp, i = start; i < end; i += Character.charCount(cp)) {
            cp = text.codePointAt(i);
            int type = Character.getType(cp);
            if (!Character.isWhitespace(cp) && type != Character.DASH_PUNCTUATION
                    && type != Character.CONNECTOR_PUNCTUATION) {
                return false;
            }
        }
        return true;
    }
}
