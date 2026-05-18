/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 OmegaT team
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

package org.omegat.gui.editor.chartable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Point;

import org.junit.Test;

import org.omegat.gui.editor.autocompleter.AutoCompleterItem;

public class CharTableModelTest {
    @Test
    public void defaultTableIncludesZeroWidthSpace() {
        CharTableModel model = new CharTableModel(null);
        Point point = findCharacter(model, CharTableModel.ZERO_WIDTH_SPACE);

        assertTrue(point.x >= 0);
        assertEquals(Character.valueOf(CharTableModel.ZERO_WIDTH_SPACE), model.getValueAt(point.y, point.x));
    }

    @Test
    public void autoCompleterSelectionUsesZeroWidthSpacePayload() {
        CharTableAutoCompleterView view = new CharTableAutoCompleterView(null);
        Point point = findCharacter((CharTableModel) view.getTable().getModel(), CharTableModel.ZERO_WIDTH_SPACE);

        view.setSelection(point);
        AutoCompleterItem selected = view.getSelectedValue();

        assertEquals(String.valueOf(CharTableModel.ZERO_WIDTH_SPACE), selected.payload);
    }

    private static Point findCharacter(CharTableModel model, char character) {
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int column = 0; column < model.getColumnCount(); column++) {
                Object value = model.getValueAt(row, column);
                if (value instanceof Character && ((Character) value).charValue() == character) {
                    return new Point(column, row);
                }
            }
        }
        return new Point(-1, -1);
    }
}
