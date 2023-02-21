/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2018 Lev Abashkin
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

package org.omegat.gui.dictionaries;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.EditorPopups;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.util.OStrings;

import org.openide.awt.Mnemonics;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

public class DictionaryPopup implements IPopupMenuConstructor {

    @Override
    public void addItems(JPopupMenu menu, JTextComponent comp, int mousepos, boolean isInActiveEntry, boolean isInActiveTranslation, SegmentBuilder sb) {

        String selection = Core.getEditor().getSelectedText();
        final String searchedText;
        if (selection == null) {
            SourceTextEntry ste = Core.getEditor().getCurrentEntry();
            searchedText = ste.getSrcText();
        } else {
            searchedText = selection;
        }

        JMenuItem searchMenuItem = new JMenuItem();
        searchMenuItem.setText(Mnemonics.removeMnemonics(OStrings.getString("TF_MENU_EDIT_SEARCH_DICTIONARY")));
        searchMenuItem.addActionListener(e -> Core.getDictionaries().searchText(searchedText));
        menu.add(searchMenuItem);
        menu.addSeparator();
    }
}
