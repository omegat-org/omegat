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
package org.omegat.gui.preferences.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Comparator;

import org.junit.After;
import org.junit.Test;

import org.omegat.gui.preferences.view.CustomColorSelectionController.ColorColumns;
import org.omegat.util.gui.ColorEntry;
import org.omegat.util.gui.ColorRegistry;

/**
 * @author stephan.pakebusch at zollsoft.de
 */
public class CustomColorSelectionControllerTest {

    @After
    public void tearDown() {
        // The registry is session-global; drop the plugin entry registered by
        // tableModelIncludesRegisteredPluginColors so re-runs in the same JVM
        // and later tests do not see it.
        ColorRegistry.clearPluginEntries();
    }

    @Test
    public void hsbComparatorGroupsUnsetThenGreyThenChromatic() {
        Comparator<Color> cmp = CustomColorSelectionController.hsbComparator();
        // Unset (null, "follows the look and feel") sorts before any real colour.
        assertTrue(cmp.compare(null, Color.GRAY) < 0);
        // Greys (achromatic) sort before chromatic colours.
        assertTrue(cmp.compare(Color.GRAY, Color.RED) < 0);
        // Within the greys, darker before lighter.
        assertTrue(cmp.compare(Color.DARK_GRAY, Color.LIGHT_GRAY) < 0);
        // Within the chromatic colours, the hue wheel orders red before blue.
        assertTrue(cmp.compare(Color.RED, Color.BLUE) < 0);
        // Equal colours compare equal (stable grouping of identical values).
        assertEquals(0, cmp.compare(Color.RED, new Color(255, 0, 0)));
    }

    @Test
    public void luminanceComparatorOrdersDarkBeforeLight() {
        Comparator<Color> cmp = CustomColorSelectionController.luminanceComparator();
        // Unset sorts first, then by perceived brightness, darkest first.
        assertTrue(cmp.compare(null, Color.BLACK) < 0);
        assertTrue(cmp.compare(Color.BLACK, Color.WHITE) < 0);
        // Blue is far less luminant than green under Rec. 709 weighting.
        assertTrue(cmp.compare(Color.BLUE, Color.GREEN) < 0);
    }

    @Test
    public void hexComparatorSortsUnsetFirstThenByHexValue() {
        Comparator<Color> cmp = CustomColorSelectionController.hexComparator();
        assertTrue(cmp.compare(null, Color.BLACK) < 0);
        assertTrue(cmp.compare(Color.BLACK, Color.WHITE) < 0);
        // Lexicographic on #rrggbb: red channel dominates.
        assertTrue(cmp.compare(new Color(0x10, 0xff, 0xff), new Color(0x20, 0x00, 0x00)) < 0);
    }

    @Test
    public void runningNumberIsTheFirstColumn() {
        // The "#" column must be the leading column so that it doubles as the
        // default sort order (natural EditorColor enum order).
        assertEquals(0, ColorColumns.NUMBER.ordinal());
        assertEquals(ColorColumns.NUMBER, ColorColumns.values()[0]);
        assertEquals(ColorColumns.NUMBER, ColorColumns.get(0));
    }

    @Test
    public void colorSortKeyFormatsAsLowercaseHex() {
        assertEquals("#ff0000", CustomColorSelectionController.colorSortKey(Color.RED));
        assertEquals("#00ff00", CustomColorSelectionController.colorSortKey(Color.GREEN));
        assertEquals("#0000ff", CustomColorSelectionController.colorSortKey(Color.BLUE));
        assertEquals("#000000", CustomColorSelectionController.colorSortKey(Color.BLACK));
        assertEquals("#0a141e", CustomColorSelectionController.colorSortKey(new Color(10, 20, 30)));
    }

    @Test
    public void tableModelIncludesRegisteredPluginColors() {
        ColorEntry entry = ColorRegistry.registerPluginColor("testcontroller", "swatch",
                "Controller Test Swatch", "Test.swatch", Color.PINK);
        CustomColorSelectionController controller = new CustomColorSelectionController();
        CustomColorSelectionController.ColorTableModel model = controller.new ColorTableModel();
        boolean found = false;
        for (int row = 0; row < model.getRowCount(); row++) {
            if (entry.getId().equals(model.getValueAt(row, ColorColumns.INTERNAL.ordinal()))) {
                assertEquals("Controller Test Swatch",
                        model.getValueAt(row, ColorColumns.NAME.ordinal()));
                assertEquals(Color.PINK, model.getValueAt(row, ColorColumns.COLOR.ordinal()));
                found = true;
            }
        }
        assertTrue("plugin color must appear in the colors table model", found);
    }

    @Test
    public void colorSortKeyForNullIsEmpty() {
        assertEquals("", CustomColorSelectionController.colorSortKey(null));
    }

    @Test
    public void colorSortKeyProducesStableSortOrder() {
        // Unset colours (null) sort before any real colour, and the fixed-width
        // hex encoding sorts lexicographically the same way as by brightness of
        // the individual channels.
        assertTrue(CustomColorSelectionController.colorSortKey(null)
                .compareTo(CustomColorSelectionController.colorSortKey(Color.BLACK)) < 0);
        assertTrue(CustomColorSelectionController.colorSortKey(Color.BLACK)
                .compareTo(CustomColorSelectionController.colorSortKey(Color.WHITE)) < 0);
        assertTrue(CustomColorSelectionController.colorSortKey(new Color(0x10, 0x00, 0x00))
                .compareTo(CustomColorSelectionController.colorSortKey(new Color(0x20, 0x00, 0x00))) < 0);
    }
}
