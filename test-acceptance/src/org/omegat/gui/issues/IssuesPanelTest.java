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
package org.omegat.gui.issues;

import org.junit.Test;
import org.omegat.gui.main.TestCoreGUI;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IssuesPanelTest extends TestCoreGUI {

    private static final Path PROJECT_PATH = Paths.get("test-acceptance/data/project/");

    @Test
    public void testIssuesPanelShow() throws Exception {
        String[] expectedType = new String[]{"Terminology", "LanguageTool"};

        // load project
        openSampleProject(PROJECT_PATH);
        robot().waitForIdle();
        //
        assertNotNull(window);
        IssuesPanelControllerMock issuesPanelController = new IssuesPanelControllerMock(window.target());
        CountDownLatch latch = new CountDownLatch(1);
        // watch for table update
        issuesPanelController.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("selectedEntry")) {
                latch.countDown();
            }
        });

        SwingUtilities.invokeAndWait(() -> issuesPanelController.showForFiles(".*txt", 1));

        try {
            assertTrue(latch.await(20, TimeUnit.SECONDS));
        } catch (InterruptedException ignored) {
        }
        robot().waitForIdle();

        var model = issuesPanelController.getPanel().table.getModel();
        assertEquals("Expected segment number of the issue", 13, model.getValueAt(0, 0));
        String type = (String) model.getValueAt(0, 2);
        assertTrue("Issue type is unexpected", expectedType[0].equals(type) || expectedType[1].equals(type));

        closeProject();
    }

    public static class IssuesPanelControllerMock extends IssuesPanelController {

        public IssuesPanelControllerMock(Window parent) {
            super(parent);
        }
    }
}
