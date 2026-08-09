/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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
package org.omegat.gui.filelist;

import static org.junit.Assert.assertNull;

import java.awt.GraphicsEnvironment;
import java.lang.ref.WeakReference;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import org.omegat.util.TestPreferencesInitializer;

/**
 * {@link ProjectFilesListController#dispose()} must unhook the controller
 * from the static event registries ({@code CoreEvents}, the preference
 * change support): the registered listeners capture the controller, so as
 * long as any of them stays registered, a disposed controller remains
 * strongly reachable for the rest of the JVM and keeps reacting to project
 * events.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class ProjectFilesListControllerDisposeTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        Assume.assumeFalse("Skipping test: headless environment",
                GraphicsEnvironment.isHeadless());
        TestPreferencesInitializer.init();
    }

    @Test
    public void disposedControllerBecomesUnreachable() throws Exception {
        ProjectFilesListController controller = new ProjectFilesListController();
        controller.dispose();
        WeakReference<ProjectFilesListController> ref = new WeakReference<>(controller);
        controller = null;

        for (int attempt = 0; attempt < 50 && ref.get() != null; attempt++) {
            System.gc();
            // Nudge collection of the freshly allocated controller graph.
            byte[][] pressure = new byte[16][];
            for (int i = 0; i < pressure.length; i++) {
                pressure[i] = new byte[1024 * 1024];
            }
            Thread.sleep(10);
        }
        assertNull("a disposed controller must not stay reachable through the"
                + " global event registries", ref.get());
    }
}
