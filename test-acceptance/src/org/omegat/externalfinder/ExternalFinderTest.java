/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Hiroshi Miura
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
package org.omegat.externalfinder;

import org.junit.Test;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.main.TestCoreGUI;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExternalFinderTest extends TestCoreGUI {

    private static final Path PROJECT_PATH = Paths.get("test-acceptance/data/project/");

    @Test
    public void testConfigLoadedAndMenuAdded() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        CoreEvents.registerProjectChangeListener(eventType -> {
            if (eventType == IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD) {
                latch.countDown();
            }
        });
        openSampleProject(PROJECT_PATH);
        robot().waitForIdle();
        assertNotNull(window);

        try {
            assertTrue(latch.await(60, TimeUnit.SECONDS));
        } catch (InterruptedException ignored) {
        }
        robot().waitForIdle();

        window.menuItem("Eng-Esp Trifecta").requireEnabled();
        window.menuItem("Esp-Eng Trifecta").requireEnabled();
    }
}
