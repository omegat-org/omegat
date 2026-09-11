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
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.Styles;

/**
 * Spell checker marker implementation. All words for displayed file will be
 * cached, because check spelling is enough long operations.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SpellCheckerMarker implements IMarker {

    /** At most this many suggestions fit a readable tooltip. */
    static final int MAX_TOOLTIP_SUGGESTIONS = 5;

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
        return Core.getSpellChecker().getMisspelledTokens(translationText).stream().map(tok -> {
            int st = tok.getOffset();
            int en = st + tok.getLength();
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, st, en);
            m.painter = highlightPainter;
            // Resolved when the mouse rests on the word: a suggestion
            // lookup is too expensive to run per mark, especially for the
            // active segment, whose marks recompute on every keystroke.
            String word = tok.getTextFromString(translationText);
            m.toolTipSupplier = () -> suggestionToolTip(word);
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * The tooltip of a misspelled word: its suggestions, cached per word by
     * the spell checker, so only the first hover of a word runs a lookup.
     */
    static String suggestionToolTip(String word) {
        return formatSuggestions(Core.getSpellChecker().suggest(word));
    }

    /**
     * The suggestions as tooltip text, or the "no suggestions" notice. The
     * suggestion tags render bold, like in the language checker tooltips
     * (MarkerController.getToolTips maps them); a trailing ellipsis points
     * to the context menu when the list was cut.
     */
    static String formatSuggestions(List<String> suggestions) {
        if (suggestions.isEmpty()) {
            return OStrings.getString("SC_NO_SUGGESTIONS");
        }
        String shown = suggestions.stream().limit(MAX_TOOLTIP_SUGGESTIONS)
                .map(StringUtil::makeValidXML)
                .map(s -> "<suggestion>" + s + "</suggestion>")
                .collect(Collectors.joining(", "));
        if (suggestions.size() > MAX_TOOLTIP_SUGGESTIONS) {
            shown += ", ...";
        }
        return StringUtil.format(OStrings.getString("SC_TOOLTIP_SUGGESTIONS"), shown);
    }
}
