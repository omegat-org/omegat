/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.spellchecker;

import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.gui.Styles;

/**
 * Spell checker marker implementation. All words for displayed file will be
 * cached, because check spelling is enough long operations.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SpellCheckerMarker implements IMarker {
    protected final HighlightPainter highlightPainter;

    public SpellCheckerMarker() {
        highlightPainter = new UnderlineFactory.WaveUnderline(Styles.EditorColor.COLOR_SPELLCHECK.getColor());
    }

    /**
     *
     * @param ste
     * @param sourceText might be null!
     * @param translationText might be null!
     * @param isActive is this an active segment in the document?
     * @return
     * @throws Exception
     */
    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive)
            throws Exception {
        if (translationText == null) {
            // translation is not displayed
            return null;
        }
        if (!isEnabled()) {
            // spell checker disabled
            return null;
        }
        return Core.getSpellChecker().getMisspelledTokens(translationText).stream().map(tok -> {
            int st = tok.getOffset();
            int en = st + tok.getLength();
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, st, en);
            m.painter = highlightPainter;
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public String getMarkerName() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getPreferenceKey() {
        return null;
    }

    @Override
    public void setEnabled(final boolean val) {
        Core.getEditor().getSettings().setAutoSpellChecking(val);
        if (Core.getProject().isProjectLoaded()) {
            Core.getEditor().remarkOneMarker(SpellCheckerMarker.class.getName());
        }
    }

    @Override
    public boolean isEnabled() {
        return Core.getEditor().getSettings().isAutoSpellChecking();
    }
}
