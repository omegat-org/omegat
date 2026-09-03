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

package org.omegat.gui.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import org.omegat.gui.editor.SegmentMetadataConfigDialog.ColumnTableModel;
import org.omegat.gui.editor.SegmentMetadataGutter.Column;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

/**
 * Tests for the column configuration of the segment metadata gutter.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SegmentMetadataConfigDialogTest {

    private AtomicInteger refreshes;
    private ColumnTableModel model;

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
        refreshes = new AtomicInteger();
        model = new ColumnTableModel(refreshes::incrementAndGet);
    }

    @Test
    public void testOffersOneRowPerColumnInDisplayOrder() {
        assertEquals(Column.values().length, model.getRowCount());
        assertEquals(Column.NUMBER.getLabel(), model.getValueAt(0, 2));
        assertEquals(Boolean.TRUE, model.getValueAt(0, 0));
        assertEquals(Boolean.FALSE, model.getValueAt(model.getRowCount() - 1, 0));
        assertTrue(model.isCellEditable(0, 0));
        assertFalse(model.isCellEditable(0, 1));
        assertFalse(model.isCellEditable(0, 2));
    }

    @Test
    public void testTheToggleWritesThePreferenceAndRefreshes() {
        model.setValueAt(Boolean.FALSE, 0, 0);
        assertFalse("The default-on column must persist its deselection",
                Column.NUMBER.isEnabled());
        assertEquals(1, refreshes.get());
    }

    @Test
    public void testMovingARowPersistsTheDisplayOrder() {
        model.moveRow(0, Column.values().length);
        List<Column> order = Column.inDisplayOrder();
        assertEquals(Column.STATUS, order.get(0));
        assertEquals(Column.NUMBER, order.get(order.size() - 1));
        assertEquals(1, refreshes.get());
        assertEquals(Column.STATUS.getLabel(), model.getValueAt(0, 2));
    }

    @Test
    public void testTheDisplayOrderSurvivesUnknownNames() {
        Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER_ORDER, "DATE,UNSINN,DATE");
        List<Column> order = Column.inDisplayOrder();
        assertEquals(Column.values().length, order.size());
        assertEquals(Column.DATE, order.get(0));
        assertEquals(Column.NUMBER, order.get(1));
    }
}
