/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024-2025 Hiroshi Miura
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
package org.omegat.gui.editor;

import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;

import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.LocaleRule;
import org.omegat.util.OStrings;

public class EditorTextAreaIntroTest extends TestCoreGUI {

    @Rule
    public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

    @Test
    public void testIntroPaneExist() {
        window.panel(OStrings.getString("DOCKING_FIRST_STEPS_TITLE")).requireEnabled();
        window.panel(OStrings.getString("DOCKING_FIRST_STEPS_TITLE")).scrollPane("EditorScrollPane").requireEnabled();
        window.panel(OStrings.getString("DOCKING_FIRST_STEPS_TITLE")).scrollPane("EditorScrollPane")
                .verticalScrollBar().requireVisible();
        window.panel(OStrings.getString("DOCKING_FIRST_STEPS_TITLE")).scrollPane("EditorScrollPane")
                .horizontalScrollBar().requireNotVisible();
    }

}
