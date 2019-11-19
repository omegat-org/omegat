/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.gui.search;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.omegat.core.Core;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.Java8Compat;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.openide.awt.Mnemonics;

@SuppressWarnings("serial")
public class SearchWindowMenu extends JMenuBar {

    private final SearchWindowController controller;

    public SearchWindowMenu(SearchWindowController controller) {
        this.controller = controller;
        init();
    }

    private void init() {
        JMenu fileMenu = add(new JMenu());
        Mnemonics.setLocalizedText(fileMenu, OStrings.getString("SW_FILE_MENU"));

        JMenuItem item;

        item = fileMenu.add(new JMenuItem());
        Mnemonics.setLocalizedText(item, OStrings.getString("SW_FILE_MENU_SELECT_SEARCH_FIELD"));
        item.setActionCommand("editFindInProjectMenuItem");
        item.addActionListener(e -> {
            String selection = controller.getViewerSelection();
            if (!StringUtil.isEmpty(selection)) {
                controller.setSearchText(selection);
            }
            controller.focusSearchField();
        });

        item = fileMenu.add(new JMenuItem());
        Mnemonics.setLocalizedText(item, OStrings.getString("SW_FILE_MENU_CLOSE"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Java8Compat.getMenuShortcutKeyMaskEx()));
        item.addActionListener(e -> controller.doCancel());

        JMenu editMenu = add(new JMenu());
        Mnemonics.setLocalizedText(editMenu, OStrings.getString("SW_EDIT_MENU"));

        // "Action Commands" must be the same as equivalent MainWindowMenu
        // members in order to get matching shortcuts.

        item = editMenu.add(new JMenuItem());
        Mnemonics.setLocalizedText(item, OStrings.getString("TF_MENU_EDIT_SOURCE_INSERT"));
        item.setActionCommand("editInsertSourceMenuItem");
        item.addActionListener(
                e -> controller.insertIntoActiveField(Core.getEditor().getCurrentEntry().getSrcText()));

        item = editMenu.add(new JMenuItem());
        Mnemonics.setLocalizedText(item, OStrings.getString("TF_MENU_EDIT_SOURCE_OVERWRITE"));
        item.setActionCommand("editOverwriteSourceMenuItem");
        item.addActionListener(
                e -> controller.replaceCurrentFieldText(Core.getEditor().getCurrentEntry().getSrcText()));

        editMenu.addSeparator();

        item = editMenu.add(new JMenuItem());
        Mnemonics.setLocalizedText(item, OStrings.getString("TF_MENU_EDIT_CREATE_GLOSSARY_ENTRY"));
        item.setActionCommand("editCreateGlossaryEntryMenuItem");
        item.addActionListener(e -> Core.getGlossary().showCreateGlossaryEntryDialog(controller.getWindow()));

        editMenu.addSeparator();

        item = editMenu.add(new JMenuItem());
        Mnemonics.setLocalizedText(item, OStrings.getString("SW_EDIT_MENU_RESET_OPTIONS"));
        item.addActionListener(e -> controller.resetOptions());

        PropertiesShortcuts.getMainMenuShortcuts().bindKeyStrokes(this);
    }
}
