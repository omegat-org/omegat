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

import org.junit.Test;

/**
 * @author stephan.pakebusch at zollsoft.de
 */
public class CustomColorSelectionControllerTest {

    @Test
    public void colorSortKeyFormatsAsLowercaseHex() {
        assertEquals("#ff0000", CustomColorSelectionController.colorSortKey(Color.RED));
        assertEquals("#00ff00", CustomColorSelectionController.colorSortKey(Color.GREEN));
        assertEquals("#0000ff", CustomColorSelectionController.colorSortKey(Color.BLUE));
        assertEquals("#000000", CustomColorSelectionController.colorSortKey(Color.BLACK));
        assertEquals("#0a141e", CustomColorSelectionController.colorSortKey(new Color(10, 20, 30)));
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
