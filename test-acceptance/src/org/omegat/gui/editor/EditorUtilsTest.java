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

import java.util.Locale;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.LocaleRule;

/**
 * @author Hiroshi Miura
 */
@RunWith(Enclosed.class)
public class EditorUtilsTest {

    public static class EditorUtilsEnTest extends TestCoreGUI {

        @Rule
        public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

        @Test
        public void testEditorUtilsGetWordEn() throws BadLocationException {
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

}
