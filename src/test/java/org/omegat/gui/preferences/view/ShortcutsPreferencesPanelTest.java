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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;

import org.junit.Test;

/**
 * Asserts the accessibility contract of the shortcuts panel and the generic
 * table search field: the screen reader relevant components carry accessible
 * names or label associations, so everything is operable without sight and
 * without a mouse.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class ShortcutsPreferencesPanelTest {

    @Test
    public void testAccessibleNamesArePresent() {
        ShortcutsPreferencesPanel panel = new ShortcutsPreferencesPanel();

        AccessibleContext table = panel.table.getAccessibleContext();
        assertNotNull(table.getAccessibleName());
        assertFalse(table.getAccessibleName().isBlank());
        assertNotNull("the table flow must be explained to screen readers",
                table.getAccessibleDescription());

        for (JComponent button : new JComponent[] { panel.assignButton, panel.removeButton,
                panel.restoreButton, panel.exportButton, panel.importButton }) {
            String name = button.getAccessibleContext().getAccessibleName();
            assertNotNull(name);
            assertFalse(name.isBlank());
        }
    }

    @Test
    public void testSearchFieldCarriesHitCountForScreenReaders() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][] { { "a" }, { "b" } }, new Object[] { "col" });
        javax.swing.JTable table = new javax.swing.JTable(model);
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter =
                new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);
        org.omegat.util.gui.TableSearchField search = new org.omegat.util.gui.TableSearchField(table,
                sorter);

        search.getField().setText("a");
        String desc = search.getField().getAccessibleContext().getAccessibleDescription();
        assertNotNull(desc);
        assertFalse("hit count must reach the screen reader", desc.isBlank());
        assertEquals(1, table.getRowCount());
    }
}
