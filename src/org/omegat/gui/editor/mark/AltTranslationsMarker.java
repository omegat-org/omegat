/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Lev Abashkin
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

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.EditorSettings;
import org.omegat.util.gui.Styles;

import javax.swing.text.Highlighter;
import java.util.Collections;
import java.util.List;

public class AltTranslationsMarker extends AbstractMarker {

    protected static final Highlighter.HighlightPainter PAINTER = new TransparentHighlightPainter(
            Styles.EditorColor.COLOR_MARK_ALT_TRANSLATION.getColor(), 0.5F);

    public static void loadPlugins() {
        Core.registerMarkerClass(AltTranslationsMarker.class);
    }
    public static void unloadPlugins() {
    }

    public AltTranslationsMarker() throws Exception {
        super();
    }

    @Override
    protected boolean isEnabled() {
        return ((EditorSettings) Core.getEditor().getSettings()).isMarkAltTranslations();
    }

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive) throws Exception {
        if (!isEnabled()) {
            return null;
        }

        if (!Core.getProject().getTranslationInfo(ste).defaultTranslation) {
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, 0, translationText.length());
            m.painter = PAINTER;
            return Collections.singletonList(m);
        }

        return null;
    }
}
