/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.core.spellchecker;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Token;

/**
 * Spell checker marker implementation. All words for displayed file will be
 * cached, because check spelling is enough long operation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SpellCheckerMarker implements IMarker {
    protected static final HighlightPainter PAINTER = new UnderlineFactory.WaveUnderline(Color.RED);

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive)
            throws Exception {
        if (translationText == null) {
            // translation not displayed
            return null;
        }
        if (!Core.getEditor().getSettings().isAutoSpellChecking()) {
            // spell checker disabled
            return null;
        }
        List<Mark> result = new ArrayList<Mark>();
        for (Token tok : Core.getProject().getTargetTokenizer().tokenizeWordsForSpelling(translationText)) {
            int st = tok.getOffset();
            int en = tok.getOffset() + tok.getLength();
            String word = translationText.substring(st, en);
            if (!Core.getSpellChecker().isCorrect(word)) {
                Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, st, en);
                m.painter = PAINTER;
                result.add(m);
            }
        }
        return result;
    }
}
