/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.util.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Color;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.TestCoreInitializer;
import org.omegat.gui.main.ConsoleWindow;
import org.omegat.gui.main.IMainWindow;

public class StaticUIUtilsTest {

    @Before
    public void setUp() throws Exception {
        IMainWindow mainWindow = new ConsoleWindow();
        TestCoreInitializer.initMainWindow(mainWindow);
    }

    @Test
    public void testGetHighlightColor() {
        // Don't make an invalid color even with big or edge-casey adjustment values
        IntStream.range(-1000, 1000).forEach(i -> StaticUIUtils.getHighlightColor(Color.WHITE, i));

        assertEquals(new Color(10, 10, 10), StaticUIUtils.getHighlightColor(Color.BLACK, 10));
        assertEquals(new Color(10, 10, 10), StaticUIUtils.getHighlightColor(Color.BLACK, -10));
        assertEquals(new Color(245, 245, 245), StaticUIUtils.getHighlightColor(Color.WHITE, 10));
        assertEquals(new Color(245, 245, 245), StaticUIUtils.getHighlightColor(Color.WHITE, -10));
    }

    @Test
    public void testIsGUI() {
        assertFalse("Run with ConsoleWindow", StaticUIUtils.isGUI());
    }
}
