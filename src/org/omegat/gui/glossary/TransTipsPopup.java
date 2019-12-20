/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Wildrich Fourie, Alex Buloichik
               2011 Didier Briel
               2013 Aaron Madlon-Kay
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

import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import org.omegat.core.Core;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.util.Token;

/**
 * Popup for TransTips processing.
 *
 * @author W. Fourie
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class TransTipsPopup implements IPopupMenuConstructor {
    public void addItems(final JPopupMenu menu, JTextComponent comp, final int mousepos,
            boolean isInActiveEntry, boolean isInActiveTranslation, SegmentBuilder sb) {
        if (!Core.getEditor().getSettings().isMarkGlossaryMatches()) {
            return;
        }

        if (!isInActiveEntry || isInActiveTranslation) {
            return;
        }

        // is mouse in active entry's source ?
        final int startSource = sb.getStartSourcePosition();
        int len = sb.getSourceText().length();
        if (mousepos < startSource || mousepos > startSource + len) {
            return;
        }
        Set<String> added = new HashSet<>();
        for (GlossaryEntry ge : GlossaryTextArea.nowEntries) {
            for (Token[] toks : Core.getGlossaryManager().searchSourceMatchTokens(sb.getSourceTextEntry(), ge)) {
                for (Token tok : toks) {
                    // is inside found word ?
                    if (startSource + tok.getOffset() <= mousepos
                            && mousepos <= startSource + tok.getOffset() + tok.getLength()) {
                        // Create the MenuItems
                        for (String s : ge.getLocTerms(true)) {
                            if (!added.contains(s)) {
                                JMenuItem it = menu.add(s);
                                it.addActionListener(e -> Core.getEditor().insertText(s));
                                added.add(s);
                            }
                        }
                    }
                }
            }
        }
        menu.addSeparator();
    }
}
