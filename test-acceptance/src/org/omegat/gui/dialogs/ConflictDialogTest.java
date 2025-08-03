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

package org.omegat.gui.dialogs;

import org.junit.Rule;
import org.junit.Test;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.LocaleRule;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConflictDialogTest extends TestCoreGUI {

    @Rule
    public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

    @Test
    public void testConflictDialogLocal() throws IOException {
        final String baseText = "base text";
        final String remoteText = "remote text";
        final String localText = "local text";
        assertNotNull(window);
        JFrame parent = (JFrame) window.target();
        ConflictDialogController controller = new ConflictDialogController(parent);
        SwingUtilities.invokeLater(() -> controller.show(baseText, remoteText, localText));
        robot().waitForIdle();
        //
        CountDownLatch latch = new CountDownLatch(1);
        window.dialog().requireVisible();
        window.dialog().requireModal();
        takeScreenshot(ConflictDialogTest.class.getName(), "testConflictDialogLocal.png");
        //
        window.dialog().textBox("textLeft").requireText(baseText);
        window.dialog().textBox("textCenter").requireText(localText);
        window.dialog().textBox("textRight").requireText(remoteText);
        //
        window.dialog().button("btnMine").click();
        robot().waitForIdle();
        SwingUtilities.invokeLater(() ->  {
            if (controller.getResult() != null) {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException ignored) {
            fail("Test is interrupted. (timeout 10 sec.)");
        }
        assertEquals("Selection should be local.", localText, controller.getResult());
    }


    @Test
    public void testConflictDialogRemote() throws IOException {
        final String baseText = "base text";
        final String remoteText = "remote text";
        final String localText = "local text";
        assertNotNull(window);
        JFrame parent = (JFrame) window.target();
        ConflictDialogController controller = new ConflictDialogController(parent);
        SwingUtilities.invokeLater(() -> controller.show(baseText, remoteText, localText));
        robot().waitForIdle();
        //
        CountDownLatch latch = new CountDownLatch(1);
        window.dialog().requireVisible();
        window.dialog().requireModal();
        takeScreenshot(ConflictDialogTest.class.getName(), "testConflictDialogRemote.png");
        //
        window.dialog().button("btnTheirs").click();
        robot().waitForIdle();
        SwingUtilities.invokeLater(() ->  {
            if (controller.getResult() != null) {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException ignored) {
            fail("Test is interrupted. (timeout 10 sec.)");
        }
        assertEquals("Selection should be remote", remoteText, controller.getResult());
    }

}
