/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Chihiro Hio
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

package org.omegat.externalfinder.item;

import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import org.omegat.core.Core;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.util.gui.MenuItemPager;

public class ExternalFinderItemPopupMenuConstructor implements IPopupMenuConstructor {

    public void addItems(JPopupMenu menu, JTextComponent comp, int mousepos,
            boolean isInActiveEntry, boolean isInActiveTranslation, SegmentBuilder sb) {
        final String selection = Core.getEditor().getSelectedText();
        if (selection == null) {
            return;
        }

        ExternalFinderItem.TARGET target;
        if (ExternalFinderItem.isASCII(selection)) {
            target = ExternalFinderItem.TARGET.ASCII_ONLY;
        } else {
            target = ExternalFinderItem.TARGET.NON_ASCII_ONLY;
        }

        IExternalFinderItemMenuGenerator generator = new ExternalFinderItemMenuGenerator(target, true);
        List<JMenuItem> newMenuItems = generator.generate();

        menu.addSeparator();
        MenuItemPager pager = new MenuItemPager(menu);
        for (JMenuItem component : newMenuItems) {
            pager.add(component);
        }
        menu.addSeparator();
    }
}
