/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.gui.exttrans;

import org.junit.Test;
import org.omegat.core.data.TestCoreState;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.machinetranslators.dummy.DummyMachineTranslator;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class MachineTranslateTest extends TestCoreGUI {

    private static final Path PROJECT_PATH = Paths.get("test-acceptance/data/project/");

    @Test
    public void testMachineTranslation() throws Exception {
        // load project
        openSampleProject(PROJECT_PATH);
        robot().waitForIdle();
        //
        MachineTranslateTextArea machineTranslateTextArea = TestCoreState.getInstance().getMachineTranslatePane();
        assertEquals(DummyMachineTranslator.ENGINE_NAME, machineTranslateTextArea.getDisplayedTranslation().translatorName);
        assertEquals(DummyMachineTranslator.TRANSLATION, machineTranslateTextArea.getDisplayedTranslation().result);
    }
}
