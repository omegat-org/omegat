/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.LocaleRule;
import org.omegat.util.Preferences;

/**
 * @author Hiroshi Miura
 */
@RunWith(Enclosed.class)
public class EditorUtilsTest {

    public static class EditorUtilsFirstStepsTest extends TestCoreGUI {

        @Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

        @Test
        public void testEditorUtilsGetWordFirstSteps() throws BadLocationException {
            int offs = 518;
            JTextComponent editPane = window.panel("First Steps").textBox("IntroPane").target();
            int posStart = EditorUtils.getWordStart(editPane, offs);
            int posEnd = EditorUtils.getWordEnd(editPane, offs);
            String word = editPane.getText(posStart, posEnd - posStart);
            assertEquals("translation", word);
            assertEquals(508, posStart);
            assertEquals(519, posEnd);
        }
    }

    public static class EditorUtilsLoadedProjectTest extends TestCoreGUI {

        @Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

        @Rule
        public final TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

        @Test
        public void testEditorUtilsGetWordLoadedProject() throws Exception {
            // Prepare a sample project
            File tmpDir = folder.newFolder("omegat-sample-project-");
            File projSrc = new File("test-acceptance/data/project_CN_JP/");
            FileUtils.copyDirectory(projSrc, tmpDir);
            FileUtils.forceDeleteOnExit(tmpDir);
            Preferences.setPreference(Preferences.PROJECT_FILES_SHOW_ON_LOAD, false);
            // Load the project and wait a completion
            CountDownLatch latch = new CountDownLatch(1);
            CoreEvents.registerProjectChangeListener(eventType -> {
                if (Core.getProject().isProjectLoaded()) {
                    latch.countDown();
                }
            });
            SwingUtilities.invokeAndWait(() -> ProjectUICommands.projectOpen(tmpDir, true));
            try {
                assertTrue(latch.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ignored) {
            }
            //
            final JTextComponent editPane = window.panel("Editor - source.txt").textBox().target();
            // select word from a source text
            int offs = 102;
            int posStart = EditorUtils.getWordStart(editPane, offs);
            int posEnd = EditorUtils.getWordEnd(editPane, offs);
            String word = editPane.getText(posStart, posEnd - posStart);
            assertEquals("太平寺中的文笔塔", word);
            // select word from a translation
            offs = 109;
            posStart = EditorUtils.getWordStart(editPane, offs);
            posEnd = EditorUtils.getWordEnd(editPane, offs);
            word = editPane.getText(posStart, posEnd - posStart);
            assertEquals("太平寺", word);
        }
    }

}
