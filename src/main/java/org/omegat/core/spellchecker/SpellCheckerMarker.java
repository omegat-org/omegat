/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2026 Stephan Pakebusch
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

package org.omegat.core.spellchecker;

import java.util.List;
import java.util.stream.Collectors;

import javax.swing.text.Highlighter.HighlightPainter;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;

/**
 * Spell checker marker implementation. All words for displayed file will be
 * cached, because check spelling is enough long operations.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SpellCheckerMarker implements IMarker {

    @Override
    public @Nullable List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText,
            String translationText, boolean isActive) throws Exception {
        if (translationText == null) {
            // translation is not displayed
            return null;
        }
        if (!Core.getEditor().getSettings().isAutoSpellChecking()) {
            // spell checker disabled
            return null;
        }
        // created per call so that color preference changes take effect
        // without restarting the application
        HighlightPainter highlightPainter = new UnderlineFactory.WaveUnderline(
                Styles.EditorColor.COLOR_SPELLCHECK.getColor());
        List<Token> misspelled = filterProtectedParts(
                Core.getSpellChecker().getMisspelledTokens(translationText), ste, translationText);
        return misspelled.stream().map(tok -> {
            int st = tok.getOffset();
            int en = st + tok.getLength();
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, st, en);
            m.painter = highlightPainter;
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * Drop misspelled tokens lying entirely inside an occurrence of one of
     * the entry's protected parts in the translation text. Placeholder
     * syntax is not prose the translator can fix, so words inside it (for
     * example the "ld" inside "%1$ld") get no spelling marks. Tokens
     * reaching beyond a single occurrence are kept: they may point at a
     * real problem around it.
     *
     * @param tokens
     *            misspelled tokens whose offsets refer to translationText
     * @param ste
     *            source entry providing the protected parts; may be null,
     *            in which case nothing is filtered
     * @param translationText
     *            the text that was checked
     * @return the tokens without those inside protected parts
     */
    public static List<Token> filterProtectedParts(List<Token> tokens, @Nullable SourceTextEntry ste,
            String translationText) {
        if (tokens.isEmpty() || ste == null || ste.getProtectedParts().length == 0) {
            return tokens;
        }
        List<int[]> occurrences = ProtectedPart.occurrencesIn(translationText, ste.getProtectedParts());
        if (occurrences.isEmpty()) {
            return tokens;
        }
        return tokens.stream()
                .filter(t -> occurrences.stream()
                        .noneMatch(o -> t.getOffset() >= o[0] && t.getOffset() + t.getLength() <= o[1]))
                .collect(Collectors.toList());
    }
}
