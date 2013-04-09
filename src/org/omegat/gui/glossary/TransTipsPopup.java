/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Wildrich Fourie, Alex Buloichik
               2011 Didier Briel
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
 * @author Didier Briel
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
                    String[] locs = parseLine(ge.getLocText());
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
	    menu.addSeparator();
    }

    /**
     * Separator for glossary entries
     */
    protected static final char SEPARATOR = ',';

    /**
     * Parse the glossary entries
     * @param line A line containing the multiple terms
     * @return An array with the multiple terms
     */
    private static String[] parseLine(String line) {
        List<String> result = new ArrayList<String>();
        StringBuilder term = new StringBuilder();
        boolean fopened = false; // Field opened by "
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            switch (c) {
            case '"':
                if (term.toString().trim().length() == 0 && !fopened) {
                    // First " in field
                    fopened = true;
                } else if (fopened) {
                    // Last " in field
                    fopened = false;
                } else {
                    term.append(c);
                }
                break;
            case SEPARATOR:
                if (fopened) {
                    term.append(c);
                } else {
                    result.add(term.toString());
                    term.setLength(0);
                }
                break;
            default:
                term.append(c);
                break;
            }
        }
        result.add(term.toString());
        return result.toArray(new String[result.size()]);
    }

}
