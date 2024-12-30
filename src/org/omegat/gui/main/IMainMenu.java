/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2011 Didier Briel
               2016 Aaron Madlon-Kay
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

package org.omegat.gui.main;

import javax.swing.JMenu;

import org.omegat.util.gui.MenuExtender;

/**
 * Main menu interface.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public interface IMainMenu {
    JMenu getMachineTranslationMenu();

    JMenu getOptionsMenu();

    JMenu getToolsMenu();

    JMenu getGlossaryMenu();

    JMenu getProjectMenu();

    JMenu getAutoCompletionMenu();

    JMenu getHelpMenu();

    JMenu getMenu(MenuExtender.MenuKey marker);

    void invokeAction(String action, int modifiers);
}
