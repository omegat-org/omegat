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

import java.nio.file.Paths;
import java.util.Locale;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.LocaleRule;

/**
 * @author Hiroshi Miura
 */
@RunWith(Enclosed.class)
public final class EditorUtilsGUITest {

    private EditorUtilsGUITest() {
    }

    public static class EditorUtilsFirstStepsTest extends TestCoreGUI {

        @Rule
        public final LocaleRule localeRule = new LocaleRule(Locale.ENGLISH);

        @Test
        public void testEditorUtilsGetWordFirstSteps() throws BadLocationException {
            int offs = 518;
            JTextComponent editPane = window.panel("First Steps").textBox("IntroPane").target();
            int posStart = EditorUtils.getWordStart(editPane, offs, Locale.ENGLISH);
            int posEnd = EditorUtils.getWordEnd(editPane, offs, Locale.ENGLISH);
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
            openSampleProject(Paths.get("test-acceptance/data/project_CN_JP/"));
            final JTextComponent editPane = window.panel("Editor - source.txt").textBox().target();
            int length = editPane.getDocument().getLength();
            assertTrue(length > 0);
            /* Edit pane shows a first entry like as follows
            #  ---------9---------9---------9---------9---------9
            44 Translated by Hiroshi Miura in 2024/11/07 21:26:21<br/>
            98 太平寺中的文笔塔<br/>
                  ^
           109 太平寺の中心的なペン塔<b>&lt;Segment 0001></b>
                ^
             */
            // select word from a source text
            int offs = 100;
            int posStart = EditorUtils.getWordStart(editPane, offs, Locale.SIMPLIFIED_CHINESE);
            int posEnd = EditorUtils.getWordEnd(editPane, offs, Locale.SIMPLIFIED_CHINESE);
            String word = editPane.getText(posStart, posEnd - posStart);
            assertEquals("太平寺", word);
            // select word from a translation
            offs = 109;
            posStart = EditorUtils.getWordStart(editPane, offs, Locale.JAPANESE);
            posEnd = EditorUtils.getWordEnd(editPane, offs, Locale.JAPANESE);
            word = editPane.getText(posStart, posEnd - posStart);
            assertEquals("太平寺", word);
        }
    }

}
