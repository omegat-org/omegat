/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2014 Piotr Kulik
               2015 Aaron Madlon-Kay
               2023 Jean-Christophe Helary
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

import java.io.File;

import org.omegat.util.OStrings;

/**
 * Main window UI.
 *
 * @author Keith Godfrey
 * @author Benjamin Siband
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Piotr Kulik
 * @author Aaron Madlon-Kay
 */
public final class MainWindowUI {

    private MainWindowUI() {
    }

    public static final String UI_LAYOUT_FILE = "uiLayout" + OStrings.getBrandingToken() + ".xml";

    /**
     * Initialize the size of OmegaT window, then load the layout prefs.
     */
    @Deprecated
    public static void initializeScreenLayout(MainWindow mainWindow) {
        mainWindow.initializeScreenLayout();
    }

    /**
     * Load the main window layout from the global preferences file. Will reset
     * to defaults if global preferences are not present or if an error occurs.
     */
    @Deprecated
    public static void loadScreenLayoutFromPreferences(MainWindow mainWindow) {
        mainWindow.loadScreenLayoutFromPreferences();
    }

    /**
     * Load the main window layout from the specified file. Will reset to
     * defaults if an error occurs.
     */
    @Deprecated
    public static void loadScreenLayout(MainWindow mainWindow, File uiLayoutFile) {
        mainWindow.loadScreenLayout(uiLayoutFile);
    }

    /**
     * Stores main window docking layout to disk.
     */
    @Deprecated
    public static void saveScreenLayout(MainWindow mainWindow) {
        mainWindow.saveScreenLayout();
    }

    /**
     * Stores main window layout to the specified output file.
     */
    @Deprecated
    static void saveScreenLayout(MainWindow mainWindow, File uiLayoutFile) {
        mainWindow.saveScreenLayout(uiLayoutFile);
    }

    /**
     * Restores main window layout to the default values (distinct from global
     * preferences).
     */
    @Deprecated
    public static void resetDesktopLayout(MainWindow mainWindow) {
        mainWindow.resetDesktopLayout();
    }
}
