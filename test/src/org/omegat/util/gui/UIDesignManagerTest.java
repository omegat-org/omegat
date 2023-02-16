/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 miurahr.
 *                Home page: http://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.omegat.util.gui;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.io.IOException;

import javax.swing.UIDefaults;

import org.junit.Test;


public class UIDesignManagerTest {

    @Test
    public void testLoadDefaultSystemDarkColors() throws IOException {
        UIDefaults uiDefaults = new UIDefaults();
        UIDesignManager.loadDefaultSystemDarkColors(uiDefaults);
        assertEquals(Color.BLACK, uiDefaults.getColor("TextPane.background"));
    }

    @Test
    public void testLoadDefaultAppDarkColors() throws IOException {
        UIDefaults uiDefaults = new UIDefaults();
        uiDefaults.put("TextArea.background", Color.BLACK);
        uiDefaults.put("Table.background", Color.BLACK);
        uiDefaults.put("Table.foreground", Color.WHITE);
        UIDesignManager.loadDefaultColors(uiDefaults);
        assertEquals(new Color(0x82b682), uiDefaults.getColor("OmegaT.source"));
    }

    @Test
    public void testLoadDefaultAppLightColors() throws IOException {
        UIDefaults uiDefaults = new UIDefaults();
        uiDefaults.put("TextArea.background", Color.WHITE);
        uiDefaults.put("Table.background", Color.WHITE);
        uiDefaults.put("Table.foreground", Color.BLACK);
        UIDesignManager.loadDefaultColors(uiDefaults);
        assertEquals(new Color(0xc0ffc0), uiDefaults.getColor("OmegaT.source"));
    }
}
