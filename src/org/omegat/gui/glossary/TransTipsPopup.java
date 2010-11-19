/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Wildrich Fourie, Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.glossary;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import org.omegat.core.Core;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.util.Preferences;

/**
 * Popup for TransTips processing.
 * 
 * @author W. Fourie
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TransTipsPopup implements IPopupMenuConstructor {
    public void addItems(final JPopupMenu menu, JTextComponent comp, final int mousepos,
            boolean isInActiveEntry, boolean isInActiveTranslation, SegmentBuilder sb) {
        if (!Preferences.isPreference(Preferences.TRANSTIPS)) {
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

        // Test if clicked on a highlighted word
        TransTips.Search callback = new TransTips.Search() {
            public void found(GlossaryEntry ge, int start, int end) {
                // is inside found word ?
                if (startSource + start <= mousepos && mousepos <= startSource + end) {
                    // Split the terms and remove the leading space.
                    String[] locs = ge.getLocText().split(",");
                    for (int l = 1; l < locs.length; l++) {
                        locs[l] = locs[l].trim();
                    }

                    // Create the MenuItems
                    for (int l = 0; l < locs.length; l++) {
                        final String txt = locs[l];
                        JMenuItem it = menu.add(locs[l]);
                        it.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                Core.getEditor().insertText(txt);
                            }
                        });
                    }
                }
            }
        };
        for (GlossaryEntry ge : GlossaryTextArea.nowEntries) {
            TransTips.search(sb.getSourceText(), ge, callback);
        }
    }
}
