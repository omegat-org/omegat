/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.core;

import org.omegat.core.threads.IAutoSave;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.glossary.IGlossaries;
import org.omegat.gui.main.IMainWindow;

/**
 * Core initializer for unit tests.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class TestCoreInitializer {

    private TestCoreInitializer() {
    }

    public static void initEditor(IEditor editor) {
        Core.editor = editor;
    }

    public static void initAutoSave(IAutoSave autoSave) {
        Core.saveThread = autoSave;
    }

    public static void initMainWindow(IMainWindow mainWindow) {
        Core.setMainWindow(mainWindow);
    }

    public static void initGlossary(IGlossaries glossaries) {
        Core.glossary = glossaries;
    }
}
