/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
            String tooltip = ent.toStyledString().toHTML();
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
        for (Token[] toks : tokens) {
            Arrays.sort(toks, Comparator.comparing(Token::getOffset));
            if (canCombineMarks(srcText, toks)) {
                Token first = toks[0];
                Token last = toks[toks.length - 1];
                int start = first.getOffset();
                int end = last.getOffset() + last.getLength();
                Mark mark = new Mark(Mark.ENTRY_PART.SOURCE, start, end);
                mark.painter = TRANSTIPS_UNDERLINER;
                mark.toolTipText = tooltip;
                result.add(mark);
            } else {
                for (Token tok : toks) {
                    int start = tok.getOffset();
                    int end = tok.getOffset() + tok.getLength();
                    Mark mark = new Mark(Mark.ENTRY_PART.SOURCE, start, end);
                    mark.painter = TRANSTIPS_UNDERLINER;
                    mark.toolTipText = tooltip;
                    result.add(mark);
                }
            }
        }
        return result;
    }

    private static boolean canCombineMarks(String text, Token[] toks) {
        if (toks.length < 2) {
            return false;
        }
        for (int i = 1; i < toks.length; i++) {
            Token prev = toks[i - 1];
            Token curr = toks[i];
            if (!canCloseSpan(text, prev.getOffset() + prev.getLength(), curr.getOffset())) {
                return false;
            }
        }
        return true;
    }

    private static boolean canCloseSpan(String text, int start, int end) {
        if (start < 0 || end >= text.length() || start > end) {
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
