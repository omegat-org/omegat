/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2011 Didier Briel
               2016 Aaron Madlon-Kay
 2024 Hiroshi Miura
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
 * @author Hiroshi Miura
 */
public interface IMainMenu {
    /**
     * Get Machine Translation menu object.
     *
     * @return machine translation menu.
     */
    JMenu getMachineTranslationMenu();

    /**
     * Get Options menu object.
     *
     * @return options menu.
     */
    JMenu getOptionsMenu();

    /**
     * Get tools menu object.
     * @return tools menu.
     */
    JMenu getToolsMenu();

    /**
     * Get Glossary menu object.
     * @return glossary menu.
     */
    JMenu getGlossaryMenu();

    /**
     * Get Project menu object.
     * @return project menu.
     */
    JMenu getProjectMenu();

    /**
     * Get AutoCompletion JMenu object.
     * @return auto completion menu.
     */
    JMenu getAutoCompletionMenu();

    /**
     * Get Help menu's JMenu object.
     * @return help menu.
     */
    JMenu getHelpMenu();

    /**
     * Get menu item specified in MenuKey marker.
     *
     * @param marker a menu item key.
     * @return JMenu object.
     */
    JMenu getMenu(MenuExtender.MenuKey marker);

    /**
     * Enable/Disable menu item.
     * When it is JCheckBoxMenuItem and JRadioButtonMenuItem, a specified
     * item will be selected.
     *
     * @param name    a component name of menu item.
     * @param enabled true when make it enabled, otherwise make it disabled.
     */
    void enableMenuItem(String name, boolean enabled);

    void invokeAction(String action, int modifiers);
}
